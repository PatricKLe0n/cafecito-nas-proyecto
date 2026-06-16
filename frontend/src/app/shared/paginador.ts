import { Component, input, output } from '@angular/core';

@Component({
  selector: 'rl-paginador',
  standalone: true,
  template: `
    <div class="mt-4 flex items-center justify-between text-xs text-ink-soft">
      <span class="font-mono tnum">{{ totalElements() }} registros · pág. {{ page() + 1 }}/{{ totalPages() || 1 }}</span>
      <div class="flex gap-1.5">
        <button
          class="rounded-md border border-line px-3 py-1.5 font-mono text-[11px] uppercase tracking-wide transition hover:bg-ink/5 disabled:opacity-40 disabled:hover:bg-transparent"
          [disabled]="first()" (click)="change.emit(page() - 1)">Anterior</button>
        <button
          class="rounded-md border border-line px-3 py-1.5 font-mono text-[11px] uppercase tracking-wide transition hover:bg-ink/5 disabled:opacity-40 disabled:hover:bg-transparent"
          [disabled]="last()" (click)="change.emit(page() + 1)">Siguiente</button>
      </div>
    </div>
  `,
})
export class Paginador {
  page = input(0);
  totalPages = input(0);
  totalElements = input(0);
  first = input(true);
  last = input(true);
  change = output<number>();
}
