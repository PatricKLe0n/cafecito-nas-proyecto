import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { DashboardService } from './dashboard-service';
import { Resumen } from '../../core/models';

@Component({
  selector: 'rl-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="eyebrow">Panel de operación</div>
    <h1 class="mt-1 font-display text-2xl font-semibold text-ink">Resumen</h1>

    @if (resumen(); as r) {
      <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div class="relative overflow-hidden rounded-card border border-line bg-panel p-5 shadow-panel">
          <span class="absolute inset-x-0 top-0 h-0.5" style="background:#5E7B53"></span>
          <div class="eyebrow">Café verde disponible</div>
          <div class="tnum mt-3 font-mono text-4xl font-semibold text-ink">{{ r.lotesVerdesDisponibles }}</div>
          <div class="mt-1 text-xs text-ink-soft">lotes en inventario</div>
        </div>

        <div class="relative overflow-hidden rounded-card border border-line bg-panel p-5 shadow-panel">
          <span class="absolute inset-x-0 top-0 h-0.5" style="background:#9C5B2E"></span>
          <div class="eyebrow">Tandas de tostado</div>
          <div class="tnum mt-3 font-mono text-4xl font-semibold text-ink">{{ r.lotesTostadosRegistrados }}</div>
          <div class="mt-1 text-xs text-ink-soft">lotes registrados</div>
        </div>

        <div class="relative overflow-hidden rounded-card border border-line bg-panel p-5 shadow-panel">
          <span class="absolute inset-x-0 top-0 h-0.5 roast-spectrum"></span>
          <div class="eyebrow">Merma media de tueste</div>
          <div class="tnum mt-3 font-mono text-4xl font-semibold text-roast-medium">
            {{ r.mermaMediaPorcentaje | number: '1.2-2' }}<span class="text-2xl text-ink-soft">%</span>
          </div>
          <div class="mt-1 text-xs text-ink-soft">pérdida de peso al tostar</div>
        </div>

        <div class="relative overflow-hidden rounded-card border border-line bg-panel p-5 shadow-panel">
          <span class="absolute inset-x-0 top-0 h-0.5" style="background:#2F6F62"></span>
          <div class="eyebrow">Fincas de origen</div>
          <div class="tnum mt-3 font-mono text-4xl font-semibold text-ink">{{ r.totalFincas }}</div>
          <div class="mt-1 text-xs text-ink-soft">orígenes registrados</div>
        </div>
      </div>

      <div class="mt-4 rounded-card border border-line bg-panel p-5 shadow-panel">
        <div class="eyebrow">Escala de tueste</div>
        <div class="roast-spectrum mt-3 h-3 w-full rounded-full"></div>
        <div class="mt-2 flex justify-between font-mono text-[11px] uppercase tracking-wider text-ink-soft">
          <span>Light · ácido, floral</span>
          <span>Medium · equilibrado</span>
          <span>Dark · cuerpo, cacao</span>
        </div>
      </div>
    } @else {
      <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        @for (i of [1, 2, 3, 4]; track i) {
          <div class="h-32 animate-pulse rounded-card border border-line bg-panel"></div>
        }
      </div>
    }
  `,
})
export class Dashboard implements OnInit {
  private service = inject(DashboardService);
  resumen = signal<Resumen | null>(null);

  ngOnInit() {
    this.service.resumen().subscribe((r) => this.resumen.set(r));
  }
}
