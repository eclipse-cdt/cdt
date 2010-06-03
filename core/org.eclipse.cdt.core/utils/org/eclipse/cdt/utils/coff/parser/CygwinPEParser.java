/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.utils.DefaultCygwinToolFactory;
import org.eclipse.cdt.utils.ICygwinToolsFactroy;
import org.eclipse.core.runtime.IPath;

/**
 */
public class CygwinPEParser extends PEParser {

	
	private DefaultCygwinToolFactory toolFactory;

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	@Override
	public String getFormat() {
		return "Cygwin PE"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.coff.parser.PEParser#createBinaryArchive(org.eclipse.core.runtime.IPath)
	 */
	@Override
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new CygwinPEBinaryArchive(this, path);
	}

	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new CygwinPEBinaryExecutable(this, path, IBinaryFile.EXECUTABLE);
	}

	@Override
	protected IBinaryObject createBinaryCore(IPath path) {
		return new CygwinPEBinaryObject(this, path, IBinaryFile.CORE);
	}

	@Override
	protected IBinaryObject createBinaryObject(IPath path) {
		return new CygwinPEBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	@Override
	protected IBinaryShared createBinaryShared(IPath path) {
		return new CygwinPEBinaryShared(this, path);
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
