export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface LoginRequest { username: string; password: string; }
export interface LoginResponse { token: string; username: string; rol: 'ADMIN' | 'USER'; expiraEnMs: number; }

export type Proceso = 'LAVADO' | 'NATURAL' | 'HONEY';
export interface Finca {
  id: number; pais: string; region: string; nombre: string;
  productor?: string; altitudMsnm?: number; variedad?: string; proceso: Proceso;
}
export type FincaRequest = Omit<Finca, 'id'>;

export type EstadoLoteVerde = 'DISPONIBLE' | 'AGOTADO';
export interface LoteVerde {
  id: number; codigo: string; fincaId: number; fincaNombre: string;
  pesoKg: number; humedadPorcentaje?: number; puntajeSca?: number;
  fechaRecepcion: string; estado: EstadoLoteVerde;
}
export interface LoteVerdeRequest {
  codigo: string; fincaId: number; pesoKg: number;
  humedadPorcentaje?: number; puntajeSca?: number; fechaRecepcion: string;
}

export type PerfilTueste = 'LIGHT' | 'MEDIUM' | 'DARK';
export type EstadoTostado = 'REGISTRADO' | 'ANULADO';
export interface LoteTostado {
  id: number; codigo: string; loteVerdeId: number; loteVerdeCodigo: string;
  perfilTueste: PerfilTueste; pesoEntradaKg: number; pesoSalidaKg: number;
  mermaPorcentaje: number; fechaTueste: string; estado: EstadoTostado;
}
export interface LoteTostadoRequest {
  codigo: string; loteVerdeId: number; perfilTueste: PerfilTueste;
  pesoEntradaKg: number; pesoSalidaKg: number; fechaTueste: string;
}

export interface Resumen {
  lotesVerdesDisponibles: number;
  lotesTostadosRegistrados: number;
  mermaMediaPorcentaje: number;
  totalFincas: number;
}
