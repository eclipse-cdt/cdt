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

/**
 * GNUElfParser
 */
public class GNUElfParser extends ElfParser {
	private IGnuToolFactory toolFactory;

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	@Override
	public String getFormat() {
		return "GNU ELF"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryCore(org.eclipse.core.runtime.IPath)
	 */
	@Override
	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path, IBinaryFile.CORE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryExecutable(org.eclipse.core.runtime.IPath)
	 */
	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new GNUElfBinaryExecutable(this, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryObject(org.eclipse.core.runtime.IPath)
	 */
	@Override
	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryShared(org.eclipse.core.runtime.IPath)
	 */
	@Override
	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new GNUElfBinaryShared(this, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryArchive(org.eclipse.core.runtime.IPath)
	 */
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
