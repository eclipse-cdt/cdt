/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.dap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryContents;
import org.eclipse.cdt.debug.dap.CDTDebugProtocol.MemoryRequestArguments;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;

public class MemoryBlock extends CDTDebugElement implements IMemoryBlockExtension {
	private ArrayList<Object> connections = new ArrayList<>();

	@SuppressWarnings("unused")
	private boolean isEnabled;

	@SuppressWarnings("unused")
	private Object context;

	private DapDebugTarget debugTarget;

	private BigInteger bigBaseAddress;

	private String expression;

	public MemoryBlock(DapDebugTarget debugTarget, String expression, BigInteger bigBaseAddress, Object context) {
		super(debugTarget);
		this.debugTarget = debugTarget;
		this.expression = expression;
		this.bigBaseAddress = bigBaseAddress;
		this.context = context;
	}

	@Override
	public long getStartAddress() {
		// Not implemented (obsoleted by IMemoryBlockExtension)
		return 0;
	}

	@Override
	public long getLength() {
		// Not implemented (obsoleted by IMemoryBlockExtension)
		return 0;
	}

	@Override
	public byte[] getBytes() throws DebugException {
		// Not implemented (obsoleted by IMemoryBlockExtension)
		return new byte[0];
	}

	@Override
	public boolean supportsValueModification() {
		// TODO
		return false;
	}

	@Override
	public void setValue(long offset, byte[] bytes) throws DebugException {
		// Not implemented (obsoleted by IMemoryBlockExtension)
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public BigInteger getBigBaseAddress() throws DebugException {
		return bigBaseAddress;
	}

	@Override
	public BigInteger getMemoryBlockStartAddress() throws DebugException {
		// Null indicates that memory can be retrieved at addresses lower than the block base address
		return null;
	}

	@Override
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		// Null indicates that memory can be retrieved at addresses higher the block base address
		return null;
	}

	@Override
	public BigInteger getBigLength() throws DebugException {
		// -1 indicates that memory block is unbounded
		return BigInteger.valueOf(-1);
	}

	@Override
	public int getAddressSize() throws DebugException {
		// TODO Get this from backend
		return 8;
	}

	@Override
	public int getAddressableSize() throws DebugException {
		// TODO Get this from backend
		return 1;
	}

	@Override
	public boolean supportBaseAddressModification() throws DebugException {
		return false;
	}

	@Override
	public boolean supportsChangeManagement() {
		return false;
	}

	@Override
	public void setBaseAddress(BigInteger address) throws DebugException {
		// Not supported
	}

	@Override
	public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
		return getBytesFromAddress(getBigBaseAddress().add(offset), units);
	}

	@Override
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException {
		MemoryRequestArguments memoryRequestArguments = new MemoryRequestArguments();
		memoryRequestArguments.setAddress("0x" + address.toString(16));
		memoryRequestArguments.setLength(units);
		CompletableFuture<MemoryContents> memory = getDebugProtocolServer().memory(memoryRequestArguments);
		MemoryContents body = complete(memory);
		String resultAddress = body.getAddress();
		String contents = body.getData();
		BigInteger bigResultAddress;
		BigInteger.valueOf(Long.decode(resultAddress));
		try {
			bigResultAddress = BigInteger.valueOf(Long.decode(resultAddress));
		} catch (NumberFormatException nfexc) {
			// TODO
			bigResultAddress = address;
		}
		int numRequestedBytes = (int) (units * getAddressableSize());
		MemoryByte[] bytes = new MemoryByte[numRequestedBytes];
		int resultOffsetFromRequest = bigResultAddress.subtract(address).intValue();

		for (int i = 0; i < resultOffsetFromRequest; i++) {
			bytes[i] = new MemoryByte((byte) 0, (byte) (0 & ~MemoryByte.READABLE));
		}
		for (int i = resultOffsetFromRequest, k = 0; i < resultOffsetFromRequest + (contents.length() / 2)
				&& i < numRequestedBytes; i++, k += 2) {
			byte b = (byte) Integer.parseInt(contents.substring(k, k + 2), 16);
			bytes[i] = new MemoryByte(b);
		}
		for (int i = resultOffsetFromRequest + (contents.length() / 2); i < numRequestedBytes; i++) {
			bytes[i] = new MemoryByte((byte) 0, (byte) (0 & ~MemoryByte.READABLE));
		}
		return bytes;
	}

	@Override
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		// Not supported
	}

	@Override
	public void dispose() throws DebugException {
		// nothing to do yet
	}

	@Override
	public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return debugTarget;
	}

	@Override
	public void connect(Object client) {
		if (!connections.contains(client))
			connections.add(client);
		if (connections.size() == 1)
			enable();
	}

	@Override
	public void disconnect(Object client) {
		if (connections.contains(client))
			connections.remove(client);
		if (connections.size() == 0)
			disable();
	}

	@Override
	public Object[] getConnections() {
		return connections.toArray();
	}

	private void enable() {
		isEnabled = true;
	}

	private void disable() {
		isEnabled = false;
	}

}
