/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

public abstract class AbstractDisassemblyBackend implements IDisassemblyBackend {

	protected IDisassemblyPartCallback fCallback;

	protected AbstractDisassemblyBackend() {
	}

	public void init(IDisassemblyPartCallback callback) {
		assert callback != null;
		fCallback = callback;
	}

	/**
	 * Evaluate the symbol address.
	 * 
	 * @param symbol the symbol
	 * @param suppressError true to suppress error dialogs
	 * @return the address, <code>null</code> if failed to evaluate symbol
	 */
	public abstract BigInteger evaluateSymbolAddress(String symbol, boolean suppressError);

}
