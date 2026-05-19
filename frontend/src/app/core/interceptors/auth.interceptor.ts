import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthApi } from '@core/api/auth.api';

/**
 * Adds {@code withCredentials: true} to every request (so the session cookie
 * and XSRF token are sent), and redirects to {@code /login} on 401 responses
 * — except for the auth probe itself, which is allowed to fail silently.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth = inject(AuthApi);

  const withCreds = req.clone({ withCredentials: true });

  return next(withCreds).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.endsWith('/auth/me') && !req.url.endsWith('/auth/login')) {
        auth.logout().subscribe(); // best-effort local state clear
        router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      }
      return throwError(() => err);
    }),
  );
};

