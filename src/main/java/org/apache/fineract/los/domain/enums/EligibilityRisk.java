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
package org.apache.fineract.los.domain.enums;

import java.math.BigDecimal;

public enum EligibilityRisk {
  LOW_RISK("Manageable installment amount! High probability of approval by the credit committee."),
  MODERATE_RISK("The installment is at the limit of your income. Consider extending the term."),
  HIGH_RISK(
      "The installment amount accounts for more than 40% of the income. High chance of rejection.");

  private final String recommendationMessage;

  EligibilityRisk(String recommendationMessage) {
    this.recommendationMessage = recommendationMessage;
  }

  public String getRecommendationMessage() {
    return recommendationMessage;
  }

  public static EligibilityRisk from(BigDecimal debtToIncomeRatio) {
    if (debtToIncomeRatio.compareTo(BigDecimal.valueOf(30)) <= 0) {
      return LOW_RISK;
    }
    if (debtToIncomeRatio.compareTo(BigDecimal.valueOf(40)) <= 0) {
      return MODERATE_RISK;
    }
    return HIGH_RISK;
  }
}
