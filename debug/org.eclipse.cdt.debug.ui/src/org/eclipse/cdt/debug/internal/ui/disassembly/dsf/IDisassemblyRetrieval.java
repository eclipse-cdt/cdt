/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.model.IStackFrame;

/**
 */
public interface IDisassemblyRetrieval {
	/**
	 */
	public static class Request implements IRequest {
		private IStatus fStatus;
		private boolean fCanceled;
		/*
		 * @see org.eclipse.debug.core.IRequest#cancel()
		 */
		public void cancel() {
			fCanceled= true;
		}

		/*
		 * @see org.eclipse.debug.core.IRequest#done()
		 */
		public void done() {
		}

		/*
		 * @see org.eclipse.debug.core.IRequest#getStatus()
		 */
		public IStatus getStatus() {
			return fStatus;
		}

		/*
		 * @see org.eclipse.debug.core.IRequest#isCanceled()
		 */
		public boolean isCanceled() {
			return fCanceled;
		}

		/*
		 * @see org.eclipse.debug.core.IRequest#setStatus(org.eclipse.core.runtime.IStatus)
		 */
		public void setStatus(IStatus status) {
			fStatus= status;
		}

	}

	/**
	 */
	public static class AddressRequest extends Request {
		private BigInteger fAddress;
		/**
		 * @return the address
		 */
		public BigInteger getAddress() {
			return fAddress;
		}

		/**
		 * @param address the address to set
		 */
		public void setAddress(BigInteger address) {
			fAddress= address;
		}
	}

	public static class DisassemblyRequest extends Request {
		IDisassemblyBlock fDisassemblyBlock;

		/**
		 * @return the disassemblyBlock
		 */
		public IDisassemblyBlock getDisassemblyBlock() {
			return fDisassemblyBlock;
		}

		/**
		 * @param disassemblyBlock the disassemblyBlock to set
		 */
		public void setDisassemblyBlock(IDisassemblyBlock disassemblyBlock) {
			fDisassemblyBlock= disassemblyBlock;
		}
	}

	/**
	 * @param stackFrame
	 * @param addressRequest
	 */
	void asyncGetFrameAddress(IStackFrame stackFrame, AddressRequest addressRequest);

	/**
	 * @param startAddress
	 * @param endAddress
	 * @param file
	 * @param lines
	 * @param disassemblyRequest
	 */
	void asyncGetDisassembly(BigInteger startAddress, BigInteger endAddress, String file, int fileNumber, int lines,
			DisassemblyRequest disassemblyRequest);

}

