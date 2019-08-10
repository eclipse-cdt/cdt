/*******************************************************************************
 * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *     Patrick Chuong (Texas Instruments) - Bug 329682
 *     Patrick Chuong (Texas Instruments) - Bug 353351
 *     Patrick Chuong (Texas Instruments) - Bug 364405
 *     Patrick Chuong (Texas Instruments) - Bug 337851
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.IAnnotationModel;

public abstract class AbstractDisassemblyBackend implements IDisassemblyBackend {

	protected final BigInteger UNKNOWN_ADDRESS = BigInteger.valueOf(-1);
	protected IDisassemblyPartCallback fCallback;

	protected AbstractDisassemblyBackend() {
	}

	@Override
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

	@Override
	public String evaluateRegister(String register) {
		return null;
	}

	@Override
	public String getHoverInfoData(AddressRangePosition pos, String ident) {
		return null;
	}

	/**
	 * Default error handler, sub-class can override this method to provide it's own error handling.
	 *
	 * @param status
	 */
	protected void handleError(final IStatus status) {
		fCallback.asyncExec(() -> ErrorDialog.openError(fCallback.getSite().getShell(), "Error", null, status));
	}

	/**
	 * Do nothing, sub-class can override to update PC annotation.
	 */
	@Override
	public void updateExtendedPCAnnotation(IAnnotationModel model) {
	}

	@Override
	public boolean canDisassemble() {
		return true;
	}

	@Override
	public BigInteger getLastKnownAddress() {
		return UNKNOWN_ADDRESS;
	}
}
