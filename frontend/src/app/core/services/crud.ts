import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../models';

export interface ListParams {
  page?: number; size?: number; sort?: string; q?: string;
  [key: string]: string | number | undefined;
}

export class CrudService<T, R> {
  constructor(protected http: HttpClient, protected baseUrl: string) {}

  list(params: ListParams = {}): Observable<Page<T>> {
    let hp = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') hp = hp.set(k, String(v));
    });
    return this.http.get<Page<T>>(this.baseUrl, { params: hp });
  }

  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${id}`);
  }

  create(body: R): Observable<T> {
    return this.http.post<T>(this.baseUrl, body);
  }

  update(id: number, body: R): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
