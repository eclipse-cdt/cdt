/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.services.terminals;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.PlatformObject;

/**
 * Abstract base class for clients to decorate an IBaseShell instance they have
 * with additional functionality.
 * 
 * Typically, such additional functionality can be provided either by additional
 * knowledge about the underlying system or process, or by analyzing the input
 * and output streams for some well-known data that gives such additional
 * knowledge.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a>
 * team.
 * </p>
 * 
 * @since org.eclipse.rse.services 3.0
 */
public abstract class BaseShellDecorator extends PlatformObject implements IBaseShell {

	protected final IBaseShell fDelegate;

	public BaseShellDecorator(IBaseShell delegate) {
		fDelegate = delegate;
	}

	public void exit() {
		fDelegate.exit();
	}

	public int exitValue() {
		return fDelegate.exitValue();
	}

	public InputStream getErrorStream() {
		return fDelegate.getErrorStream();
	}

	public InputStream getInputStream() {
		return fDelegate.getInputStream();
	}

	public OutputStream getOutputStream() {
		return fDelegate.getOutputStream();
	}

	public boolean isActive() {
		return fDelegate.isActive();
	}

	public boolean waitFor(long timeout) throws InterruptedException {
		return fDelegate.waitFor(timeout);
	}

	public Object getAdapter(Class adapterType) {
		// TODO do we want to delegate here or have our own adapter???
		// I think we're most flexible first letting adapt to ourselves,
		// and only if not successful then ask the delegate to adapt.
		Object adapter = super.getAdapter(adapterType);
		if (adapter == null) {
			adapter = fDelegate.getAdapter(adapterType);
		}
		return adapter;
	}

}
