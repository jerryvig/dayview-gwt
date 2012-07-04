package com.DayviewApp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DayviewAppServiceAsync {
    void getMessage(String msg, AsyncCallback<String> async);
}
