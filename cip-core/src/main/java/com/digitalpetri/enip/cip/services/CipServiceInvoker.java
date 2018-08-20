package com.digitalpetri.enip.cip.services;

import java.util.concurrent.CompletableFuture;

public interface CipServiceInvoker {

    /**
     * Invoke a service request using CIP unconnected messaging.
     * <p>
     * The service is invoked directly, without being passed through the Unconnected_Send service of the Connection
     * Manager object.
     *
     * @param service the service to invoke.
     * @return a {@link CompletableFuture} containing the eventual service response or failure.
     */
    <T> CompletableFuture<T> invoke(CipService<T> service);

    /**
     * Invoke a service request using CIP unconnected messaging, allowing for a number of retries if the destination
     * node returns an error status indicating it is currently busy.
     * <p>
     * The service is invoked directly, without being passed through the Unconnected_Send service of the Connection
     * Manager object.
     *
     * @param service    the service to invoke.
     * @param maxRetries the maximum number of retries to attempt.
     * @return a {@link CompletableFuture} containing the eventual service response or failure.
     */
    <T> CompletableFuture<T> invoke(CipService<T> service, int maxRetries);

    /**
     * Invoke a service request using CIP connected messaging on the provided connection id.
     *
     * @param connectionId the id of the connection to use.
     * @param service      the service to invoke.
     * @return a {@link CompletableFuture} containing the eventual service response or failure.
     */
    <T> CompletableFuture<T> invokeConnected(int connectionId, CipService<T> service);

    /**
     * Invoke a service request using CIP unconnected messaging using the Unconnected_Send Service (0x52) of the
     * Connection Manager object.
     *
     * @param service the service to invoke.
     * @return a {@link CompletableFuture} containing the eventual service response or failure.
     */
    <T> CompletableFuture<T> invokeUnconnected(CipService<T> service);

    /**
     * Invoke a service request using CIP unconnected messaging using the Unconnected_Send Service (0x52) of the
     * Connection Manager object, allowing for a number of retries if the destination node returns an error status
     * indicating it is currently busy.
     *
     * @param service    the service to invoke.
     * @param maxRetries the maximum number of retries to attempt.
     * @return a {@link CompletableFuture} containing the eventual service response or failure.
     */
    <T> CompletableFuture<T> invokeUnconnected(CipService<T> service, int maxRetries);

}
