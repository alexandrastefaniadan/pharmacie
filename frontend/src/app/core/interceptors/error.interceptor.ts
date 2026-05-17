import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { catchError, throwError } from 'rxjs';

/**
 * Reads the RFC 7807 problem JSON returned by the backend and surfaces a
 * user-friendly toast. The original error is re-thrown so components can
 * still react (e.g. close a dialog only on success).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(MessageService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const problem = err.error as { title?: string; detail?: string } | null;
      const title = problem?.title ?? defaultTitle(err.status);
      const detail = problem?.detail ?? err.message ?? 'Une erreur est survenue.';

      toast.add({
        severity: err.status >= 500 ? 'error' : 'warn',
        summary: title,
        detail,
        life: 6000,
      });

      return throwError(() => err);
    }),
  );
};

function defaultTitle(status: number): string {
  if (status === 0) return 'Réseau';
  if (status === 401 || status === 403) return 'Accès refusé';
  if (status === 404) return 'Introuvable';
  if (status === 409) return 'Conflit';
  if (status >= 500) return 'Erreur serveur';
  return 'Erreur';
}

