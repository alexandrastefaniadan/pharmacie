import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
  computed,
  effect,
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
import { MedicationFilter } from '@core/models/medication.model';

/**
 * "Big filter" panel: free text + 4 multi-selects + parapharmacy checkbox.
 * Emits a {@link MedicationFilter} every time the user changes anything
 * (debounced for the text input to avoid spamming the backend).
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
  template: `
    <div class="filter-grid">
      <p-iconField iconPosition="left">
        <p-inputIcon styleClass="pi pi-search" />
        <input
          pInputText
          type="text"
          placeholder="Rechercher (nom, DCI)..."
          [ngModel]="q()"
          (ngModelChange)="onTextChange($event)"
          style="width: 100%"
        />
      </p-iconField>

      <p-multiSelect
        [options]="lookups.forms()"
        optionLabel="labelFr"
        optionValue="id"
        [(ngModel)]="formIds"
        (onChange)="emit()"
        placeholder="Forme"
        [filter]="true"
        [showClear]="true"
        appendTo="body"
        styleClass="w-full"
      />

      <p-multiSelect
        [options]="lookups.indications()"
        optionLabel="labelFr"
        optionValue="id"
        [(ngModel)]="indicationIds"
        (onChange)="emit()"
        placeholder="Indication"
        [filter]="true"
        [showClear]="true"
        appendTo="body"
        styleClass="w-full"
      />

      <p-multiSelect
        [options]="lookups.ageGroups()"
        optionLabel="labelFr"
        optionValue="id"
        [(ngModel)]="ageGroupIds"
        (onChange)="emit()"
        placeholder="Âge"
        [showClear]="true"
        appendTo="body"
        styleClass="w-full"
      />

      <p-multiSelect
        [options]="lookups.therapeuticClasses()"
        optionLabel="labelFr"
        optionValue="id"
        [(ngModel)]="therapeuticClassIds"
        (onChange)="emit()"
        placeholder="Classe thérapeutique"
        [filter]="true"
        [showClear]="true"
        appendTo="body"
        styleClass="w-full"
      />

      <label class="flex items-center gap-2">
        <p-checkbox
          [(ngModel)]="parapharmacy"
          [binary]="true"
          (onChange)="emit()"
        />
        <span>Parapharmacie</span>
      </label>

      <p-button
        label="Réinitialiser"
        icon="pi pi-times"
        severity="secondary"
        [text]="true"
        (onClick)="reset()"
      />
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 1rem;
        background: var(--p-surface-0);
        border: 1px solid var(--p-surface-200);
        border-radius: 0.5rem;
        margin-bottom: 1rem;
      }
      .filter-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 0.75rem;
        align-items: center;
      }
    `,
  ],
})
export class MedicationFilterComponent {
  protected readonly lookups = inject(LookupsApi);

  @Output() readonly filterChange = new EventEmitter<MedicationFilter>();

  protected readonly q = signal<string>('');
  protected formIds: number[] = [];
  protected ageGroupIds: number[] = [];
  protected indicationIds: number[] = [];
  protected therapeuticClassIds: number[] = [];
  protected parapharmacy = false;

  /** debounced text → effect that emits */
  private readonly textDebounced = computed(() => this.q());
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
    this.filterChange.emit({
      q: this.q().trim() || undefined,
      formIds: nonEmpty(this.formIds),
      ageGroupIds: nonEmpty(this.ageGroupIds),
      indicationIds: nonEmpty(this.indicationIds),
      therapeuticClassIds: nonEmpty(this.therapeuticClassIds),
      parapharmacy: this.parapharmacy || undefined,
    });
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
}

function nonEmpty<T>(arr: T[]): T[] | undefined {
  return arr.length === 0 ? undefined : arr;
}

