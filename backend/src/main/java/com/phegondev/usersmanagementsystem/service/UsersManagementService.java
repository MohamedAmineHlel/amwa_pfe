package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.config.JWTAuthFilter;
import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${upload.path}")
    private String uploadPath;
    public ReqRes uploadImage(MultipartFile file, String id) {
        ReqRes response = new ReqRes();
        try {

            int num = Integer.parseInt(id);
            Optional<OurUsers> userOptional = usersRepo.findById(num);
            if (!userOptional.isPresent()) {
                response.setStatusCode(404);
                response.setMessage("User not found");
                return response;
            }

            OurUsers user = userOptional.get();

            // Ensure the 'uploads/image' directory exists
            File directory = new File(uploadPath+"/faceID"); // Using the upload.path defined in application.properties
            if (!directory.exists()) {
                directory.mkdirs(); // Create the directories if they don't exist
            }

            // Generate a unique filename using the user's name and current timestamp
            String filename = user.getName() + "_" + System.currentTimeMillis() + ".jpg"; // Adjust extension if needed

            // Path to save the file
            Path path = Paths.get(uploadPath + "/faceID/" + filename);

            // Save the file to the specified path
            Files.write(path, file.getBytes());

            // Update the user's image field with the image path
            user.setFaceId(path.toString());
            usersRepo.save(user);

            response.setStatusCode(200);
            response.setMessage("Image uploaded successfully");
            response.setFaceId(path.toString());  // Return the image path in the response

        } catch (IOException e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while uploading image: " + e.getMessage());
        }
        return response;
    }
    public ReqRes getAllUserImages() {
        ReqRes reqRes = new ReqRes();
        try {
            List<OurUsers> users = usersRepo.findAll();
            if (!users.isEmpty()) {
                List<Map<String, String>> userImages = users.stream()
                        .filter(user -> user.getFaceId() != null)
                        .map(user -> {
                            Map<String, String> userData = new HashMap<>();
                            // Construct the URL instead of returning the raw file path
                            String faceIdUrl = "http://localhost:1010/uploads/faceID/" + Paths.get(user.getFaceId()).getFileName().toString();
                            userData.put("faceId", faceIdUrl); // Return URL
                            userData.put("email", user.getEmail());
                            return userData;
                        })
                        .collect(Collectors.toList());
                reqRes.setData(userImages);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User images and emails retrieved successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }
    public ReqRes getAllImages() {
        ReqRes response = new ReqRes();
        List<String> imagePaths = new ArrayList<>();

        try {
            // Create a File object to point to the image directory
            File directory = new File(uploadPath+"/faceID");

            // Check if directory exists
            if (!directory.exists() || !directory.isDirectory()) {
                response.setStatusCode(404);
                response.setMessage("Image directory not found");
                return response;
            }

            // List all files in the directory
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

            if (files != null && files.length > 0) {
                for (File file : files) {
                    imagePaths.add(file.getAbsolutePath());
                }

                response.setStatusCode(200);
                response.setMessage("Images retrieved successfully");
                response.setData(imagePaths); // You can set the list of image paths here
            } else {
                response.setStatusCode(404);
                response.setMessage("No images found");
            }

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while retrieving images: " + e.getMessage());
        }

        return response;
    }
    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();

        try {
            OurUsers ourUser = new OurUsers();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setCity(registrationRequest.getCity());
            ourUser.setRole(registrationRequest.getRole());
            ourUser.setName(registrationRequest.getName());
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            OurUsers ourUsersResult = usersRepo.save(ourUser);


            if (ourUsersResult.getId()>0) {
                resp.setOurUsers((ourUsersResult));
                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);

                // Send the email to the user with their registration details
                String emailBody = "Hello " + ourUsersResult.getName() + ",<br><br>" +
                        "Welcome to our Vision!<br>" +
                        "Your registration is complete. Here are your login details:<br>" +
                        "Email: " + ourUsersResult.getEmail() + "<br>" +
                        "Password: " + registrationRequest.getPassword() + "<br><br>" +
                        "Make sure to change your password after you login"+"<br><br>" +
                        "Best regards,<br>" +
                        "BPO Team";
                emailService.sendEmail(ourUsersResult.getEmail(), "Welcome to Our Platform", emailBody);
            }

        }catch (Exception e){
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }
    public ReqRes loginViaFaceID(String email) {
        ReqRes response = new ReqRes();
        try {
            // Check if the email is provided
            if (email == null || email.isEmpty()) {
                response.setStatusCode(400);
                response.setMessage("Email is required");
                return response;
            }

            // Find the user by email
            Optional<OurUsers> userOptional = usersRepo.findByEmail(email);
            if (userOptional.isPresent()) {
                OurUsers user = userOptional.get();

                // Generate a JWT token for the user
                String token = jwtUtils.generateToken(user);
                String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

                // Set the response
                response.setStatusCode(200);
                response.setMessage("Login successful");
                response.setToken(token);
                response.setRefreshToken(refreshToken);
                response.setRole(user.getRole());
                response.setExpirationTime("24Hrs");
            } else {
                response.setStatusCode(404);
                response.setMessage("User not found");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred: " + e.getMessage());
        }
        return response;
    }

    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes();
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                            loginRequest.getPassword()));
            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }





    public ReqRes refreshToken(ReqRes refreshTokenReqiest){
        ReqRes response = new ReqRes();
        try{
            String ourEmail = jwtUtils.extractUsername(refreshTokenReqiest.getToken());
            OurUsers users = usersRepo.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenReqiest.getToken(), users)) {
                var jwt = jwtUtils.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReqiest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }


    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();

        try {
            List<OurUsers> result = usersRepo.findAllWithTeams();
            if (!result.isEmpty()) {
                reqRes.setOurUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
            return reqRes;
        }
    }


    public ReqRes getUsersById(Integer id) {
        ReqRes reqRes = new ReqRes();
        try {
            OurUsers usersById = usersRepo.findById(id).orElseThrow(() -> new RuntimeException("User Not found"));
            reqRes.setOurUsers(usersById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Users with id '" + id + "' found successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }


    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findById(userId);
            if (userOptional.isPresent()) {
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for deletion");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes updateUser(Integer userId, OurUsers updatedUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findById(userId);
            if (userOptional.isPresent()) {
                OurUsers existingUser = userOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setName(updatedUser.getName());
                existingUser.setCity(updatedUser.getCity());
                existingUser.setRole(updatedUser.getRole());

                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                OurUsers savedUser = usersRepo.save(existingUser);
                reqRes.setOurUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return reqRes;
    }


    public ReqRes getMyInfo(String email){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findByEmail(email);
            if (userOptional.isPresent()) {
                reqRes.setOurUsers(userOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }

        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return reqRes;

    }
}
