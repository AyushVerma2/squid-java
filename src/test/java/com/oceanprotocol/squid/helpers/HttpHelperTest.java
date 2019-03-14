/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import com.oceanprotocol.squid.models.HttpResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpHelperTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void downloadResponseHandler() throws IOException  {
        String destPath= folder.newFile("kk").getPath();

        HttpHelper.DownloadResponseHandler downloadHandler= new HttpHelper.DownloadResponseHandler(destPath);
        org.apache.http.HttpResponse response= mock(org.apache.http.HttpResponse.class);

        HttpEntity httpEntity= mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream( "test".getBytes() ));

        StatusLine statusLine = mock(StatusLine.class);
        when( statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);

       // when(response.getStatusLine().getStatusCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(httpEntity);

        assertTrue(downloadHandler.handleResponse(response).getResult());

    }


    @Test
    public void httpClientGenericMethod() throws HttpException, UnsupportedEncodingException, IOException {

        String payload= "{'message': 'hi there'}";
        ArrayList<NameValuePair> list= new ArrayList<>();
        list.add(new NameValuePair("key", "value"));

        HttpClient client= mock(HttpClient.class);
        EntityEnclosingMethod method= mock(PostMethod.class);

        when(client.executeMethod(method)).thenReturn(1);

        when(method.getStatusCode()).thenReturn(201);
        when(method.getResponseBodyAsStream()).thenReturn(new ByteArrayInputStream( "".getBytes() ));
        when(method.getResponseCharSet()).thenReturn("UTF-8");
        when(method.getResponseContentLength()).thenReturn(0L);

        HttpResponse response= HttpHelper.httpClientGenericMethod(client, method, list, payload);

        assertEquals(201, response.getStatusCode());
    }


    @Test
    public void httpClientGet() throws HttpException, IOException {
        HttpClient client= mock(HttpClient.class);
        GetMethod method= mock(GetMethod.class);

        when(client.executeMethod(method)).thenReturn(1);

        when(method.getStatusCode()).thenReturn(200);
        when(method.getResponseBodyAsStream()).thenReturn(new ByteArrayInputStream( "".getBytes() ));
        when(method.getResponseCharSet()).thenReturn("UTF-8");
        when(method.getResponseContentLength()).thenReturn(0L);


        HttpResponse response= HttpHelper.httpClientGet(client, method);
        assertEquals(200, response.getStatusCode());
    }


}