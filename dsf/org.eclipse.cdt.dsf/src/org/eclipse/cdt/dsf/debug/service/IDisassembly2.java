/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Bug 328168
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * This interface extends the disassembly service with support for
 * address alignment extension request.
 *
 * @since 2.2
 */
public interface IDisassembly2 extends IDisassembly {
	/**
	 * Aligns the given opCode address. This method will be call for each
	 * disassembly request, the service should try to resolve
	 * the given address and align it to a valid opCode address.
	 *
	 * @param context context of the disassembly code
	 * @param address the address to align
	 * @param drm aligned address
	 */
	void alignOpCodeAddress(IDisassemblyDMContext context, BigInteger address, DataRequestMonitor<BigInteger> drm);
}
