import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../models';

/** Parámetros de consulta para el listado paginado; admite filtros adicionales por clave. */
export interface ListParams {
  page?: number; size?: number; sort?: string; q?: string;
  [key: string]: string | number | undefined;
}

/**
 * Base genérica de CRUD sobre una API REST, parametrizada por el tipo de la entidad
 * de lectura (`T`) y el de su payload de escritura (`R`). Los servicios de cada feature
 * la extienden indicando su `baseUrl`, evitando duplicar la lógica HTTP común.
 *
 * @typeParam T Forma de la entidad tal como la devuelve el backend.
 * @typeParam R Cuerpo de la petición usado en alta y edición.
 */
export class CrudService<T, R> {
  constructor(protected http: HttpClient, protected baseUrl: string) {}

  /**
   * Recupera una página de entidades. Solo se añaden a la query los parámetros con
   * valor (se descartan `undefined`, `null` y cadenas vacías).
   *
   * @param params Paginación, orden, búsqueda y filtros opcionales.
   * @returns Observable con la página de resultados.
   */
  list(params: ListParams = {}): Observable<Page<T>> {
    let hp = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') hp = hp.set(k, String(v));
    });
    return this.http.get<Page<T>>(this.baseUrl, { params: hp });
  }

  /**
   * Obtiene una entidad por su identificador.
   *
   * @param id Identificador de la entidad.
   * @returns Observable con la entidad solicitada.
   */
  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${id}`);
  }

  /**
   * Crea una nueva entidad.
   *
   * @param body Datos de la entidad a crear.
   * @returns Observable con la entidad ya persistida.
   */
  create(body: R): Observable<T> {
    return this.http.post<T>(this.baseUrl, body);
  }

  /**
   * Actualiza por completo una entidad existente.
   *
   * @param id Identificador de la entidad a actualizar.
   * @param body Nuevos datos de la entidad.
   * @returns Observable con la entidad actualizada.
   */
  update(id: number, body: R): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${id}`, body);
  }

  /**
   * Elimina una entidad por su identificador.
   *
   * @param id Identificador de la entidad a eliminar.
   * @returns Observable que completa al confirmarse el borrado.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
