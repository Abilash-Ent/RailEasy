import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Schedule,
  ScheduleRequest,
  ScheduleSearchParams,
} from '../models/schedule.model';
import { SeatMap } from '../models/booking.model';
import { TravelClass } from '../models/travel-class.model';

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/schedules`;

  search(params: ScheduleSearchParams): Observable<Schedule[]> {
    const httpParams = new HttpParams()
      .set('from', params.from)
      .set('to', params.to)
      .set('date', params.date);
    return this.http
      .get<Schedule[]>(this.baseUrl, { params: httpParams })
      .pipe(map((list) => this.normalizeList(list)));
  }

  getById(id: number): Observable<Schedule> {
    return this.http
      .get<Schedule>(`${this.baseUrl}/${id}`)
      .pipe(map((s) => this.normalize(s)));
  }

  getSeatMap(id: number, travelClass: TravelClass): Observable<SeatMap> {
    const params = new HttpParams().set('class', travelClass);
    return this.http.get<SeatMap>(`${this.baseUrl}/${id}/seats`, { params });
  }

  getAll(): Observable<Schedule[]> {
    return this.http
      .get<Schedule[]>(`${this.baseUrl}/all`)
      .pipe(map((list) => this.normalizeList(list)));
  }

  getByTrain(trainId: number): Observable<Schedule[]> {
    return this.http
      .get<Schedule[]>(`${this.baseUrl}/by-train/${trainId}`)
      .pipe(map((list) => this.normalizeList(list)));
  }

  create(payload: ScheduleRequest): Observable<Schedule> {
    return this.http
      .post<Schedule>(this.baseUrl, payload)
      .pipe(map((s) => this.normalize(s)));
  }

  update(id: number, payload: ScheduleRequest): Observable<Schedule> {
    return this.http
      .put<Schedule>(`${this.baseUrl}/${id}`, payload)
      .pipe(map((s) => this.normalize(s)));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /**
   * The API returns fares nested under a `fares` object
   * ({ SLEEPER, AC_3, AC_2 }). Flatten them onto the schedule so the UI can
   * read fareSleeper / fareAc3 / fareAc2 consistently.
   */
  private normalize(schedule: Schedule): Schedule {
    if (!schedule) {
      return schedule;
    }
    const fares = schedule.fares;
    return {
      ...schedule,
      fareSleeper: schedule.fareSleeper ?? fares?.SLEEPER ?? 0,
      fareAc3: schedule.fareAc3 ?? fares?.AC_3 ?? 0,
      fareAc2: schedule.fareAc2 ?? fares?.AC_2 ?? 0,
    };
  }

  private normalizeList(list: Schedule[]): Schedule[] {
    return (list ?? []).map((s) => this.normalize(s));
  }
}
