import { TravelClass } from './travel-class.model';

export interface Seat {
  seatNumber: string;
  booked: boolean;
}

export interface SeatMap {
  scheduleId: number;
  travelClass: TravelClass;
  bookedSeats?: string[];
  seats?: Seat[];
}

export interface BookingRequest {
  scheduleId: number;
  travelClass: TravelClass;
  seatNumbers: string[];
}

export type BookingStatus = 'CONFIRMED' | 'CANCELLED' | string;

export interface Booking {
  id: number;
  scheduleId: number;
  travelClass: TravelClass;
  seatNumbers: string[];
  status: BookingStatus;
  totalFare: number;
  fromStation?: string;
  toStation?: string;
  journeyDate?: string;
  departureTime?: string;
  arrivalTime?: string;
  trainName?: string;
  trainNumber?: string;
  createdAt?: string;
}
