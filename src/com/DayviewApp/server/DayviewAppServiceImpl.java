package com.DayviewApp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.DayviewApp.client.DayviewAppService;

public class DayviewAppServiceImpl extends RemoteServiceServlet implements DayviewAppService {
    // Implementation of sample interface method
    public String getMessage(String msg) {
        return "Client said: \"" + msg + "\"<br>Server answered: \"Hola!\"";
    }
}