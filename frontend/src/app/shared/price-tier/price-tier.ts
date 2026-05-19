import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Visual price ranking using 5 dollar icons. Renders a row of {@code pi-dollar}
 * glyphs where the first {@code value} ones are filled (gold) and the rest are
 * outlined (muted).
 *
 * <p>Works in two modes:
 * <ul>
 *   <li><strong>Display</strong> (default): purely informational.</li>
 *   <li><strong>Editable</strong> (set {@code editable=true}): clicking an
 *       icon sets that value; clicking the currently-selected one clears it
 *       to 0 ("not rated").</li>
 * </ul>
 */
@Component({
  selector: 'app-price-tier',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <span
      class="price-tier"
      [class.editable]="editable"
      [attr.aria-label]="ariaLabel"
      [attr.role]="editable ? 'radiogroup' : 'img'"
    >
      @for (i of slots; track i) {
        <button
          type="button"
          class="slot"
          [class.on]="i <= value"
          [class.off]="i > value"
          [disabled]="!editable"
          [attr.aria-label]="i + ' dollar' + (i > 1 ? 's' : '')"
          (click)="onClick(i, $event)"
        >
          <i class="pi pi-dollar"></i>
        </button>
      }
    </span>
  `,
  styles: [`
    :host { display: inline-flex; }
    .price-tier { display: inline-flex; gap: 0.1rem; line-height: 1; }
    .slot {
      all: unset;
      display: inline-flex; align-items: center; justify-content: center;
      width: 1rem; height: 1rem;
      font-size: 0.85rem;
      color: var(--p-surface-300);
    }
    .slot.on { color: #d4a017; } /* gold */
    .price-tier.editable .slot {
      cursor: pointer;
      transition: transform 0.08s ease, color 0.08s ease;
    }
    .price-tier.editable .slot:hover { transform: scale(1.15); }
    .price-tier.editable .slot:focus-visible {
      outline: 2px solid var(--p-primary-color);
      outline-offset: 2px;
      border-radius: 3px;
    }
  `],
})
export class PriceTierComponent {
  /** Current value, 0..5. */
  @Input() value = 0;
  /** When true, clicks change the value and emit {@link valueChange}. */
  @Input() editable = false;

  @Output() readonly valueChange = new EventEmitter<number>();

  protected readonly slots = [1, 2, 3, 4, 5] as const;

  protected get ariaLabel(): string {
    return this.value === 0 ? 'Prix non évalué' : `Prix ${this.value}/5`;
  }

  protected onClick(i: number, ev: Event): void {
    if (!this.editable) return;
    ev.preventDefault();
    // Clicking the currently-selected level clears the rating.
    const next = this.value === i ? 0 : i;
    this.value = next;
    this.valueChange.emit(next);
  }
}

