/** "2025-11-12" → "2025.11.12" */
export function formatDotDate(isoDate: string): string {
  return isoDate.replaceAll('-', '.');
}
