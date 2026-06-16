import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteTostadoService } from './lote-tostado-service';
import { LoteVerdeService } from '../lotes-verdes/lote-verde-service';
import { LoteTostado, LoteTostadoRequest, LoteVerde, PerfilTueste, Page } from '../../core/models';
import { AuthService } from '../../core/services/auth';
import { Badge } from '../../shared/badge';
import { Paginador } from '../../shared/paginador';
import { ModalConfirmacion } from '../../shared/modal-confirmacion';

const ROAST_DOT: Record<PerfilTueste, string> = {
  LIGHT: '#C99A57',
  MEDIUM: '#9C5B2E',
  DARK: '#4A2E20',
};

@Component({
  selector: 'rl-lotes-tostados',
  standalone: true,
  imports: [ReactiveFormsModule, Badge, Paginador, ModalConfirmacion],
  template: `
    <div class="flex items-end justify-between gap-4">
      <div>
        <div class="eyebrow">Cadena · 03</div>
        <h1 class="mt-1 font-display text-2xl font-semibold text-ink">Lotes tostados</h1>
      </div>
      @if (auth.isAdmin()) {
        <button (click)="nuevo()" class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper transition hover:bg-verdigris-deep">+ Nuevo tueste</button>
      }
    </div>

    <div class="mt-5 mb-4 flex flex-wrap gap-2">
      <input [value]="q()" (input)="buscar($any($event.target).value)" placeholder="Buscar por código…"
        class="w-60 rounded-md border border-line bg-panel px-3 py-2 text-sm outline-none transition focus:border-verdigris" />
      <select [value]="perfil()" (change)="filtrarPerfil($any($event.target).value)"
        class="rounded-md border border-line bg-panel px-3 py-2 text-sm outline-none transition focus:border-verdigris">
        <option value="">Todos los perfiles</option>
        <option value="LIGHT">Light</option>
        <option value="MEDIUM">Medium</option>
        <option value="DARK">Dark</option>
      </select>
    </div>

    <div class="overflow-hidden rounded-card border border-line bg-panel shadow-panel">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-line text-left">
            <th class="px-4 py-3 eyebrow font-medium">Código</th>
            <th class="px-4 py-3 eyebrow font-medium">Lote verde</th>
            <th class="px-4 py-3 eyebrow font-medium">Perfil</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Entrada → Salida</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Merma</th>
            <th class="px-4 py-3 eyebrow font-medium">Estado</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (t of page()?.content ?? []; track t.id) {
            <tr class="border-b border-line/60 last:border-0 transition hover:bg-ink/[0.02]" [class.opacity-50]="t.estado === 'ANULADO'">
              <td class="px-4 py-3 font-mono font-medium text-ink">{{ t.codigo }}</td>
              <td class="px-4 py-3 font-mono text-ink-soft">{{ t.loteVerdeCodigo }}</td>
              <td class="px-4 py-3"><rl-badge [label]="t.perfilTueste" [tone]="perfilTone(t.perfilTueste)" [dotColor]="dot(t.perfilTueste)" /></td>
              <td class="px-4 py-3 text-right tnum font-mono text-ink-soft">{{ t.pesoEntradaKg }} → {{ t.pesoSalidaKg }} <span class="text-ink-soft/60">kg</span></td>
              <td class="px-4 py-3 text-right tnum font-mono font-semibold text-roast-medium">{{ t.mermaPorcentaje }}%</td>
              <td class="px-4 py-3"><rl-badge [label]="t.estado" [tone]="t.estado === 'REGISTRADO' ? 'sage' : 'clay'" /></td>
              <td class="px-4 py-3 text-right">
                @if (auth.isAdmin() && t.estado === 'REGISTRADO') {
                  <button class="font-medium text-verdigris-deep hover:underline" (click)="editar(t)">Editar</button>
                  <button class="ml-3 text-clay hover:underline" (click)="aAnular.set(t)">Anular</button>
                } @else { <span class="text-ink-soft/50">—</span> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="px-4 py-10 text-center text-ink-soft">Sin lotes tostados.</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      <rl-paginador [page]="p.page" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
        [first]="p.first" [last]="p.last" (change)="irPagina($event)" />
    }

    @if (panelAbierto()) {
      <div class="fixed inset-0 z-40 flex justify-end bg-ink/30 backdrop-blur-sm" (click)="cerrar()">
        <div class="h-full w-full max-w-md overflow-auto border-l border-line bg-paper p-6" (click)="$event.stopPropagation()">
          <div class="eyebrow">{{ editando() ? 'Editar registro' : 'Nuevo registro' }}</div>
          <h2 class="mt-1 font-display text-xl font-semibold text-ink">{{ editando() ? editando()!.codigo : 'Nueva tanda de tostado' }}</h2>

          <form [formGroup]="form" (ngSubmit)="guardar()" class="mt-6 space-y-4">
            <label class="block"><span class="eyebrow">Código *</span><input formControlName="codigo" class="rl-input font-mono" /></label>
            <label class="block"><span class="eyebrow">Lote verde *</span>
              <select formControlName="loteVerdeId" class="rl-input">
                <option [ngValue]="null" disabled>Selecciona lote disponible…</option>
                @for (l of lotesVerdes(); track l.id) { <option [ngValue]="l.id">{{ l.codigo }} · {{ l.pesoKg }} kg</option> }
              </select>
              @if (editando()) { <span class="mt-1 block font-mono text-[11px] text-ink-soft">El lote verde no se modifica al editar.</span> }
            </label>
            <label class="block"><span class="eyebrow">Perfil de tueste *</span>
              <select formControlName="perfilTueste" class="rl-input">
                <option value="LIGHT">Light</option>
                <option value="MEDIUM">Medium</option>
                <option value="DARK">Dark</option>
              </select></label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block"><span class="eyebrow">Peso entrada (kg) *</span><input type="number" step="0.01" formControlName="pesoEntradaKg" class="rl-input tnum font-mono" /></label>
              <label class="block"><span class="eyebrow">Peso salida (kg) *</span><input type="number" step="0.01" formControlName="pesoSalidaKg" class="rl-input tnum font-mono" /></label>
            </div>

            <div class="flex items-center justify-between rounded-md border border-line bg-panel px-3 py-2.5">
              <span class="eyebrow">Merma estimada</span>
              <span class="tnum font-mono text-lg font-semibold text-roast-medium">{{ mermaEstimada() }}%</span>
            </div>

            <label class="block"><span class="eyebrow">Fecha y hora de tueste *</span><input type="datetime-local" formControlName="fechaTueste" class="rl-input font-mono" /></label>

            @if (errorApi()) { <p class="rounded-md border border-clay/30 bg-clay/5 px-3 py-2 text-sm text-clay">{{ errorApi() }}</p> }
            <div class="flex justify-end gap-2 pt-2">
              <button type="button" class="rounded-md border border-line px-4 py-2 text-sm hover:bg-ink/5" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid" class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper hover:bg-verdigris-deep disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <rl-modal-confirmacion [open]="!!aAnular()" title="Anular tueste" confirmLabel="Anular"
      [message]="'¿Anular el lote ' + (aAnular()?.codigo ?? '') + '? Se devolverá el peso al stock del lote verde.'"
      (confirm)="confirmarAnular()" (cancel)="aAnular.set(null)" />
  `,
})
export class LotesTostados implements OnInit {
  private service = inject(LoteTostadoService);
  private verdeService = inject(LoteVerdeService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<LoteTostado> | null>(null);
  lotesVerdes = signal<LoteVerde[]>([]);
  q = signal('');
  perfil = signal('');
  panelAbierto = signal(false);
  editando = signal<LoteTostado | null>(null);
  aAnular = signal<LoteTostado | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    loteVerdeId: [null as number | null, Validators.required],
    perfilTueste: ['MEDIUM' as PerfilTueste, Validators.required],
    pesoEntradaKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    pesoSalidaKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    fechaTueste: ['', Validators.required],
  });

  mermaEstimada = computed(() => {
    const e = Number(this.form.controls.pesoEntradaKg.value);
    const s = Number(this.form.controls.pesoSalidaKg.value);
    if (!e || !s || s >= e) return '0.00';
    return (((e - s) / e) * 100).toFixed(2);
  });

  ngOnInit() {
    this.cargar();
    this.verdeService.list({ size: 100, estado: 'DISPONIBLE' }).subscribe((p) => this.lotesVerdes.set(p.content));
  }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q(), perfil: this.perfil() || undefined }).subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  filtrarPerfil(v: string) { this.perfil.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  perfilTone(p: PerfilTueste): 'roast-light' | 'roast-medium' | 'roast-dark' {
    return p === 'LIGHT' ? 'roast-light' : p === 'MEDIUM' ? 'roast-medium' : 'roast-dark';
  }
  dot(p: PerfilTueste): string { return ROAST_DOT[p]; }

  nuevo() { this.editando.set(null); this.form.reset({ perfilTueste: 'MEDIUM' }); this.form.controls.loteVerdeId.enable(); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(t: LoteTostado) {
    this.editando.set(t);
    this.form.patchValue({
      codigo: t.codigo, loteVerdeId: t.loteVerdeId, perfilTueste: t.perfilTueste,
      pesoEntradaKg: t.pesoEntradaKg, pesoSalidaKg: t.pesoSalidaKg,
      fechaTueste: t.fechaTueste.substring(0, 16),
    });
    this.form.controls.loteVerdeId.disable();
    this.errorApi.set(null);
    this.panelAbierto.set(true);
  }
  cerrar() { this.panelAbierto.set(false); this.form.controls.loteVerdeId.enable(); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as LoteTostadoRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  confirmarAnular() {
    const t = this.aAnular();
    if (!t) return;
    this.service.delete(t.id).subscribe({
      next: () => { this.aAnular.set(null); this.cargar(); },
      error: (e) => { const m = e?.error?.message ?? 'No se pudo anular'; this.aAnular.set(null); alert(m); },
    });
  }
}
