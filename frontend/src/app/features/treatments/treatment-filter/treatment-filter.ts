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
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

import { LookupsApi } from '@core/api/lookups.api';
import { TreatmentFilter } from '@core/models/treatment.model';

/**
 * Simple filter panel for the treatments page: free-text search + indication
 * multi-select. The usage-type is NOT shown here — it is fixed by the parent
 * page (HUMAN or VETERINARY) and included in the emitted filter automatically.
 */
@Component({
  selector: 'app-treatment-filter',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MultiSelectModule,
    InputTextModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
  ],
  templateUrl: './treatment-filter.html',
  styleUrl: './treatment-filter.scss',
})
export class TreatmentFilterComponent {
  protected readonly lookups = inject(LookupsApi);

  @Output() readonly filterChange = new EventEmitter<TreatmentFilter>();

  protected readonly q = signal<string>('');
  protected indicationIds: number[] = [];

  private textTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    this.lookups.loadAll();
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
      indicationIds: this.indicationIds.length ? this.indicationIds : undefined,
    });
  }

  reset(): void {
    this.q.set('');
    this.indicationIds = [];
    this.emit();
  }
}

