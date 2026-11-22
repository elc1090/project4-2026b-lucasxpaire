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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.apache.fineract.los.api.dto.response.LoanSimulationResponse;
import org.apache.fineract.los.domain.enums.EligibilityRisk;
import org.apache.fineract.los.domain.enums.LoanPurpose;
import org.apache.fineract.los.dto.request.LoanSimulationRequest;
import org.apache.fineract.los.scoring.ScoringWeightsProperties;
import org.apache.fineract.los.scoring.factors.LoanPurposeRiskFactor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoanSimulationService")
class LoanSimulationServiceTest {

  private LoanSimulationService simulationService;

  @BeforeEach
  void setUp() {
    simulationService =
        new LoanSimulationService(new LoanPurposeRiskFactor(new ScoringWeightsProperties()));
  }

  @Test
  @DisplayName(
      "Should calculate French amortization (Price) and classify as LOW_RISK when DTI <= 30%")
  void shouldCalculatePriceInstallmentAndLowRiskSuccessfully() {
    // 10,000 for 12 months at 1.5% a.m. (EDUCATION)
    // PMT = 10,000 * [0.015 * (1.015)^12] / [(1.015)^12 - 1] = 916.80
    // Total to pay = 916.80 * 12 = 11,001.60
    // Total interest = 1,001.60
    // DTI = (0 + 916.80) / 5,000 * 100 = 18.34% -> LOW_RISK
    LoanSimulationRequest request =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("10000.00"))
            .tenorMonths(12)
            .loanPurpose("EDUCATION")
            .monthlyIncome(new BigDecimal("5000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();

    LoanSimulationResponse response = simulationService.simulate(request);

    assertThat(response.getMonthlyInterestRate()).isEqualByComparingTo("0.015");
    assertThat(response.getMonthlyInstallment()).isEqualByComparingTo("916.80");
    assertThat(response.getTotalAmountToPay()).isEqualByComparingTo("11001.60");
    assertThat(response.getTotalInterest()).isEqualByComparingTo("1001.60");
    assertThat(response.getDebtToIncomeRatio()).isEqualByComparingTo("18.34");
    assertThat(response.getEligibilityStatus()).isEqualTo(EligibilityRisk.LOW_RISK.name());
    assertThat(response.getRecommendationMessage())
        .isEqualTo(EligibilityRisk.LOW_RISK.getRecommendationMessage());
  }

  @Test
  @DisplayName("Should classify as MODERATE_RISK when DTI is between 30% and 40%")
  void shouldClassifyAsModerateRiskWhenDtiIsBetween30And40Percent() {
    // Installment = 916.80 + 200 existing debt = 1116.80
    // Income = 3,000.00 -> DTI = 1116.80 / 3000 * 100 = 37.23% -> MODERATE_RISK
    LoanSimulationRequest request =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("10000.00"))
            .tenorMonths(12)
            .loanPurpose("EDUCATION")
            .monthlyIncome(new BigDecimal("3000.00"))
            .existingLoanObligations(new BigDecimal("200.00"))
            .build();

    LoanSimulationResponse response = simulationService.simulate(request);

    assertThat(response.getDebtToIncomeRatio()).isEqualByComparingTo("37.23");
    assertThat(response.getEligibilityStatus()).isEqualTo(EligibilityRisk.MODERATE_RISK.name());
    assertThat(response.getRecommendationMessage())
        .isEqualTo(EligibilityRisk.MODERATE_RISK.getRecommendationMessage());
  }

  @Test
  @DisplayName("Should classify as HIGH_RISK when DTI is greater than 40%")
  void shouldClassifyAsHighRiskWhenDtiExceeds40Percent() {
    // Installment = 916.80 / 2,000 * 100 = 45.84% -> HIGH_RISK
    LoanSimulationRequest request =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("10000.00"))
            .tenorMonths(12)
            .loanPurpose("EDUCATION")
            .monthlyIncome(new BigDecimal("2000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();

    LoanSimulationResponse response = simulationService.simulate(request);

    assertThat(response.getDebtToIncomeRatio()).isEqualByComparingTo("45.84");
    assertThat(response.getEligibilityStatus()).isEqualTo(EligibilityRisk.HIGH_RISK.name());
    assertThat(response.getRecommendationMessage())
        .isEqualTo(EligibilityRisk.HIGH_RISK.getRecommendationMessage());
  }

  @Test
  @DisplayName("Should apply dynamic interest rates based on loan purpose")
  void shouldApplyDynamicInterestRatesBasedOnLoanPurpose() {
    LoanSimulationRequest educationRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("AGRICULTURE")
            .monthlyIncome(new BigDecimal("4000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();
    assertThat(simulationService.simulate(educationRequest).getMonthlyInterestRate())
        .isEqualByComparingTo("0.015");

    LoanSimulationRequest businessRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("BUSINESS")
            .monthlyIncome(new BigDecimal("4000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();
    assertThat(simulationService.simulate(businessRequest).getMonthlyInterestRate())
        .isEqualByComparingTo("0.018");

    LoanSimulationRequest medicalRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("MEDICAL")
            .monthlyIncome(new BigDecimal("4000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();
    assertThat(simulationService.simulate(medicalRequest).getMonthlyInterestRate())
        .isEqualByComparingTo("0.025");

    LoanSimulationRequest otherRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("OTHER")
            .monthlyIncome(new BigDecimal("4000.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();
    assertThat(simulationService.simulate(otherRequest).getMonthlyInterestRate())
        .isEqualByComparingTo("0.030");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when monthlyIncome is zero or negative")
  void shouldThrowExceptionWhenMonthlyIncomeIsZeroOrNegative() {
    LoanSimulationRequest zeroIncomeRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("EDUCATION")
            .monthlyIncome(BigDecimal.ZERO)
            .existingLoanObligations(BigDecimal.ZERO)
            .build();

    assertThatThrownBy(() -> simulationService.simulate(zeroIncomeRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("monthlyIncome must be greater than zero");

    LoanSimulationRequest negativeIncomeRequest =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("5000.00"))
            .tenorMonths(6)
            .loanPurpose("EDUCATION")
            .monthlyIncome(new BigDecimal("-100.00"))
            .existingLoanObligations(BigDecimal.ZERO)
            .build();

    assertThatThrownBy(() -> simulationService.simulate(negativeIncomeRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("monthlyIncome must be greater than zero");
  }

  @Test
  @DisplayName(
      "Should treat null existingLoanObligations as zero without throwing NullPointerException")
  void shouldHandleNullExistingObligationsGracefully() {
    LoanSimulationRequest request =
        LoanSimulationRequest.builder()
            .requestedAmount(new BigDecimal("10000.00"))
            .tenorMonths(12)
            .loanPurpose("EDUCATION")
            .monthlyIncome(new BigDecimal("5000.00"))
            .existingLoanObligations(null)
            .build();

    LoanSimulationResponse response = simulationService.simulate(request);

    assertThat(response.getDebtToIncomeRatio()).isEqualByComparingTo("18.34");
    assertThat(response.getEligibilityStatus()).isEqualTo(EligibilityRisk.LOW_RISK.name());
  }

  @Test
  @DisplayName(
      "Should validate all LoanPurpose enum values including HOME_IMPROVEMENT, CONSUMER and SPECULATION")
  void shouldAcceptAllLoanPurposeEnumValuesInRequestValidation() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    for (LoanPurpose purpose : LoanPurpose.values()) {
      LoanSimulationRequest request =
          LoanSimulationRequest.builder()
              .requestedAmount(new BigDecimal("10000.00"))
              .tenorMonths(12)
              .loanPurpose(purpose.name())
              .monthlyIncome(new BigDecimal("5000.00"))
              .existingLoanObligations(BigDecimal.ZERO)
              .build();

      assertThat(validator.validate(request))
          .as("Validation violations for purpose %s", purpose)
          .isEmpty();
    }
  }
}
