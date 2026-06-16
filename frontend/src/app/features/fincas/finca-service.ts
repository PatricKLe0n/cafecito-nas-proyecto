import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CrudService } from '../../core/services/crud';
import { environment } from '../../../environments/environment';
import { Finca, FincaRequest } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class FincaService extends CrudService<Finca, FincaRequest> {
  constructor() {
    super(inject(HttpClient), `${environment.apiUrl}/fincas`);
  }
}
