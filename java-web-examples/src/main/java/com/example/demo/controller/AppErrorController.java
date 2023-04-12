package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Basic Controller which is called for unhandled errors
 */
@Controller
public class AppErrorController implements ErrorController {

  /**
   * Error Attributes in the Application
   */
  private ErrorAttributes errorAttributes;

  private final static String ERROR_PATH = "/error";

  /**
   * Controller for the Error Controller
   * @param errorAttributes
   */
  public AppErrorController(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  /**
   * Supports the HTML Error View
   * @param request
   * @return
   */

  @RequestMapping(value = ERROR_PATH, produces = "text/html")
  public /*ModelAndView*/  ResponseEntity<String> errorHtml(HttpServletRequest request) {
        //return new ModelAndView("/", getErrorAttributes(request, false));
    Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
    HttpStatus status = body.containsKey("status") ? HttpStatus.valueOf((int)(body.get("status"))) : getStatus(request);
    return new ResponseEntity<>(body.toString(), status);
  }

  /**
   * Supports other formats like JSON, XML
   * @param request
   * @return
   */
  @RequestMapping(value = ERROR_PATH)
  @ResponseBody
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<Map<String, Object>>(body, status);
  }

  private boolean getTraceParameter(HttpServletRequest request) {
    String parameter = request.getParameter("trace");
    if (parameter == null) {
      return false;
    }
    return !"false".equals(parameter.toLowerCase());
  }

  private Map<String, Object> getErrorAttributes(HttpServletRequest request,
                                                 boolean includeStackTrace) {
    RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    ErrorAttributeOptions options = ErrorAttributeOptions.of();
    return this.errorAttributes.getErrorAttributes(new ServletWebRequest(request), options);
  }

  private HttpStatus getStatus(HttpServletRequest request) {
    Integer statusCode = (Integer) request
      .getAttribute("javax.servlet.error.status_code");
    if (statusCode != null) {
      try {
        return HttpStatus.valueOf(statusCode);
      }
      catch (Exception ex) {
      }
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
