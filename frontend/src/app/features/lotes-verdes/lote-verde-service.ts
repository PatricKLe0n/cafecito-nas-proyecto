import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud';
import { environment } from '../../../environments/environment';
import { LoteVerde, LoteVerdeRequest } from '../../core/models';

/** CRUD de lotes de café verde sobre el endpoint `/lotes-verdes`. */
@Injectable({ providedIn: 'root' })
export class LoteVerdeService extends CrudService<LoteVerde, LoteVerdeRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/lotes-verdes`);
  }
}
