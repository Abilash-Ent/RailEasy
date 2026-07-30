import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Train, TrainRequest } from '../models/train.model';

@Injectable({ providedIn: 'root' })
export class TrainService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/trains`;

  getAll(): Observable<Train[]> {
    return this.http.get<Train[]>(this.baseUrl);
  }

  getById(id: number): Observable<Train> {
    return this.http.get<Train>(`${this.baseUrl}/${id}`);
  }

  create(payload: TrainRequest): Observable<Train> {
    return this.http.post<Train>(this.baseUrl, payload);
  }

  update(id: number, payload: TrainRequest): Observable<Train> {
    return this.http.put<Train>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
