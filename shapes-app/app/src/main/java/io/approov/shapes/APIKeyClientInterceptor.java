//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.approov.shapes;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class APIKeyClientInterceptor implements ClientInterceptor {

    /** API key header name */
    private final String apiKeyHeaderName;

    /** API key */
    private final String apiKey;

    /**
     * Constructs a new interceptor that adds Approov tokens.
     *
     * @param apiKeyHeaderName the header name to use for the API key
     * @param apiKey the API key
     */
    APIKeyClientInterceptor(String apiKeyHeaderName, String apiKey) {
        this.apiKeyHeaderName = apiKeyHeaderName;
        this.apiKey = apiKey;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        return new APIKeyClientCall<>(next.newCall(method, callOptions));
    }

    /**
     * Call to a remote method where an Approov token is added to the metadata (i.e. headers) for channels to hosts that
     * are configured to be Approov protected.
     * Note that an invocation of start may cause an Approov token to be fetched over the network.
     */
    private final class APIKeyClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        // Non private to avoid synthetic class
        APIKeyClientCall(ClientCall<ReqT, RespT> call) {
            super(call);
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            try {
                headers.put(Metadata.Key.of(apiKeyHeaderName, Metadata.ASCII_STRING_MARSHALLER), apiKey);
                super.start(responseListener, headers);
            } catch (Exception e) {
                super.cancel(null, e);
            }
        }
    }

}

