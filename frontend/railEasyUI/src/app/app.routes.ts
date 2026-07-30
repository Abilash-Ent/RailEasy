import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'search' },
  {
    path: 'search',
    title: 'Search Trains · RailEasy',
    loadComponent: () =>
      import('./features/schedules/search/search.component').then(
        (m) => m.SearchComponent
      ),
  },
  {
    path: 'login',
    title: 'Log in · RailEasy',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },
  {
    path: 'register',
    title: 'Sign up · RailEasy',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(
        (m) => m.RegisterComponent
      ),
  },
  {
    path: 'book/:id',
    title: 'Select Seats · RailEasy',
    canActivate: [authGuard],
    loadComponent: () =>
      import(
        './features/schedules/seat-selection/seat-selection.component'
      ).then((m) => m.SeatSelectionComponent),
  },
  {
    path: 'bookings',
    title: 'My Bookings · RailEasy',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/bookings/my-bookings/my-bookings.component').then(
        (m) => m.MyBookingsComponent
      ),
  },
  {
    path: 'admin/trains',
    title: 'Manage Trains · RailEasy',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/trains/admin-trains.component').then(
        (m) => m.AdminTrainsComponent
      ),
  },
  {
    path: 'admin/schedules',
    title: 'Manage Schedules · RailEasy',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/schedules/admin-schedules.component').then(
        (m) => m.AdminSchedulesComponent
      ),
  },
  { path: '**', redirectTo: 'search' },
];
