/**
 * Whether a catalog item (medication, treatment) is for humans or animals.
 * Matches the Java enum {@code ma.pharmacie.common.enums.UsageType} byte-for-byte.
 */
export type UsageType = 'HUMAN' | 'VETERINARY';

/** Options used by select / segmented-control widgets. */
export const USAGE_TYPE_OPTIONS: ReadonlyArray<{
  value: UsageType;
  label: string;
  icon: string;
}> = [
  { value: 'HUMAN',      label: 'Humain',      icon: 'pi pi-user' },
  { value: 'VETERINARY', label: 'Vétérinaire', icon: 'pi pi-paw' },
];

/** Human-readable label for a usage type — used in badges / table cells. */
export function usageTypeLabel(t: UsageType): string {
  return t === 'VETERINARY' ? 'Vétérinaire' : 'Humain';
}

