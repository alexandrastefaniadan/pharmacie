import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { filter } from 'rxjs/operators';

import { AuthApi } from '@core/api/auth.api';
import { ChangePasswordDialog } from '@features/auth/change-password-dialog/change-password-dialog';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MenubarModule,
    ButtonModule,
    ToastModule,
    ConfirmDialogModule,
    ChangePasswordDialog,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthApi);

  /** Hide the chrome (top bar, menu) on the login screen. */
  protected readonly showChrome = signal(true);

  /** Toggles the "Change password" modal. */
  protected readonly showChangePassword = signal(false);

  protected get showChangePasswordModel(): boolean { return this.showChangePassword(); }
  protected set showChangePasswordModel(v: boolean) { this.showChangePassword.set(v); }

  /** Initials used as the avatar fallback, e.g. "Alex" → "A". */
  protected readonly userInitial = computed(() =>
    (this.auth.user()?.username?.charAt(0) ?? '?').toUpperCase(),
  );

  protected readonly menuItems = signal<MenuItem[]>([
    { label: 'Médicaments', icon: 'pi pi-prescription', routerLink: '/medications' },
    { label: 'Catégories', icon: 'pi pi-sliders-h', routerLink: '/categories' },
  ]);

  constructor() {
    // Probe the server once on startup so a page refresh keeps the user
    // logged in (the cookie is still valid).
    this.auth.refreshMe().subscribe();

    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      const url = this.router.url;
      this.showChrome.set(!url.startsWith('/login'));
      this.menuItems.update((items) =>
        items.map((it) => ({
          ...it,
          styleClass: url.startsWith(it['routerLink'] as string) ? 'menu-active' : '',
        })),
      );
    });
  }

  logout(): void {
    this.auth.logout().subscribe(() => this.router.navigate(['/login']));
  }
}
