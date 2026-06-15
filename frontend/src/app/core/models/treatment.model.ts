import { LookupDto } from './lookup.model';
import { MedicationSummary } from './medication.model';
import { UsageType } from './usage-type.model';

/** Treatment as returned by the API. */
export interface TreatmentResponse {
  id: string; // UUID
  name: string;
  description: string | null;
  notes: string | null;
  usageType: UsageType;
  medications: MedicationSummary[];
  indications: LookupDto[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

/** Body for POST /api/v1/treatments. */
export interface TreatmentCreateRequest {
  name: string;
  description?: string | null;
  notes?: string | null;
  usageType: UsageType;
  /** Ordered list of medication ids. At least one required. */
  medicationIds: string[];
  indicationIds?: number[];
}

/** Body for PUT /api/v1/treatments/{id}. {@code version} is required. */
export interface TreatmentUpdateRequest extends TreatmentCreateRequest {
  version: number;
}

/** Composable filter sent as query params to GET /api/v1/treatments. */
export interface TreatmentFilter {
  q?: string;
  usageType?: UsageType;
  indicationIds?: number[];
}

