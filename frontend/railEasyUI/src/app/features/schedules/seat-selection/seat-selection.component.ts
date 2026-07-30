import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { ScheduleService } from '../../../core/services/schedule.service';
import { BookingService } from '../../../core/services/booking.service';
import { ToastService } from '../../../core/services/toast.service';
import { Schedule } from '../../../core/models/schedule.model';
import {
  TRAVEL_CLASSES,
  TravelClass,
  travelClassLabel,
} from '../../../core/models/travel-class.model';
import { extractErrorMessage, isAuthHandledError } from '../../../core/utils/http-error.util';

const ROWS = [1, 2, 3, 4, 5, 6, 7, 8];
const COLS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
const MAX_SEATS = 4;

@Component({
  selector: 'app-seat-selection',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './seat-selection.component.html',
  styleUrl: './seat-selection.component.css',
})
export class SeatSelectionComponent {
  private readonly scheduleService = inject(ScheduleService);
  private readonly bookingService = inject(BookingService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  /** Bound from route param via withComponentInputBinding. */
  readonly id = input<string | number | null>(null);

  readonly classes = TRAVEL_CLASSES;
  readonly rows = ROWS;
  readonly cols = COLS;
  readonly maxSeats = MAX_SEATS;

  readonly schedule = signal<Schedule | null>(null);
  readonly loadingSchedule = signal(true);
  readonly loadingSeats = signal(false);
  readonly booking = signal(false);

  readonly travelClass = signal<TravelClass>('AC_3');
  readonly bookedSeats = signal<Set<string>>(new Set());
  readonly selectedSeats = signal<string[]>([]);

  readonly farePerSeat = computed(() => {
    const s = this.schedule();
    if (!s) return 0;
    switch (this.travelClass()) {
      case 'SLEEPER':
        return s.fareSleeper;
      case 'AC_2':
        return s.fareAc2;
      default:
        return s.fareAc3;
    }
  });

  readonly totalFare = computed(
    () => this.farePerSeat() * this.selectedSeats().length
  );

  constructor() {
    // Load schedule when the route id becomes available.
    effect(
      () => {
        const rawId = this.id();
        if (rawId == null) return;
        const scheduleId = Number(rawId);
        if (Number.isNaN(scheduleId)) return;
        this.fetchSchedule(scheduleId);
      },
      { allowSignalWrites: true }
    );

    // Reload the seat map whenever the class changes (and schedule is ready).
    effect(
      () => {
        const s = this.schedule();
        const cls = this.travelClass();
        if (!s) return;
        this.fetchSeats(s.id, cls);
      },
      { allowSignalWrites: true }
    );
  }

  label(cls: TravelClass): string {
    return travelClassLabel(cls);
  }

  selectClass(cls: TravelClass): void {
    if (cls === this.travelClass()) return;
    this.selectedSeats.set([]);
    this.travelClass.set(cls);
  }

  isBooked(seat: string): boolean {
    return this.bookedSeats().has(seat);
  }

  isSelected(seat: string): boolean {
    return this.selectedSeats().includes(seat);
  }

  toggleSeat(seat: string): void {
    if (this.isBooked(seat)) return;
    const current = this.selectedSeats();
    if (current.includes(seat)) {
      this.selectedSeats.set(current.filter((s) => s !== seat));
      return;
    }
    if (current.length >= MAX_SEATS) {
      this.toast.info(`You can select up to ${MAX_SEATS} seats.`);
      return;
    }
    this.selectedSeats.set([...current, seat]);
  }

  confirm(): void {
    const s = this.schedule();
    if (!s || this.selectedSeats().length === 0) {
      this.toast.info('Please select at least one seat.');
      return;
    }
    this.booking.set(true);
    this.bookingService
      .book({
        scheduleId: s.id,
        travelClass: this.travelClass(),
        seatNumbers: this.selectedSeats(),
      })
      .subscribe({
        next: (b) => {
          this.toast.success('Booking confirmed! 🎉');
          this.router.navigate(['/bookings'], {
            queryParams: { highlight: b.id },
          });
        },
        error: (err) => {
          this.booking.set(false);
          if (!isAuthHandledError(err)) {
            this.toast.error(extractErrorMessage(err, 'Booking failed.'));
          }
          // Refresh seat map in case someone else booked the seats.
          this.fetchSeats(s.id, this.travelClass());
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/search']);
  }

  private fetchSchedule(scheduleId: number): void {
    this.loadingSchedule.set(true);
    this.scheduleService.getById(scheduleId).subscribe({
      next: (data) => {
        this.schedule.set(data);
        this.loadingSchedule.set(false);
      },
      error: (err) => {
        this.loadingSchedule.set(false);
        if (!isAuthHandledError(err)) {
          this.toast.error(
            extractErrorMessage(err, 'Could not load train details.')
          );
        }
        this.router.navigate(['/search']);
      },
    });
  }

  private fetchSeats(scheduleId: number, cls: TravelClass): void {
    this.loadingSeats.set(true);
    this.scheduleService.getSeatMap(scheduleId, cls).subscribe({
      next: (map) => {
        const booked = new Set<string>();
        // API returns a `bookedSeats` string array; also support a detailed
        // `seats` array as a fallback.
        for (const seat of map?.bookedSeats ?? []) {
          booked.add(seat);
        }
        for (const seat of map?.seats ?? []) {
          if (seat.booked) {
            booked.add(seat.seatNumber);
          }
        }
        this.bookedSeats.set(booked);
        this.loadingSeats.set(false);
      },
      error: () => {
        // Fall back to an all-available grid if the seat map is unavailable.
        this.bookedSeats.set(new Set());
        this.loadingSeats.set(false);
      },
    });
  }
}
