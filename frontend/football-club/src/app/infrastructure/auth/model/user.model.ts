export enum RoleEnum {
  ROLE_HEAD_COACH = 'ROLE_HEAD_COACH',
  ROLE_ASSISTANT_COACH = 'ROLE_ASSISTANT_COACH',
  ROLE_STATISTICIAN = 'ROLE_STATISTICIAN',
  ROLE_SCOUT = 'ROLE_SCOUT',
  ROLE_SPORTS_DIRECTOR = 'ROLE_SPORTS_DIRECTOR',
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_BUYER = 'ROLE_BUYER'
}

export interface User {
  id: number;
  username: string;
  email: string;
  isActive: boolean;
  role: RoleEnum;
}