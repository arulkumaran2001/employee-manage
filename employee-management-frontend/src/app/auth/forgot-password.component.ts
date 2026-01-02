import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  email = '';
  isLoading = false;

  toastMessage = '';
  toastType: 'success' | 'error' | '' = '';

  constructor(private http: HttpClient) {}

  sendResetLink() {
    if (!this.email || this.isLoading) return;

    this.isLoading = true;
    this.clearToast();

    this.http.post(
      'http://localhost:8080/api/auth/forgot-password',
      {},
      {
        params: { email: this.email },
        responseType: 'text'
      }
    ).subscribe({
      next: (res: string) => {
        this.isLoading = false;
        this.showToast(res, 'success');
      },
      error: () => {
        this.isLoading = false;
        this.showToast(
          'You are not in the organization. Please contact admin.',
          'error'
        );
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
