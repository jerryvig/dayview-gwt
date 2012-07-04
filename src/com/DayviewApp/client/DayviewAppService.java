package com.DayviewApp.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DayviewAppService")
public interface DayviewAppService extends RemoteService {
    // Sample interface method of remote interface
    String getMessage(String msg);

    /**
     * Utility/Convenience class.
     * Use DayviewAppService.App.getInstance() to access static instance of DayviewAppServiceAsync
     */
    public static class App {
        private static DayviewAppServiceAsync ourInstance = GWT.create(DayviewAppService.class);

        public static synchronized DayviewAppServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
