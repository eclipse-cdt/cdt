/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.memory.transport;

import java.math.BigInteger;
import java.util.function.Supplier;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

public class AddressableSize implements Supplier<BigInteger> {

	private final IMemoryBlockExtension memory;

	public AddressableSize(IMemoryBlockExtension memory) {
		this.memory = memory;
	}

	@Override
	public BigInteger get() {
		try {
			return BigInteger.valueOf(memory.getAddressableSize());
		} catch (DebugException e1) {
			// sane value for most cases
			return BigInteger.ONE;
		}
	}

}
