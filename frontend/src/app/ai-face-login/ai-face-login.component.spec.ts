import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiFaceLoginComponent } from './ai-face-login.component';

describe('AiFaceLoginComponent', () => {
  let component: AiFaceLoginComponent;
  let fixture: ComponentFixture<AiFaceLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiFaceLoginComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AiFaceLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
