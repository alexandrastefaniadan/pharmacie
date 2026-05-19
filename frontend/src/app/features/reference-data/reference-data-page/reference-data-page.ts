import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabsModule } from 'primeng/tabs';

import { LookupsApi } from '@core/api/lookups.api';
import { LookupTableComponent } from '../lookup-table/lookup-table';

/**
 * Reference-data management screen: 4 tabs, one per lookup dimension. Each
 * tab embeds the reusable {@link LookupTableComponent} pointed at the right
 * kind. The shared {@link LookupsApi} cache means the data is loaded once
 * and refreshed automatically after every write.
 */
@Component({
  selector: 'app-reference-data-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, TabsModule, LookupTableComponent],
  templateUrl: './reference-data-page.html',
  styleUrl: './reference-data-page.scss',
})
export class ReferenceDataPage implements OnInit {
  protected readonly lookups = inject(LookupsApi);

  ngOnInit(): void {
    // Force a refresh in case the user navigated here after editing things
    // elsewhere; cheap (4 small GETs) and guarantees up-to-date counts.
    this.lookups.refresh().subscribe();
  }
}

