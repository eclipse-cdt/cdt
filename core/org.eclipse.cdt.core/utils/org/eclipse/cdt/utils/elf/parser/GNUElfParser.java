/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;

import org.eclipse.cdt.utils.DefaultGnuToolFactory;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.core.runtime.IPath;

public class GNUElfParser extends ElfParser {
	private IGnuToolFactory toolFactory;

	@Override
	public String getFormat() {
		return "GNU ELF"; //$NON-NLS-1$
	}

	@Override
	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path, IBinaryFile.CORE);
	}

	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new GNUElfBinaryExecutable(this, path);
	}

	@Override
	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	@Override
	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new GNUElfBinaryShared(this, path);
	}

	@Override
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new GNUElfBinaryArchive(this, path);
	}

	protected IGnuToolFactory createGNUToolFactory() {
		return new DefaultGnuToolFactory(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IGnuToolFactory.class)) {
			if (toolFactory == null) {
				toolFactory = createGNUToolFactory();
			}
			return (T) toolFactory;
		}
		return super.getAdapter(adapter);
	}
}
