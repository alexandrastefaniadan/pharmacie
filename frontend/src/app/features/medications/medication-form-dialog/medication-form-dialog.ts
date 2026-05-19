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
import { SelectModule } from 'primeng/select';
import { MultiSelectModule } from 'primeng/multiselect';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageService } from 'primeng/api';

import { LookupsApi } from '@core/api/lookups.api';
import { MedicationsApi } from '@core/api/medications.api';
import {
  MedicationCreateRequest,
  MedicationResponse,
  MedicationUpdateRequest,
} from '@core/models/medication.model';
import { PriceTierComponent } from '@shared/price-tier/price-tier';

/**
 * Create / edit dialog. Used in two modes:
 * <ul>
 *   <li><strong>Create</strong>: pass {@code medication = null}.</li>
 *   <li><strong>Edit</strong>: pass an existing {@link MedicationResponse}.</li>
 * </ul>
 *
 * Emits {@code saved} on successful POST / PUT and closes itself.
 * All errors are handled by the global HTTP interceptor (toast).
 */
@Component({
  selector: 'app-medication-form-dialog',
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
    SelectModule,
    MultiSelectModule,
    CheckboxModule,
    PriceTierComponent,
  ],
  templateUrl: './medication-form-dialog.html',
  styleUrl: './medication-form-dialog.scss',
})
export class MedicationFormDialogComponent implements OnInit {
  protected readonly lookups = inject(LookupsApi);
  private readonly api = inject(MedicationsApi);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(MessageService);

  /** Two-way bound by parent to show / hide. */
  @Input() visible = false;
  @Output() readonly visibleChange = new EventEmitter<boolean>();

  /** null → create mode; otherwise edit mode. */
  @Input() medication: MedicationResponse | null = null;

  @Output() readonly saved = new EventEmitter<MedicationResponse>();

  protected readonly saving = signal(false);

  protected readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    inn: ['', Validators.maxLength(255)],
    dosage: ['', Validators.maxLength(80)],
    description: [''],
    parapharmacy: [false],
    priceTier: [0],
    formId: [null as number | null],
    ageGroupIds: [[] as number[]],
    therapeuticClassIds: [[] as number[]],
    indicationIds: [[] as number[]],
  });

  ngOnInit(): void {
    this.lookups.loadAll();
    if (this.medication) {
      const m = this.medication;
      this.form.reset({
        name: m.name,
        inn: m.inn ?? '',
        dosage: m.dosage ?? '',
        description: m.description ?? '',
        parapharmacy: m.parapharmacy,
        priceTier: m.priceTier ?? 0,
        formId: m.form?.id ?? null,
        ageGroupIds: m.ageGroups.map((a) => a.id),
        therapeuticClassIds: m.therapeuticClasses.map((c) => c.id),
        indicationIds: m.indications.map((i) => i.id),
      });
    }
  }

  save(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();

    const base = {
      name: raw.name.trim(),
      inn: raw.inn?.trim() || null,
      dosage: raw.dosage?.trim() || null,
      description: raw.description?.trim() || null,
      parapharmacy: !!raw.parapharmacy,
      priceTier: typeof raw.priceTier === 'number' ? raw.priceTier : 0,
      formId: raw.formId,
      ageGroupIds: raw.ageGroupIds ?? [],
      therapeuticClassIds: raw.therapeuticClassIds ?? [],
      indicationIds: raw.indicationIds ?? [],
    };

    this.saving.set(true);

    const call$ = this.medication
      ? this.api.update(this.medication.id, {
          ...base,
          version: this.medication.version,
        } as MedicationUpdateRequest)
      : this.api.create(base as MedicationCreateRequest);

    call$.subscribe({
      next: (saved) => {
        this.toast.add({
          severity: 'success',
          summary: this.medication ? 'Modifié' : 'Créé',
          detail: saved.name,
          life: 3000,
        });
        this.saving.set(false);
        this.saved.emit(saved);
        this.close();
      },
      error: () => this.saving.set(false), // toast shown by error interceptor
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

