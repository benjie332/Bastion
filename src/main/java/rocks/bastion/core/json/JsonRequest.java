package rocks.bastion.core.json;

import com.google.common.io.Files;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.entity.ContentType;
import rocks.bastion.core.ApiHeader;
import rocks.bastion.core.ApiQueryParam;
import rocks.bastion.core.HttpMethod;
import rocks.bastion.core.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import static java.lang.String.format;

/**
 * A JSON request for use during Bastion tests. The JSON body is supplied either inline or from a JSON file on the
 * classpath. Automatically sets the correct content type to use. This request will verify that the provided JSON body
 * contains valid JSON.
 */
public class JsonRequest implements HttpRequest {

    /**
     * Construct an HTTP request containing a JSON body from the given JSON string. Initially, the request will have
     * the "application/json" HTTP header and no other additional headers and no query parameters. It will also have
     * a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param method The HTTP method to use for this request
     * @param url    The URL to send this request on
     * @param json   The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest fromString(HttpMethod method, String url, String json) throws InvalidJsonException {
        return new JsonRequest(method, url, json);
    }

    /**
     * Construct an HTTP request, using the POST method, containing a JSON body from the given JSON string. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url  The URL to send this request on
     * @param json The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest postFromString(String url, String json) throws InvalidJsonException {
        return fromString(HttpMethod.POST, url, json);
    }

    /**
     * Construct an HTTP request, using the PUT method, containing a JSON body from the given JSON string. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url  The URL to send this request on
     * @param json The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest putFromString(String url, String json) throws InvalidJsonException {
        return fromString(HttpMethod.PUT, url, json);
    }

    /**
     * Construct an HTTP request containing a JSON body that is read from the given file. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param method   The HTTP method to use for this request
     * @param url      The URL to send this request on
     * @param jsonFile The file to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest fromFile(HttpMethod method, String url, File jsonFile) throws InvalidJsonException {
        try {
            Objects.requireNonNull(jsonFile);
            return new JsonRequest(method, url, Files.asCharSource(jsonFile, Charset.defaultCharset()).read());
        } catch (IOException e) {
            throw new RuntimeException(format("An error occurred while reading a file %s", jsonFile), e);
        }
    }

    /**
     * Construct an HTTP request, using the POST method, containing a JSON body that is read from the given file. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url      The URL to send this request on
     * @param jsonFile The file to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest postFromFile(String url, File jsonFile) throws InvalidJsonException {
        return fromFile(HttpMethod.POST, url, jsonFile);
    }

    /**
     * Construct an HTTP request, using the PUT method, containing a JSON body that is read from the given file. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url      The URL to send this request on
     * @param jsonFile The file to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest putFromFile(String url, File jsonFile) throws InvalidJsonException {
        return fromFile(HttpMethod.PUT, url, jsonFile);
    }

    private String name;
    private String url;
    private HttpMethod method;
    private ContentType contentType;
    private Collection<ApiHeader> headers;
    private Collection<ApiQueryParam> queryParams;
    private String body;

    protected JsonRequest(HttpMethod method, String url, String json) throws InvalidJsonException {
        Objects.requireNonNull(method);
        Objects.requireNonNull(url);
        Objects.requireNonNull(json);

        this.method = method;
        this.url = url;
        name = method.getValue() + ' ' + url;
        contentType = ContentType.APPLICATION_JSON;
        headers = new LinkedList<>();
        queryParams = new LinkedList<>();
        body = json;

        validateJson();
    }

    /**
     * Override the content-type that will be used for this request. Initially, the content-type for a {@code JSONRequest}
     * is "application/json" but you can override what is sent using this method.
     *
     * @param contentType A non-{@literal null} content-type to use for this request
     * @return This request (for method chaining)
     */
    public JsonRequest overrideContentType(ContentType contentType) {
        Objects.requireNonNull(contentType);
        this.contentType = contentType;
        return this;
    }

    /**
     * Add a new HTTP header that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new header
     * @param value A non-{@literal null} value for the new header
     * @return This request (for method chaining)
     */
    public JsonRequest addHeader(String name, String value) {
        headers.add(new ApiHeader(name, value));
        return this;
    }

    /**
     * Add a new HTTP query parameter that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new query parameter
     * @param value A non-{@literal null} value for the new query parameter
     * @return This request (for method chaining)
     */
    public JsonRequest addQueryParam(String name, String value) {
        queryParams.add(new ApiQueryParam(name, value));
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public Collection<ApiHeader> headers() {
        return headers;
    }

    @Override
    public Collection<ApiQueryParam> queryParams() {
        return queryParams;
    }

    @Override
    public Object body() {
        return body;
    }

    private void validateJson() throws InvalidJsonException {
        try {
            new JsonParser().parse(body);
        } catch (JsonParseException parseException) {
            throw new InvalidJsonException(parseException, body);
        }
    }
}
