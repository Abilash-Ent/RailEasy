export interface AuthResponse {
  token: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface CurrentUser {
  email: string;
  fullName?: string;
  isAdmin: boolean;
  roles: string[];
}
