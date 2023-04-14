package com.example.demo.service;

public class Index {
    /**
     * Returns the index page.
     */
    public static String getInfo() {
        return "<h1> Java Travel Sample API </h1>"
            + "A sample API for getting started with Couchbase Server and the Java SDK."
            + "<li> <a href=index.html>Start Here</a><br>"
            + "<li> <a href = \"https://github.com/mikereiche/developer-experience\"> Find this on GitHub </a>"
            + "<li> <a href = \"/apidocs\"> Learn the API, interactively </a>";
    }
}
