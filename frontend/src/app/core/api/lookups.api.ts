import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { forkJoin, tap } from 'rxjs';
import { environment } from '@env/environment';
import { LookupDto } from '@core/models/lookup.model';

/**
 * Loads the 4 lookup lists from the backend once and caches them in signals
 * so dropdowns can read them synchronously.
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
    forkJoin({
      forms: this.http.get<LookupDto[]>(`${this.base}/forms`),
      ageGroups: this.http.get<LookupDto[]>(`${this.base}/age-groups`),
      therapeuticClasses: this.http.get<LookupDto[]>(`${this.base}/therapeutic-classes`),
      indications: this.http.get<LookupDto[]>(`${this.base}/indications`),
    })
      .pipe(
        tap(({ forms, ageGroups, therapeuticClasses, indications }) => {
          this._forms.set(forms);
          this._ageGroups.set(ageGroups);
          this._therapeuticClasses.set(therapeuticClasses);
          this._indications.set(indications);
          this._loaded.set(true);
        }),
      )
      .subscribe();
  }
}

