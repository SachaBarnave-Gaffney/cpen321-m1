const pad = (n: number): string => String(n).padStart(2, '0');

/** Formats a date as "hh:mm:ss GMT+hh:mm" (24-hour, local time zone). */
export function formatTimeWithGmtOffset(date: Date): string {
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  const offsetMinutes = -date.getTimezoneOffset(); // e.g. Vancouver PDT -> -420
  const sign = offsetMinutes >= 0 ? '+' : '-';
  const abs = Math.abs(offsetMinutes);
  return `${time} GMT${sign}${pad(Math.floor(abs / 60))}:${pad(abs % 60)}`;
}
