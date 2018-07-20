package com.digitalpetri.enip.pccc.services;

import com.digitalpetri.enip.EtherNetIpClientConfig;
import com.digitalpetri.enip.cip.epath.EPath.PaddedEPath;
import com.digitalpetri.enip.cip.epath.LogicalSegment.ClassId;
import com.digitalpetri.enip.cip.epath.LogicalSegment.InstanceId;
import com.digitalpetri.enip.cip.structs.MessageRouterRequest;
import com.digitalpetri.enip.cip.structs.MessageRouterResponse;
import com.digitalpetri.enip.pccc.PcccResponseException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

//refer to https://www.rockwellautomation.com/resources/downloads/rockwellautomation/pdf/sales-partners/technology-licensing/CIPandPCCC_v1_1.pdf
//refer section Delivery of PCCC in CIP
public class ExecutePcccAsCipService<T> implements PcccService<T> {
	public static final int SERVICE_CODE = 0x4B;

	public static final int REQUEST_ID_SIZE = 0x7;
	public static final int PCCC_TYPED_CMD = 0x0F;
	public static final int PCCC_STATUS = 0x0;

	private static final PaddedEPath CONNECTION_MANAGER_PATH = new PaddedEPath(new ClassId(0x67), new InstanceId(0x01));

	private final PcccService<T> service;
	private final short sequenceNumber;
	private final EtherNetIpClientConfig clientConfig;

	public ExecutePcccAsCipService(PcccService<T> service, short sequenceNumber, EtherNetIpClientConfig clientConfig) {
		this.service = service;
		this.sequenceNumber = sequenceNumber;
		this.clientConfig = clientConfig;
	}

	@Override
	public void encodeRequest(ByteBuf buffer) {
		MessageRouterRequest request = new MessageRouterRequest(SERVICE_CODE, CONNECTION_MANAGER_PATH, this::encode);

		MessageRouterRequest.encode(request, buffer);
	}

	private ByteBuf encode(ByteBuf buffer) {
		buffer.writeByte(REQUEST_ID_SIZE);
		buffer.writeShort(clientConfig.getVendorId());
		buffer.writeInt(clientConfig.getSerialNumber());

		int bytesWritten = encodeEmbeddedService(buffer);

		// pad byte if length was odd
		if (bytesWritten % 2 != 0)
			buffer.writeByte(0x00);

		return buffer;
	}

	private int encodeEmbeddedService(ByteBuf buffer) {
		buffer.writeByte(PCCC_TYPED_CMD);
		buffer.writeByte(PCCC_STATUS);
		buffer.writeShort(sequenceNumber);

		// embedded message
		int messageStartIndex = buffer.writerIndex();
		service.encodeRequest(buffer);

		// go back and update length
		int bytesWritten = buffer.writerIndex() - messageStartIndex;

		return bytesWritten;
	}

	@Override
	public T decodeResponse(ByteBuf buffer) throws PcccResponseException, PartialResponseException {
		MessageRouterResponse response = MessageRouterResponse.decode(buffer);

		int generalStatus = response.getGeneralStatus();

		try {
			if (generalStatus == 0x0)
				return decode(response);
			else
				throw new PcccResponseException(generalStatus, response.getAdditionalStatus());
		} finally {
			ReferenceCountUtil.release(response.getData());
		}
	}

	private T decode(MessageRouterResponse response) throws PcccResponseException, PartialResponseException {
		ByteBuf buffer = response.getData().retain();

		buffer.readUnsignedByte(); // request id size always 7
		buffer.readUnsignedShort(); // vendor id
		buffer.readInt(); // skip vendor SR no

		return decodeEmbeddedservice(buffer);
	}

	private T decodeEmbeddedservice(ByteBuf buffer) throws PcccResponseException, PartialResponseException {
		buffer.readUnsignedByte(); // PCCC command
		int pcccStatus = buffer.readUnsignedByte(); // PCCC status
		buffer.readUnsignedShort(); // sequence number

		if (pcccStatus == 0x0) {
			ByteBuf data = buffer.isReadable() ? buffer.readSlice(buffer.readableBytes()).retain()
					: Unpooled.EMPTY_BUFFER;

			return service.decodeResponse(data);
		} else {
			int extendedStatus = -1;

			if (pcccStatus == 0xF0 && buffer.isReadable())
				extendedStatus = buffer.readUnsignedByte();

			throw new PcccResponseException(pcccStatus, extendedStatus);
		}
	}
}