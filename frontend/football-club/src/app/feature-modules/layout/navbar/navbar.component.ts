import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../../infrastructure/auth/auth.service";
import {Router} from "@angular/router";
import {RoleEnum} from "../../../infrastructure/auth/model/user.model";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLoggedIn: boolean = false;
  userRole: RoleEnum | undefined = undefined;
  clubId: number | undefined = undefined;

  constructor(
    private authService: AuthService,
    public router: Router,
  ) {}

  ngOnInit(): void {
    this.authService.checkIfUserExists();
    this.authService.user$.subscribe(user => {
      this.isLoggedIn = !!user;
      this.userRole = user?.role;
    });
  }

  onReportsClick() {
    this.router.navigate(['/my-reports']);
  }

  onLogoutClick() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  onLoginClick() {
    this.router.navigate(['/login']);
  }

  onLogoClicked() {
    if (this.isLoggedIn) {
      if (this.userRole === 'ROLE_SCOUT' || this.userRole === 'ROLE_SPORTS_DIRECTOR') {
        this.router.navigate(['/scouting-dashboard']);
      } else {
        this.router.navigate(['/']);
      }
    }
  }

  onMetricsClick() {
    this.router.navigate(['/metrics-dashboard']);
  }

  onWishlistsClick() {
    this.router.navigate(['/wishlists']);
  }

  onRequestsClick() {
    this.router.navigate(['/scouting-requests']);
  }

  onPlayerRecommendationClick() {
    this.router.navigate(['/player-recommendation']);
  }

  onSeachTemplatesClick() {
    this.router.navigate(['/search-templates']);
  }
}
