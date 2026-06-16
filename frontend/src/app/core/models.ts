/** Envoltorio genérico de respuesta paginada del backend. */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Credenciales enviadas al endpoint de login. */
export interface LoginRequest { username: string; password: string; }
/** Respuesta del login: token JWT, identidad y caducidad del token. */
export interface LoginResponse { token: string; username: string; rol: 'ADMIN' | 'USER'; expiraEnMs: number; }

/** Método de beneficiado del café en origen. */
export type Proceso = 'LAVADO' | 'NATURAL' | 'HONEY';
/** Finca de origen: primer eslabón de la cadena de trazabilidad. */
export interface Finca {
  id: number; pais: string; region: string; nombre: string;
  productor?: string; altitudMsnm?: number; variedad?: string; proceso: Proceso;
}
/** Payload de alta/edición de una finca (la finca sin su `id`). */
export type FincaRequest = Omit<Finca, 'id'>;

/** Estado de inventario de un lote de café verde. */
export type EstadoLoteVerde = 'DISPONIBLE' | 'AGOTADO';
/** Lote de café verde recibido de una finca; segundo eslabón de la trazabilidad. */
export interface LoteVerde {
  id: number; codigo: string; fincaId: number; fincaNombre: string;
  pesoKg: number; humedadPorcentaje?: number; puntajeSca?: number;
  fechaRecepcion: string; estado: EstadoLoteVerde;
}
/** Payload de alta/edición de un lote de café verde. */
export interface LoteVerdeRequest {
  codigo: string; fincaId: number; pesoKg: number;
  humedadPorcentaje?: number; puntajeSca?: number; fechaRecepcion: string;
}

/** Grado de tueste aplicado a una tanda. */
export type PerfilTueste = 'LIGHT' | 'MEDIUM' | 'DARK';
/** Estado de una tanda de tostado (`ANULADO` revierte el peso al stock verde). */
export type EstadoTostado = 'REGISTRADO' | 'ANULADO';
/** Tanda de tostado de un lote verde; tercer eslabón, con cálculo de merma. */
export interface LoteTostado {
  id: number; codigo: string; loteVerdeId: number; loteVerdeCodigo: string;
  perfilTueste: PerfilTueste; pesoEntradaKg: number; pesoSalidaKg: number;
  mermaPorcentaje: number; fechaTueste: string; estado: EstadoTostado;
}
/** Payload de alta/edición de una tanda de tostado. */
export interface LoteTostadoRequest {
  codigo: string; loteVerdeId: number; perfilTueste: PerfilTueste;
  pesoEntradaKg: number; pesoSalidaKg: number; fechaTueste: string;
}

/** Indicadores agregados que alimentan las tarjetas del dashboard. */
export interface Resumen {
  lotesVerdesDisponibles: number;
  lotesTostadosRegistrados: number;
  mermaMediaPorcentaje: number;
  totalFincas: number;
}

/** Total de tandas por perfil de tueste (gráfico de distribución). */
export interface PerfilTotal { perfil: string; total: number; }
/** Stock de café verde disponible por finca, en kilogramos. */
export interface StockPorFinca { finca: string; stockKg: number; }
