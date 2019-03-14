/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

public class HttpResponse {

    private int statusCode;
    private String body;
    private String charset;
    private long contentLength;

    /**
     * Constructor
     * @param statusCode status code
     * @param body body string
     * @param charset charset
     * @param contentLength content length
     */
    public HttpResponse(int statusCode, String body, String charset, long contentLength) {
        this.statusCode = statusCode;
        this.body = body;
        this.charset = charset;
        this.contentLength = contentLength;
    }

    /**
     * Ge the http status code
     * @return status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Set the http status code
     * @param statusCode status code
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the body
     * @return string body
     */
    public String getBody() {
        return body;
    }

    /**
     * Set the body
     * @param body string
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Get the charset
     * @return charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Set the charset
     * @param charset charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Get content length
     * @return content length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Set content length
     * @param contentLength content length
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Get the string representation of the object
     * @return string
     */
    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", body='" + body + '\'' +
                ", charset='" + charset + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}