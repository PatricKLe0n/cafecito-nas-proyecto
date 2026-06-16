import { Component, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import ApexCharts from 'apexcharts';
import { DashboardService } from './dashboard-service';
import { Resumen, PerfilTotal, StockPorFinca, LoteTostado } from '../../core/models';

const PERFIL_LABEL: Record<string, string> = { LIGHT: 'Light', MEDIUM: 'Medium', DARK: 'Dark' };
const PERFIL_COLOR: Record<string, string> = { LIGHT: '#C99A57', MEDIUM: '#9C5B2E', DARK: '#4A2E20' };
const AXIS = '#6B5D4F';
const GRID = '#EDE6DA';

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
    } @else {
      <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        @for (i of [1, 2, 3, 4]; track i) {
          <div class="h-32 animate-pulse rounded-card border border-line bg-panel"></div>
        }
      </div>
    }

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="rounded-card border border-line bg-panel p-5 shadow-panel">
        <div class="eyebrow">Distribución por perfil de tueste</div>
        <div #perfilEl class="mt-2 min-h-[260px]"></div>
      </div>

      <div class="rounded-card border border-line bg-panel p-5 shadow-panel">
        <div class="eyebrow">Escala de tueste</div>
        <div class="roast-spectrum mt-3 h-3 w-full rounded-full"></div>
        <div class="mt-2 flex justify-between font-mono text-[11px] uppercase tracking-wider text-ink-soft">
          <span>Light</span><span>Medium</span><span>Dark</span>
        </div>
        <ul class="mt-5 space-y-2.5 text-sm text-ink-soft">
          <li class="flex items-start gap-2"><span class="mt-1 h-2.5 w-2.5 shrink-0 rounded-full" style="background:#C99A57"></span> <b class="text-ink">Light</b> · ácido y floral, resalta el origen.</li>
          <li class="flex items-start gap-2"><span class="mt-1 h-2.5 w-2.5 shrink-0 rounded-full" style="background:#9C5B2E"></span> <b class="text-ink">Medium</b> · equilibrado, dulzor y cuerpo.</li>
          <li class="flex items-start gap-2"><span class="mt-1 h-2.5 w-2.5 shrink-0 rounded-full" style="background:#4A2E20"></span> <b class="text-ink">Dark</b> · cuerpo intenso, notas a cacao.</li>
        </ul>
      </div>
    </div>

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="rounded-card border border-line bg-panel p-5 shadow-panel">
        <div class="eyebrow">Merma por lote tostado <span class="normal-case tracking-normal text-ink-soft/70">· barra = perfil · línea = media</span></div>
        <div #mermaEl class="mt-2 min-h-[300px]"></div>
      </div>

      <div class="rounded-card border border-line bg-panel p-5 shadow-panel">
        <div class="eyebrow">Stock de café verde por origen <span class="normal-case tracking-normal text-ink-soft/70">· kg disponibles</span></div>
        <div #stockEl class="mt-2 min-h-[300px]"></div>
      </div>
    </div>
  `,
})
export class Dashboard implements OnInit, AfterViewInit, OnDestroy {
  private service = inject(DashboardService);

  resumen = signal<Resumen | null>(null);
  private perfiles: PerfilTotal[] | null = null;
  private mermas: LoteTostado[] | null = null;
  private stock: StockPorFinca[] | null = null;

  @ViewChild('perfilEl') perfilEl?: ElementRef<HTMLDivElement>;
  @ViewChild('mermaEl') mermaEl?: ElementRef<HTMLDivElement>;
  @ViewChild('stockEl') stockEl?: ElementRef<HTMLDivElement>;
  private charts: ApexCharts[] = [];

  private get reduce(): boolean {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  ngOnInit() {
    this.service.resumen().subscribe((r) => this.resumen.set(r));
    this.service.tostadosPorPerfil().subscribe((d) => { this.perfiles = d; this.renderPerfil(); });
    this.service.mermaPorLote().subscribe((d) => { this.mermas = d; this.renderMerma(); });
    this.service.stockPorFinca().subscribe((d) => { this.stock = d; this.renderStock(); });
  }

  ngAfterViewInit() {
    this.renderPerfil();
    this.renderMerma();
    this.renderStock();
  }

  private renderPerfil() {
    const el = this.perfilEl?.nativeElement;
    const data = this.perfiles;
    if (!el || !data || el.dataset['done']) return;
    const total = data.reduce((s, p) => s + p.total, 0);
    if (total === 0) return;
    el.dataset['done'] = '1';
    const chart = new ApexCharts(el, {
      chart: { type: 'donut', height: 270, fontFamily: '"IBM Plex Mono", monospace', animations: { enabled: !this.reduce, easing: 'easeinout', speed: 700 } },
      series: data.map((p) => p.total),
      labels: data.map((p) => PERFIL_LABEL[p.perfil] ?? p.perfil),
      colors: data.map((p) => PERFIL_COLOR[p.perfil] ?? '#9C5B2E'),
      stroke: { width: 2, colors: ['#F8F4EC'] },
      dataLabels: { enabled: true, style: { fontSize: '11px', fontWeight: 600, colors: ['#FBF7F0'] }, dropShadow: { enabled: false }, formatter: (v: number) => Math.round(v) + '%' },
      legend: { position: 'bottom', fontSize: '12px', labels: { colors: AXIS }, markers: { width: 9, height: 9, radius: 9 }, itemMargin: { horizontal: 10, vertical: 4 } },
      plotOptions: { pie: { donut: { size: '68%', labels: { show: true, name: { fontSize: '11px', color: AXIS }, value: { fontSize: '24px', fontWeight: 700, color: '#211A14' }, total: { show: true, label: 'Tandas', color: AXIS, formatter: () => String(total) } } } } },
      tooltip: { y: { formatter: (v: number) => v + ' lotes' } },
      states: { hover: { filter: { type: 'darken', value: 0.92 } } },
    } as any);
    chart.render();
    this.charts.push(chart);
  }

  private renderMerma() {
    const el = this.mermaEl?.nativeElement;
    const data = this.mermas;
    if (!el || !data || el.dataset['done']) return;
    if (data.length === 0) return;
    el.dataset['done'] = '1';
    const avg = data.reduce((s, t) => s + t.mermaPorcentaje, 0) / data.length;
    const chart = new ApexCharts(el, {
      chart: { type: 'bar', height: 300, fontFamily: '"IBM Plex Mono", monospace', toolbar: { show: false }, animations: { enabled: !this.reduce, speed: 700 } },
      series: [{ name: 'Merma', data: data.map((t) => ({ x: t.codigo.replace('LT-', ''), y: t.mermaPorcentaje })) }],
      plotOptions: { bar: { distributed: true, borderRadius: 3, columnWidth: '62%' } },
      colors: data.map((t) => PERFIL_COLOR[t.perfilTueste] ?? '#9C5B2E'),
      dataLabels: { enabled: false },
      legend: { show: false },
      xaxis: { labels: { rotate: -50, hideOverlappingLabels: false, style: { fontSize: '9px', colors: AXIS } }, axisBorder: { color: '#DED5C6' }, axisTicks: { color: '#DED5C6' } },
      yaxis: { max: 25, tickAmount: 5, labels: { style: { fontSize: '10px', colors: AXIS }, formatter: (v: number) => v.toFixed(0) + '%' } },
      grid: { borderColor: GRID, strokeDashArray: 4 },
      tooltip: { y: { formatter: (v: number) => v.toFixed(2) + '%' } },
      annotations: { yaxis: [{ y: avg, borderColor: '#2F6F62', strokeDashArray: 4, label: { text: 'media ' + avg.toFixed(2) + '%', style: { background: '#2F6F62', color: '#fff', fontSize: '10px' } } }] },
    } as any);
    chart.render();
    this.charts.push(chart);
  }

  private renderStock() {
    const el = this.stockEl?.nativeElement;
    const data = this.stock;
    if (!el || !data || el.dataset['done']) return;
    if (data.length === 0) return;
    el.dataset['done'] = '1';
    const chart = new ApexCharts(el, {
      chart: { type: 'bar', height: 300, fontFamily: '"IBM Plex Mono", monospace', toolbar: { show: false }, animations: { enabled: !this.reduce, speed: 700 } },
      series: [{ name: 'Stock', data: data.map((s) => s.stockKg) }],
      plotOptions: { bar: { horizontal: true, borderRadius: 3, barHeight: '62%' } },
      colors: ['#5E7B53'],
      dataLabels: { enabled: true, formatter: (v: number) => v + ' kg', style: { fontSize: '10px', fontWeight: 600, colors: ['#FBF7F0'] } },
      xaxis: { categories: data.map((s) => s.finca), labels: { style: { fontSize: '10px', colors: AXIS } }, axisBorder: { color: '#DED5C6' }, axisTicks: { color: '#DED5C6' } },
      yaxis: { labels: { style: { fontSize: '10px', colors: AXIS } } },
      grid: { borderColor: GRID, strokeDashArray: 4 },
      tooltip: { y: { formatter: (v: number) => v + ' kg' } },
      legend: { show: false },
    } as any);
    chart.render();
    this.charts.push(chart);
  }

  ngOnDestroy() {
    this.charts.forEach((c) => c.destroy());
  }
}
