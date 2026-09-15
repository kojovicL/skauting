export enum RoleEnum {
  ROLE_SCOUT = 'ROLE_SCOUT',
  ROLE_SPORTS_DIRECTOR = 'ROLE_SPORTS_DIRECTOR',
  ROLE_ADMIN = 'ROLE_ADMIN',
}

export interface User {
  id: number;
  username: string;
  email: string;
  isActive: boolean;
  role: RoleEnum;
}