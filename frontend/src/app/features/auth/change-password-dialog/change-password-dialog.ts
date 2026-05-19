import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { MessageService } from 'primeng/api';

import { AuthApi } from '@core/api/auth.api';

/**
 * Modal that lets the signed-in user change their own password. The new
 * password must be at least 10 characters and different from the current one;
 * the confirmation field must match.
 */
@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    PasswordModule,
    MessageModule,
  ],
  templateUrl: './change-password-dialog.html',
  styleUrl: './change-password-dialog.scss',
})
export class ChangePasswordDialog {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthApi);
  private readonly toast = inject(MessageService);

  @Input() visible = false;
  @Output() readonly visibleChange = new EventEmitter<boolean>();

  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(10)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: [matchValidator] },
  );

  protected get visibleModel(): boolean { return this.visible; }
  protected set visibleModel(v: boolean) {
    this.visible = v;
    this.visibleChange.emit(v);
    if (!v) this.reset();
  }

  submit(): void {
    if (this.form.invalid || this.saving()) return;
    const { currentPassword, newPassword } = this.form.getRawValue();

    this.saving.set(true);
    this.error.set(null);

    this.auth.changePassword(currentPassword!, newPassword!).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.add({
          severity: 'success',
          summary: 'Mot de passe modifié',
          detail: 'Utilisez votre nouveau mot de passe à la prochaine connexion.',
          life: 4000,
        });
        this.visibleModel = false;
      },
      error: (err) => {
        this.saving.set(false);
        if (err.status === 401) {
          this.error.set('Le mot de passe actuel est incorrect.');
        } else if (err.status === 400) {
          this.error.set(
            err.error?.detail ??
              'Le nouveau mot de passe est invalide (au moins 10 caractères, différent de l\'actuel).',
          );
        } else {
          this.error.set('Une erreur est survenue. Veuillez réessayer.');
        }
      },
    });
  }

  private reset(): void {
    this.form.reset({ currentPassword: '', newPassword: '', confirmPassword: '' });
    this.error.set(null);
  }
}

/** Group-level validator: confirmPassword must equal newPassword. */
function matchValidator(group: AbstractControl) {
  const a = group.get('newPassword')?.value;
  const b = group.get('confirmPassword')?.value;
  return a && b && a !== b ? { mismatch: true } : null;
}

