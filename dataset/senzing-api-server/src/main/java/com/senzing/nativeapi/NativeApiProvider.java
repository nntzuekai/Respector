package com.senzing.nativeapi;

import com.senzing.g2.engine.*;

public interface NativeApiProvider {
  /**
   * Provides a new instance of {@link G2Engine} to use.
   *
   * @return A new instance of {@link G2Engine} to use.
   */
  G2Engine createEngineApi();

  /**
   * Provides a new instance of {@link G2Config} to use.
   *
   * @return A new instance of {@link G2Config} to use.
   */
  G2Config createConfigApi();

  /**
   * Provides a new instance of {@link G2Product} to use.
   *
   * @return A new instance of {@link G2Product} to use.
   */
  G2Product createProductApi();

  /**
   * Provides a new instance of {@link G2ConfigMgr} to use.
   *
   * @return A new instance of {@link G2ConfigMgr} to use.
   *
   */
  G2ConfigMgr createConfigMgrApi();

  /**
   * Provides a new instance of {@link G2Diagnostic} to use.
   *
   * @return A new instance of {@link G2Diagnostic} to use.
   */
  G2Diagnostic createDiagnosticApi();
}
