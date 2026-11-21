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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { LoanSimulationResponse } from '../../core/models';
import { LoanSimulationService } from '../../core/services/loan-simulation.service';
import { LoanSimulatorComponent } from './loan-simulator.component';

describe('LoanSimulatorComponent', () => {
  let component: LoanSimulatorComponent;
  let fixture: ComponentFixture<LoanSimulatorComponent>;
  let mockSimulationService: { simulate: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };

  const mockResponse: LoanSimulationResponse = {
    monthlyInstallment: 916.8,
    totalInterest: 1001.6,
    monthlyInterestRate: 0.015,
    totalAmountToPay: 11001.6,
    debtToIncomeRatio: 18.34,
    eligibilityStatus: 'LOW_RISK',
    recommendationMessage: 'Manageable installment amount!',
  };

  beforeEach(async () => {
    mockSimulationService = {
      simulate: vi.fn().mockReturnValue(of(mockResponse)),
    };
    mockRouter = {
      navigate: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoanSimulatorComponent],
      providers: [
        { provide: LoanSimulationService, useValue: mockSimulationService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanSimulatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates successfully and triggers initial simulation', () => {
    expect(component).toBeTruthy();
    expect(mockSimulationService.simulate).toHaveBeenCalledTimes(1);
    expect(component.result()).toEqual(mockResponse);
  });

  it('navigates to /apply with query params when applyWithSimulation is clicked', () => {
    component.form.patchValue({
      requestedAmount: 15000,
      tenorMonths: 24,
      loanPurpose: 'EDUCATION',
      monthlyIncome: 6000,
      existingLoanObligations: 100,
    });

    component.applyWithSimulation();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/apply'], {
      queryParams: {
        amount: 15000,
        tenor: 24,
        purpose: 'EDUCATION',
        income: 6000,
        obligations: 100,
      },
    });
  });
});
