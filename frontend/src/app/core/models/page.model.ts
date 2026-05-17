/** Spring Data Page<T> envelope mirrored as a TS interface. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page index (0-based)
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

