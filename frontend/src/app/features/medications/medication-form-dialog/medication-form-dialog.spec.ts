import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MessageService } from 'primeng/api';

import { MedicationFormDialogComponent } from './medication-form-dialog';

describe('MedicationFormDialogComponent', () => {
  let fixture: ComponentFixture<MedicationFormDialogComponent>;
  let component: MedicationFormDialogComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicationFormDialogComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAnimationsAsync(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MedicationFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates', () => {
    expect(component).toBeTruthy();
  });

  it('starts with the form marked invalid (name is required)', () => {
    expect(component['form'].invalid).toBe(true);
  });
});

