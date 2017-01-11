package de.behringer24.net;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Send GET and POST requests to server with support of multipart requests and file uploads
 * Inspired by example code from www.codejava.net
 * @author Andreas Behringer <abe@activecube.de>
 */
public class ServiceClient {

	public enum RequestMethod {
		GET,
		POST
	}

	private ArrayList <NameValuePair> parameters;
	private ArrayList <NameValuePair> files;
	private ArrayList <NameValuePair> headers;

	private final String boundary;
	private final String hyphens = "--";
	private final String requestUrl;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConn;
	private String charset = "UTF-8";
	private RequestMethod method;
	private OutputStream outputStream;
	private PrintWriter writer;

	/**
	 * Constructor initializes class
	 * @param requestURL Url to send the request to
	 * @param method Of type RequestMethod, switches between POST AND GET
	 */
	public ServiceClient(String requestURL, RequestMethod method) {
		this.requestUrl = requestURL;
		this.method = method;

		parameters = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
		files = new ArrayList<NameValuePair>();

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";
	}

	/**
	 * Add simple parameter to the request
	 * @param name Name of the request parameter
	 * @param value Value of the request parameter
	 */
	public void addParam(String name, String value) {
		parameters.add(new BasicNameValuePair(name, value));
	}

	/**
	 * Add file to the request. Will switch the request type to multipart POST request
	 * @param name Name of the request parameter (not filename)
	 * @param url Absolute uri of the file on the device
	 */
	public void addFile(String name, String url) {
		files.add(new BasicNameValuePair(name, url));
	}

	/**
	 * Add http-header to request
	 * @param name the name of the http-header
	 * @param value the value/content of the header
	 */
	public void addHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	/**
	 * Set char encoding
	 * @param charset the charset in string format. Default is "UTF-8"
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Send request to server and parse/get response as JSON
	 * @return JSONObject
	 * @throws java.io.IOException, JSONException
	 */
	public JSONObject requestJson() throws IOException, JSONException {
		String result = request();
		return new JSONObject(result);
	}

	/**
	 * Writes a multipart form field to the request
	 * @param name field name
	 * @param value field value
	 */
	private void writeMultipartParameter(String name, String value) {
		writer.append(hyphens + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Writes parameters to simple POST request
	 * @param urlParams url encoded string of all parameters
	 */
	private void writePostParameters(String urlParams) {
		writer.append(urlParams);
	}

	/**
	 * Writes a file upload section to the request
	 * @param fieldName name attribute in <input type="file" name="..." />
	 * @param uploadFile a File to be uploaded
	 * @throws java.io.IOException
	 */
	private void writeMultipartFile(String fieldName, File uploadFile) throws IOException {
		String fileName = uploadFile.getName();
		writer.append(hyphens + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName	+ "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Builds the request and receives response from the server
	 * @return String with server response body
	 * @throws java.io.IOException
	 */
	public String request() throws IOException {
		String urlParams = "";
		URL url = null;
		String response = null;

		// Default header
		addHeader("User-Agent", "ServiceClient (behringer24.de) Java Client");

		// Set Content-Type depending on request method
		if (files.size() > 0) {
			method = RequestMethod.POST;
			addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
		} else if (method == RequestMethod.POST) {
			addHeader("Content-Type", "application/x-www-form-urlencoded");
		}

		// Encode parameters as request string for get or simple post requests
		if (files.size() == 0) {
			for (NameValuePair parameter: parameters) {
				String paramString = parameter.getName() + "=" + URLEncoder.encode(parameter.getValue(), "UTF-8");
				if(urlParams.length() > 1) {
					urlParams +=  "&" + paramString;
				} else {
					urlParams += paramString;
				}
			}
		}

		// Build URL
		if (method == RequestMethod.GET) {
			url = new URL(this.requestUrl + "?" + urlParams);
			Log.d("ServiceClient", "GET Request: " + this.requestUrl + "?" + urlParams);
		} else {
			url = new URL(this.requestUrl);
			Log.d("ServiceClient", "POST Request: " + this.requestUrl);
		}

		// Open connection and set parameters
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setDoInput(true);
		httpConn.setUseCaches(false);

		// Set request headers
		for (NameValuePair header: headers) {
			httpConn.addRequestProperty(header.getName(), header.getValue());
		}

		// Set request parameters special to POST requests
		if (method == RequestMethod.POST) {
			httpConn.setDoOutput(true); // indicates POST method
			outputStream = httpConn.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
		}

		// Build POSt request for simple or multipart requests
		if (method == RequestMethod.POST) {
			if (files.size() == 0) {
				writePostParameters(urlParams);
			} else {
				for (NameValuePair parameter: parameters) {
					writeMultipartParameter(parameter.getName(), parameter.getValue());
				}

				for (NameValuePair file: files) {
					File sendfile = new File(file.getValue());
					writeMultipartFile(file.getName(), sendfile);
				}
				writer.append(LINE_FEED).flush();
				writer.append("--" + boundary + "--").append(LINE_FEED);
			}
		}

		writer.close();

		response = "";

		// Checks server status code first
		int status = httpConn.getResponseCode();

		// If 200 OK read input stream and build response string
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response += line;
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return response;
	}
}