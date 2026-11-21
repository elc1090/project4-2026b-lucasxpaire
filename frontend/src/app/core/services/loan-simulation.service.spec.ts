/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoanSimulationRequest, LoanSimulationResponse } from '../models';
import { LoanSimulationService } from './loan-simulation.service';

const BASE = 'http://localhost:8082/api/v1/loan-simulation';

describe('LoanSimulationService', () => {
  let service: LoanSimulationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoanSimulationService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LoanSimulationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends POST request to /api/v1/loan-simulation with simulation parameters', () => {
    const mockRequest: LoanSimulationRequest = {
      requestedAmount: 10000,
      tenorMonths: 12,
      loanPurpose: 'EDUCATION',
      monthlyIncome: 5000,
      existingLoanObligations: 0,
    };

    const mockResponse: LoanSimulationResponse = {
      monthlyInstallment: 916.8,
      totalInterest: 1001.6,
      monthlyInterestRate: 0.015,
      totalAmountToPay: 11001.6,
      debtToIncomeRatio: 18.34,
      eligibilityStatus: 'LOW_RISK',
      recommendationMessage: 'Manageable installment amount!',
    };

    service.simulate(mockRequest).subscribe((res) => {
      expect(res).toEqual(mockResponse);
      expect(res.eligibilityStatus).toBe('LOW_RISK');
    });

    const req = http.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
  });
});
