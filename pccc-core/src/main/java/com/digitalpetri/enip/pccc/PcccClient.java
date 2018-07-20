package com.digitalpetri.enip.pccc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.enip.EtherNetIpClient;
import com.digitalpetri.enip.EtherNetIpClientConfig;
import com.digitalpetri.enip.commands.SendRRData;
import com.digitalpetri.enip.cpf.CpfItem;
import com.digitalpetri.enip.cpf.CpfPacket;
import com.digitalpetri.enip.cpf.NullAddressItem;
import com.digitalpetri.enip.cpf.UnconnectedDataItemRequest;
import com.digitalpetri.enip.cpf.UnconnectedDataItemResponse;
import com.digitalpetri.enip.pccc.services.ExecutePcccAsCipService;
import com.digitalpetri.enip.pccc.services.PcccService;
import com.digitalpetri.enip.pccc.services.PcccServiceInvoker;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class PcccClient extends EtherNetIpClient implements PcccServiceInvoker {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final AtomicInteger sequenceNumber = new AtomicInteger(0);

	public PcccClient(EtherNetIpClientConfig config) {
		super(config);
	}

	@Override
	public <T> CompletableFuture<T> invokeUnconnected(PcccService<T> service) {
		return invokeUnconnected(service, 0);
	}

	@Override
	public <T> CompletableFuture<T> invokeUnconnected(PcccService<T> service, int maxRetries) {
		CompletableFuture<T> future = new CompletableFuture<>();

		ExecutePcccAsCipService<T> uss = new ExecutePcccAsCipService<T>(service, nextSequenceNumber(), getConfig());

		return invokeUnconnected(uss, future, 0, maxRetries);
	}

	private <T> CompletableFuture<T> invokeUnconnected(PcccService<T> service, CompletableFuture<T> future, int count,
			int max) {
		sendUnconnectedData(service::encodeRequest).whenComplete((buffer, ex) -> {
			if (buffer != null) {
				try {
					T response = service.decodeResponse(buffer);
					future.complete(response);
				} catch (PcccService.PartialResponseException e) {
					invokeUnconnected(service, future, count, max);
				} catch (PcccResponseException e) {
					if (e.getGeneralStatus() == 0x01) {
						boolean requestTimedOut = Arrays.stream(e.getAdditionalStatus()).anyMatch(i -> i == 0x0204);

						if (requestTimedOut && count < max) {
							logger.info("Unconnected request timed out; retrying, count={}, max={}", count, max);
							invokeUnconnected(service, future, count + 1, max);
						} else {
							future.completeExceptionally(e);
						}
					} else {
						future.completeExceptionally(e);
					}
				} catch (Exception responseEx) {
					future.completeExceptionally(responseEx);
				} finally {
					ReferenceCountUtil.release(buffer);
				}
			} else {
				future.completeExceptionally(ex);
			}
		});

		return future;
	}

	public CompletableFuture<ByteBuf> sendUnconnectedData(ByteBuf data) {
		return sendUnconnectedData((buffer) -> buffer.writeBytes(data));
	}

	public CompletableFuture<ByteBuf> sendUnconnectedData(Consumer<ByteBuf> dataEncoder) {
		CompletableFuture<ByteBuf> future = new CompletableFuture<>();

		UnconnectedDataItemRequest dataItem = new UnconnectedDataItemRequest(dataEncoder);
		CpfPacket packet = new CpfPacket(new NullAddressItem(), dataItem);

		sendRRData(new SendRRData(packet)).whenComplete((command, ex) -> {
			if (command != null) {
				CpfItem[] items = command.getPacket().getItems();

				if (items.length == 2 && items[0].getTypeId() == NullAddressItem.TYPE_ID
						&& items[1].getTypeId() == UnconnectedDataItemResponse.TYPE_ID) {
					ByteBuf data = ((UnconnectedDataItemResponse) items[1]).getData();

					future.complete(data);
				} else {
					future.completeExceptionally(new Exception("received unexpected items"));
				}
			} else {
				future.completeExceptionally(ex);
			}
		});

		return future;
	}

	private short nextSequenceNumber() {
		return (short) sequenceNumber.incrementAndGet();
	}
}