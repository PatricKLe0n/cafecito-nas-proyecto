import { Component, input, output } from '@angular/core';

@Component({
  selector: 'rl-modal-confirmacion',
  standalone: true,
  template: `
    @if (open()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4 backdrop-blur-sm" (click)="cancel.emit()">
        <div class="w-full max-w-sm rounded-card border border-line bg-panel p-6 shadow-panel" (click)="$event.stopPropagation()">
          <div class="eyebrow mb-2">Confirmar acción</div>
          <h3 class="font-display text-lg text-ink">{{ title() }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-ink-soft">{{ message() }}</p>
          <div class="mt-6 flex justify-end gap-2">
            <button class="rounded-md border border-line px-4 py-2 text-sm transition hover:bg-ink/5" (click)="cancel.emit()">Cancelar</button>
            <button class="rounded-md bg-clay px-4 py-2 text-sm font-medium text-paper transition hover:opacity-90" (click)="confirm.emit()">{{ confirmLabel() }}</button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ModalConfirmacion {
  open = input(false);
  title = input('Confirmar');
  message = input('');
  confirmLabel = input('Confirmar');
  confirm = output<void>();
  cancel = output<void>();
}
