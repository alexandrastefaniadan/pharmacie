/** Generic lookup row returned by /api/v1/lookups/*. */
export interface LookupDto {
  id: number;
  code: string;
  labelFr: string;
  sortOrder: number;
}

