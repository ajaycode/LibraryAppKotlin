export interface IClient {
  id?: number;
  firstName?: string;
  lastName?: string;
  email?: string | null;
  address?: string | null;
  phone?: string | null;
}

export const defaultValue: Readonly<IClient> = {};
