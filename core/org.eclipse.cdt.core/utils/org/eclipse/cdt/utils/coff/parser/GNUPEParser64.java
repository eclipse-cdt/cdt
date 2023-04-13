/*******************************************************************************
 * Copyright (c) 2000, 2023 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - Initial CygwinPEParser class
 *     John Dallaway - Initial GNUPEParser64 class (#361)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.utils.DefaultGnuToolFactory;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.core.runtime.IPath;

/** @since 8.2 */
public class GNUPEParser64 extends PEParser64 {

	private IGnuToolFactory toolFactory;

	@Override
	public String getFormat() {
		return "GNU PE"; //$NON-NLS-1$
	}

	@Override
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new GNUPEBinaryArchive64(this, path);
	}

	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new GNUPEBinaryExecutable64(this, path, IBinaryFile.EXECUTABLE);
	}

	@Override
	protected IBinaryObject createBinaryCore(IPath path) {
		return new GNUPEBinaryObject64(this, path, IBinaryFile.CORE);
	}

	@Override
	protected IBinaryObject createBinaryObject(IPath path) {
		return new GNUPEBinaryObject64(this, path, IBinaryFile.OBJECT);
	}

	@Override
	protected IBinaryShared createBinaryShared(IPath path) {
		return new GNUPEBinaryShared64(this, path);
	}

	protected IGnuToolFactory createToolFactory() {
		return new DefaultGnuToolFactory(this);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IGnuToolFactory.class)) {
			if (toolFactory == null) {
				toolFactory = createToolFactory();
			}
			return adapter.cast(toolFactory);
		}
		return super.getAdapter(adapter);
	}
}
