import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TreatmentsListPage } from './treatments-list';

describe('TreatmentsListPage', () => {
  let component: TreatmentsListPage;
  let fixture: ComponentFixture<TreatmentsListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TreatmentsListPage],
    }).compileComponents();
    fixture = TestBed.createComponent(TreatmentsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

