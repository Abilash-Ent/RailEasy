export interface Train {
  id: number;
  trainNumber: string;
  trainName: string;
  totalSeatsPerClass: number;
}

export interface TrainRequest {
  trainNumber: string;
  trainName: string;
  totalSeatsPerClass: number;
}
