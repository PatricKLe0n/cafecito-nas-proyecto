import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Resumen, PerfilTotal, StockPorFinca, LoteTostado, Page } from '../../core/models';

/**
 * Acceso a los datos agregados que alimentan el dashboard: indicadores de resumen,
 * distribución por perfil, stock por finca y la serie de merma por lote tostado.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  /** Indicadores agregados para las tarjetas de resumen. */
  resumen(): Observable<Resumen> {
    return this.http.get<Resumen>(`${environment.apiUrl}/dashboard/resumen`);
  }

  /** Total de tandas agrupadas por perfil de tueste. */
  tostadosPorPerfil(): Observable<PerfilTotal[]> {
    return this.http.get<PerfilTotal[]>(`${environment.apiUrl}/dashboard/tostados-por-perfil`);
  }

  /** Stock de café verde disponible por finca, en kilogramos. */
  stockPorFinca(): Observable<StockPorFinca[]> {
    return this.http.get<StockPorFinca[]>(`${environment.apiUrl}/dashboard/stock-por-finca`);
  }

  /** Lotes tostados registrados, ordenados por fecha, para la tendencia de merma. */
  mermaPorLote(): Observable<LoteTostado[]> {
    return this.http
      .get<Page<LoteTostado>>(`${environment.apiUrl}/lotes-tostados`, {
        params: { size: '100', estado: 'REGISTRADO', sort: 'fechaTueste,asc' },
      })
      .pipe(map((p) => p.content));
  }
}
