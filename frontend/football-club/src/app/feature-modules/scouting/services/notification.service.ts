import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from 'src/env/environment';
import { Notification } from '../models/notification.model';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private baseUrl = `${environment.apiHost}notifications`;
  
  // WebSocket fields
  private stompClient: Client | null = null;
  public liveNotifications$ = new Subject<Notification>();

  constructor(private http: HttpClient) {}

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/unread`);
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.patch<Notification>(`${this.baseUrl}/${id}/read`, {});
  }

  // --- WebSocket Logic ---
  connectToWebSocket(scoutId: number): void {
    const wsUrl = environment.apiHost.replace('api/', 'ws-live');
    
    const socket = new SockJS(wsUrl);
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      debug: (msg: string) => console.log('STOMP: ', msg)
    });

    this.stompClient.onConnect = () => {
      console.log('Connected to WebSocket for notifications.');
      this.stompClient?.subscribe(`/topic/scout/${scoutId}/notifications`, (message: Message) => {
        if (message.body) {
          const notification: Notification = JSON.parse(message.body);
          this.liveNotifications$.next(notification);
        }
      });
    };

    this.stompClient.activate();
  }

  disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}