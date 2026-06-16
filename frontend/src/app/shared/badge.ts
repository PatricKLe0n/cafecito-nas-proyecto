import { Component, input } from '@angular/core';

type Tone = 'neutral' | 'verdigris' | 'sage' | 'clay' | 'roast-light' | 'roast-medium' | 'roast-dark';

const TONES: Record<Tone, string> = {
  neutral: 'bg-ink/5 text-ink-soft',
  verdigris: 'bg-verdigris/10 text-verdigris-deep',
  sage: 'bg-sage/15 text-sage',
  clay: 'bg-clay/10 text-clay',
  'roast-light': 'bg-roast-light/20 text-roast-dark',
  'roast-medium': 'bg-roast-medium/15 text-roast-medium',
  'roast-dark': 'bg-roast-dark/10 text-roast-dark',
};

@Component({
  selector: 'rl-badge',
  standalone: true,
  template: `
    <span [class]="cls()">
      @if (dotColor()) {
        <span class="h-1.5 w-1.5 rounded-full" [style.background-color]="dotColor()"></span>
      }
      {{ label() }}
    </span>
  `,
})
/**
 * Etiqueta compacta reutilizable. Pinta un texto con un tono predefinido del sistema
 * de diseño y, opcionalmente, un punto de color para reforzar el estado o la categoría.
 */
export class Badge {
  label = input('');
  tone = input<Tone>('neutral');
  dotColor = input<string | null>(null);

  cls(): string {
    return (
      'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-[10.5px] font-semibold uppercase tracking-[0.08em] ' +
      TONES[this.tone()]
    );
  }
}
