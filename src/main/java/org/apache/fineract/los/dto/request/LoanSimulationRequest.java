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

package org.apache.fineract.los.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSimulationRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "requestedAmount must be greater than zero")
    private BigDecimal requestedAmount;

    @NotNull(message = "tenorMonths is required")
    @Min(value = 1, message = "tenorMonths must be at least 1")
    private Integer tenorMonths;

    @NotNull
    @Size(max = 50, message = "loanPurpose must not exceed 50 characters")
    @Pattern(
        regexp = "^(AGRICULTURE|EDUCATION|BUSINESS|PERSONAL|HOUSING|MEDICAL|OTHER)$",
        message = "loanPurpose must be one of: AGRICULTURE, EDUCATION, BUSINESS, PERSONAL, HOUSING, MEDICAL, OTHER")
    private String loanPurpose;

    @NotNull
    @PositiveOrZero(message = "monthlyIncome must be zero or positive")
    private BigDecimal monthlyIncome;

    @NotNull
    @PositiveOrZero(message = "existingLoanObligations must be zero or positive")
    private BigDecimal existingLoanObligations;
}
