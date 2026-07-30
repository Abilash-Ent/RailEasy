import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/services/booking.service';
import { ToastService } from '../../../core/services/toast.service';
import { Booking } from '../../../core/models/booking.model';
import { travelClassLabel } from '../../../core/models/travel-class.model';
import { extractErrorMessage, isAuthHandledError } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.css',
})
export class MyBookingsComponent {
  private readonly bookingService = inject(BookingService);
  private readonly toast = inject(ToastService);

  readonly loading = signal(true);
  readonly bookings = signal<Booking[]>([]);
  readonly cancellingId = signal<number | null>(null);
  readonly downloadingId = signal<number | null>(null);

  readonly hasBookings = computed(() => this.bookings().length > 0);

  constructor() {
    this.load();
  }

  label(cls: string): string {
    return travelClassLabel(cls);
  }

  isCancelled(b: Booking): boolean {
    return (b.status ?? '').toUpperCase() === 'CANCELLED';
  }

  load(): void {
    this.loading.set(true);
    this.bookingService.myTickets().subscribe({
      next: (data) => {
        this.bookings.set(data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not load bookings.'));
        }
      },
    });
  }

  cancel(b: Booking): void {
    if (this.isCancelled(b)) return;
    const seatCount = b.seatNumbers?.length ?? 0;
    const seatWord = seatCount === 1 ? 'seat' : 'seats';
    const train = b.trainName || 'this train';
    if (
      !confirm(
        `Cancel your booking on ${train}? Your ${seatCount} ${seatWord} will be released and made available to others. This can't be undone.`
      )
    ) {
      return;
    }
    this.cancellingId.set(b.id);
    this.bookingService.cancel(b.id).subscribe({
      next: (updated) => {
        this.bookings.update((list) =>
          list.map((item) =>
            item.id === b.id
              ? { ...item, ...updated, status: updated?.status ?? 'CANCELLED' }
              : item
          )
        );
        this.cancellingId.set(null);
        this.toast.success('Booking cancelled.');
      },
      error: (err) => {
        this.cancellingId.set(null);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not cancel booking.'));
        }
      },
    });
  }

  downloadTicket(b: Booking): void {
    if (this.downloadingId() !== null) return;
    this.downloadingId.set(b.id);
    this.bookingService.downloadTicket(b.id).subscribe({
      next: (response) => {
        this.downloadingId.set(null);
        const blob = response.body;
        if (!blob) {
          this.toast.error('The ticket file is empty.');
          return;
        }
        this.saveBlob(blob, this.resolveFileName(response, b));
        this.toast.success('Ticket downloaded.');
      },
      error: (err) => {
        this.downloadingId.set(null);
        if (!isAuthHandledError(err)) {
          this.toast.error(
            extractErrorMessage(err, 'Could not download the ticket.')
          );
        }
      },
    });
  }

  private resolveFileName(
    response: HttpResponse<Blob>,
    b: Booking
  ): string {
    const disposition = response.headers.get('Content-Disposition');
    const match = disposition?.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
    if (match?.[1]) {
      return decodeURIComponent(match[1]);
    }
    const type = response.body?.type ?? '';
    const ext = type.includes('pdf') ? 'pdf' : 'bin';
    return `raileasy-ticket-${b.id}.${ext}`;
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  }
}
