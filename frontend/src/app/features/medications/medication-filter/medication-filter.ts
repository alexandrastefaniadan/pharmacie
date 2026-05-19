import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import { LookupsApi } from '@core/api/lookups.api';
import { MedicationsApi } from '@core/api/medications.api';
import { LookupDto } from '@core/models/lookup.model';
import { MedicationFacets, MedicationFilter } from '@core/models/medication.model';

/** A lookup option enriched with the facet count and a "disabled" flag. */
interface FacetOption extends LookupDto {
  count: number;
  disabled: boolean;
}

/**
 * "Big filter" panel: free-text + 4 multi-selects + parapharmacy toggle.
 * Emits a {@link MedicationFilter} every time the user changes anything
 * (text input is debounced 300 ms to avoid spamming the backend).
 *
 * <p>After every emit, also fetches cascading-filter facets from the backend
 * and decorates each multi-select's options so that:
 *   - the current count is appended to the label ("Sirop (12)")
 *   - options that would yield zero results are disabled (greyed out).
 */
@Component({
  selector: 'app-medication-filter',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MultiSelectModule,
    InputTextModule,
    ButtonModule,
    CheckboxModule,
    IconFieldModule,
    InputIconModule,
  ],
  templateUrl: './medication-filter.html',
  styleUrl: './medication-filter.scss',
})
export class MedicationFilterComponent {
  protected readonly lookups = inject(LookupsApi);
  private readonly medsApi = inject(MedicationsApi);

  @Output() readonly filterChange = new EventEmitter<MedicationFilter>();

  protected readonly q = signal<string>('');
  protected formIds: number[] = [];
  protected ageGroupIds: number[] = [];
  protected indicationIds: number[] = [];
  protected therapeuticClassIds: number[] = [];
  protected parapharmacy = false;

  /** Latest facet counts from the backend, keyed by lookup id per dimension. */
  private readonly facets = signal<MedicationFacets>({
    forms: [],
    ageGroups: [],
    therapeuticClasses: [],
    indications: [],
  });

  // Derived option lists for each multi-select: original lookup + count + disabled.
  protected readonly formOptions = computed(() =>
    decorate(this.lookups.forms(), this.facets().forms, this.formIds),
  );
  protected readonly ageGroupOptions = computed(() =>
    decorate(this.lookups.ageGroups(), this.facets().ageGroups, this.ageGroupIds),
  );
  protected readonly indicationOptions = computed(() =>
    decorate(this.lookups.indications(), this.facets().indications, this.indicationIds),
  );
  protected readonly therapeuticClassOptions = computed(() =>
    decorate(
      this.lookups.therapeuticClasses(),
      this.facets().therapeuticClasses,
      this.therapeuticClassIds,
    ),
  );

  private textTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    this.lookups.loadAll();
    // Emit an initial empty filter so the parent loads page 0.
    queueMicrotask(() => this.emit());
  }

  onTextChange(value: string): void {
    this.q.set(value);
    if (this.textTimer) clearTimeout(this.textTimer);
    this.textTimer = setTimeout(() => this.emit(), 300);
  }

  emit(): void {
    const filter: MedicationFilter = {
      q: this.q().trim() || undefined,
      formIds: nonEmpty(this.formIds),
      ageGroupIds: nonEmpty(this.ageGroupIds),
      indicationIds: nonEmpty(this.indicationIds),
      therapeuticClassIds: nonEmpty(this.therapeuticClassIds),
      parapharmacy: this.parapharmacy || undefined,
    };
    this.filterChange.emit(filter);
    this.refreshFacets(filter);
  }

  reset(): void {
    this.q.set('');
    this.formIds = [];
    this.ageGroupIds = [];
    this.indicationIds = [];
    this.therapeuticClassIds = [];
    this.parapharmacy = false;
    this.emit();
  }

  private refreshFacets(filter: MedicationFilter): void {
    this.medsApi.facets(filter).subscribe((f) => this.facets.set(f));
  }
}

function nonEmpty<T>(arr: T[]): T[] | undefined {
  return arr.length === 0 ? undefined : arr;
}

/**
 * Merge the static lookup list with the latest facet counts and hide options
 * that have no matching medication. Currently-selected options are always kept
 * (even at count 0) so the user can still un-select them.
 */
function decorate(
  all: LookupDto[],
  counts: { id: number; count: number }[],
  selectedIds: number[],
): FacetOption[] {
  const byId = new Map(counts.map((c) => [c.id, c.count]));
  const selected = new Set(selectedIds);
  return all
    .map((o) => {
      const count = byId.get(o.id!) ?? 0;
      return {
        ...o,
        count,
        disabled: false,
        labelFr: count > 0 ? `${o.labelFr} (${count})` : o.labelFr,
      };
    })
    .filter((o) => o.count > 0 || selected.has(o.id!));
}
