import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LeaveRequest {
  id: number;
  employeeUsername: string;
  startDate: string;
  endDate: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

@Injectable({
  providedIn: 'root'
})
export class LeaveService {
  private apiUrl = 'http://localhost:8080/api/leaves';

  constructor(private http: HttpClient) {}

  // Fetch all leaves (HR view)
  getAllLeaves(): Observable<LeaveRequest[]> {
    return this.http.get<LeaveRequest[]>(this.apiUrl);
  }

  // Update leave status (HR action)
  updateLeaveStatus(id: number, status: 'APPROVED' | 'REJECTED'): Observable<LeaveRequest> {
    // 👇 send status inside the request body
    return this.http.put<LeaveRequest>(`${this.apiUrl}/${id}/status`, { status });
  }
}
