/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.ICygwinToolsProvider;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.coff.PE.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 *
 */
public abstract class BinaryFile extends PlatformObject implements IBinaryFile {

	protected IPath path;
	protected ICygwinToolsProvider toolsProvider;
	protected long timestamp;

	public BinaryFile(IPath p) {
		path = p;
	}

	public void setToolsProvider(ICygwinToolsProvider p) {
		toolsProvider = p;
	}

	public Addr2line getAddr2Line() {
		if (toolsProvider != null)
			return toolsProvider.getAddr2Line(path);
		return null;
	}

	public CPPFilt getCPPFilt() {
		if (toolsProvider != null)
			return toolsProvider.getCPPFilt();
		return null;
	}

	public CygPath getCygPath() {
		if (toolsProvider != null)
			return toolsProvider.getCygPath();
		return null;
	}

	public Objdump getObjdump() {
		if (toolsProvider != null) {
			return toolsProvider.getObjdump(path);
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getFile()
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public abstract int getType();

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (path != null) {
			Objdump objdump = getObjdump();
			if (objdump != null) {
				try {
					byte[] contents = objdump.getOutput();
					stream = new ByteArrayInputStream(contents);
				} catch (IOException e) {
					// Nothing
				}
			} else {
				try {
					stream = new FileInputStream(path.toFile());
				} catch (IOException e) {
				}
			}
		}
		if (stream == null) {
			stream = new ByteArrayInputStream(new byte[0]);
		}
		return stream;
	}

	/**
	 * @return
	 */
	protected abstract Attribute getAttribute();

	protected boolean hasChanged() {
		long modification = getPath().toFile().lastModified();
		boolean changed = modification != timestamp;
		timestamp = modification;
		return changed;
	}

}
