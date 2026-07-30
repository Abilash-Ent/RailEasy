import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ToastService } from '../../../core/services/toast.service';
import { Schedule } from '../../../core/models/schedule.model';
import { extractErrorMessage, isAuthHandledError } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css',
})
export class SearchComponent {
  private readonly fb = inject(FormBuilder);
  private readonly scheduleService = inject(ScheduleService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly searched = signal(false);
  readonly results = signal<Schedule[]>([]);

  readonly today = new Date().toISOString().split('T')[0];

  readonly form = this.fb.nonNullable.group({
    from: ['', [Validators.required]],
    to: ['', [Validators.required]],
    date: [this.today, [Validators.required]],
  });

  readonly resultCount = computed(() => this.results().length);

  swap(): void {
    const { from, to } = this.form.getRawValue();
    this.form.patchValue({ from: to, to: from });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.searched.set(true);
    this.scheduleService.search(this.form.getRawValue()).subscribe({
      next: (data) => {
        this.results.set(data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.results.set([]);
        if (!isAuthHandledError(err)) {
          this.toast.error(extractErrorMessage(err, 'Search failed.'));
        }
      },
    });
  }

  book(schedule: Schedule): void {
    this.router.navigate(['/book', schedule.id]);
  }
}
