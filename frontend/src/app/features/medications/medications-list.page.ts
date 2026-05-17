import {
  ChangeDetectionStrategy,
  Component,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { SkeletonModule } from 'primeng/skeleton';

import { MedicationsApi } from '@core/api/medications.api';
import {
  MedicationFilter,
  MedicationResponse,
} from '@core/models/medication.model';
import { Page } from '@core/models/page.model';

import { MedicationFilterComponent } from './medication-filter.component';
import { MedicationFormDialogComponent } from './medication-form.dialog';
import { JoinLabelsPipe } from '@shared/pipes/join-labels.pipe';

/**
 * Main catalog screen.
 *
 * <p>The table uses PrimeNG's <strong>lazy mode</strong>: the backend always
 * returns one page at a time. Pagination, multi-column sort and the filter
 * panel all feed into a single {@link loadLazy} call that hits
 * {@code GET /api/v1/medications}.
 */
@Component({
  selector: 'app-medications-list-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    TagModule,
    ConfirmDialogModule,
    TooltipModule,
    SkeletonModule,
    MedicationFilterComponent,
    MedicationFormDialogComponent,
    JoinLabelsPipe,
  ],
  template: `
    <div class="flex items-center justify-between mb-3">
      <h2 style="margin:0">Catalogue des médicaments</h2>
      <p-button
        label="Ajouter"
        icon="pi pi-plus"
        (onClick)="openCreate()"
      />
    </div>

    <app-medication-filter (filterChange)="onFilterChange($event)" />

    <p-table
      #dt
      [value]="rows()"
      [lazy]="true"
      (onLazyLoad)="loadLazy($event)"
      dataKey="id"
      [paginator]="true"
      [rows]="pageSize()"
      [totalRecords]="total()"
      [rowsPerPageOptions]="[25, 50, 100, 200]"
      [loading]="loading()"
      sortMode="single"
      [showCurrentPageReport]="true"
      currentPageReportTemplate="{first} – {last} sur {totalRecords}"
      [scrollable]="true"
      scrollHeight="flex"
      stripedRows
      responsiveLayout="scroll"
    >
      <ng-template pTemplate="header">
        <tr>
          <th pSortableColumn="name" style="min-width: 14rem">
            Nom <p-sortIcon field="name" />
          </th>
          <th pSortableColumn="form.labelFr" style="min-width: 9rem">
            Forme <p-sortIcon field="form.labelFr" />
          </th>
          <th pSortableColumn="dosage" style="min-width: 7rem">
            Dosage <p-sortIcon field="dosage" />
          </th>
          <th pSortableColumn="ageGroup.labelFr" style="min-width: 9rem">
            Âge <p-sortIcon field="ageGroup.labelFr" />
          </th>
          <th style="min-width: 14rem">Indications</th>
          <th style="min-width: 14rem">Classes thérapeutiques</th>
          <th pSortableColumn="updatedAt" style="min-width: 9rem">
            Modifié le <p-sortIcon field="updatedAt" />
          </th>
          <th style="width: 7rem; text-align: right">Actions</th>
        </tr>
      </ng-template>

      <ng-template pTemplate="body" let-m>
        <tr>
          <td>
            <div style="font-weight: 500">{{ m.name }}</div>
            @if (m.inn) {
              <div class="text-muted" style="font-size: 0.8125rem">{{ m.inn }}</div>
            }
          </td>
          <td>
            @if (m.form) {
              <p-tag [value]="m.form.labelFr" severity="info" />
            }
          </td>
          <td>{{ m.dosage }}</td>
          <td>{{ m.ageGroup?.labelFr }}</td>
          <td>{{ m.indications | joinLabels }}</td>
          <td>{{ m.therapeuticClasses | joinLabels }}</td>
          <td>{{ m.updatedAt | date: 'dd/MM/yy HH:mm' }}</td>
          <td style="text-align: right; white-space: nowrap">
            <p-button
              icon="pi pi-pencil"
              [text]="true"
              severity="secondary"
              pTooltip="Modifier"
              (onClick)="openEdit(m)"
            />
            <p-button
              icon="pi pi-trash"
              [text]="true"
              severity="danger"
              pTooltip="Supprimer"
              (onClick)="confirmDelete(m)"
            />
          </td>
        </tr>
      </ng-template>

      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="8" style="text-align: center; padding: 2rem">
            Aucun médicament trouvé.
          </td>
        </tr>
      </ng-template>
    </p-table>

    @if (showDialog()) {
      <app-medication-form-dialog
        [(visible)]="showDialogModel"
        [medication]="editing()"
        (saved)="onSaved()"
      />
    }
  `,
  styles: [
    `
      :host { display: block; }
    `,
  ],
})
export class MedicationsListPage {
  private readonly api = inject(MedicationsApi);
  private readonly toast = inject(MessageService);
  private readonly confirm = inject(ConfirmationService);

  @ViewChild('dt') private table?: Table;

  // ---- table state (signals) ----
  protected readonly rows = signal<MedicationResponse[]>([]);
  protected readonly total = signal(0);
  protected readonly pageSize = signal(50);
  protected readonly loading = signal(false);

  // ---- filter / dialog state ----
  private filter: MedicationFilter = {};
  protected readonly editing = signal<MedicationResponse | null>(null);
  protected readonly showDialog = signal(false);

  /** Bridge property for [(visible)] two-way binding on the dialog. */
  protected get showDialogModel(): boolean { return this.showDialog(); }
  protected set showDialogModel(v: boolean) { this.showDialog.set(v); }

  /** Called by PrimeNG on first render and every page/sort change. */
  loadLazy(event: TableLazyLoadEvent): void {
    const size = event.rows ?? this.pageSize();
    const page = Math.floor((event.first ?? 0) / size);
    this.pageSize.set(size);

    const sort: string[] = [];
    if (event.sortField) {
      const dir = (event.sortOrder ?? 1) === 1 ? 'asc' : 'desc';
      sort.push(`${event.sortField as string},${dir}`);
    }

    this.loading.set(true);
    this.api.search(this.filter, { page, size, sort }).subscribe({
      next: (p: Page<MedicationResponse>) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onFilterChange(filter: MedicationFilter): void {
    this.filter = filter;
    // Reset to page 0 whenever the filter changes.
    if (this.table) {
      this.table.first = 0;
      this.table.reset(); // triggers a new lazy load
    }
  }

  openCreate(): void {
    this.editing.set(null);
    this.showDialog.set(true);
  }

  openEdit(m: MedicationResponse): void {
    this.editing.set(m);
    this.showDialog.set(true);
  }

  onSaved(): void {
    this.reload();
  }

  confirmDelete(m: MedicationResponse): void {
    this.confirm.confirm({
      header: 'Supprimer ce médicament ?',
      message: `« ${m.name} » sera retiré du catalogue. Cette action est réversible côté base de données.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.api.delete(m.id).subscribe({
          next: () => {
            this.toast.add({
              severity: 'success',
              summary: 'Supprimé',
              detail: m.name,
              life: 3000,
            });
            this.reload();
          },
        });
      },
    });
  }

  private reload(): void {
    if (this.table) this.table.reset();
  }
}

