import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeavesUsersComponent } from './leaves-users.component';

describe('LeavesUsersComponent', () => {
  let component: LeavesUsersComponent;
  let fixture: ComponentFixture<LeavesUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeavesUsersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LeavesUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
