// leave.model.ts
export interface Leave {
    id: number;
    startDate: string;   // Date de début du congé
    endDate: string;     // Date de fin du congé
    description: string; // Description du congé (ex: "Vacances", "Maladie", etc.)
    status: string;      // Statut du congé, par exemple : 'PENDING', 'APPROVED', 'REJECTED'
  }
  