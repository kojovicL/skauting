import {Component} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../auth.service';
import {RoleEnum} from "../../model/user.model";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          if (this.authService.user$.value !== null && (
            this.authService.user$.value?.role === RoleEnum.ROLE_SCOUT)
          ) 
          {
              this.router.navigate(['/metrics-dashboard']); // Redirect to metrics dashboard for scouts and sports directors
          } else if (this.authService.user$.value?.role === RoleEnum.ROLE_SPORTS_DIRECTOR) {
              this.router.navigate(['/director-dashboard']); // Redirect to director dashboard for sports directors
          }
          else {
            this.router.navigate(['/']); // Redirect to home or dashboard on success
          }
        },
        error: (err) => {
          console.error('Login failed', err);
          this.errorMessage = 'Invalid username or password.';
        }
      });
    }
  }
}
