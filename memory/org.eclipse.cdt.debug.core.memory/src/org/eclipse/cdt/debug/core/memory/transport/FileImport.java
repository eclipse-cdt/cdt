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
package org.eclipse.cdt.debug.core.memory.transport;

import java.io.File;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.FrameworkUtil;

/**
 * Imports memory information from a given file
 *
 * @since 0.1
 */
public abstract class FileImport implements ICoreRunnable {

	//FIXME: private after moving common code here
	protected final Consumer<BigInteger> scroll;
	protected final File inputFile;
	protected final BigInteger startAddress;

	protected FileImport(File input, BigInteger start, Consumer<BigInteger> scroll) {
		this.inputFile = input;
		this.startAddress = start;
		this.scroll = scroll;
	}

	protected void requestFailed(String message, Throwable exception) throws CoreException {
		failed(DebugException.REQUEST_FAILED, message, exception);
	}

	protected void internalError(String message, Throwable exception) throws CoreException {
		failed(DebugException.INTERNAL_ERROR, message, exception);
	}

	protected void failed(int code, String message, Throwable exception) throws CoreException {
		Status status = new Status(//
				IStatus.ERROR, //
				FrameworkUtil.getBundle(getClass()).getSymbolicName(), //
				code, //
				message, //
				exception);
		failed(new CoreException(status));
	}

	protected void failed(CoreException exception) throws CoreException {
		Platform.getLog(FrameworkUtil.getBundle(getClass())).log(exception.getStatus());
		throw exception;
	}
}
