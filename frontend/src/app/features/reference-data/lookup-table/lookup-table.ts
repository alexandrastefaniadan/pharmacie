import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';

import { LookupDto } from '@core/models/lookup.model';
import { LookupKind, LookupsApi, LookupWriteRequest } from '@core/api/lookups.api';

interface DialogModel {
  id: number | null;
  labelFr: string;
}

/**
 * Sortable table + create/edit modal + delete confirmation for one lookup
 * kind (forms, age groups, therapeutic classes or indications).
 *
 * <p>All four tabs reuse this single component; the parent only passes
 * {@code kind} + {@code rows} + a friendly {@code title}.
 */
@Component({
  selector: 'app-lookup-table',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ConfirmDialogModule,
    TooltipModule,
  ],
  templateUrl: './lookup-table.html',
  styleUrl: './lookup-table.scss',
})
export class LookupTableComponent {
  @Input({ required: true }) kind!: LookupKind;
  @Input({ required: true }) rows: LookupDto[] = [];
  @Input({ required: true }) title!: string;

  /** Emitted after any successful write so the parent can refresh counters. */
  @Output() readonly changed = new EventEmitter<void>();

  protected readonly showDialog = signal(false);
  protected readonly saving = signal(false);
  protected readonly model = signal<DialogModel>(emptyModel());

  constructor(
    private readonly api: LookupsApi,
    private readonly toast: MessageService,
    private readonly confirm: ConfirmationService,
  ) {}

  protected get dialogVisible(): boolean { return this.showDialog(); }
  protected set dialogVisible(v: boolean) { this.showDialog.set(v); }

  openCreate(): void {
    this.model.set(emptyModel());
    this.showDialog.set(true);
  }

  openEdit(row: LookupDto): void {
    this.model.set({ id: row.id, labelFr: row.labelFr });
    this.showDialog.set(true);
  }

  save(): void {
    const m = this.model();
    const label = m.labelFr?.trim();
    if (!label) return;

    // We only send the label; the backend auto-derives the code and keeps
    // the existing sortOrder (or defaults it to 100 on create).
    const body: LookupWriteRequest = { labelFr: label };

    this.saving.set(true);
    const obs = m.id == null
      ? this.api.create(this.kind, body)
      : this.api.update(this.kind, m.id, body);

    obs.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.showDialog.set(false);
        this.toast.add({
          severity: 'success',
          summary: m.id == null ? 'Créé' : 'Mis à jour',
          detail: saved.labelFr,
          life: 3000,
        });
        this.changed.emit();
      },
      error: () => this.saving.set(false),
    });
  }

  confirmDelete(row: LookupDto): void {
    this.confirm.confirm({
      header: `Supprimer « ${row.labelFr} » ?`,
      message:
        'Cet élément sera retiré de la liste. La suppression est refusée s\'il est encore utilisé par un médicament.',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.api.delete(this.kind, row.id).subscribe({
          next: () => {
            this.toast.add({
              severity: 'success',
              summary: 'Supprimé',
              detail: row.labelFr,
              life: 3000,
            });
            this.changed.emit();
          },
        });
      },
    });
  }
}

function emptyModel(): DialogModel {
  return { id: null, labelFr: '' };
}
