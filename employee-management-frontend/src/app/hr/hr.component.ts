import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LeaveService, LeaveRequest } from '../leave.service';
import { UserService, Employee } from '../user.service';
import { finalize } from 'rxjs/operators';

export interface Attendance {
  date: string;
  status: string;
  employeeId?: number;
  employeeName?: string;
}
type FilterStatus = 'ALL' | 'PRESENT' | 'ABSENT' | 'LEAVE' | 'NOT_MARKED';

@Component({
  selector: 'app-hr',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  providers: [DatePipe],
  templateUrl: './hr.component.html'
})
export class HrComponent implements OnInit {
  menuOpen = false;
  currentSection = 'dashboard';

  leaveRequests: (LeaveRequest & { updating?: boolean })[] = [];
  employees: Employee[] = [];

  profilePic = 'assets/default-profile.png';
  private apiUrl = 'http://localhost:8080';
  name = '';
  email = '';
  hrId: number | null = null;
  role = 'HR';

  toastMessages: string[] = [];
  lastLeaveStatuses: { [id: number]: string } = {};

  showAttendanceModal = false;
  isLoading = false;

  selectedEmployee: Employee | null = null;
  attendanceRecords: Attendance[] = [];

  attendanceList: Attendance[] = [];
  filteredAttendance: Attendance[] = [];
  attendanceSearchText = '';
  attendanceSearchDate = '';
  attendanceFilterStatus: FilterStatus = 'ALL';

  constructor(
    private leaveService: LeaveService,
    private userService: UserService,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadEmployees();
    this.loadLeaves(true);
    setInterval(() => this.loadLeaves(true), 15000);
  }

  // --- Menu ---
  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  showSection(section: string): void {
    this.currentSection = section;
    if (section === 'leaveRequests') this.loadLeaves();
    if (section === 'employees') this.loadEmployees();
    if (section === 'attendance') this.loadAllAttendance();
  }

  // --- Profile ---
  loadProfile(): void {
    this.isLoading = true;
    this.userService.getCurrentUser()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(res => {
        this.name = res.name;
        this.email = res.email;
        this.hrId = res.id;
        this.role = 'HR';
        this.profilePic = res.profilePic
          ? `${this.apiUrl}${res.profilePic}`
          : 'assets/default-profile.png';
      });
  }

  // --- Leaves ---
  loadLeaves(showPendingToast: boolean = false): void {
    this.leaveService.getAllLeaves().subscribe((res: LeaveRequest[]) => {
      if (showPendingToast) {
        const pendingCount = res.filter(l => l.status === 'PENDING').length;
        if (pendingCount > 0 && Object.keys(this.lastLeaveStatuses).length === 0) {
          this.showToast(`You have ${pendingCount} pending leave request(s).`);
        }
      }

      res.forEach(l => {
        const prevStatus = this.lastLeaveStatuses[l.id!];
        if (prevStatus && prevStatus !== l.status && l.status) {
          const start = this.datePipe.transform(l.startDate, 'mediumDate');
          const end = this.datePipe.transform(l.endDate, 'mediumDate');
          if (start && end) {
            this.showToast(
              start === end
                ? `Leave on ${start} was ${l.status}`
                : `Leave from ${start} to ${end} was ${l.status}`
            );
          }
        }
        this.lastLeaveStatuses[l.id!] = l.status || 'PENDING';
      });

      this.leaveRequests = res.map(l => ({ ...l, updating: false }));
    });
  }

  updateLeaveStatus(leaveId: number, status: 'APPROVED' | 'REJECTED'): void {
    const leave = this.leaveRequests.find(l => l.id === leaveId);
    if (!leave) return;

    this.isLoading = true;
    leave.updating = true;

    this.leaveService.updateLeaveStatus(leaveId, status)
      .pipe(finalize(() => {
        this.isLoading = false;
        leave.updating = false;
      }))
      .subscribe(res => {
        leave.status = res.status;
        this.lastLeaveStatuses[leaveId] = res.status || 'PENDING';

        const start = this.datePipe.transform(leave.startDate, 'mediumDate');
        const end = this.datePipe.transform(leave.endDate, 'mediumDate');
        if (start && end) {
          this.showToast(
            start === end
              ? `Leave on ${start} was ${leave.status}`
              : `Leave from ${start} to ${end} was ${leave.status}`
          );
        }
      });
  }

  // --- Employees ---
  loadEmployees(): void {
    this.isLoading = true;
    this.userService.getEmployees()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(res => this.employees = res);
  }

  openAttendanceModal(emp: Employee): void {
    this.isLoading = true;
    this.selectedEmployee = emp;

    this.userService.getEmployeeAttendance(emp.id)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(res => {
        this.attendanceRecords = [...res].sort(
          (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
        );
        this.showAttendanceModal = true;
      });
  }

  closeAttendanceModal(): void {
    this.showAttendanceModal = false;
    this.selectedEmployee = null;
    this.attendanceRecords = [];
  }

  // --- Attendance page ---
  loadAllAttendance(): void {
    this.isLoading = true;
    this.userService.getAllAttendance()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(res => {
        this.attendanceList = [...res].sort(
          (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
        );
        this.filteredAttendance = [...this.attendanceList];
      });
  }

  private toYMD(dateStr: string): string {
    const d = new Date(dateStr);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
      d.getDate()
    ).padStart(2, '0')}`;
  }

  filterAttendance(): void {
    this.filteredAttendance = this.attendanceList.filter(a => {
      const matchesText = this.attendanceSearchText
        ? a.employeeName?.toLowerCase().includes(this.attendanceSearchText.toLowerCase()) ||
          a.employeeId?.toString().includes(this.attendanceSearchText)
        : true;

      const matchesDate = this.attendanceSearchDate
        ? this.toYMD(a.date) === this.attendanceSearchDate
        : true;

      const matchesStatus =
        this.attendanceFilterStatus !== 'ALL'
          ? a.status === this.attendanceFilterStatus
          : true;

      return matchesText && matchesDate && matchesStatus;
    });
  }

  // --- Toast ---
  showToast(msg: string): void {
    this.toastMessages.push(msg);
    setTimeout(() => this.toastMessages.shift(), 3500);
  }

  removeToast(msg: string): void {
    const i = this.toastMessages.indexOf(msg);
    if (i > -1) this.toastMessages.splice(i, 1);
  }

  // --- Logout ---
  logout(): void {
    this.isLoading = true;
    this.userService.logout();
    this.isLoading = false;
  }
}
