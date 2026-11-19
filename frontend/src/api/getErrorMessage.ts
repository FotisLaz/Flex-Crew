import axios from "axios";

export const getErrorMessage = (err: unknown, fallback: string): string => {
  if (axios.isAxiosError(err)) {
    const serverMessage = err.response?.data?.message;
    if (typeof serverMessage === "string" && serverMessage) {
      return serverMessage;
    }
  }
  if (err instanceof Error && err.message) {
    return err.message;
  }
  return fallback;
};
