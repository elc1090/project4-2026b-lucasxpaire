-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.

-- Password for all users below is: 123456 (minimum 6 characters required by Bean Validation)
-- BCrypt hash: {bcrypt}$2a$10$XUz2U02ij40LweNjSJ30g.tQaTuDP/tcww1fWORw2xtxFMZB2SWP.

-- 1. Admin user (ROLE_ADMIN)
INSERT INTO staff_credentials (username, password_hash, email, role, tenant_id, active, created_at)
VALUES ('admin', '{bcrypt}$2a$10$XUz2U02ij40LweNjSJ30g.tQaTuDP/tcww1fWORw2xtxFMZB2SWP.', 'admin@localhost', 'ROLE_ADMIN', 'default', TRUE, NOW())
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash;

-- 2. Credit committee / Staff user (ROLE_STAFF)
INSERT INTO staff_credentials (username, password_hash, email, role, tenant_id, active, created_at)
VALUES ('comite', '{bcrypt}$2a$10$XUz2U02ij40LweNjSJ30g.tQaTuDP/tcww1fWORw2xtxFMZB2SWP.', 'comite@localhost', 'ROLE_STAFF', 'default', TRUE, NOW())
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash;

-- 3. Customer user
INSERT INTO customer_credentials (username, password_hash, fineract_client_id, tenant_id, created_at)
VALUES ('cliente', '{bcrypt}$2a$10$XUz2U02ij40LweNjSJ30g.tQaTuDP/tcww1fWORw2xtxFMZB2SWP.', 1, 'default', NOW())
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash;
