import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Resumen, PerfilTotal, StockPorFinca, LoteTostado, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  resumen(): Observable<Resumen> {
    return this.http.get<Resumen>(`${environment.apiUrl}/dashboard/resumen`);
  }

  tostadosPorPerfil(): Observable<PerfilTotal[]> {
    return this.http.get<PerfilTotal[]>(`${environment.apiUrl}/dashboard/tostados-por-perfil`);
  }

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
