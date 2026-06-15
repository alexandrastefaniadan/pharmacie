import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { MessageService } from 'primeng/api';

import { LookupsApi } from '@core/api/lookups.api';
import { MedicationsApi } from '@core/api/medications.api';
import { TreatmentsApi } from '@core/api/treatments.api';
import {
  TreatmentCreateRequest,
  TreatmentResponse,
  TreatmentUpdateRequest,
} from '@core/models/treatment.model';
import { MedicationResponse } from '@core/models/medication.model';
import { UsageType } from '@core/models/usage-type.model';

/**
 * Create / edit dialog for a treatment. Used in two modes:
 * - Create: pass {@code treatment = null}.
 * - Edit:   pass an existing {@link TreatmentResponse}.
 *
 * The {@code usageType} is fixed by the parent page — it is never shown in
 * the form, which prevents accidentally creating mixed bundles.
 *
 * The medication picker pre-loads all medications of the matching usage type
 * once when the dialog opens (client-side multiselect approach — fast for a
 * stock up to ~2 000 meds). The options are filtered by the same usageType
 * so only matching medications are selectable.
 */
@Component({
  selector: 'app-treatment-form-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    MultiSelectModule,
  ],
  templateUrl: './treatment-form-dialog.html',
  styleUrl: './treatment-form-dialog.scss',
})
export class TreatmentFormDialogComponent implements OnInit {
  protected readonly lookups = inject(LookupsApi);
  private readonly api = inject(TreatmentsApi);
  private readonly medsApi = inject(MedicationsApi);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(MessageService);

  @Input() visible = false;
  @Output() readonly visibleChange = new EventEmitter<boolean>();

  /** null → create mode. */
  @Input() treatment: TreatmentResponse | null = null;

  /** Fixed by the parent page. Never displayed in the form. */
  @Input() usageType: UsageType = 'HUMAN';

  @Output() readonly saved = new EventEmitter<TreatmentResponse>();

  protected readonly saving = signal(false);
  /** Medication options available for the current usageType. */
  protected readonly medicationOptions = signal<MedicationResponse[]>([]);
  protected readonly medsLoading = signal(false);

  protected readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    notes: [''],
    medicationIds: [[] as string[], Validators.required],
    indicationIds: [[] as number[]],
  });

  ngOnInit(): void {
    this.lookups.loadAll();
    this.loadMedications();

    if (this.treatment) {
      const t = this.treatment;
      this.form.reset({
        name: t.name,
        description: t.description ?? '',
        notes: t.notes ?? '',
        medicationIds: t.medications.map((m) => m.id),
        indicationIds: t.indications.map((i) => i.id),
      });
    }
  }

  private loadMedications(): void {
    this.medsLoading.set(true);
    this.medsApi
      .search({ usageType: this.usageType }, { page: 0, size: 2000, sort: ['name,asc'] })
      .subscribe({
        next: (page) => {
          this.medicationOptions.set(page.content);
          this.medsLoading.set(false);
        },
        error: () => this.medsLoading.set(false),
      });
  }

  save(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();

    const medicationIds: string[] = raw.medicationIds ?? [];
    if (!medicationIds.length) return;

    const base: TreatmentCreateRequest = {
      name: raw.name.trim(),
      description: raw.description?.trim() || null,
      notes: raw.notes?.trim() || null,
      usageType: this.usageType,
      medicationIds,
      indicationIds: raw.indicationIds ?? [],
    };

    this.saving.set(true);

    const call$ = this.treatment
      ? this.api.update(this.treatment.id, {
          ...base,
          version: this.treatment.version,
        } as TreatmentUpdateRequest)
      : this.api.create(base);

    call$.subscribe({
      next: (result) => {
        this.toast.add({
          severity: 'success',
          summary: this.treatment ? 'Modifié' : 'Créé',
          detail: result.name,
          life: 3000,
        });
        this.saving.set(false);
        this.saved.emit(result);
        this.close();
      },
      error: () => this.saving.set(false),
    });
  }

  onCancel(): void {
    this.close();
  }

  private close(): void {
    this.visible = false;
    this.visibleChange.emit(false);
  }
}

