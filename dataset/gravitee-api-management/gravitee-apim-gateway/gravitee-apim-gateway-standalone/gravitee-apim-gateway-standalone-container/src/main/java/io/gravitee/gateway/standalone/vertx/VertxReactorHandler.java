/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.gateway.standalone.vertx;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.IdGenerator;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.http.vertx.TimeoutServerResponse;
import io.gravitee.gateway.http.vertx.VertxHttp2ServerRequest;
import io.gravitee.gateway.http.vertx.VertxHttpServerRequest;
import io.gravitee.gateway.http.vertx.grpc.VertxGrpcServerRequest;
import io.gravitee.gateway.reactor.Reactor;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class VertxReactorHandler implements Handler<HttpServerRequest> {

    private final Reactor reactor;
    private final IdGenerator idGenerator;
    private final long requestTimeout;
    private final Vertx vertx;

    public VertxReactorHandler(final Reactor reactor, IdGenerator idGenerator, final Vertx vertx, long requestTimeout) {
        this.reactor = reactor;
        this.idGenerator = idGenerator;
        this.vertx = vertx;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        VertxHttpServerRequest request;

        if (httpServerRequest.version() == HttpVersion.HTTP_2) {
            if (MediaType.APPLICATION_GRPC.equals(httpServerRequest.getHeader(HttpHeaders.CONTENT_TYPE))) {
                request = new VertxGrpcServerRequest(httpServerRequest, idGenerator);
            } else {
                request = new VertxHttp2ServerRequest(httpServerRequest, idGenerator);
            }
        } else {
            request = new VertxHttpServerRequest(httpServerRequest, idGenerator);
        }

        route(request, request.createResponse());
    }

    protected void route(final Request request, final Response response) {
        if (!request.isWebSocket() && requestTimeout > 0) {
            long timeoutId = vertx.setTimer(
                requestTimeout,
                event -> {
                    if (!response.ended()) {
                        io.gravitee.gateway.api.handler.Handler<Long> handler = request.timeoutHandler();
                        handler.handle(event);
                    }
                }
            );

            // Release timeout when response ends
            reactor.route(request, new TimeoutServerResponse(vertx, response, timeoutId), __ -> {});
        } else {
            reactor.route(request, response, __ -> {});
        }
    }
}
