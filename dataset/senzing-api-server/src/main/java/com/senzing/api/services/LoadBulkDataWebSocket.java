package com.senzing.api.services;

import com.senzing.api.websocket.JsonEncoder;
import com.senzing.api.websocket.StringDecoder;
import com.senzing.io.IOUtilities;
import com.senzing.util.Timers;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation of {@link BulkDataWebSocket} that loads bulk
 * data records.
 */
@ServerEndpoint(value="/bulk-data/load",
    decoders = StringDecoder.class,
    encoders = JsonEncoder.class)
public class LoadBulkDataWebSocket extends BulkDataWebSocket
  implements BlockUpgradeIfReadOnly
{
  /**
   * The data source to assign to the records loaded unless there is another
   * mapping that supercedes this one.
   */
  protected String dataSource;

  /**
   * The JSON string mapping specific data sources to alternate data source
   * names.  A mapping from empty-string is used for mapping records with no
   * data source specified.
   */
  protected String mapDataSources;

  /**
   * The {@link List} of delimited strings that begin the delimiter, followed
   * by the "from" data source then the delimiter then the target data source.
   */
  protected List<String> mapDataSourceList;

  /**
   * The optional load ID to use for loading the records.
   */
  protected String loadId;

  /**
   * The maximum number of failures or a negative number if no maximum.
   */
  protected int maxFailures;

  /**
   * Default constructor.
   */
  public LoadBulkDataWebSocket() {
    // do nothing
  }

  @Override
  public void onOpen(Session session)
      throws IOException, IllegalArgumentException
  {
    super.onOpen(session);

    // get the other query parameters
    Map<String, List<String>> params = this.session.getRequestParameterMap();
    List<String> paramList = params.get("dataSource");

    this.dataSource = (paramList == null || paramList.size() == 0) ? null
        : paramList.get(0);

    paramList = params.get("mapDataSources");
    this.mapDataSources = (paramList == null || paramList.size() == 0) ? null
        : paramList.get(0);

    this.mapDataSourceList = params.get("mapDataSource");

    paramList = params.get("loadId");
    this.loadId = (paramList == null || paramList.size() == 0) ? null
        : paramList.get(0);

    paramList = params.get("maxFailures");
    if (paramList != null && paramList.size() > 0) {
      try {
        this.maxFailures = Integer.parseInt(paramList.get(0));

      } catch (IllegalArgumentException e) {
        throw new BadRequestException(
            "The specified maximum number of failures (maxFailures) must be "
                + "an integer: " + paramList.get(0));
      }
    }
  }

  /**
   * Implemented to load the records once the thread is started.
   */
  protected void doRun() {
    SzApiProvider provider  = this.getApiProvider();

    this.loadBulkRecords(provider,
                         this.timers,
                         this.dataSource,
                         this.mapDataSources,
                         this.mapDataSourceList,
                         this.loadId,
                         this.maxFailures,
                         this.mediaType,
                         this.pipedInputStream,
                         null,
                         this.uriInfo,
                         this.progressPeriod,
                         null,
                         null,
                         this.session);

    // complete the run
    this.completeRun();
  }
}
