import { Pipe, PipeTransform } from '@angular/core';
import { LookupDto } from '@core/models/lookup.model';

/** Joins an array of {@link LookupDto} into a comma-separated French label. */
@Pipe({ name: 'joinLabels' })
export class JoinLabelsPipe implements PipeTransform {
  transform(values: ReadonlyArray<LookupDto> | null | undefined, separator = ', '): string {
    if (!values || values.length === 0) return '';
    return values.map((v) => v.labelFr).join(separator);
  }
}

