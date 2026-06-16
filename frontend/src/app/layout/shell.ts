import { Component, computed, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../core/services/auth';
import { CoffeeMark } from '../shared/coffee-mark';

@Component({
  selector: 'rl-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CoffeeMark],
  template: `
    <div class="flex min-h-screen">
      <aside class="flex w-64 flex-col border-r border-line bg-panel">
        <div class="px-5 pb-4 pt-6">
          <div class="flex items-center gap-2.5">
            <rl-coffee-mark [size]="30" class="text-ink-soft" />
            <span class="font-display text-[15px] font-bold leading-none tracking-tight text-ink">ROAST&nbsp;LOG</span>
          </div>
          <div class="eyebrow mt-2.5">Trazabilidad de café</div>
        </div>
        <div class="roast-spectrum mx-5 h-px opacity-70"></div>

        <nav class="flex-1 space-y-0.5 px-3 py-4">
          <a routerLink="/dashboard" routerLinkActive="bg-ink/[0.06] !text-ink font-medium"
             class="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-ink-soft transition hover:bg-ink/5 hover:text-ink">
            <span class="font-mono text-[10px] text-ink-soft/60">—</span> Resumen
          </a>
          <div class="px-3 pb-1 pt-4"><span class="eyebrow">Cadena de trazabilidad</span></div>
          <a routerLink="/fincas" routerLinkActive="bg-ink/[0.06] !text-ink font-medium"
             class="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-ink-soft transition hover:bg-ink/5 hover:text-ink">
            <span class="font-mono text-[10px] text-roast-light">01</span> Fincas de origen
          </a>
          <a routerLink="/lotes-verdes" routerLinkActive="bg-ink/[0.06] !text-ink font-medium"
             class="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-ink-soft transition hover:bg-ink/5 hover:text-ink">
            <span class="font-mono text-[10px] text-roast-medium">02</span> Lotes de café verde
          </a>
          <a routerLink="/lotes-tostados" routerLinkActive="bg-ink/[0.06] !text-ink font-medium"
             class="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-ink-soft transition hover:bg-ink/5 hover:text-ink">
            <span class="font-mono text-[10px] text-roast-dark">03</span> Lotes tostados
          </a>
        </nav>

        <div class="border-t border-line p-4">
          <div class="mb-3 flex items-center gap-2.5">
            <span class="grid h-8 w-8 place-items-center rounded-full bg-verdigris/15 font-mono text-xs font-semibold uppercase text-verdigris-deep">{{ inicial() }}</span>
            <div class="leading-tight">
              <div class="text-sm font-medium text-ink">{{ auth.user() }}</div>
              <div class="font-mono text-[10px] uppercase tracking-wide text-ink-soft">{{ auth.rol() }}</div>
            </div>
          </div>
          <button (click)="salir()"
            class="w-full rounded-md border border-line py-2 font-mono text-[11px] uppercase tracking-wide text-ink-soft transition hover:bg-ink/5 hover:text-ink">
            Cerrar sesión
          </button>
        </div>
      </aside>

      <main class="flex-1 overflow-auto">
        <div class="roast-spectrum h-1"></div>
        <div class="px-8 py-7"><router-outlet /></div>
      </main>
    </div>
  `,
})
export class Shell {
  auth = inject(AuthService);
  private router = inject(Router);
  inicial = computed(() => (this.auth.user() ?? '?').charAt(0));

  salir() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
