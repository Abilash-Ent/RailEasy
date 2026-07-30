export interface ScheduleFares {
  SLEEPER: number;
  AC_3: number;
  AC_2: number;
}

export interface Schedule {
  id: number;
  trainId: number;
  trainNumber?: string;
  trainName?: string;
  fromStation: string;
  toStation: string;
  departureTime: string;
  arrivalTime: string;
  journeyDate: string;
  fares?: ScheduleFares;
  fareSleeper: number;
  fareAc3: number;
  fareAc2: number;
  availableSleeper?: number;
  availableAc3?: number;
  availableAc2?: number;
}

export interface ScheduleRequest {
  trainId: number;
  fromStation: string;
  toStation: string;
  departureTime: string;
  arrivalTime: string;
  journeyDate: string;
  fareSleeper: number;
  fareAc3: number;
  fareAc2: number;
}

export interface ScheduleSearchParams {
  from: string;
  to: string;
  date: string;
}
