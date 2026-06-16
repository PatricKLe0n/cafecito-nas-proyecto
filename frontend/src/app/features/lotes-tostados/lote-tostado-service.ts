import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud';
import { environment } from '../../../environments/environment';
import { LoteTostado, LoteTostadoRequest } from '../../core/models';

/** CRUD de tandas de tostado sobre el endpoint `/lotes-tostados`. */
@Injectable({ providedIn: 'root' })
export class LoteTostadoService extends CrudService<LoteTostado, LoteTostadoRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/lotes-tostados`);
  }
}
