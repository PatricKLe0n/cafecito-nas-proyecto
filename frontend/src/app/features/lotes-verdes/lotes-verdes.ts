import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoteVerdeService } from './lote-verde-service';
import { FincaService } from '../fincas/finca-service';
import { LoteVerde, LoteVerdeRequest, Finca, Page } from '../../core/models';
import { AuthService } from '../../core/services/auth';
import { Badge } from '../../shared/badge';
import { Paginador } from '../../shared/paginador';
import { ModalConfirmacion } from '../../shared/modal-confirmacion';

@Component({
  selector: 'rl-lotes-verdes',
  standalone: true,
  imports: [ReactiveFormsModule, Badge, Paginador, ModalConfirmacion],
  template: `
    <div class="flex items-end justify-between gap-4">
      <div>
        <div class="eyebrow">Cadena · 02</div>
        <h1 class="mt-1 font-display text-2xl font-semibold text-ink">Lotes de café verde</h1>
      </div>
      @if (auth.isAdmin()) {
        <button (click)="nuevo()" class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper transition hover:bg-verdigris-deep">+ Nuevo lote</button>
      }
    </div>

    <div class="mt-5 mb-4 flex flex-wrap gap-2">
      <input [value]="q()" (input)="buscar($any($event.target).value)" placeholder="Buscar por código…"
        class="w-60 rounded-md border border-line bg-panel px-3 py-2 text-sm outline-none transition focus:border-verdigris" />
      <select [value]="estado()" (change)="filtrarEstado($any($event.target).value)"
        class="rounded-md border border-line bg-panel px-3 py-2 text-sm outline-none transition focus:border-verdigris">
        <option value="">Todos los estados</option>
        <option value="DISPONIBLE">Disponible</option>
        <option value="AGOTADO">Agotado</option>
      </select>
    </div>

    <div class="overflow-hidden rounded-card border border-line bg-panel shadow-panel">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-line text-left">
            <th class="px-4 py-3 eyebrow font-medium">Código</th>
            <th class="px-4 py-3 eyebrow font-medium">Finca</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Stock</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">SCA</th>
            <th class="px-4 py-3 eyebrow font-medium">Recepción</th>
            <th class="px-4 py-3 eyebrow font-medium">Estado</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (l of page()?.content ?? []; track l.id) {
            <tr class="border-b border-line/60 last:border-0 transition hover:bg-ink/[0.02]">
              <td class="px-4 py-3 font-mono font-medium text-ink">{{ l.codigo }}</td>
              <td class="px-4 py-3 text-ink-soft">{{ l.fincaNombre }}</td>
              <td class="px-4 py-3 text-right tnum font-mono text-ink">{{ l.pesoKg }} <span class="text-ink-soft">kg</span></td>
              <td class="px-4 py-3 text-right tnum font-mono text-ink-soft">{{ l.puntajeSca ?? '—' }}</td>
              <td class="px-4 py-3 font-mono text-ink-soft">{{ l.fechaRecepcion }}</td>
              <td class="px-4 py-3">
                <rl-badge [label]="l.estado" [tone]="l.estado === 'DISPONIBLE' ? 'sage' : 'neutral'" />
              </td>
              <td class="px-4 py-3 text-right">
                @if (auth.isAdmin()) {
                  <button class="font-medium text-verdigris-deep hover:underline" (click)="editar(l)">Editar</button>
                  <button class="ml-3 text-clay hover:underline" (click)="aBorrar.set(l)">Eliminar</button>
                } @else { <span class="text-ink-soft/50">solo lectura</span> }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="px-4 py-10 text-center text-ink-soft">Sin lotes de café verde.</td></tr>
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
          <h2 class="mt-1 font-display text-xl font-semibold text-ink">{{ editando() ? editando()!.codigo : 'Nuevo lote verde' }}</h2>

          <form [formGroup]="form" (ngSubmit)="guardar()" class="mt-6 space-y-4">
            <label class="block"><span class="eyebrow">Código *</span><input formControlName="codigo" class="rl-input font-mono" /></label>
            <label class="block"><span class="eyebrow">Finca *</span>
              <select formControlName="fincaId" class="rl-input">
                <option [ngValue]="null" disabled>Selecciona origen…</option>
                @for (f of fincas(); track f.id) { <option [ngValue]="f.id">{{ f.nombre }}</option> }
              </select></label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block"><span class="eyebrow">Peso (kg) *</span><input type="number" step="0.01" formControlName="pesoKg" class="rl-input tnum font-mono" /></label>
              <label class="block"><span class="eyebrow">Humedad (%)</span><input type="number" step="0.1" formControlName="humedadPorcentaje" class="rl-input tnum font-mono" /></label>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <label class="block"><span class="eyebrow">Puntaje SCA</span><input type="number" step="0.1" formControlName="puntajeSca" class="rl-input tnum font-mono" /></label>
              <label class="block"><span class="eyebrow">Recepción *</span><input type="date" formControlName="fechaRecepcion" class="rl-input font-mono" /></label>
            </div>
            @if (errorApi()) { <p class="rounded-md border border-clay/30 bg-clay/5 px-3 py-2 text-sm text-clay">{{ errorApi() }}</p> }
            <div class="flex justify-end gap-2 pt-2">
              <button type="button" class="rounded-md border border-line px-4 py-2 text-sm hover:bg-ink/5" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid" class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper hover:bg-verdigris-deep disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <rl-modal-confirmacion [open]="!!aBorrar()" title="Eliminar lote verde" confirmLabel="Eliminar"
      [message]="'¿Eliminar el lote ' + (aBorrar()?.codigo ?? '') + '? No se podrá si tiene tostados asociados.'"
      (confirm)="confirmarBorrado()" (cancel)="aBorrar.set(null)" />
  `,
})
export class LotesVerdes implements OnInit {
  private service = inject(LoteVerdeService);
  private fincaService = inject(FincaService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<LoteVerde> | null>(null);
  fincas = signal<Finca[]>([]);
  q = signal('');
  estado = signal('');
  panelAbierto = signal(false);
  editando = signal<LoteVerde | null>(null);
  aBorrar = signal<LoteVerde | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    codigo: ['', Validators.required],
    fincaId: [null as number | null, Validators.required],
    pesoKg: [null as number | null, [Validators.required, Validators.min(0)]],
    humedadPorcentaje: [null as number | null],
    puntajeSca: [null as number | null],
    fechaRecepcion: ['', Validators.required],
  });

  ngOnInit() {
    this.cargar();
    this.fincaService.list({ size: 100 }).subscribe((p) => this.fincas.set(p.content));
  }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q(), estado: this.estado() || undefined }).subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  filtrarEstado(v: string) { this.estado.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  nuevo() { this.editando.set(null); this.form.reset(); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(l: LoteVerde) {
    this.editando.set(l);
    this.form.patchValue({
      codigo: l.codigo, fincaId: l.fincaId, pesoKg: l.pesoKg,
      humedadPorcentaje: l.humedadPorcentaje ?? null, puntajeSca: l.puntajeSca ?? null,
      fechaRecepcion: l.fechaRecepcion,
    });
    this.errorApi.set(null);
    this.panelAbierto.set(true);
  }
  cerrar() { this.panelAbierto.set(false); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as LoteVerdeRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  confirmarBorrado() {
    const l = this.aBorrar();
    if (!l) return;
    this.service.delete(l.id).subscribe({
      next: () => { this.aBorrar.set(null); this.cargar(); },
      error: (e) => { const m = e?.error?.message ?? 'No se pudo eliminar'; this.aBorrar.set(null); alert(m); },
    });
  }
}
