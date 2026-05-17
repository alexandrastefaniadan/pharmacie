import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { MedicationFilterComponent } from './medication-filter';

describe('MedicationFilterComponent', () => {
  let fixture: ComponentFixture<MedicationFilterComponent>;
  let component: MedicationFilterComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicationFilterComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MedicationFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates', () => {
    expect(component).toBeTruthy();
  });

  it('emits filterChange when reset is called', () => {
    const spy = vi.fn();
    component.filterChange.subscribe(spy);
    component.reset();
    expect(spy).toHaveBeenCalled();
  });
});

