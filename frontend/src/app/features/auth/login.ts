import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'rl-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="grid min-h-screen lg:grid-cols-2">
      <!-- Panel de identidad -->
      <div class="relative hidden flex-col justify-between overflow-hidden bg-ink p-10 text-paper lg:flex">
        <div class="absolute inset-y-0 left-0 w-1.5 roast-spectrum"></div>
        <div class="flex items-center gap-3">
          <span class="roast-spectrum h-8 w-8 rounded-md"></span>
          <span class="font-display text-lg font-bold tracking-tight">ROAST&nbsp;LOG</span>
        </div>
        <div class="max-w-md">
          <div class="eyebrow !text-paper/50">Café de especialidad</div>
          <h1 class="mt-3 font-display text-4xl font-semibold leading-tight">
            De la finca a la taza, cada lote con su historia.
          </h1>
          <p class="mt-4 text-sm leading-relaxed text-paper/60">
            Registro de fincas de origen, lotes de café verde y tandas de tostado —
            con control de merma, stock y trazabilidad completa.
          </p>
          <div class="mt-8">
            <div class="roast-spectrum h-2 w-full rounded-full"></div>
            <div class="mt-2 flex justify-between font-mono text-[10px] uppercase tracking-wider text-paper/40">
              <span>Light</span><span>Medium</span><span>Dark</span>
            </div>
          </div>
        </div>
        <div class="font-mono text-[11px] text-paper/30">v1.0 · trazabilidad-cafe</div>
      </div>

      <!-- Formulario -->
      <div class="flex items-center justify-center p-8">
        <div class="w-full max-w-sm">
          <div class="lg:hidden mb-8 flex items-center gap-2.5">
            <span class="roast-spectrum h-7 w-7 rounded-md"></span>
            <span class="font-display text-base font-bold tracking-tight text-ink">ROAST&nbsp;LOG</span>
          </div>
          <div class="eyebrow">Acceso</div>
          <h2 class="mt-2 font-display text-2xl font-semibold text-ink">Inicia sesión</h2>
          <p class="mt-1 text-sm text-ink-soft">Introduce tus credenciales para continuar.</p>

          <form [formGroup]="form" (ngSubmit)="enviar()" class="mt-7 space-y-4">
            <label class="block">
              <span class="eyebrow">Usuario</span>
              <input formControlName="username" autocomplete="username"
                class="mt-1.5 w-full rounded-md border border-line bg-panel px-3 py-2.5 text-sm text-ink outline-none transition focus:border-verdigris" />
            </label>
            <label class="block">
              <span class="eyebrow">Contraseña</span>
              <input type="password" formControlName="password" autocomplete="current-password"
                class="mt-1.5 w-full rounded-md border border-line bg-panel px-3 py-2.5 text-sm text-ink outline-none transition focus:border-verdigris" />
            </label>

            @if (error()) {
              <p class="rounded-md border border-clay/30 bg-clay/5 px-3 py-2 text-sm text-clay">{{ error() }}</p>
            }

            <button type="submit" [disabled]="form.invalid || cargando()"
              class="w-full rounded-md bg-verdigris py-2.5 text-sm font-medium text-paper transition hover:bg-verdigris-deep disabled:opacity-50">
              {{ cargando() ? 'Verificando…' : 'Entrar' }}
            </button>
          </form>

          <div class="mt-6 rounded-md border border-line bg-panel/60 px-3 py-2.5">
            <div class="eyebrow">Cuentas demo</div>
            <div class="mt-1.5 grid grid-cols-2 gap-2 font-mono text-[11px] text-ink-soft">
              <span>admin / admin123</span><span class="text-right">ADMIN</span>
              <span>user / user123</span><span class="text-right">solo lectura</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  cargando = signal(false);
  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  enviar() {
    if (this.form.invalid) return;
    this.cargando.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error.set('Credenciales inválidas. Revisa usuario y contraseña.');
        this.cargando.set(false);
      },
    });
  }
}
