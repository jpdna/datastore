package org.opencb.datastore.core;

import java.util.Collection;
import java.util.List;

/**
 * Created by imedina on 20/03/14.
 */
public class QueryResponse<T>{

    private static final long serialVersionUID = -2978952531219554024L;

    private int time;
    private String apiVersion;
    private String warning;
    private String error;

    private QueryOptions queryOptions;
    private Collection<T> response;

    public QueryResponse() {
        this(null, null);
    }

    public QueryResponse(QueryOptions queryOptions, List<T> response) {
        this(queryOptions, response, null, null, -1);
    }

    public QueryResponse(QueryOptions queryOptions, List<T> response, String version, String species, int time) {
        this.apiVersion = "v2";
        this.warning = "";
        this.error = "";
        this.queryOptions = queryOptions;
        this.response = response;
        this.time = time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
    }

    public Collection<T> getResponse() {
        return response;
    }

    public void setResponse(Collection<T> response) {
        this.response = response;
    }
}
