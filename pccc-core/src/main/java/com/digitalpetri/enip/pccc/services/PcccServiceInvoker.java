package com.digitalpetri.enip.pccc.services;

import java.util.concurrent.CompletableFuture;

public interface PcccServiceInvoker {
	/**
	 * Invoke a service request using PCCC unconnected messaging.
	 *
	 * @param service
	 *            the service to invoke.
	 * @return a {@link CompletableFuture} containing the eventual service
	 *         response or failure.
	 */
	<T> CompletableFuture<T> invokeUnconnected(PcccService<T> service);

	/**
	 * Invoke a service request using PCCC unconnected messaging, allowing for a
	 * number of retries if the destination node returns an error status
	 * indicating it is currently busy.
	 *
	 * @param service
	 *            the service to invoke.
	 * @param maxRetries
	 *            the maximum number of retries to attempt.
	 * @return a {@link CompletableFuture} containing the eventual service
	 *         response or failure.
	 */
	<T> CompletableFuture<T> invokeUnconnected(PcccService<T> service, int maxRetries);
}