package com.senzing.api.services;

import com.senzing.api.websocket.JsonEncoder;
import com.senzing.api.websocket.StringDecoder;
import com.senzing.io.IOUtilities;
import com.senzing.util.Timers;

import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Provides an implementation of {@link BulkDataWebSocket} that analyzes bulk
 * data records.
 */
@ServerEndpoint(value="/bulk-data/analyze",
    decoders = StringDecoder.class,
    encoders = JsonEncoder.class)
public class AnalyzeBulkDataWebSocket extends BulkDataWebSocket {
  /**
   * Implemented to load the records once the thread is started.
   */
  protected void doRun() {
    SzApiProvider provider  = this.getApiProvider();

    this.analyzeBulkRecords(provider,
                            this.timers,
                            this.mediaType,
                            this.pipedInputStream,
                            this.uriInfo,
                            this.progressPeriod,
                            null,
                            null,
                            this.session);

    // complete the run
    this.completeRun();
  }
}
