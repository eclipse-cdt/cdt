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
package org.eclipse.cdt.utils.elf.parser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 *
 */
public abstract class BinaryFile extends PlatformObject implements IBinaryFile {

	protected IPath path;
	protected IPath addr2linePath;
	protected IPath cppfiltPath;

	public BinaryFile(IPath p) {
		path = p;
	}

	public void setAddr2LinePath(IPath p) {
		addr2linePath = p;
	}

	public IPath getAddr2LinePath() {
		return addr2linePath;
	}

	public void setCPPFiltPath(IPath p) {
		cppfiltPath = p;
	}

	public IPath getCPPFiltPath() {
		return cppfiltPath;
	}

	/**
	 * @return
	 */
	protected abstract Attribute getAttribute();

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
			try {
				stream = new FileInputStream(path.toFile());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = new ByteArrayInputStream(new byte[0]);
		}
		return stream;
	}

}
