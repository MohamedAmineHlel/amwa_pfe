import { Component, ViewChild, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AiFaceIdService } from '../service/ai-face-id.service';
@Component({
  selector: 'app-ai-face-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-face-login.component.html',
  styleUrl: './ai-face-login.component.css'
})
export class AiFaceLoginComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement', { static: true }) videoElement!: ElementRef;
  mediaRecorder!: MediaRecorder;
  recordedChunks: Blob[] = [];
  videoBlob!: Blob;
  isRecording: boolean = false;
  retryRecording: boolean = true; // Continue recording if face is unknown
  message: string="Détection faciale en cours...";
  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.setupCamera();
  }

  async setupCamera() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false }); // Audio désactivé si inutile
      this.videoElement.nativeElement.srcObject = stream;
      this.mediaRecorder = new MediaRecorder(stream);

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };

      this.mediaRecorder.onstop = () => {
        this.videoBlob = new Blob(this.recordedChunks, { type: 'video/webm' });
        this.uploadVideo(); // Auto-upload après l'arrêt
      };

      this.startRecording(); // Démarre l'enregistrement dès que la caméra est prête
    } catch (error) {
      console.error('Erreur accès caméra:', error);
    }
  }

  startRecording() {
    if (!this.isRecording) {
      this.isRecording = true;
      this.recordedChunks = [];
      this.mediaRecorder.start();

      // Arrêter automatiquement après 2s et envoyer la vidéo
      setTimeout(() => {
        this.stopRecording();
      }, 2000);
    }
  }

  stopRecording() {
    if (this.isRecording) {
      this.isRecording = false;
      this.mediaRecorder.stop();
    }
  }
  closeCamera() {
    const stream = this.videoElement.nativeElement.srcObject as MediaStream;
    if (stream) {
      stream.getTracks().forEach(track => track.stop()); // Arrêter chaque piste (vidéo & audio)
      this.videoElement.nativeElement.srcObject = null;
    }
  }
  
 
  async uploadVideo(retryCount: number = 0) {
    this.message='upload to serever'
    const maxRetries = 3;
    const formData = new FormData();
    formData.append('video', this.videoBlob, 'recorded_video.webm');
  
    try {
        const response = await lastValueFrom(this.http.post<any>('http://127.0.0.1:5000/upload', formData));
  
        if (response.email && response.login_response) {
            this.closeCamera();
            localStorage.setItem('token', response.login_response.token);
            localStorage.setItem('role', response.login_response.role);
            this.router.navigate(['/profile']).then(() => {
              window.location.reload();
            });
        } else {
            this.message = "No recognized face found.";
            if (retryCount < maxRetries) {
                this.retryUpload(retryCount + 1);
            }
        }
    } catch (error) {
        this.message = `Error processing login:`;
        if (retryCount < maxRetries) {
            this.retryUpload(retryCount + 1);
        }
    }
  }
  
  retryUpload(retryCount: number) {
    this.message = `Retrying... Attempt ${retryCount}`;
    this.startRecording(); // Restart recording and send another video
  }
  
  ngOnDestroy() {
    this.retryRecording = false; // Arrêter les tentatives lorsque l'utilisateur quitte la page
  }
}
