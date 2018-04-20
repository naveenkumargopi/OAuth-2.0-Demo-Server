package com.pramati.ts.oauth.server.thumbsignin.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/*import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;*/

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ThumbsignInClient {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    private static final String BASE_URL = "https://api.thumbsignin.com";
    private static String REGISTER_URL = "/ts/secure/register";
    private static String AUTHENTICATE_URL = "/ts/secure/authenticate";
    private static String STATUS_URL = "/ts/secure/txn-status/";
    private static String GET_USER_URL = "/ts/secure/getUser/";
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    private final String baseUrl;

    private String apiKey;

    private String apiSecret;
    
    public ThumbsignInClient() {
    	this.baseUrl = BASE_URL;
    }

    public ThumbsignInClient(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = BASE_URL;
    }

    public ThumbsignInClient(String baseUrl, String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl;
    }

    public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public ThumbsignInResponse get(ThumbsignInRequest request) throws IOException {
        URI uri = buildURI(request);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader(HmacSignatureBuilder.X_TS_DATE_HEADER, HmacSignatureBuilder.getTimeStamp());
        signRequest(uri, request);
        HttpGet httpget = new HttpGet(uri);
        Set<Map.Entry<String, String>> entries = request.getHeaders().entrySet();
        for (Map.Entry<String, String> e : entries) {
            httpget.addHeader(e.getKey(), e.getValue());
        }
        ResponseHandler<ThumbsignInResponse> responseHandler = new ResponseHandler<ThumbsignInResponse>() {
            @Override
            public ThumbsignInResponse handleResponse(HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String s = entity != null ? EntityUtils.toString(entity) : null;
                Map<String, Object> data = new HashMap<>();
                if (s != null) {
                    // convert JSON string to Map
                    data = MAPPER.readValue(s, new TypeReference<Map<String, String>>() {
                    });
                }
                return new ThumbsignInResponse(status, data);
            }
        };
        return httpclient.execute(httpget, responseHandler);
    }

    private URI buildURI(ThumbsignInRequest request) {
        String path = getPath(request);
        if (path == null || path.trim().length() == 0) {
            throw new ThumbsigninException("Invalid action or url");
        }
        String uri = baseUrl + path;
        try {
            URIBuilder builder = new URIBuilder(uri);
            for (Map.Entry<String, String> e : request.getQueryParams().entrySet()) {
                builder.addParameter(e.getKey(), e.getValue());
            }
            return builder.build();
        } catch (Exception e) {
            throw new ThumbsigninException("Unable to build Url");
        }
    }

    private void signRequest(URI uri, ThumbsignInRequest request) {
        final HmacSignatureBuilder signatureBuilder = new HmacSignatureBuilder.Builder(apiKey, apiSecret)
                .scheme("https")
                .httpMethod(HttpGet.METHOD_NAME)
                .canonicalURI(uri.getPath())
                .headers(getCanonicalizeHeaders(request))
                .queryParams(request.getQueryParams())
                .date(request.getHeaders().get(HmacSignatureBuilder.X_TS_DATE_HEADER))
                .build();
        String authHeader = signatureBuilder.sign();
        request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }

    private String getPath(ThumbsignInRequest request) {
        String path = null;
        switch (request.getAction()) {
            case REGISTER:
                String userId = request.getQueryParam("userId");
                if (userId == null || userId.trim().length() == 0) {
                    throw new ThumbsigninException("userId is missing in queryParams");
                }
                path = REGISTER_URL;
                break;
            case AUTHENTICATE:
                path = AUTHENTICATE_URL;
                break;
            case STATUS:
                if (request.getTransactionId() == null || request.getTransactionId().trim().length() == 0) {
                    throw new ThumbsigninException("transactionId is missing in request");
                }
                path = STATUS_URL + request.getTransactionId();
                break;
            case GET_USER:
                if (request.getTransactionId() == null || request.getTransactionId().trim().length() == 0) {
                    throw new ThumbsigninException("transactionId is missing in request");
                }
                path = GET_USER_URL + request.getTransactionId();
        }

        return path;
    }

    public static TreeMap<String, String> getCanonicalizeHeaders(ThumbsignInRequest request) {

        TreeMap<String, String> canonicalizeHeaders = new TreeMap<>();
        Set<Map.Entry<String, String>> entries = request.getHeaders().entrySet();
        for (Map.Entry<String, String> e : entries) {
            canonicalizeHeaders.put(e.getKey().toLowerCase(), e.getValue());
        }
        return canonicalizeHeaders;
    }

    public static TreeMap<String, String> getCanonicalizeQueryString(ThumbsignInRequest request) {

        TreeMap<String, String> canonicalizeQueryStrings = new TreeMap<>();
        canonicalizeQueryStrings.putAll(request.getQueryParams());
        return canonicalizeQueryStrings;
    }

    public ThumbsignInResponse getAuthenticatedUser(String transactionId) throws IOException {
        ThumbsignInRequest request = new ThumbsignInRequest(Action.GET_USER);
        request.setTransactionId(transactionId);
        return get(request);
    }
}
