export interface Notification {
  id: number;
  scoutId: number;
  playerId: number;
  matchId: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}