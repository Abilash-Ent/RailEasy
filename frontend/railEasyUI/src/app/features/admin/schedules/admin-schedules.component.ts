import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ScheduleService } from '../../../core/services/schedule.service';
import { TrainService } from '../../../core/services/train.service';
import { ToastService } from '../../../core/services/toast.service';
import { Schedule } from '../../../core/models/schedule.model';
import { Train } from '../../../core/models/train.model';
import { extractErrorMessage, isAuthHandledError, applyServerFieldErrors } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-admin-schedules',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './admin-schedules.component.html',
  styleUrl: '../admin.css',
})
export class AdminSchedulesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly scheduleService = inject(ScheduleService);
  private readonly trainService = inject(TrainService);
  private readonly toast = inject(ToastService);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly schedules = signal<Schedule[]>([]);
  readonly trains = signal<Train[]>([]);
  readonly modalOpen = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly today = new Date().toISOString().split('T')[0];

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Edit schedule' : 'Add schedule'
  );

  readonly form = this.fb.nonNullable.group({
    trainId: [null as number | null, [Validators.required]],
    fromStation: ['', [Validators.required]],
    toStation: ['', [Validators.required]],
    departureTime: ['21:30', [Validators.required]],
    arrivalTime: ['06:45', [Validators.required]],
    journeyDate: ['', [Validators.required]],
    fareSleeper: [800, [Validators.required, Validators.min(0)]],
    fareAc3: [1400, [Validators.required, Validators.min(0)]],
    fareAc2: [2000, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.load();

    // Clear a field's server-side error as soon as the user edits it.
    for (const control of Object.values(this.form.controls) as AbstractControl[]) {
      control.valueChanges.subscribe(() => {
        const errors = control.errors;
        if (errors?.['server']) {
          const { server, ...rest } = errors;
          control.setErrors(Object.keys(rest).length ? rest : null);
        }
      });
    }
  }

  trainLabel(trainId: number): string {
    const t = this.trains().find((tr) => tr.id === trainId);
    return t ? `${t.trainName} (#${t.trainNumber})` : `Train ${trainId}`;
  }

  load(): void {
    this.loading.set(true);
    forkJoin({
      schedules: this.scheduleService.getAll(),
      trains: this.trainService.getAll(),
    }).subscribe({
      next: ({ schedules, trains }) => {
        this.schedules.set(schedules ?? []);
        this.trains.set(trains ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not load data.'));
        }
      },
    });
  }

  openCreate(): void {
    if (this.trains().length === 0) {
      this.toast.info('Please add a train before creating a schedule.');
      return;
    }
    this.editingId.set(null);
    this.form.reset({
      trainId: this.trains()[0]?.id ?? null,
      fromStation: '',
      toStation: '',
      departureTime: '21:30',
      arrivalTime: '06:45',
      journeyDate: '',
      fareSleeper: 800,
      fareAc3: 1400,
      fareAc2: 2000,
    });
    this.modalOpen.set(true);
  }

  openEdit(s: Schedule): void {
    this.editingId.set(s.id);
    this.form.reset({
      trainId: s.trainId,
      fromStation: s.fromStation,
      toStation: s.toStation,
      departureTime: this.trimSeconds(s.departureTime),
      arrivalTime: this.trimSeconds(s.arrivalTime),
      journeyDate: (s.journeyDate ?? '').split('T')[0],
      fareSleeper: s.fareSleeper,
      fareAc3: s.fareAc3,
      fareAc2: s.fareAc2,
    });
    this.modalOpen.set(true);
  }

  closeModal(): void {
    this.modalOpen.set(false);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const raw = this.form.getRawValue();
    const payload = {
      trainId: Number(raw.trainId),
      fromStation: raw.fromStation,
      toStation: raw.toStation,
      departureTime: this.withSeconds(raw.departureTime),
      arrivalTime: this.withSeconds(raw.arrivalTime),
      journeyDate: raw.journeyDate,
      fareSleeper: raw.fareSleeper,
      fareAc3: raw.fareAc3,
      fareAc2: raw.fareAc2,
    };
    const editId = this.editingId();
    const request$ = editId
      ? this.scheduleService.update(editId, payload)
      : this.scheduleService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.modalOpen.set(false);
        this.toast.success(editId ? 'Schedule updated.' : 'Schedule created.');
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        const hadFieldErrors = applyServerFieldErrors(this.form, err);
        if (!isAuthHandledError(err)) {
          this.toast.error(
            extractErrorMessage(
              err,
              hadFieldErrors
                ? 'Please correct the highlighted fields and try again.'
                : 'Could not save schedule.'
            )
          );
        }
      },
    });
  }

  remove(s: Schedule): void {
    if (!confirm(`Delete schedule ${s.fromStation} → ${s.toStation}?`)) {
      return;
    }
    this.scheduleService.delete(s.id).subscribe({
      next: () => {
        this.schedules.update((list) => list.filter((x) => x.id !== s.id));
        this.toast.success('Schedule deleted.');
      },
      error: (err) => {
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not delete schedule.'));
        }
      },
    });
  }

  private trimSeconds(time: string): string {
    return (time ?? '').slice(0, 5);
  }

  private withSeconds(time: string): string {
    return time?.length === 5 ? `${time}:00` : time;
  }
}
