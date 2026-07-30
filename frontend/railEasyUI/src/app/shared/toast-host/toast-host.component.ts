import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  template: `
    <div class="toast-host">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast toast-{{ t.kind }}" (click)="toast.dismiss(t.id)">
          <span class="toast-icon">
            @switch (t.kind) {
              @case ('success') { ✓ }
              @case ('error') { ⚠ }
              @default { ℹ }
            }
          </span>
          <span class="toast-msg">{{ t.message }}</span>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .toast-icon {
        font-weight: 700;
        font-size: 1rem;
        line-height: 1.3;
      }
      .toast-msg {
        font-size: 0.9rem;
        color: var(--ink-700);
      }
      .toast {
        cursor: pointer;
      }
    `,
  ],
})
export class ToastHostComponent {
  readonly toast = inject(ToastService);
}
