import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  salary?: number;
  profilePic?: string;
}

export type Employee = User;

export interface Attendance {
  date: string;
  status: string;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { Authorization: `Bearer ${token}` };
  }

  /** USERS **/

  // Fetch all users (Admin view)
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`, {
      headers: this.getAuthHeaders(),
    });
  }

  // Fetch only employees (HR view)
  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/users/hr/employees`, {
      headers: this.getAuthHeaders(),
    });
  }

  // Fetch current logged-in user
  getCurrentUser(): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/users/me`, {
      headers: this.getAuthHeaders(),
    });
  }

  // Upload profile picture for current user
  uploadProfilePic(formData: FormData): Observable<{ profilePic: string }> {
    return this.http.post<{ profilePic: string }>(
      `${this.apiUrl}/users/me/profile-pic`,
      formData,
      { headers: this.getAuthHeaders() }
    );
  }

  // Logout
  logout(): void {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }

  /** ATTENDANCE **/

  // Mark attendance for today (current user)
  markTodayAttendance(): Observable<Attendance> {
    return this.http.post<Attendance>(
      `${this.apiUrl}/attendance/mark`,
      { status: 'PRESENT' },
      { headers: this.getAuthHeaders() }
    );
  }

  // Get attendance for current user
  getMyAttendance(): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/attendance/me`, {
      headers: this.getAuthHeaders(),
    });
  }

  // Get attendance of any employee (HR/Admin)
  getEmployeeAttendance(employeeId: number): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(
      `${this.apiUrl}/attendance/${employeeId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // HR/Admin view: get all attendance
  getAllAttendance(): Observable<Attendance[]> {
    return this.http.get<Attendance[]>(`${this.apiUrl}/attendance/all`, {
      headers: this.getAuthHeaders(),
    });
  }



}
