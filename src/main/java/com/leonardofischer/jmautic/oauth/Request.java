package com.leonardofischer.jmautic.oauth;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple abstraction for HTTP requests. You can set a method, endpoint and request parameters.
 * Future versions of jMautic may handle other parts of HTTP requests (such as headers, body,
 * file uploads, etc).
 */
public class Request {

    public enum Method {
        GET, POST
    }

    private Method method = Method.GET;

    private String endpoint;

    private Map<String,String> parameters = new HashMap<String,String>();

    public Request() {
        setMethod(Method.GET);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public Map getParameters() {
        return parameters;
    }
}
