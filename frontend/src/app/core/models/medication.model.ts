import { LookupDto } from './lookup.model';

/** Medication as returned by the API. */
export interface MedicationResponse {
  id: string; // UUID
  name: string;
  inn: string | null;
  dosage: string | null;
  description: string | null;
  parapharmacy: boolean;
  form: LookupDto | null;
  ageGroup: LookupDto | null;
  therapeuticClasses: LookupDto[];
  indications: LookupDto[];
  barcode: string | null;
  externalCip: string | null;
  dataSource: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

/** Body for POST /api/v1/medications. */
export interface MedicationCreateRequest {
  name: string;
  inn?: string | null;
  dosage?: string | null;
  description?: string | null;
  parapharmacy?: boolean;
  formId?: number | null;
  ageGroupId?: number | null;
  therapeuticClassIds?: number[];
  indicationIds?: number[];
  barcode?: string | null;
  externalCip?: string | null;
}

/** Body for PUT /api/v1/medications/{id}. {@code version} is required. */
export interface MedicationUpdateRequest extends MedicationCreateRequest {
  version: number;
}

/** Composable filter sent as query params to GET /api/v1/medications. */
export interface MedicationFilter {
  q?: string;
  formIds?: number[];
  ageGroupIds?: number[];
  therapeuticClassIds?: number[];
  indicationIds?: number[];
  parapharmacy?: boolean;
  dataSource?: string;
}

/** Pagination + sort request shape (mirrors Spring Data's Pageable on the wire). */
export interface PageRequest {
  page: number;        // 0-based
  size: number;
  sort?: string[];     // each element is "field,asc" or "field,desc"
}

