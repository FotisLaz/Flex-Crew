import api from "../api/axiosConfig";
import { AuthResponse } from "../types/AuthResponse";

export const registerUser = async (
  email: string,
  names: string,
  firstSurname: string,
  secondSurname: string,
  password: string,
): Promise<AuthResponse> => {
  // The server always registers new accounts as USER, so no role is sent.
  const response = await api.post(
    "/api/auth/register",
    { email, names, firstSurname, secondSurname, password },
    {
      headers: { "Content-Type": "application/json" },
      withCredentials: true,
    },
  );

  return response.data;
};
