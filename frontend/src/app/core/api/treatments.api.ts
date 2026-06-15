import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import {
  TreatmentCreateRequest,
  TreatmentFilter,
  TreatmentResponse,
  TreatmentUpdateRequest,
} from '@core/models/treatment.model';
import { Page } from '@core/models/page.model';

/**
 * Thin wrapper around /api/v1/treatments. The only place in the app that
 * knows about HTTP for treatments. Returns plain typed observables.
 */
@Injectable({ providedIn: 'root' })
export class TreatmentsApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBaseUrl}/treatments`;

  search(
    filter: TreatmentFilter,
    pageReq: { page: number; size: number; sort?: string[] },
  ): Observable<Page<TreatmentResponse>> {
    let params = new HttpParams()
      .set('page', pageReq.page)
      .set('size', pageReq.size);

    (pageReq.sort ?? []).forEach((s) => (params = params.append('sort', s)));

    if (filter.q) params = params.set('q', filter.q);
    if (filter.usageType) params = params.set('usageType', filter.usageType);
    if (filter.indicationIds?.length) {
      filter.indicationIds.forEach((id) => (params = params.append('indicationIds', String(id))));
    }

    return this.http.get<Page<TreatmentResponse>>(this.url, { params });
  }

  getById(id: string): Observable<TreatmentResponse> {
    return this.http.get<TreatmentResponse>(`${this.url}/${id}`);
  }

  create(body: TreatmentCreateRequest): Observable<TreatmentResponse> {
    return this.http.post<TreatmentResponse>(this.url, body);
  }

  update(id: string, body: TreatmentUpdateRequest): Observable<TreatmentResponse> {
    return this.http.put<TreatmentResponse>(`${this.url}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

