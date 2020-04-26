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

import org.eclipse.core.runtime.ICoreRunnable;

/**
 * Exports memory information to a given file
 *
 * @since 0.1
 */
public abstract class FileExport<O extends AutoCloseable> implements ICoreRunnable {

	protected final BigInteger start;
	protected final BigInteger end;
	protected final BigInteger addressable;
	protected final ReadMemory read;

	protected final File file;

	protected FileExport(File input, ExportRequest request) {
		this.file = input;
		this.start = request.start();
		this.end = request.end();
		this.addressable = request.addressable();
		this.read = request.read();
	}

}
