/*******************************************************************************
 * Copyright (c) 2000, 2019 Space Codesign Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.utils.DefaultCygwinToolFactory;
import org.eclipse.cdt.utils.ICygwinToolsFactroy;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class CygwinPEParser64 extends PEParser64 {

	private DefaultCygwinToolFactory toolFactory;

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	@Override
	public String getFormat() {
		return "Cygwin PE"; //$NON-NLS-1$
	}

	@Override
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new CygwinPEBinaryArchive64(this, path);
	}

	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new CygwinPEBinaryExecutable64(this, path, IBinaryFile.EXECUTABLE);
	}

	@Override
	protected IBinaryObject createBinaryCore(IPath path) {
		return new CygwinPEBinaryObject64(this, path, IBinaryFile.CORE);
	}

	@Override
	protected IBinaryObject createBinaryObject(IPath path) {
		return new CygwinPEBinaryObject64(this, path, IBinaryFile.OBJECT);
	}

	@Override
	protected IBinaryShared createBinaryShared(IPath path) {
		return new CygwinPEBinaryShared64(this, path);
	}

	protected DefaultCygwinToolFactory createToolFactory() {
		return new DefaultCygwinToolFactory(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICygwinToolsFactroy.class)) {
			if (toolFactory == null) {
				toolFactory = createToolFactory();
			}
			return toolFactory;
		}
		return super.getAdapter(adapter);
	}
}
