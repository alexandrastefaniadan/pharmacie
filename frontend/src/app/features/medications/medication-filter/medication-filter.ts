import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
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
 * "Big filter" panel: free-text + 4 multi-selects + parapharmacy toggle.
 * Emits a {@link MedicationFilter} every time the user changes anything
 * (text input is debounced 300 ms to avoid spamming the backend).
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

  @Output() readonly filterChange = new EventEmitter<MedicationFilter>();

  protected readonly q = signal<string>('');
  protected formIds: number[] = [];
  protected ageGroupIds: number[] = [];
  protected indicationIds: number[] = [];
  protected therapeuticClassIds: number[] = [];
  protected parapharmacy = false;

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


