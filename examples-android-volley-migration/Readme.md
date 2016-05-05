# Migration from Volley to Jus

## Introduction
Google provides some [documentation and examples](http://developer.android.com/training/volley/index.html) of Volley 
in their Developer/Training section. 

**This module aims to help you migrating from Volley to Jus**, following step by step Googles's examples
while giving you the equivalent functions using Jus.

## How to use the module
Simply follow [Google's tutorial for Volley](http://developer.android.com/training/volley/index.html) and for each section,
have a look at **fragments\VolleyFragment.java** Once you have understood how the request was made, just have compare it with how
it was done in **fragments\JusFragment.java**

1. [Create a RequestQueue and add a stringRequest](http://developer.android.com/training/volley/simple.html#simple) -> standardQueueStringRequest()
2. [Set a tag to a Request and Cancel a Request](http://developer.android.com/training/volley/simple.html#cancel) -> standardQueueStringRequest() and onStop()
3. [Use a Singleton Pattern](http://developer.android.com/training/volley/requestqueue.html#singleton) -> customQueueImageRequest()
4. [Request an Image](http://developer.android.com/training/volley/request.html#request-image) -> customQueueImageRequest() & networkImageViewRequest()
5. [Custom Request Json and Gson example](http://developer.android.com/training/volley/request-custom.html#custom-request) -> jsonRequest() & gsonRequest()
