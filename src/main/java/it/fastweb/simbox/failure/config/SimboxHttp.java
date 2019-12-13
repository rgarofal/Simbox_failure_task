package it.fastweb.simbox.failure.config;

import it.fastweb.simbox.failure.client.SessionClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SimboxHttp {

    private final SessionClient sessionClient;
    private static final Logger log = LoggerFactory.getLogger(SimboxHttp.class);

    @Autowired
    public SimboxHttp(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    public String sendTicket(String fileTmt) throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpUriRequest request = RequestBuilder
                    .post(sessionClient.getPost_url())
                    .setEntity(new StringEntity(fileTmt, ContentType.TEXT_PLAIN))
                    .build();

            log.info("Executing request " + request.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpclient.execute(request, responseHandler);

            return responseBody;
        }
    }
}
