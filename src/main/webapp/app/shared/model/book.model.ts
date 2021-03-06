import { IPublisher } from 'app/shared/model/publisher.model';
import { IAuthor } from 'app/shared/model/author.model';

export interface IBook {
  id?: number;
  isbn?: string;
  name?: string;
  publishYear?: string;
  copies?: number;
  coverContentType?: string | null;
  cover?: string | null;
  publisher?: IPublisher | null;
  authors?: IAuthor[] | null;
}

export const defaultValue: Readonly<IBook> = {};
