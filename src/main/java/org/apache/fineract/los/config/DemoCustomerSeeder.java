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

package org.apache.fineract.los.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.los.domain.CustomerCredential;
import org.apache.fineract.los.repository.CustomerCredentialRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds the demo customer account (cliente / 123456) on startup if missing. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoCustomerSeeder implements ApplicationRunner {

  private final CustomerCredentialRepository customerCredentialRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(final ApplicationArguments args) {
    if (customerCredentialRepository.findByUsername("cliente").isEmpty()) {
      customerCredentialRepository.save(
          new CustomerCredential("cliente", passwordEncoder.encode("123456"), 1L, "default"));
      log.info("Seeded demo customer account 'cliente'.");
    }
  }
}
