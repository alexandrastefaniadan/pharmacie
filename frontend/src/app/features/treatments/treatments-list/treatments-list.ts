import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  ViewChild,
  inject,
  input,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

import { TreatmentsApi } from '@core/api/treatments.api';
import { TreatmentFilter, TreatmentResponse } from '@core/models/treatment.model';
import { UsageType } from '@core/models/usage-type.model';
import { Page } from '@core/models/page.model';
import { JoinLabelsPipe } from '@shared/pipes/join-labels/join-labels.pipe';
import { TreatmentFilterComponent } from '../treatment-filter/treatment-filter';
import { TreatmentFormDialogComponent } from '../treatment-form-dialog/treatment-form-dialog';

/**
 * Parametric treatments list page: the same component is used for human
 * treatments ({@code usageType = "HUMAN"}) and veterinary treatments
 * ({@code usageType = "VETERINARY"}). The parent route supplies the value
 * via {@code route.data} + {@code withComponentInputBinding()}.
 */
@Component({
  selector: 'app-treatments-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    TagModule,
    TooltipModule,
    SkeletonModule,
    ConfirmDialogModule,
    JoinLabelsPipe,
    TreatmentFilterComponent,
    TreatmentFormDialogComponent,
  ],
  templateUrl: './treatments-list.html',
  styleUrl: './treatments-list.scss',
})
export class TreatmentsListPage implements OnInit {
  private readonly api = inject(TreatmentsApi);
  private readonly toast = inject(MessageService);
  private readonly confirm = inject(ConfirmationService);

  /** Supplied by the route's {@code data} property via {@code withComponentInputBinding()}. */
  readonly usageType = input<UsageType>('HUMAN');

  @ViewChild('dt') private table?: Table;

  protected readonly rows = signal<TreatmentResponse[]>([]);
  protected readonly total = signal(0);
  protected readonly pageSize = signal(50);
  protected readonly loading = signal(false);

  private filter: TreatmentFilter = {};

  protected readonly editing = signal<TreatmentResponse | null>(null);
  protected readonly showDialog = signal(false);

  protected get showDialogModel(): boolean { return this.showDialog(); }
  protected set showDialogModel(v: boolean) { this.showDialog.set(v); }

  protected get pageTitle(): string {
    return this.usageType() === 'VETERINARY'
      ? 'Traitements vétérinaires'
      : 'Traitements';
  }

  ngOnInit(): void {
    // The filter component emits on construct, which triggers loadLazy via
    // onFilterChange. Nothing extra needed here.
  }

  loadLazy(event: TableLazyLoadEvent): void {
    const size = event.rows ?? this.pageSize();
    const page = Math.floor((event.first ?? 0) / size);
    this.pageSize.set(size);

    const sort: string[] = [];
    if (event.sortField) {
      const dir = (event.sortOrder ?? 1) === 1 ? 'asc' : 'desc';
      sort.push(`${event.sortField as string},${dir}`);
      if (event.sortField !== 'name') sort.push('name,asc');
    }

    this.loading.set(true);
    this.api
      .search({ ...this.filter, usageType: this.usageType() }, { page, size, sort })
      .subscribe({
        next: (p: Page<TreatmentResponse>) => {
          this.rows.set(p.content);
          this.total.set(p.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  onFilterChange(filter: TreatmentFilter): void {
    this.filter = filter;
    if (this.table) {
      this.table.first = 0;
      this.table.reset();
    }
  }

  openCreate(): void {
    this.editing.set(null);
    this.showDialog.set(true);
  }

  openEdit(t: TreatmentResponse): void {
    this.editing.set(t);
    this.showDialog.set(true);
  }

  onSaved(): void {
    this.reload();
  }

  confirmDelete(t: TreatmentResponse): void {
    this.confirm.confirm({
      header: 'Supprimer ce traitement ?',
      message: `« ${t.name} » sera retiré de la liste.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.api.delete(t.id).subscribe({
          next: () => {
            this.toast.add({
              severity: 'success',
              summary: 'Supprimé',
              detail: t.name,
              life: 3000,
            });
            this.reload();
          },
        });
      },
    });
  }

  /** Join medication names into a comma-separated string for display. */
  protected medNames(t: TreatmentResponse): string {
    return t.medications.map((m) => m.name).join(', ');
  }

  private reload(): void {
    if (this.table) this.table.reset();
  }
}

