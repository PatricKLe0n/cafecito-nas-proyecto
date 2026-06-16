import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FincaService } from './finca-service';
import { Finca, FincaRequest, Page, Proceso } from '../../core/models';
import { AuthService } from '../../core/services/auth';
import { Badge } from '../../shared/badge';
import { Paginador } from '../../shared/paginador';
import { ModalConfirmacion } from '../../shared/modal-confirmacion';

@Component({
  selector: 'rl-fincas',
  standalone: true,
  imports: [ReactiveFormsModule, Badge, Paginador, ModalConfirmacion],
  template: `
    <div class="flex items-end justify-between gap-4">
      <div>
        <div class="eyebrow">Cadena · 01</div>
        <h1 class="mt-1 font-display text-2xl font-semibold text-ink">Fincas de origen</h1>
      </div>
      @if (auth.isAdmin()) {
        <button (click)="nuevo()"
          class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper transition hover:bg-verdigris-deep">
          + Nueva finca
        </button>
      }
    </div>

    <div class="mt-5 mb-4 flex gap-2">
      <input [value]="q()" (input)="buscar($any($event.target).value)" placeholder="Buscar por nombre o región…"
        class="w-72 rounded-md border border-line bg-panel px-3 py-2 text-sm outline-none transition focus:border-verdigris" />
    </div>

    <div class="overflow-hidden rounded-card border border-line bg-panel shadow-panel">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-line text-left">
            <th class="px-4 py-3 eyebrow font-medium">Nombre</th>
            <th class="px-4 py-3 eyebrow font-medium">Origen</th>
            <th class="px-4 py-3 eyebrow font-medium">Altitud</th>
            <th class="px-4 py-3 eyebrow font-medium">Variedad</th>
            <th class="px-4 py-3 eyebrow font-medium">Proceso</th>
            <th class="px-4 py-3 eyebrow font-medium text-right">Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (f of page()?.content ?? []; track f.id) {
            <tr class="border-b border-line/60 last:border-0 transition hover:bg-ink/[0.02]">
              <td class="px-4 py-3 font-medium text-ink">{{ f.nombre }}</td>
              <td class="px-4 py-3 text-ink-soft">{{ f.pais }} · {{ f.region }}</td>
              <td class="px-4 py-3 tnum font-mono text-ink-soft">{{ f.altitudMsnm ? f.altitudMsnm + ' msnm' : '—' }}</td>
              <td class="px-4 py-3 text-ink-soft">{{ f.variedad || '—' }}</td>
              <td class="px-4 py-3"><rl-badge [label]="f.proceso" [tone]="procesoTone(f.proceso)" /></td>
              <td class="px-4 py-3 text-right">
                @if (auth.isAdmin()) {
                  <button class="font-medium text-verdigris-deep hover:underline" (click)="editar(f)">Editar</button>
                  <button class="ml-3 text-clay hover:underline" (click)="aBorrar.set(f)">Eliminar</button>
                } @else {
                  <span class="text-ink-soft/50">solo lectura</span>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="px-4 py-10 text-center text-ink-soft">Sin fincas registradas.</td></tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      <rl-paginador [page]="p.page" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
        [first]="p.first" [last]="p.last" (change)="irPagina($event)" />
    }

    <!-- Panel lateral -->
    @if (panelAbierto()) {
      <div class="fixed inset-0 z-40 flex justify-end bg-ink/30 backdrop-blur-sm" (click)="cerrar()">
        <div class="h-full w-full max-w-md overflow-auto border-l border-line bg-paper p-6" (click)="$event.stopPropagation()">
          <div class="eyebrow">{{ editando() ? 'Editar registro' : 'Nuevo registro' }}</div>
          <h2 class="mt-1 font-display text-xl font-semibold text-ink">{{ editando() ? editando()!.nombre : 'Nueva finca' }}</h2>

          <form [formGroup]="form" (ngSubmit)="guardar()" class="mt-6 space-y-4">
            <label class="block"><span class="eyebrow">Nombre *</span>
              <input formControlName="nombre" class="rl-input" /></label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block"><span class="eyebrow">País *</span><input formControlName="pais" class="rl-input" /></label>
              <label class="block"><span class="eyebrow">Región *</span><input formControlName="region" class="rl-input" /></label>
            </div>
            <label class="block"><span class="eyebrow">Productor</span><input formControlName="productor" class="rl-input" /></label>
            <div class="grid grid-cols-2 gap-3">
              <label class="block"><span class="eyebrow">Altitud (msnm)</span>
                <input type="number" formControlName="altitudMsnm" class="rl-input tnum font-mono" /></label>
              <label class="block"><span class="eyebrow">Variedad</span><input formControlName="variedad" class="rl-input" /></label>
            </div>
            <label class="block"><span class="eyebrow">Proceso *</span>
              <select formControlName="proceso" class="rl-input">
                <option value="LAVADO">Lavado</option>
                <option value="NATURAL">Natural</option>
                <option value="HONEY">Honey</option>
              </select></label>

            @if (errorApi()) {
              <p class="rounded-md border border-clay/30 bg-clay/5 px-3 py-2 text-sm text-clay">{{ errorApi() }}</p>
            }
            <div class="flex justify-end gap-2 pt-2">
              <button type="button" class="rounded-md border border-line px-4 py-2 text-sm hover:bg-ink/5" (click)="cerrar()">Cancelar</button>
              <button type="submit" [disabled]="form.invalid"
                class="rounded-md bg-verdigris px-4 py-2 text-sm font-medium text-paper hover:bg-verdigris-deep disabled:opacity-50">Guardar</button>
            </div>
          </form>
        </div>
      </div>
    }

    <rl-modal-confirmacion [open]="!!aBorrar()" title="Eliminar finca" confirmLabel="Eliminar"
      [message]="'¿Eliminar la finca ' + (aBorrar()?.nombre ?? '') + '? No se podrá si tiene lotes asociados.'"
      (confirm)="confirmarBorrado()" (cancel)="aBorrar.set(null)" />
  `,
})
export class Fincas implements OnInit {
  private service = inject(FincaService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  page = signal<Page<Finca> | null>(null);
  q = signal('');
  panelAbierto = signal(false);
  editando = signal<Finca | null>(null);
  aBorrar = signal<Finca | null>(null);
  errorApi = signal<string | null>(null);
  private pagina = 0;

  form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    pais: ['', Validators.required],
    region: ['', Validators.required],
    productor: [''],
    altitudMsnm: [null as number | null],
    variedad: [''],
    proceso: ['LAVADO' as Proceso, Validators.required],
  });

  ngOnInit() { this.cargar(); }

  cargar() {
    this.service.list({ page: this.pagina, q: this.q() }).subscribe((p) => this.page.set(p));
  }
  buscar(v: string) { this.q.set(v); this.pagina = 0; this.cargar(); }
  irPagina(p: number) { this.pagina = p; this.cargar(); }

  procesoTone(p: Proceso): 'verdigris' | 'sage' | 'roast-light' {
    return p === 'LAVADO' ? 'verdigris' : p === 'NATURAL' ? 'sage' : 'roast-light';
  }

  nuevo() { this.editando.set(null); this.form.reset({ proceso: 'LAVADO' }); this.errorApi.set(null); this.panelAbierto.set(true); }
  editar(f: Finca) { this.editando.set(f); this.form.patchValue(f); this.errorApi.set(null); this.panelAbierto.set(true); }
  cerrar() { this.panelAbierto.set(false); }

  guardar() {
    if (this.form.invalid) return;
    const body = this.form.getRawValue() as FincaRequest;
    const editing = this.editando();
    const obs = editing ? this.service.update(editing.id, body) : this.service.create(body);
    obs.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (e) => this.errorApi.set(e?.error?.message ?? 'Error al guardar'),
    });
  }

  confirmarBorrado() {
    const f = this.aBorrar();
    if (!f) return;
    this.service.delete(f.id).subscribe({
      next: () => { this.aBorrar.set(null); this.cargar(); },
      error: (e) => { const m = e?.error?.message ?? 'No se pudo eliminar'; this.aBorrar.set(null); this.errorApi.set(m); alert(m); },
    });
  }
}
