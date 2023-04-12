package com.example.demo.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class Controller {
  public static final String GO_BACK = "Use browser Back button to go back";
  private String displayString = displayText();
  private long t0;
  private StringBuffer sb = new StringBuffer();

  final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  static final String LOG_FAILURE_MESSAGE = "Failed with exception";

  void before() {
    sb.setLength(0);
    t0 = System.currentTimeMillis();
    if ((System.currentTimeMillis() - t0) > 1000) {
      log("creating index on _class");
      // QueryIndexManager indexManager = template.getCouchbaseClientFactory().getCluster().queryIndexes();
      Collection<String> fields = Arrays.asList("_class");
      // indexManager.createIndex(template.getCouchbaseClientFactory().getBucket().name(), "class_index", fields,
      // CreateQueryIndexOptions.createQueryIndexOptions().ignoreIfExists(true));
    }
    log(displayString);
    log("method: ", methodName());
    t0 = System.currentTimeMillis();
  }

  String after() {
    log("<br>Ran in: ", System.currentTimeMillis() - t0, "ms");
    String output = sb.toString();
    sb.setLength(0);
    return output;
  }

  private <T> void removeAll(Class<T> clazz) {}

  String doWorkAndValidate(Runnable work, Runnable validate) {
    before();
    work.run();
    validate.run();
    return after();
  }

  String methodName() { // called from before(), called from doWork()
    return Thread.currentThread().getStackTrace()[4].getMethodName();
  }

  String displayText() {
    StringBuffer s = new StringBuffer();
    String urlPrefix = "http://localhost:8080";
    s.append("<table>");
    List<Method> methods = Arrays.asList(this.getClass().getDeclaredMethods());
    Collections.sort(methods, new Comparator<Method>() {
      @Override
      public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    String classRequestValue = null;
    RequestMapping classRequestMapping = this.getClass().getAnnotation(RequestMapping.class);
    if(classRequestMapping!= null){
      classRequestValue = classRequestMapping.value()[0];
    }
    for (Method m : methods) {

      String value = null;
      GetMapping gm = m.getAnnotation(GetMapping.class);
      if (gm != null) {
        value = gm.value()[0];
      } else {
        RequestMapping rm = m.getAnnotation(RequestMapping.class);
        if ( rm != null && rm.value() != null && rm.value().length > 0){
          value = rm.value()[0];
        }
      }
      if (value != null) {
        s.append("<tr>");
        s.append("<td>");
        s.append("<a href=\"");
        s.append(urlPrefix + classRequestValue + value.replaceAll("\\{[^\\}]*\\}","*"));
        s.append("\">");
        s.append("."+value);
        s.append("</a>");
        s.append("</td>");
        s.append("</tr>");
      }
    }
    s.append("</table>");
    displayString = s.toString();
    return displayString;
  }

  public Object log(Object... args) {
    if (args.length > 0 && args[0] instanceof String && !((String) args[0]).startsWith("<")) {
      sb.append("<br> ");
    }
    for (Object s : args) {
      if (s instanceof Exception) {
        sb.append("<b>");
      }
      sb.append(s);
      if (s instanceof Exception) {
        sb.append("</b>");
      }
    }
    sb.append("\n");
    for (Object a : args) {
      if (!(a instanceof String)) {
        return a;
      }
    }
    return null;
  }

  @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<? extends Object> index() {
    String body = doWorkAndValidate(() -> {}, () -> {} );
    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).contentType(MediaType.TEXT_HTML).body(body);
  }

  @RequestMapping(value = "index", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<? extends Object> index2() {
    return index();
  }
  /**
   * Supports the HTML Error View
   * @param request
   * @return
   */
  @RequestMapping(value = "/error", produces = "text/html")
  public String someError(HttpServletRequest request) {
    return
      "<a href=api/hotels>api/hotels</a><br>";
  }

}
