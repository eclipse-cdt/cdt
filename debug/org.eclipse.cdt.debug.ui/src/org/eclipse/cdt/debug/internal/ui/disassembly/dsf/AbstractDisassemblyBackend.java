/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *     Patrick Chuong (Texas Instruments) - Bug fix (329682)
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
	 * Evaluate the expression to an address. This might be the address of a symbol or
	 * the value of the numeric evaluation depending on the type of the expression.
	 *
	 * @param expression the expression
	 * @param suppressError <code>true</code> to suppress error dialogs
	 * @return the address or <code>null</code> if the expression could not be evaluated
	 * @since 7.0
	 */
	public abstract BigInteger evaluateAddressExpression(String expression, boolean suppressError);


	public String evaluateRegister(String register) {
		return null;
	}

    public String getHoverInfoData(AddressRangePosition pos, String ident) {
    	return null;
	}

}
