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
    {
      label: 'Traitements',
      icon: 'pi pi-list-check',
      items: [
        { label: 'Traitements humains',      icon: 'pi pi-user',       routerLink: '/treatments' },
        { label: 'Traitements vétérinaires', icon: 'pi pi-wave-pulse',  routerLink: '/veterinary-treatments' },
      ],
    },
    { label: 'Catégories', icon: 'pi pi-sliders-h', routerLink: '/categories' },
  ]);

  constructor() {
    // Probe the server once on startup so a page refresh keeps the user
    // logged in (the cookie is still valid).
    this.auth.refreshMe().subscribe();

    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      const url = this.router.url;
      this.showChrome.set(!url.startsWith('/login'));
      this.menuItems.update((items) => applyActiveClass(items, url));
    });
  }

  logout(): void {
    this.auth.logout().subscribe(() => this.router.navigate(['/login']));
  }
}

/**
 * Recursively marks menu items (and sub-items) with the 'menu-active' CSS
 * class when their routerLink matches the current URL.
 */
function applyActiveClass(items: import('primeng/api').MenuItem[], url: string): import('primeng/api').MenuItem[] {
  return items.map((it) => ({
    ...it,
    styleClass: it['routerLink'] && url.startsWith(it['routerLink'] as string) ? 'menu-active' : '',
    items: it.items ? applyActiveClass(it.items, url) : it.items,
  }));
}

