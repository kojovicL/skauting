import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { environment } from 'src/env/environment';

import { TokenStorage } from './jwt/token.service';
import { User, RoleEnum } from './model/user.model';
import { Login } from './model/login.model';
import { AuthenticationResponse } from './model/authentication-response.model';
import { Registration } from './model/registration.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  user$ = new BehaviorSubject<User | null>(null);

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorage,
    private router: Router
  ) { }

  login(login: Login): Observable<AuthenticationResponse> {
    return this.http
      .post<AuthenticationResponse>(`${environment.apiHost}auth/login`, login)
      .pipe(
        tap((response) => {
          this.tokenStorage.saveAccessToken(response.token);
          this.setUser();
        })
      );
  }

  register(registration: Registration): Observable<string> {
    return this.http
      .post(`${environment.apiHost}auth/signup`, registration, { responseType: 'text' });
  }

  logout(): void {
    // Redirekcija na login/home i čišćenje stanja
    this.router.navigate(['/login']).then(() => {
      this.tokenStorage.clear();
      this.user$.next(null);
    });
  }

  checkIfUserExists(): void {
    const accessToken = this.tokenStorage.getAccessToken();
    if (!accessToken) {
      return;
    }
    this.setUser();
  }

  private setUser(): void {
    const jwtHelperService = new JwtHelperService();
    const accessToken = this.tokenStorage.getAccessToken();

    if (!accessToken) return;

    // Dekodiramo token samo jednom
    const decodedToken = jwtHelperService.decodeToken(accessToken);

    // Mapiramo podatke iz JWT claims-a u naš User interfejs
    const user: User = {
      id: Number(decodedToken.id),
      // Spring Security obično stavlja username u claim 'sub' (subject), ali fallback na 'username'
      username: decodedToken.sub || decodedToken.username,
      email: decodedToken.email || '',
      isActive: true, // Čim ima validan token, aktivan je
      role: decodedToken.role as RoleEnum, // Bekend treba da upakuje ulogu pod 'role' claim
    };

    this.user$.next(user);
  }
}
