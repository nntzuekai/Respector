/*-
 * ---license-start
 * Corona-Warn-App / cwa-verification
 * ---
 * Copyright (C) 2020 - 2022 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.verification.repository;

import app.coronawarn.verification.domain.VerificationAppSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * This class represents the AppSession repository.
 */
public interface VerificationAppSessionRepository extends JpaRepository<VerificationAppSession, Long> {

  /**
   * This method looks in the Database for an Appsession with the given registrationTokenHash.
   *
   * @param registrationTokenHash hash to search for
   * @return Optional VerificationAppSession the optional Appsession
   */
  Optional<VerificationAppSession> findByRegistrationTokenHash(String registrationTokenHash);

  /**
   * This method looks in the Database for an Appsession with the given hashedGuid.
   *
   * @param hashedGuid hash to search for
   * @param hashedGuidDob hash to search for
   * @return Optional VerificationAppSession the optional Appsession
   */
  Optional<VerificationAppSession> findByHashedGuidOrHashedGuidDob(String hashedGuid, String hashedGuidDob);
  
  /**
   * This method looks in the Database for an Appsession with the given teleTanHash.
   *
   * @param teleTanHash hash to search for
   * @return Optional VerificationAppSession the optional Appsession
   */
  Optional<VerificationAppSession> findByTeleTanHash(String teleTanHash);
  
  /**
   * This method looks in the Database for Appsessions that are older than the before value and deletes them.
   *
   * @param before the Date to delete by
   */
  @Modifying
  @Query("delete from VerificationAppSession a where a.createdAt < ?1")
  void deleteByCreatedAtBefore(LocalDateTime before);
}
