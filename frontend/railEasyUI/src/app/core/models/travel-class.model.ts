export type TravelClass = 'SLEEPER' | 'AC_3' | 'AC_2';

export const TRAVEL_CLASSES: { value: TravelClass; label: string }[] = [
  { value: 'SLEEPER', label: 'Sleeper' },
  { value: 'AC_3', label: 'AC 3 Tier' },
  { value: 'AC_2', label: 'AC 2 Tier' },
];

export function travelClassLabel(value: TravelClass | string): string {
  return TRAVEL_CLASSES.find((c) => c.value === value)?.label ?? value;
}
