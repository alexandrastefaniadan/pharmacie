import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '@env/environment';

export interface UserInfo {
  username: string;
  roles: string[];
}

/**
 * Session state for the SPA. Holds the currently-authenticated user (if any)
 * and exposes login / logout helpers. The actual session is server-side
 * (HTTP-only cookie); this service only mirrors the server's view so
 * components can render conditionally.
 */
@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/auth`;

  private readonly _user = signal<UserInfo | null>(null);
  private readonly _checked = signal(false);

  /** {@code null} when logged out, {@link UserInfo} when logged in. */
  readonly user = computed(() => this._user());
  readonly isAuthenticated = computed(() => this._user() !== null);
  /** {@code true} once we've at least tried {@link refreshMe} once. */
  readonly checked = computed(() => this._checked());

  /** Probe the server for an existing session. Safe to call repeatedly. */
  refreshMe(): Observable<UserInfo | null> {
    return this.http.get<UserInfo>(`${this.base}/me`, { withCredentials: true }).pipe(
      tap((u) => {
        this._user.set(u);
        this._checked.set(true);
      }),
      catchError(() => {
        this._user.set(null);
        this._checked.set(true);
        return of(null);
      }),
    );
  }

  login(username: string, password: string): Observable<UserInfo> {
    return this.http
      .post<UserInfo>(
        `${this.base}/login`,
        { username, password },
        { withCredentials: true },
      )
      .pipe(tap((u) => this._user.set(u)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.base}/logout`, null, { withCredentials: true })
      .pipe(tap(() => this._user.set(null)));
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(
      `${this.base}/change-password`,
      { currentPassword, newPassword },
      { withCredentials: true },
    );
  }
}

