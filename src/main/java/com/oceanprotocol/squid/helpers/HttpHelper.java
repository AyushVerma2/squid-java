/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import com.oceanprotocol.squid.exceptions.OceanException;
import com.oceanprotocol.squid.models.HttpResponse;
import org.apache.commons.codec.Charsets;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * HTTP Helper functions
 */
public abstract class HttpHelper {

    protected static final Logger log = LogManager.getLogger(HttpHelper.class);

    private HttpHelper() {
    }


    public static class DownloadResult {

        private Boolean result;
        private Integer code;
        private String message;

        public Boolean getResult() {
            return result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    static class DownloadResponseHandler implements ResponseHandler<DownloadResult> {

        String destinationPath;

        public DownloadResponseHandler(String  destinationPath) {

            this.destinationPath = destinationPath;
        }

        @Override
        public DownloadResult handleResponse(org.apache.http.HttpResponse response) throws ClientProtocolException, IOException {


            DownloadResult downloadResult = new DownloadResult();
            downloadResult.setCode(response.getStatusLine().getStatusCode());

            downloadResult.result = downloadResult.code == 200;

            if (!downloadResult.result){
                downloadResult.setMessage(response.getStatusLine().toString());
                return downloadResult;
            }

            FileUtils.copyInputStreamToFile( response.getEntity().getContent(), new File(destinationPath));
            return downloadResult;

        }
    }

    /**
     * Send a HTTP POST request and return the body
     * @param url url to call
     * @return returned http body
     * @throws HttpException Http error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static final String httpClientPostBody(String url) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new PostMethod(url), new ArrayList<>(), null).getBody();
    }

    /**
     * Send a HTTP POST request with parameters and return the body
     * @param url url to call
     * @param list parameters
     * @return returned http body
     * @throws HttpException Http error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static final String httpClientPostBody(String url, ArrayList<NameValuePair> list) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new PostMethod(url), list, null).getBody();
    }

    /**
     * Send a HTTP POST request and return the HttpResponse object
     * @param url url to call
     * @return HttpResponse returned
     * @throws HttpException Http error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static final HttpResponse httpClientPost(String url) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new PostMethod(url), new ArrayList<>(), null);
    }

    /**
     * Send a HTTP POST request and return the HttpResponse object
     * @param url url to call
     * @param list parameters
     * @param payload payload to add to the request
     * @return HttpResponse returned
     * @throws HttpException Http error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static final HttpResponse httpClientPost(String url, ArrayList<NameValuePair> list, String payload) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new PostMethod(url), list, payload);
    }

    /**
     * Send a HTTP PUT request and return the HttpResponse object
     * @param url url to call
     * @param list parameters
     * @param payload payload to add to the request
     * @return HttpResponse returned
     * @throws HttpException Http error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static final HttpResponse httpClientPut(String url, ArrayList<NameValuePair> list, String payload) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new PutMethod(url), list, payload);
    }

    /**
     * Send a HTTP request with parameters and return the HttpResponse object
     * @param method EntityEnclosingMethod
     * @param list list of params
     * @param payload payload to add to the request
     * @return HttpResponse
     * @throws HttpException HttpException
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */

    public static final HttpResponse httpClientGenericMethod(EntityEnclosingMethod method, ArrayList<NameValuePair> list, String payload) throws HttpException, UnsupportedEncodingException {
        return httpClientGenericMethod(new HttpClient(), method, list, payload);
    }

    /**
     * Send a HTTP request with parameters and return the HttpResponse object
     * @param client HttpClient
     * @param method EntityEnclosingMethod
     * @param list list of params
     * @param payload payload to add to the request
     * @return HttpResponse
     * @throws HttpException HttpException
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    public static final HttpResponse httpClientGenericMethod(HttpClient client, EntityEnclosingMethod method, ArrayList<NameValuePair> list, String payload) throws HttpException, UnsupportedEncodingException {

        HttpResponse response;
        StringRequestEntity requestEntity= null;

        if (null != payload && payload.length() >0) {
            requestEntity = new StringRequestEntity(
                    payload,
                    ContentType.APPLICATION_JSON.toString(),
                    "UTF-8");

            method.setRequestEntity(requestEntity);
        }

        try {
            if (list.size() >0) {
                NameValuePair[] params = new NameValuePair[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    params[i] = list.get(i);
                }

                if (method instanceof PostMethod)
                    ((PostMethod) method).addParameters(params);
            }

            client.executeMethod(method);
            response = new HttpResponse(
                    method.getStatusCode(),
                    IOUtils.toString(method.getResponseBodyAsStream(), Charsets.UTF_8),
                    method.getResponseCharSet(),
                    method.getResponseContentLength()
            );
        } catch (Exception e) {
            log.error("Error in HTTP Method request " + e.getMessage());
            throw new HttpException("Error in HTTP Method request");
        } finally {
            method.releaseConnection();
        }
        return response;
    }

    /**
     * Send a HTTP GET request and return the HttpResponse object
     * @param url the url
     * @return HttpResponse
     * @throws HttpException HttpException
     */
    public static final HttpResponse httpClientGet(String url) throws HttpException {
        return httpClientGet(new HttpClient(), new GetMethod(url));
    }


    /**
     * Send a HTTP GET request and return the HttpResponse object
     * @param client HttpClient
     * @param getMethod GetMethod
     * @return HttpResponse
     * @throws HttpException HttpException
     */
    public static final HttpResponse httpClientGet(HttpClient client, GetMethod getMethod) throws HttpException {

        log.debug("Getting URL: "+ getMethod.getURI());

        HttpResponse response;
        try {

            client.executeMethod(getMethod);
            response = new HttpResponse(
                    getMethod.getStatusCode(),
                    IOUtils.toString(getMethod.getResponseBodyAsStream(), Charsets.UTF_8),
                    getMethod.getResponseCharSet(),
                    getMethod.getResponseContentLength()
            );

        } catch (Exception e) {
            log.error("Error in HTTP GET request " + e.getMessage());
            throw new HttpException("Error in HTTP GET request");
        } finally {
            getMethod.releaseConnection();
        }
        return response;
    }

    /**
     * Download the content of a resource
     * @param url the url of the resource
     * @param destinationPath the path where the resource will be downloaded
     * @return Boolean flag
     * @throws IOException IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static DownloadResult downloadResource(String url, String destinationPath) throws IOException, URISyntaxException {

        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                .build();

       try {

            HttpGet get = new HttpGet(new URL(url).toURI()); // we're using GET but it could be via POST as well
            return httpclient.execute(get, new DownloadResponseHandler(destinationPath));

        } catch (IOException e) {
            throw e;
        } catch (URISyntaxException e) {
            throw e;
        }

    }


}
