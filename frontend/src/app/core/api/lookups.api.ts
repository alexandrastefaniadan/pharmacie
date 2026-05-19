import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, forkJoin, tap } from 'rxjs';
import { environment } from '@env/environment';
import { LookupDto } from '@core/models/lookup.model';

/** Which lookup table an operation targets. Values match the backend URL. */
export type LookupKind =
  | 'forms'
  | 'age-groups'
  | 'therapeutic-classes'
  | 'indications';

/** Body for POST/PUT on /api/v1/lookups/{kind}. */
export interface LookupWriteRequest {
  code?: string | null;
  labelFr: string;
  sortOrder?: number | null;
}

/**
 * Loads the 4 lookup lists from the backend and caches them in signals so
 * dropdowns can read them synchronously. Also exposes write operations
 * (create / update / delete) that refresh the cache on success.
 */
@Injectable({ providedIn: 'root' })
export class LookupsApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/lookups`;

  private readonly _forms = signal<LookupDto[]>([]);
  private readonly _ageGroups = signal<LookupDto[]>([]);
  private readonly _therapeuticClasses = signal<LookupDto[]>([]);
  private readonly _indications = signal<LookupDto[]>([]);
  private readonly _loaded = signal(false);

  readonly forms = computed(() => this._forms());
  readonly ageGroups = computed(() => this._ageGroups());
  readonly therapeuticClasses = computed(() => this._therapeuticClasses());
  readonly indications = computed(() => this._indications());
  readonly loaded = computed(() => this._loaded());

  /** Idempotent: call from any component, only hits the network once. */
  loadAll(): void {
    if (this._loaded()) return;
    this.refresh();
  }

  /** Force a re-fetch of all 4 lists (used after a write). */
  refresh(): Observable<void> {
    const obs = forkJoin({
      forms: this.http.get<LookupDto[]>(`${this.base}/forms`),
      ageGroups: this.http.get<LookupDto[]>(`${this.base}/age-groups`),
      therapeuticClasses: this.http.get<LookupDto[]>(`${this.base}/therapeutic-classes`),
      indications: this.http.get<LookupDto[]>(`${this.base}/indications`),
    }).pipe(
      tap(({ forms, ageGroups, therapeuticClasses, indications }) => {
        this._forms.set(forms);
        this._ageGroups.set(ageGroups);
        this._therapeuticClasses.set(therapeuticClasses);
        this._indications.set(indications);
        this._loaded.set(true);
      }),
    );
    // Fire immediately for callers that don't subscribe, but also let
    // callers subscribe to react when data is ready.
    const shared = new Observable<void>((sub) => {
      obs.subscribe({
        next: () => { sub.next(); sub.complete(); },
        error: (e) => sub.error(e),
      });
    });
    return shared;
  }

  /** Snapshot accessor for the given kind. */
  list(kind: LookupKind): LookupDto[] {
    switch (kind) {
      case 'forms': return this._forms();
      case 'age-groups': return this._ageGroups();
      case 'therapeutic-classes': return this._therapeuticClasses();
      case 'indications': return this._indications();
    }
  }

  create(kind: LookupKind, body: LookupWriteRequest): Observable<LookupDto> {
    return this.http.post<LookupDto>(`${this.base}/${kind}`, body)
      .pipe(tap(() => this.refresh().subscribe()));
  }

  update(kind: LookupKind, id: number, body: LookupWriteRequest): Observable<LookupDto> {
    return this.http.put<LookupDto>(`${this.base}/${kind}/${id}`, body)
      .pipe(tap(() => this.refresh().subscribe()));
  }

  delete(kind: LookupKind, id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${kind}/${id}`)
      .pipe(tap(() => this.refresh().subscribe()));
  }
}
