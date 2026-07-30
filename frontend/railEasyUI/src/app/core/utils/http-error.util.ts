import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

/**
 * True for auth-related statuses (401/403) that the auth interceptor already
 * surfaces to the user. Components should skip their own toast for these to
 * avoid duplicate messages.
 */
export function isAuthHandledError(error: unknown): boolean {
  return (
    error instanceof HttpErrorResponse &&
    (error.status === 401 || error.status === 403)
  );
}

/**
 * Parses a backend validation response whose `details` array contains strings
 * shaped like "fieldName: human readable message" into a { field: message } map.
 */
export function parseFieldErrors(error: unknown): Record<string, string> {
  const result: Record<string, string> = {};
  if (!(error instanceof HttpErrorResponse)) {
    return result;
  }
  const details = error.error?.details;
  if (!Array.isArray(details)) {
    return result;
  }
  for (const entry of details) {
    if (typeof entry !== 'string') {
      continue;
    }
    const separator = entry.indexOf(':');
    if (separator === -1) {
      continue;
    }
    const field = entry.slice(0, separator).trim();
    const message = entry.slice(separator + 1).trim();
    if (field) {
      result[field] = message || entry;
    }
  }
  return result;
}

/**
 * Applies backend field validation errors to the matching form controls so the
 * inputs highlight and show the server message. Returns true if any field error
 * was applied.
 */
export function applyServerFieldErrors(
  form: FormGroup,
  error: unknown
): boolean {
  const fieldErrors = parseFieldErrors(error);
  let applied = false;
  for (const [field, message] of Object.entries(fieldErrors)) {
    const control = form.get(field);
    if (control) {
      control.setErrors({ ...(control.errors ?? {}), server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}

export function extractErrorMessage(
  error: unknown,
  fallback = 'Something went wrong. Please try again.'
): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Cannot reach the server. Please check your connection.';
    }
    const body = error.error;
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (body && typeof body === 'object') {
      return (
        body.message ??
        body.error ??
        body.detail ??
        error.message ??
        fallback
      );
    }
    return error.message || fallback;
  }
  return fallback;
}
