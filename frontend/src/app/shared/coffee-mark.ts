import { Component, input } from '@angular/core';

/** Marca animada: taza de café con vapor (el vapor usa currentColor). */
@Component({
  selector: 'rl-coffee-mark',
  standalone: true,
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <defs>
        <linearGradient id="rlRoastGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#C99A57" />
          <stop offset=".55" stop-color="#9C5B2E" />
          <stop offset="1" stop-color="#4A2E20" />
        </linearGradient>
      </defs>

      <!-- vapor -->
      <g class="steam" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" fill="none">
        <path class="s s1" d="M8.5 7 q-1.6 -1.4 0 -2.8 q1.6 -1.4 0 -2.8" />
        <path class="s s2" d="M12.5 7 q-1.6 -1.4 0 -2.8 q1.6 -1.4 0 -2.8" />
      </g>

      <!-- taza -->
      <path d="M3.5 9.2 H14.5 V13.5 A5.5 5.5 0 0 1 3.5 13.5 Z" fill="url(#rlRoastGrad)" />
      <!-- asa -->
      <path d="M14.5 10.4 H16.7 A2.4 2.4 0 0 1 16.7 15 H14.5" stroke="#7A4A2A" stroke-width="1.5" fill="none" />
      <!-- plato -->
      <rect x="2.6" y="20" width="13.3" height="1.7" rx=".85" fill="#9C5B2E" opacity=".55" />
    </svg>
  `,
  styles: [`
    :host { display: inline-flex; line-height: 0; }
    .steam .s {
      opacity: .7;
      transform-box: fill-box;
      transform-origin: center bottom;
      animation: rl-steam 2.8s ease-in-out infinite;
    }
    .steam .s2 { animation-delay: 1.3s; }
    @keyframes rl-steam {
      0%   { opacity: 0;   transform: translateY(2px) scaleY(.9); }
      30%  { opacity: .75; }
      70%  { opacity: .4; }
      100% { opacity: 0;   transform: translateY(-4px) scaleY(1.1); }
    }
  `],
})
export class CoffeeMark {
  size = input(28);
}
