# ServiceRequest

A utility Java class developed for Android to handle easy web service requests. The response can be plain text or JSON. For the low level connection HttpUrlConnection is used.

## Features

- easy handling of POST and GET requests
- supports String or JSONObject as response types
- supports Multipart-Form-Data POST for uploading files

## How to include the dependency using JitPack.io

[![](https://jitpack.io/v/behringer24/ServiceClient.svg)](https://jitpack.io/#behringer24/ServiceClient)
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```gradle
dependencies {
	        compile 'com.github.behringer24:ServiceClient:1.1'
	}
```

## Example code

String es response
```java
ServiceClient service = new ServiceClient("http://yoursite.com/api/service", RequestMethod.POST);
service.addParameter("name", "Max Mustermann");
service.addParameter("email", "max@mustermann.com");

try {
	String response = service.request();
} catch {IOException e} {
	// handle connection errors
}
```

JSONObject as response
```java
ServiceClient service = new ServiceClient("http://yoursite.com/api/jsonService", RequestMethod.POST);
service.addParameter("name", "Max Mustermann");
service.addParameter("email", "max@mustermann.com");

try {
	JSONObject response = service.requestJson();
} catch {IOException e} {
	// handle connection errors
} catch {JSONException e}
	// handle JSON parse errors
}
```

Please remember to put network access in AsyncTask to keep it out of the UI-thread
