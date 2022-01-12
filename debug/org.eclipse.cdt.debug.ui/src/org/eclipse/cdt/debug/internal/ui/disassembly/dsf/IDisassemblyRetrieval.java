/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.model.IStackFrame;

/**
 */
public interface IDisassemblyRetrieval {

	interface AddressRequest extends IRequest {
		BigInteger getAddress();

		void setAddress(BigInteger address);
	}

	interface DisassemblyRequest extends IRequest {
		IDisassemblyBlock getDisassemblyBlock();

		void setDisassemblyBlock(IDisassemblyBlock disassemblyBlock);
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
	 * @param mixed  whether mixed assembly is preferred
	 * @param disassemblyRequest
	 */
	void asyncGetDisassembly(BigInteger startAddress, BigInteger endAddress, String file, int fileNumber, int lines,
			boolean mixed, DisassemblyRequest disassemblyRequest);

}
