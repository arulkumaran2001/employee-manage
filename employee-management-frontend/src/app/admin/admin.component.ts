import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  salary: number;
  status: 'ACTIVE' | 'DEACTIVATED';
  profilePic?: string;
  profilePicUrl?: string;
}

interface Attendance {
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  date: string;
  status: string;
  showEmail?: boolean;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {

  /* ================= UI STATE ================= */
  isLoading = false;
  toastMessage = '';
  toastType: 'success' | 'error' | '' = '';

  /* ================= DATA ================= */
  currentSection: 'dashboard' | 'viewUsers' | 'addUser' | 'attendance' | 'overrideAttendance' = 'dashboard';

  profilePic = 'assets/default-profile.png';
  name = '';
  email = '';
  employeeId = '';
  role = '';
  get roleClass() { return this.role.toLowerCase(); }

  @ViewChild('profileFileInput') profileFileInput!: ElementRef<HTMLInputElement>;

  users: User[] = [];
  filteredUsers: User[] = [];
  searchEmail = '';

  newUser: Partial<User> = { name: '', email: '', role: 'EMPLOYEE', salary: 0, status: 'ACTIVE' };
  newUserFile: File | null = null;

  editingUser: User | null = null;
  editUserFile: File | null = null;

  attendanceList: Attendance[] = [];
  filteredAttendance: Attendance[] = [];
  searchAttendanceText = '';
  searchAttendanceDate = '';
  filterStatus: 'ALL' | 'PRESENT' | 'ABSENT' | 'LEAVE' = 'ALL';

  overrideModalOpen = false;
  overrideEmployeeId: number | null = null;
  overrideDate: string | null = null;
  overrideStatus: 'PRESENT' | 'ABSENT' | 'LEAVE' = 'PRESENT';

  deleteModalOpen = false;
  userIdToDelete: number | null = null;

  profileModalOpen = false;
  selectedProfile: User | null = null;

  backendUrl = 'http://localhost:8080';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  /* ================= TOAST ================= */
  showToast(message: string, type: 'success' | 'error') {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = '';
      this.toastType = '';
    }, 3000);
  }

  /* ================= PROFILE ================= */
  loadProfile() {
    this.isLoading = true;
    this.http.get<any>(`${this.backendUrl}/api/users/me`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(data => {
        this.name = data.name;
        this.email = data.email;
        this.employeeId = data.id;
        this.role = data.role;
        this.profilePic = data.profilePic
          ? `${this.backendUrl}${data.profilePic}`
          : 'assets/default-profile.png';
      });
  }

  triggerFileInput() {
    this.profileFileInput.nativeElement.click();
  }

  changeProfilePic(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const formData = new FormData();
    formData.append('file', input.files[0]);

    this.isLoading = true;
    this.http.post<any>(`${this.backendUrl}/api/users/me/profile-pic`, formData)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: res => {
          this.profilePic = `${this.backendUrl}${res.profilePic}`;
          this.showToast('Profile picture updated', 'success');
        },
        error: () => this.showToast('Profile upload failed', 'error')
      });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  /* ================= SECTION SWITCH ================= */
  switchSection(section: typeof this.currentSection) {
    this.currentSection = section;
    if (section === 'viewUsers') this.loadUsers();
    if (section === 'attendance' || section === 'overrideAttendance') this.loadAttendance();
  }

  /* ================= USERS ================= */
  loadUsers() {
    this.isLoading = true;
    this.http.get<User[]>(`${this.backendUrl}/api/admin/view-users`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: users => {
          this.users = users.map(u => ({
            ...u,
            profilePicUrl: u.profilePic
              ? `${this.backendUrl}${u.profilePic}`
              : 'assets/default-profile.png'
          }));
          this.filteredUsers = [...this.users];
        },
        error: () => this.showToast('Failed to load users', 'error')
      });
  }

  searchByEmail() {
    this.filteredUsers = this.searchEmail
      ? this.users.filter(u => u.email.toLowerCase().includes(this.searchEmail.toLowerCase()))
      : [...this.users];
  }

  toggleStatus(user: User) {
    if (this.isLoading) return;

    this.isLoading = true;
    this.http.put(`${this.backendUrl}/api/admin/toggle-status/${user.id}`, {})
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.loadUsers();
          this.showToast('User status updated', 'success');
        },
        error: () => this.showToast('Failed to update status', 'error')
      });
  }

  onNewUserPic(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.newUserFile = input.files[0];
  }

  onEditUserPic(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.editUserFile = input.files[0];
  }

  createUser() {
    if (this.isLoading) return;

    const formData = new FormData();
    Object.entries(this.newUser).forEach(([k, v]) => formData.append(k, String(v)));
    if (this.newUserFile) formData.append('profilePicture', this.newUserFile);

    this.isLoading = true;
    this.http.post(`${this.backendUrl}/api/admin/create-user`, formData)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.showToast('User created successfully', 'success');
          this.newUser = { name: '', email: '', role: 'EMPLOYEE', salary: 0, status: 'ACTIVE' };
          this.newUserFile = null;
          this.loadUsers();
          this.currentSection = 'viewUsers';
        },
        error: () => this.showToast('Failed to create user', 'error')
      });
  }

  openEditModal(user: User) {
    this.editingUser = { ...user };
  }

  closeEditModal() {
    this.editingUser = null;
    this.editUserFile = null;
  }

  updateUser() {
    if (!this.editingUser) return;

    const formData = new FormData();
    Object.entries(this.editingUser).forEach(([k, v]) => formData.append(k, String(v)));
    if (this.editUserFile) formData.append('profilePicture', this.editUserFile);

    this.isLoading = true;
    this.http.put(`${this.backendUrl}/api/admin/users/${this.editingUser.id}`, formData)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.showToast('User updated', 'success');
          this.loadUsers();
          this.closeEditModal();
        },
        error: () => this.showToast('Update failed', 'error')
      });
  }

  deleteUser(id: number) {
    const user = this.users.find(u => u.id === id);
  if (user?.role === 'ADMIN') {
    this.showToast('Admin user cannot be deleted', 'error');
    return;
  }

    this.userIdToDelete = id;
    this.deleteModalOpen = true;
  }

  confirmDelete() {
    if (!this.userIdToDelete) return;

    this.isLoading = true;
    this.http.delete(`${this.backendUrl}/api/admin/delete-user/${this.userIdToDelete}`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.showToast('User deleted', 'success');
          this.loadUsers();
          this.closeDeleteModal();
        },
        error: () => this.showToast('Delete failed', 'error')
      });
  }

  closeDeleteModal() {
    this.deleteModalOpen = false;
    this.userIdToDelete = null;
  }

  /* ================= ATTENDANCE ================= */
  loadAttendance() {
    this.isLoading = true;
    this.http.get<Attendance[]>(`${this.backendUrl}/api/attendance/all`)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: data => {
          this.attendanceList = data;
          this.filteredAttendance = [...data];
        },
        error: () => this.showToast('Failed to load attendance', 'error')
      });
  }

  filterAttendance() {
    this.filteredAttendance = this.attendanceList.filter(a =>
      (!this.searchAttendanceText ||
        a.employeeName.toLowerCase().includes(this.searchAttendanceText.toLowerCase())) &&
      (!this.searchAttendanceDate || a.date.includes(this.searchAttendanceDate)) &&
      (this.filterStatus === 'ALL' || a.status === this.filterStatus)
    );
  }

  toggleRow(row: Attendance) {
    row.showEmail = !row.showEmail;
  }

  openOverrideModal(employeeId: number, date: string, status: string) {
    this.overrideEmployeeId = employeeId;
    this.overrideDate = date;
    this.overrideStatus = status as any;
    this.overrideModalOpen = true;
  }

  closeOverrideModal() {
    this.overrideModalOpen = false;
  }

  confirmOverride() {
    this.isLoading = true;
    this.http.post(`${this.backendUrl}/api/attendance/override`, {
      employeeId: this.overrideEmployeeId,
      date: this.overrideDate,
      status: this.overrideStatus
    })
    .pipe(finalize(() => this.isLoading = false))
    .subscribe(() => {
      this.showToast('Attendance overridden', 'success');
      this.loadAttendance();
      this.closeOverrideModal();
    });
  }

  openProfileModal(user: User) {
    this.selectedProfile = user;
    this.profileModalOpen = true;
  }

  closeProfileModal() {
    this.profileModalOpen = false;
    this.selectedProfile = null;
  }
}
