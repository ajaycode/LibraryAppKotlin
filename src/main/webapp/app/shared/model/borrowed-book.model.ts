import dayjs from 'dayjs';
import { IBook } from 'app/shared/model/book.model';
import { IClient } from 'app/shared/model/client.model';

export interface IBorrowedBook {
  id?: number;
  borrowDate?: string | null;
  book?: IBook | null;
  client?: IClient | null;
}

export const defaultValue: Readonly<IBorrowedBook> = {};
