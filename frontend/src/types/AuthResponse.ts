import { Role } from "./Role";

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  role: Role;
}
