/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [258631][api] ITerminalService should be public API
 *******************************************************************************/

package org.eclipse.rse.services.terminals;


/**
 * Abstract base class for clients to decorate an ITerminalShell instance they
 * have with additional functionality.
 *
 * Typically, such additional functionality can be provided either by additional
 * knowledge about the underlying system or process, or by analyzing the input
 * and output streams for some well-known data that gives such additional
 * knowledge.
 *
 * @since org.eclipse.rse.services 3.1
 */
public abstract class TerminalShellDecorator extends BaseShellDecorator implements ITerminalShell {

	public TerminalShellDecorator(ITerminalShell delegate) {
		super(delegate);
	}

	protected ITerminalShell getDelegate() {
		return (ITerminalShell) fDelegate;
	}

	public String getPtyType() {
		return getDelegate().getPtyType();
	}

	public String getDefaultEncoding() {
		return getDelegate().getDefaultEncoding();
	}

	public boolean isLocalEcho() {
		return getDelegate().isLocalEcho();
	}

	public void setTerminalSize(int newWidth, int newHeight) {
		getDelegate().setTerminalSize(newWidth, newHeight);
	}

}
