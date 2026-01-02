import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth/auth.service';

interface LeaveRequest {
  id?: number;
  startDate: string;
  endDate: string;
  reason: string;
  status?: string;
}

interface Attendance {
  date: string;
  status: string;
}

interface Toast {
  id: number;
  message: string;
}

@Component({
  selector: 'app-employee',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employee.component.html'
})
export class EmployeeComponent implements OnInit {

  profilePic: string = 'assets/default-profile.png';
  name = '';
  email = '';
  employeeId = '';
  role = '';
  roleClass = '';

  leaveRequests: LeaveRequest[] = [];
  attendanceList: Attendance[] = [];
  newLeave: LeaveRequest = { startDate: '', endDate: '', reason: '' };

  todayAttendanceStatus: string = '';
  loading = false;
  error = '';
  menuOpen = false;
  currentSection: string = 'dashboard';

  toasts: Toast[] = [];
  toastCounter = 0;
  lastLeaveStatuses: { [id: number]: string } = {};
  private apiUrl = 'http://localhost:8080';
  private firstLoad = true;

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    if (this.authService.getToken()) {
      this.loadProfile();
      setInterval(() => this.loadLeaves(), 15000);
    } else {
      this.showToast('Please login first');
    }
  }

  toggleMenu() { this.menuOpen = !this.menuOpen; }
  showSection(section: string) { this.currentSection = section; this.menuOpen = false; }
  logout() { this.authService.clearToken(); window.location.href = '/login'; }

  loadProfile() {
    this.http.get<any>(`${this.apiUrl}/api/users/me`).subscribe({
      next: data => {
        this.name = data.name;
        this.email = data.email;
        this.employeeId = data.id;
        this.role = data.role;
        this.roleClass = data.role.toLowerCase();
        this.profilePic = data.profilePic ? `${this.apiUrl}${data.profilePic}` : 'assets/default-profile.png';
        this.loadLeaves();
        this.loadAttendance();
      },
      error: () => this.showToast('Failed to load profile. Make sure you are logged in.')
    });
  }

  loadLeaves() {
    this.http.get<LeaveRequest[]>(`${this.apiUrl}/api/leaves/my`).subscribe({
      next: data => {
        data.forEach(l => {
          const prevStatus = this.lastLeaveStatuses[l.id!];
          if (!this.firstLoad && prevStatus && prevStatus !== l.status && l.status) {
            const start = l.startDate;
            const end = l.endDate;
            this.showToast(
              start === end
                ? `Your leave on ${start} was ${l.status}`
                : `Your leave from ${start} to ${end} was ${l.status}`
            );
          }
          this.lastLeaveStatuses[l.id!] = l.status!;
        });
        this.leaveRequests = data;
        this.firstLoad = false;
      },
      error: () => this.showToast('Failed to load leave requests.')
    });
  }

  loadAttendance() {
    this.http.get<Attendance[]>(`${this.apiUrl}/api/attendance/me`).subscribe({
      next: data => {
        this.attendanceList = data;
        const today = new Date();
        const todayAttendance = this.attendanceList.find(a => {
          const aDate = new Date(a.date);
          return aDate.getFullYear() === today.getFullYear() &&
                 aDate.getMonth() === today.getMonth() &&
                 aDate.getDate() === today.getDate();
        });
        this.todayAttendanceStatus = todayAttendance ? todayAttendance.status : '';
      },
      error: () => this.showToast('Failed to load attendance.')
    });
  }

  markTodayAttendance() {
    if (this.todayAttendanceStatus) return;
    this.http.post<Attendance>(`${this.apiUrl}/api/attendance/mark`, { status: 'PRESENT' }).subscribe({
      next: (res) => {
        this.todayAttendanceStatus = res.status;
        this.attendanceList.unshift(res);
        this.showToast(`Attendance for today marked as PRESENT`);
      },
      error: () => this.showToast('Failed to mark attendance')
    });
  }

  submitLeaveRequest() {
    if (!this.newLeave.startDate || !this.newLeave.endDate || !this.newLeave.reason) {
      this.showToast('All fields are required.');
      return;
    }
    const start = new Date(this.newLeave.startDate);
    const end = new Date(this.newLeave.endDate);
    const today = new Date(); today.setHours(0, 0, 0, 0);

    if (start < today) { this.showToast('Leave cannot start in the past.'); return; }
    if (end < start) { this.showToast('End date cannot be before start date.'); return; }

    this.loading = true;
    this.http.post(`${this.apiUrl}/api/leaves`, this.newLeave).subscribe({
      next: () => {
        this.showToast('Leave request submitted successfully!');
        this.loadLeaves();
        this.newLeave = { startDate: '', endDate: '', reason: '' };
        this.loading = false;
      },
      error: (err) => {
        const backendMsg =
          err?.error?.details?.trim() ||
          err?.error?.error?.trim() ||
          (typeof err.error === 'string' && err.error.trim()) ||
          'Failed to submit leave request.';
        this.showToast(backendMsg);
        this.loading = false;
      }
    });
  }

  showToast(message: string) {
    const id = this.toastCounter++;
    this.toasts.push({ id, message });
    setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id); }, 3000);
  }
get sortedAttendanceList() {
  return [...this.attendanceList].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
}


  get pendingLeaves() { return this.leaveRequests.filter(l => l.status === 'PENDING').length; }
  get presentDays() { return this.attendanceList.filter(a => a.status === 'PRESENT').length; }
  get absentDays() { return this.attendanceList.filter(a => a.status === 'ABSENT').length; }
  get leaveDays() { return this.attendanceList.filter(a => a.status === 'LEAVE').length; }
}
