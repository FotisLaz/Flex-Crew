import { IssueStatus } from "./IssueStatus";

export interface Issue {
  id: number;
  issueStatus: IssueStatus;
  delay: string;
  description: string;
}
