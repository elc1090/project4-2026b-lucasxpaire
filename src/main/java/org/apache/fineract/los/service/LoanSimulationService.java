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

package org.apache.fineract.los.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.los.api.dto.response.LoanSimulationResponse;
import org.apache.fineract.los.domain.enums.EligibilityRisk;
import org.apache.fineract.los.dto.request.LoanSimulationRequest;
import org.apache.fineract.los.scoring.factors.LoanPurposeRiskFactor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanSimulationService {

  private static final int CALCULATION_SCALE = 10;
  private static final int MONETARY_SCALE = 2;

  private final LoanPurposeRiskFactor loanPurposeRiskFactor;

  public LoanSimulationResponse simulate(LoanSimulationRequest request) {
    BigDecimal monthlyInterestRate = getInterestPurpose(request.getLoanPurpose());
    BigDecimal monthlyInstallment = getMonthlyInstallment(request, monthlyInterestRate);
    BigDecimal totalAmountToPay = monthlyInstallment.multiply(BigDecimal.valueOf(request.getTenorMonths())).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
    BigDecimal totalInterest = totalAmountToPay.subtract(request.getRequestedAmount());
    BigDecimal debtToIncomeRatio = getDebtToIncomeRatio(request, monthlyInstallment);
    EligibilityRisk eligibilityStatus = EligibilityRisk.from(debtToIncomeRatio);

    return LoanSimulationResponse.builder()
        .monthlyInstallment(monthlyInstallment)
        .totalInterest(totalInterest)
        .monthlyInterestRate(monthlyInterestRate)
        .totalAmountToPay(totalAmountToPay)
        .debtToIncomeRatio(debtToIncomeRatio)
        .eligibilityStatus(eligibilityStatus.name())
        .recommendationMessage(eligibilityStatus.getRecommendationMessage())
        .build();
  }

  private BigDecimal getDebtToIncomeRatio(LoanSimulationRequest request, BigDecimal newMonthlyInstallment) {
    if (request.getMonthlyIncome() == null || request.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("monthlyIncome must be greater than zero");
    }
    BigDecimal existingObligations = request.getExistingLoanObligations() != null
            ? request.getExistingLoanObligations()
            : BigDecimal.ZERO;
    BigDecimal numerator = existingObligations.add(newMonthlyInstallment).multiply(BigDecimal.valueOf(100));
    BigDecimal denominator = request.getMonthlyIncome();
    return numerator.divide(denominator, MONETARY_SCALE, RoundingMode.HALF_UP);
  }

  private BigDecimal getMonthlyInstallment(LoanSimulationRequest request, BigDecimal monthlyInterestRate) {
    BigDecimal compoundFactor = BigDecimal.ONE.add(monthlyInterestRate).pow(request.getTenorMonths());
    BigDecimal numerator = monthlyInterestRate.multiply(compoundFactor);
    BigDecimal denominator = compoundFactor.subtract(BigDecimal.ONE);
    BigDecimal installmentFactor = numerator.divide(denominator, CALCULATION_SCALE, RoundingMode.HALF_UP);
    return request.getRequestedAmount().multiply(installmentFactor).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
  }

  private BigDecimal getInterestPurpose(String loanPurpose) {
    return switch (loanPurposeRiskFactor.resolvePercentage(loanPurpose)) {
      case 100 -> new BigDecimal("0.015");
      case 75 -> new BigDecimal("0.018");
      case 50 -> new BigDecimal("0.025");
      default -> new BigDecimal("0.030");
    };
  }
}
