// IDataProvideService.aidl
package com.gshubina.lullzzz.testappkotlin.service;

import com.gshubina.lullzzz.testappkotlin.service.IDataServiceCallback;

// Declare any non-default types here with import statements

oneway interface IDataProvideService {
     void registerCallback(IDataServiceCallback callback);
     void unregisterCallback(IDataServiceCallback callback);
     void requestSpeedData();
     void requestTachometerData();
}
