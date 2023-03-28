/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FixedDelayRetry;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import okhttp3.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    public static int count = 1;

    public static  OkHttpClient okHttpClient=null;
    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(false);
        builder.connectionPool(new ConnectionPool(30, 30, TimeUnit.MINUTES));
        builder.connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS);
        okHttpClient = builder.build();
    }

    private String defaultUrl = "https://api.openai.com/v1/chat/completions";
    private String defaultImageUrl = "https://api.openai.com/v1/images/generations";
    private String defaultToken = "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";
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws IOException {
        context.getLogger().info("Java HTTP trigger processed a request.");
        String token = request.getQueryParameters().get("token");
        String url = request.getQueryParameters().get("url");
        context.getLogger().info("token:"+token+",url:"+url);

        final String params = request.getBody().orElse("");
        if (params == null || params .equals("")) {
            return request.createResponseBuilder(HttpStatus.OK).body("请求参数不能为空").build();
        }
        RequestBody body = RequestBody.create(params, MediaType.parse("application/json; charset=utf-8"));
        Request req = new Request.Builder().header("Authorization","Bearer "+Optional.ofNullable(token).orElse(defaultToken)).url(Optional.ofNullable(url).orElse(defaultUrl)).post(body).build();
        Response execute = okHttpClient.newCall(req).execute();
        return request.createResponseBuilder(HttpStatus.OK).body(execute.body().string()).build();
    }

    @FunctionName("HttpImageService")
    public HttpResponseMessage HttpExampleRetry(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) throws Exception {
        String token = request.getQueryParameters().get("token");
        String url = request.getQueryParameters().get("url");
        context.getLogger().info("token:"+token+",url:"+url);

        final String params = request.getBody().orElse("");
        if (params == null || params .equals("")) {
            return request.createResponseBuilder(HttpStatus.OK).body("请求参数不能为空").build();
        }
        RequestBody body = RequestBody.create(params, MediaType.parse("application/json; charset=utf-8"));
        Request req = new Request.Builder().header("Authorization","Bearer "+Optional.ofNullable(token).orElse(defaultToken)).url(Optional.ofNullable(url).orElse(defaultImageUrl)).post(body).build();
        Response execute = okHttpClient.newCall(req).execute();
        return request.createResponseBuilder(HttpStatus.OK).body(execute.body().string()).build();
    }

    /**
     * This function listens at endpoint "/api/HttpTriggerJavaVersion".
     * It can be used to verify the Java home and java version currently in use in your Azure function
     */
    @FunctionName("HttpTriggerJavaVersion")
    public static HttpResponseMessage HttpTriggerJavaVersion(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        final String javaVersion = getJavaVersion();
        context.getLogger().info("Function - HttpTriggerJavaVersion" + javaVersion);
        return request.createResponseBuilder(HttpStatus.OK).body(javaVersion).build();
    }

    public static String getJavaVersion() {
        return String.join(" - ", System.getProperty("java.home"), System.getProperty("java.version"));
    }
}
