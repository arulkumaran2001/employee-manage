import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  showPassword = false;

  isLoading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  login() {
    if (!this.email || !this.password || this.isLoading) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (response: any) => {
        this.isLoading = false;

        if (response?.token) {
          this.authService.saveToken(response.token);

          const role = response.role?.toUpperCase();
          if (role === 'ADMIN') this.router.navigate(['/admin']);
          else if (role === 'EMPLOYEE') this.router.navigate(['/employee']);
          else if (role === 'HR') this.router.navigate(['/hr']);
          else this.showError('Unknown role');
        } else {
          this.showError('Login failed');
        }
      },
      error: () => {
        this.isLoading = false;
        this.showError('Invalid credentials');
      }
    });
  }

  private showError(message: string) {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = '';
    }, 3000);
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    const active = document.activeElement as HTMLElement;
    const order = ['emailField', 'passwordField', 'loginButton'];
    const index = order.indexOf(active?.id);

    if (index === -1) return;

    if (event.key === 'ArrowDown') {
      document.getElementById(order[(index + 1) % order.length])?.focus();
      event.preventDefault();
    } else if (event.key === 'ArrowUp') {
      document.getElementById(order[(index - 1 + order.length) % order.length])?.focus();
      event.preventDefault();
    }
  }
}
