import { TestBed } from '@angular/core/testing';

import { AiFaceIdService } from './ai-face-id.service';

describe('AiFaceIdService', () => {
  let service: AiFaceIdService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AiFaceIdService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
