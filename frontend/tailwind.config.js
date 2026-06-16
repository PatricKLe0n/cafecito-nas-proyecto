/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // Roast Log — consola de instrumentación de tostaduría
        paper: '#EFEAE0',      // papel cálido (fondo)
        panel: '#F8F4EC',      // panel / tarjeta
        ink: '#211A14',        // tinta espresso (texto)
        'ink-soft': '#6B5D4F', // texto secundario
        line: '#DED5C6',       // hairlines / bordes
        verdigris: '#2F6F62',  // acento interactivo (cobre oxidado)
        'verdigris-deep': '#244F46',
        // espectro de tueste (codifica Light / Medium / Dark)
        'roast-light': '#C99A57',
        'roast-medium': '#9C5B2E',
        'roast-dark': '#4A2E20',
        sage: '#5E7B53',       // estado positivo (disponible)
        clay: '#A6492F',       // estado negativo (agotado / anulado)
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'system-ui', 'sans-serif'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
      boxShadow: {
        panel: '0 1px 2px rgba(33,26,20,0.04), 0 8px 24px -16px rgba(33,26,20,0.25)',
      },
      borderRadius: {
        card: '14px',
      },
    },
  },
  plugins: [],
};
