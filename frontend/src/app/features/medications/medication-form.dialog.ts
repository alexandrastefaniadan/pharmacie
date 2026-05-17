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

/**
 * Create / edit dialog. Used in two modes:
 *  - create: pass {@code medication = null}
 *  - edit:   pass an existing {@link MedicationResponse}
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
  ],
  template: `
    <p-dialog
      [(visible)]="visible"
      [modal]="true"
      [closable]="true"
      [dismissableMask]="false"
      [style]="{ width: 'min(720px, 95vw)' }"
      [header]="medication ? 'Modifier le médicament' : 'Nouveau médicament'"
      (onHide)="onCancel()"
    >
      <form [formGroup]="form" class="form-grid">
        <div class="field full">
          <label>Nom *</label>
          <input pInputText type="text" formControlName="name" />
          @if (form.get('name')?.touched && form.get('name')?.invalid) {
            <small class="text-danger">Le nom est requis (255 caractères max).</small>
          }
        </div>

        <div class="field">
          <label>DCI (substance active)</label>
          <input pInputText type="text" formControlName="inn" />
        </div>

        <div class="field">
          <label>Dosage</label>
          <input pInputText type="text" formControlName="dosage" placeholder="ex. 500 mg" />
        </div>

        <div class="field">
          <label>Forme</label>
          <p-select
            [options]="lookups.forms()"
            optionLabel="labelFr"
            optionValue="id"
            formControlName="formId"
            placeholder="—"
            [filter]="true"
            [showClear]="true"
            appendTo="body"
            styleClass="w-full"
          />
        </div>

        <div class="field">
          <label>Âge</label>
          <p-select
            [options]="lookups.ageGroups()"
            optionLabel="labelFr"
            optionValue="id"
            formControlName="ageGroupId"
            placeholder="—"
            [showClear]="true"
            appendTo="body"
            styleClass="w-full"
          />
        </div>

        <div class="field full">
          <label>Classes thérapeutiques</label>
          <p-multiSelect
            [options]="lookups.therapeuticClasses()"
            optionLabel="labelFr"
            optionValue="id"
            formControlName="therapeuticClassIds"
            placeholder="—"
            [filter]="true"
            appendTo="body"
            styleClass="w-full"
          />
        </div>

        <div class="field full">
          <label>Indications / usages</label>
          <p-multiSelect
            [options]="lookups.indications()"
            optionLabel="labelFr"
            optionValue="id"
            formControlName="indicationIds"
            placeholder="—"
            [filter]="true"
            appendTo="body"
            styleClass="w-full"
          />
        </div>

        <div class="field full">
          <label>Description</label>
          <textarea
            pTextarea
            rows="3"
            formControlName="description"
            placeholder="Notes internes, posologie, contre-indications…"
          ></textarea>
        </div>

        <label class="field flex items-center gap-2">
          <p-checkbox formControlName="parapharmacy" [binary]="true" />
          <span>Parapharmacie</span>
        </label>
      </form>

      <ng-template pTemplate="footer">
        <p-button label="Annuler" severity="secondary" [text]="true" (onClick)="onCancel()" />
        <p-button
          [label]="medication ? 'Enregistrer' : 'Créer'"
          icon="pi pi-check"
          [disabled]="form.invalid || saving()"
          [loading]="saving()"
          (onClick)="save()"
        />
      </ng-template>
    </p-dialog>
  `,
  styles: [
    `
      .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 0.75rem 1rem;
      }
      .field { display: flex; flex-direction: column; gap: 0.25rem; }
      .field.full { grid-column: 1 / -1; }
      .field label { font-weight: 500; font-size: 0.875rem; }
      .text-danger { color: var(--p-red-500); }
    `,
  ],
})
export class MedicationFormDialogComponent implements OnInit {
  protected readonly lookups = inject(LookupsApi);
  private readonly api = inject(MedicationsApi);
  private readonly fb = inject(FormBuilder);
  private readonly toast = inject(MessageService);

  /** Two-way bound by parent to show / hide. */
  @Input() visible = false;
  @Output() readonly visibleChange = new EventEmitter<boolean>();

  /** null = create mode; otherwise edit mode. */
  @Input() medication: MedicationResponse | null = null;

  @Output() readonly saved = new EventEmitter<MedicationResponse>();

  protected readonly saving = signal(false);

  protected readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    inn: ['', Validators.maxLength(255)],
    dosage: ['', Validators.maxLength(80)],
    description: [''],
    parapharmacy: [false],
    formId: [null as number | null],
    ageGroupId: [null as number | null],
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
        formId: m.form?.id ?? null,
        ageGroupId: m.ageGroup?.id ?? null,
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
      formId: raw.formId,
      ageGroupId: raw.ageGroupId,
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
      error: () => this.saving.set(false), // toast already shown by error interceptor
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

