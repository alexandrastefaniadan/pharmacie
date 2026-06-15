import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import {
  MedicationCreateRequest,
  MedicationFacets,
  MedicationFilter,
  MedicationResponse,
  MedicationUpdateRequest,
  PageRequest,
} from '@core/models/medication.model';
import { Page } from '@core/models/page.model';

/**
 * Thin wrapper around /api/v1/medications. The only place in the app that
 * knows about HTTP. Returns plain typed observables.
 */
@Injectable({ providedIn: 'root' })
export class MedicationsApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBaseUrl}/medications`;

  search(filter: MedicationFilter, pageReq: PageRequest): Observable<Page<MedicationResponse>> {
    let params = new HttpParams()
      .set('page', pageReq.page)
      .set('size', pageReq.size);

    (pageReq.sort ?? []).forEach((s) => (params = params.append('sort', s)));

    params = appendIfDefined(params, 'q', filter.q);
    params = appendList(params, 'formIds', filter.formIds);
    params = appendList(params, 'ageGroupIds', filter.ageGroupIds);
    params = appendList(params, 'therapeuticClassIds', filter.therapeuticClassIds);
    params = appendList(params, 'indicationIds', filter.indicationIds);
    if (filter.parapharmacy !== undefined && filter.parapharmacy !== null) {
      params = params.set('parapharmacy', String(filter.parapharmacy));
    }
    params = appendIfDefined(params, 'usageType', filter.usageType);
    params = appendIfDefined(params, 'dataSource', filter.dataSource);

    return this.http.get<Page<MedicationResponse>>(this.url, { params });
  }

  /** Cascading-filter facets for the current filter selection. */
  facets(filter: MedicationFilter): Observable<MedicationFacets> {
    let params = new HttpParams();
    params = appendIfDefined(params, 'q', filter.q);
    params = appendList(params, 'formIds', filter.formIds);
    params = appendList(params, 'ageGroupIds', filter.ageGroupIds);
    params = appendList(params, 'therapeuticClassIds', filter.therapeuticClassIds);
    params = appendList(params, 'indicationIds', filter.indicationIds);
    if (filter.parapharmacy !== undefined && filter.parapharmacy !== null) {
      params = params.set('parapharmacy', String(filter.parapharmacy));
    }
    params = appendIfDefined(params, 'usageType', filter.usageType);
    params = appendIfDefined(params, 'dataSource', filter.dataSource);
    return this.http.get<MedicationFacets>(`${this.url}/facets`, { params });
  }

  getById(id: string): Observable<MedicationResponse> {
    return this.http.get<MedicationResponse>(`${this.url}/${id}`);
  }

  create(body: MedicationCreateRequest): Observable<MedicationResponse> {
    return this.http.post<MedicationResponse>(this.url, body);
  }

  update(id: string, body: MedicationUpdateRequest): Observable<MedicationResponse> {
    return this.http.put<MedicationResponse>(`${this.url}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

function appendIfDefined(p: HttpParams, key: string, value: string | undefined | null): HttpParams {
  return value !== undefined && value !== null && value !== '' ? p.set(key, value) : p;
}

function appendList(p: HttpParams, key: string, values?: number[]): HttpParams {
  if (!values || values.length === 0) return p;
  let out = p;
  for (const v of values) out = out.append(key, String(v));
  return out;
}
