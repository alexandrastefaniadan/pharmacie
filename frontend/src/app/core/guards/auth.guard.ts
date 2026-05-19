import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';

import { AuthApi } from '@core/api/auth.api';

/**
 * Route guard: forwards to {@code /login} when no session exists. Calls
 * {@code /auth/me} the first time (lazy probe) to allow page refreshes.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthApi);
  const router = inject(Router);

  if (auth.isAuthenticated()) return true;

  return auth.refreshMe().pipe(
    map((u) => {
      if (u) return true;
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }),
  );
};

