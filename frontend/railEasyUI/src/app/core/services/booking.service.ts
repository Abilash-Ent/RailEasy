import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Booking, BookingRequest } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/bookings`;

  book(payload: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.baseUrl, payload);
  }

  myTickets(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.baseUrl}/mine`);
  }

  getById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.baseUrl}/${id}`);
  }

  cancel(id: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.baseUrl}/${id}/cancel`, {});
  }

  downloadTicket(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/${id}/ticket`, {
      observe: 'response',
      responseType: 'blob',
    });
  }
}
