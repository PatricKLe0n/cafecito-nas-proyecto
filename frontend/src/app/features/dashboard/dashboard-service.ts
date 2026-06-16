import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Resumen, PerfilTotal } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  resumen(): Observable<Resumen> {
    return this.http.get<Resumen>(`${environment.apiUrl}/dashboard/resumen`);
  }

  tostadosPorPerfil(): Observable<PerfilTotal[]> {
    return this.http.get<PerfilTotal[]>(`${environment.apiUrl}/dashboard/tostados-por-perfil`);
  }
}
