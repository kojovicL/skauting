import { Component, OnInit, OnDestroy, ElementRef, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../infrastructure/auth/auth.service';
import { RoleEnum } from '../../../infrastructure/auth/model/user.model';
import { NotificationService } from 'src/app/feature-modules/scouting/services/notification.service';
import { Notification } from 'src/app/feature-modules/scouting/models/notification.model';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn: boolean = false;
  userRole: RoleEnum | undefined = undefined;
  
  // Notifications state
  notifications: Notification[] = [];
  showNotifications: boolean = false;
  private wsSubscription: Subscription | undefined;

  constructor(
    private authService: AuthService,
    public router: Router,
    private notificationService: NotificationService,
    private eRef: ElementRef
  ) {}

  ngOnInit(): void {
    this.authService.checkIfUserExists();
    
    this.authService.user$.subscribe(user => {
      const wasLoggedOut = !this.isLoggedIn;
      this.isLoggedIn = !!user;
      this.userRole = user?.role;

      // Ako se skaut ulogovao, povuci nepročitane notifikacije i otvori WS konekciju
      if (this.isLoggedIn && this.userRole === RoleEnum.ROLE_SCOUT && user) {
        this.loadUnreadNotifications();
        this.notificationService.connectToWebSocket(user.id);
        
        this.wsSubscription = this.notificationService.liveNotifications$.subscribe(newNotif => {
          // Dodaj novu notifikaciju na vrh liste
          this.notifications.unshift(newNotif);
        });
      }

      // Ako se izlogovao, očisti stanje
      if (!this.isLoggedIn && !wasLoggedOut) {
        this.cleanupNotifications();
      }
    });
  }

  ngOnDestroy(): void {
    this.cleanupNotifications();
  }

  private cleanupNotifications(): void {
    this.wsSubscription?.unsubscribe();
    this.notificationService.disconnectWebSocket();
    this.notifications = [];
    this.showNotifications = false;
  }

  loadUnreadNotifications(): void {
    this.notificationService.getUnreadNotifications().subscribe({
      next: (data) => {
        // Sortiramo tako da su najnovije prve
        this.notifications = data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      },
      error: (err) => console.error('Greška pri učitavanju obaveštenja', err)
    });
  }

  toggleNotifications(event: Event): void {
    event.stopPropagation();
    this.showNotifications = !this.showNotifications;
  }

  // Zatvara popup ako se klikne van njega
  @HostListener('document:click', ['$event'])
  clickout(event: Event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.showNotifications = false;
    }
  }

  onNotificationClick(notif: Notification, event: Event): void {
    event.stopPropagation();
    
    // Obeleži kao pročitano na backendu
    this.notificationService.markAsRead(notif.id).subscribe({
      next: () => {
        // Ukloni je iz lokalne liste nepročitanih
        this.notifications = this.notifications.filter(n => n.id !== notif.id);
        this.showNotifications = false;
        
        // Redirekcija na kreiranje izveštaja sa query parametrima
        this.router.navigate(['/reports/create'], { 
          queryParams: { playerId: notif.playerId, matchId: notif.matchId } 
        });
      },
      error: (err) => console.error('Greška pri obeležavanju obaveštenja', err)
    });
  }

  // --- Postojeće metode navigacije ---
  onReportsClick() { this.router.navigate(['/my-reports']); }
  onRequestsClick() { this.router.navigate(['/scouting-requests']); }
  onMetricsClick() { this.router.navigate(['/metrics-dashboard']); }
  onPlayerRecommendationClick() { this.router.navigate(['/player-recommendation']); }
  onSeachTemplatesClick() { this.router.navigate(['/search-templates']); }
  onScoutsClick() { this.router.navigate(['/scout-management']); }

  onLogoutClick() {
    this.cleanupNotifications();
    this.authService.logout();
    this.router.navigate(['/']);
  }

  onLoginClick() { this.router.navigate(['/login']); }

  onLogoClicked() {
    if (this.isLoggedIn) {
      if (this.userRole === 'ROLE_SCOUT') {
        this.router.navigate(['/scout-dashboard']);
      } else if (this.userRole === 'ROLE_SPORTS_DIRECTOR') {
        this.router.navigate(['/director-dashboard']);
      } else {
        this.router.navigate(['/']);
      }
    }
  }
}