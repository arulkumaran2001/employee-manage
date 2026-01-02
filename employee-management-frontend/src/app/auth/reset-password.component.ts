import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
  token = '';
  newPassword = '';
  confirmPassword = '';

  showNewPassword = false;
  showConfirmPassword = false;
  isLoading = false;

  toastMessage = '';
  toastType: 'success' | 'error' | '' = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParams['token'] || '';
  }

  toggleNewPassword() {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  resetPassword() {
    if (!this.newPassword || !this.confirmPassword || this.isLoading) return;

    if (this.newPassword !== this.confirmPassword) {
      this.showToast('Passwords do not match', 'error');
      return;
    }

    this.isLoading = true;
    this.clearToast();

    this.http.post(
      'http://localhost:8080/api/auth/reset-password',
      null,
      {
        params: {
          token: this.token,
          newPassword: this.newPassword
        }
      }
    ).subscribe({
      next: () => {
        this.isLoading = false;
        this.showToast('Password reset successfully. Redirecting...', 'success');

        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: () => {
        this.isLoading = false;
        this.showToast('Failed to reset password. Token may be expired.', 'error');
      }
    });
  }

  private showToast(message: string, type: 'success' | 'error') {
    this.toastMessage = message;
    this.toastType = type;

    setTimeout(() => this.clearToast(), 3000);
  }

  private clearToast() {
    this.toastMessage = '';
    this.toastType = '';
  }
}
