package com.yeahmobi.yscheduler.common.notice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ryan Sun
 */
public class DefaultEmailSender implements EmailSender {

    private static final Log    LOGGER       = LogFactory.getLog(DefaultEmailSender.class);

    private static final String DEV_MODE     = "yscheduler.devMode";

    private boolean             devMode      = true;

    private String              mailApi;

    private static final String SUCCESS_CODE = "\"code\":0";

    public DefaultEmailSender() {
        String devModeStr = System.getProperty(DEV_MODE, "true");
        if ("false".equals(devModeStr)) {
            this.devMode = false;
        }
    }

    public String getMailApi() {
        return this.mailApi;
    }

    public void setMailApi(String mailApi) {
        this.mailApi = mailApi;
    }

    public void send(Message email) {
        LOGGER.info("Send email>>> subject:" + email.getSubject() + "; to:" + email.getTo());
        if (this.devMode) {
            return;
        }
        try {
            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod(this.mailApi);
            NameValuePair[] parameters = new NameValuePair[3];
            String to = "";
            for (String emailAddress : email.getTo()) {
                if (StringUtils.isNotBlank(to)) {
                    to += ";";
                }
                to += emailAddress;
            }
            parameters[0] = new NameValuePair("to", to);
            parameters[1] = new NameValuePair("subject", email.getSubject());
            parameters[2] = new NameValuePair("body", email.getContent());
            postMethod.addParameters(parameters);
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");

            client.executeMethod(postMethod);

            String response = postMethod.getResponseBodyAsString();
            if (!response.contains(SUCCESS_CODE)) {
                LOGGER.error(response);
            }
        } catch (Exception e) {
            if (this.devMode) {
                LOGGER.error(e);
            } else {
                LOGGER.error(e, e);
            }
        }
    }

    // public static void main(String[] args) {
    // DefaultEmailSender emailSender = new DefaultEmailSender();
    // Message msg = new Message();
    // List to = new ArrayList<String>();
    // to.add("platform@ndpmedia.com");
    // msg.setContent("test");
    // msg.setSubject("测试");
    // msg.setTo(to);
    // emailSender.mailApi = "http://ips.ymtech.info/notify/api/email";
    // emailSender.send(msg);
    // }
}
