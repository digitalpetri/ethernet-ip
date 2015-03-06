package com.digitalpetri.enip.cip.services;

import java.util.concurrent.CompletableFuture;

public interface CipServiceInvoker {

    <T> CompletableFuture<T> invokeConnected(int connectionId, CipService<T> service);

    <T> CompletableFuture<T> invokeUnconnected(CipService<T> service);

//    <T> CompletableFuture<?> invokeMultiple(int connectionId,
//                                            List<CipService<T>> services,
//                                            List<CompletableFuture<T>> futures);

}
