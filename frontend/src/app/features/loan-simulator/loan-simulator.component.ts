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

import { CommonModule, CurrencyPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EMPTY, catchError, debounceTime, distinctUntilChanged, filter, switchMap } from 'rxjs';
import { LoanPurpose, LoanSimulationRequest, LoanSimulationResponse } from '../../core/models';
import { LoanSimulationService } from '../../core/services/loan-simulation.service';

@Component({
  selector: 'los-loan-simulator',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CurrencyPipe, PercentPipe],
  templateUrl: './loan-simulator.component.html',
  styleUrl: './loan-simulator.component.scss',
})
export class LoanSimulatorComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly simulationService = inject(LoanSimulationService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<LoanSimulationResponse | null>(null);

  readonly loanPurposes: { label: string; value: LoanPurpose }[] = [
    { label: 'Agriculture', value: 'AGRICULTURE' },
    { label: 'Education', value: 'EDUCATION' },
    { label: 'Business', value: 'BUSINESS' },
    { label: 'Home Improvement', value: 'HOME_IMPROVEMENT' },
    { label: 'Consumer', value: 'CONSUMER' },
    { label: 'Medical', value: 'MEDICAL' },
    { label: 'Speculation', value: 'SPECULATION' },
    { label: 'Other', value: 'OTHER' },
  ];

  form = this.fb.group({
    requestedAmount: [10000, [Validators.required, Validators.min(100), Validators.max(100000)]],
    tenorMonths: [12, [Validators.required, Validators.min(1), Validators.max(60)]],
    loanPurpose: ['BUSINESS' as LoanPurpose, Validators.required],
    monthlyIncome: [5000, [Validators.required, Validators.min(0.01)]],
    existingLoanObligations: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.form.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
        filter(() => this.form.valid),
        switchMap((value) => {
          this.loading.set(true);
          this.error.set(null);
          return this.simulationService.simulate(value as unknown as LoanSimulationRequest).pipe(
            catchError((err) => {
              this.loading.set(false);
              this.error.set(
                err?.error?.message ?? 'Could not calculate simulation. Please check your inputs.',
              );
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((res) => {
        this.loading.set(false);
        this.result.set(res);
      });
  }

  ngOnInit(): void {
    this.triggerSimulation();
  }

  triggerSimulation(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(null);

    const request = this.form.getRawValue() as unknown as LoanSimulationRequest;
    this.simulationService.simulate(request).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.result.set(res);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(
          err?.error?.message ?? 'Could not calculate simulation. Please check your inputs.',
        );
      },
    });
  }

  onRangeInput(controlName: 'requestedAmount' | 'tenorMonths', event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    this.form.controls[controlName].setValue(value);
  }

  applyWithSimulation(): void {
    const values = this.form.getRawValue();
    this.router.navigate(['/apply'], {
      queryParams: {
        amount: values.requestedAmount,
        tenor: values.tenorMonths,
        purpose: values.loanPurpose,
        income: values.monthlyIncome,
        obligations: values.existingLoanObligations,
      },
    });
  }
}
