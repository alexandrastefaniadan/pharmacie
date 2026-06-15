import { LookupDto } from './lookup.model';
import { UsageType } from './usage-type.model';

/** Medication as returned by the API. */
export interface MedicationResponse {
  id: string; // UUID
  name: string;
  inn: string | null;
  dosage: string | null;
  description: string | null;
  parapharmacy: boolean;
  /** HUMAN or VETERINARY. */
  usageType: UsageType;
  /** Manual visual price ranking (0..5). 0 = not rated. */
  priceTier: number;
  form: LookupDto | null;
  ageGroups: LookupDto[];
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
  /** Defaults to HUMAN on the backend if omitted. */
  usageType?: UsageType | null;
  /** Manual visual price ranking (0..5). 0 = not rated. */
  priceTier?: number | null;
  formId?: number | null;
  ageGroupIds?: number[];
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
  /** Filter by usage type. Undefined = both. */
  usageType?: UsageType;
  dataSource?: string;
}

/** Lightweight medication info embedded in other resources (e.g. treatments). */
export interface MedicationSummary {
  id: string;
  name: string;
  inn: string | null;
  dosage: string | null;
  /** Pharmaceutical form label (e.g. "Sirop"). Null if not set. */
  formLabel: string | null;
  usageType: UsageType;
  priceTier: number;
}

/** Pagination + sort request shape (mirrors Spring Data's Pageable on the wire). */
export interface PageRequest {
  page: number;        // 0-based
  size: number;
  sort?: string[];     // each element is "field,asc" or "field,desc"
}

/** Single facet entry returned by GET /api/v1/medications/facets. */
export interface FacetCount {
  id: number;
  count: number;
}

/** Cascading-filter facets — one list per lookup dimension. */
export interface MedicationFacets {
  forms: FacetCount[];
  ageGroups: FacetCount[];
  therapeuticClasses: FacetCount[];
  indications: FacetCount[];
}


