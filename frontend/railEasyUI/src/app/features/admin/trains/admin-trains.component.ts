import { Component, computed, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TrainService } from '../../../core/services/train.service';
import { ToastService } from '../../../core/services/toast.service';
import { Train } from '../../../core/models/train.model';
import { extractErrorMessage, isAuthHandledError } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-admin-trains',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './admin-trains.component.html',
  styleUrl: '../admin.css',
})
export class AdminTrainsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly trainService = inject(TrainService);
  private readonly toast = inject(ToastService);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly trains = signal<Train[]>([]);
  readonly modalOpen = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Edit train' : 'Add train'
  );

  readonly form = this.fb.nonNullable.group({
    trainNumber: ['', [Validators.required]],
    trainName: ['', [Validators.required]],
    totalSeatsPerClass: [64, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.trainService.getAll().subscribe({
      next: (data) => {
        this.trains.set(data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not load trains.'));
        }
      },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ trainNumber: '', trainName: '', totalSeatsPerClass: 64 });
    this.modalOpen.set(true);
  }

  openEdit(train: Train): void {
    this.editingId.set(train.id);
    this.form.reset({
      trainNumber: train.trainNumber,
      trainName: train.trainName,
      totalSeatsPerClass: train.totalSeatsPerClass,
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
    const payload = this.form.getRawValue();
    const editId = this.editingId();
    const request$ = editId
      ? this.trainService.update(editId, payload)
      : this.trainService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.modalOpen.set(false);
        this.toast.success(editId ? 'Train updated.' : 'Train created.');
        this.load();
      },
      error: (err) => {
        this.saving.set(false);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not save train.'));
        }
      },
    });
  }

  remove(train: Train): void {
    if (!confirm(`Delete train ${train.trainName} (#${train.trainNumber})?`)) {
      return;
    }
    this.trainService.delete(train.id).subscribe({
      next: () => {
        this.trains.update((list) => list.filter((t) => t.id !== train.id));
        this.toast.success('Train deleted.');
      },
      error: (err) => {
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Could not delete train.'));
        }
      },
    });
  }
}
