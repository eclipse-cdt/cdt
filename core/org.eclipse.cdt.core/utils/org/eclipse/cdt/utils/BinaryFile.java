/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 *
 */
public abstract class BinaryFile extends PlatformObject implements IBinaryFile {

	private final IPath path;
	private final IBinaryParser parser;
	private final int type;
	private long timestamp;

	public BinaryFile(IBinaryParser parser, IPath path, int type) {
		this.path = path;
		this.parser = parser;
		this.type = type;
	}

	public final IBinaryParser getBinaryParser() {
		return parser;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getFile()
	 */
	public final IPath getPath() {
		return path;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public final int getType() {
		return type;
	}

	/**
	 * @throws IOException
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() throws IOException {
		InputStream stream = null;
		if (path != null) {
			stream = new FileInputStream(path.toFile());
		}
		if (stream == null) {
			stream = new ByteArrayInputStream(new byte[0]);
		}
		return stream;
	}

	protected boolean hasChanged() {
		long modification = getPath().toFile().lastModified();
		boolean changed = modification != timestamp;
		if (changed) {
			timestamp = modification;
		}
		return changed;
	}
 
}
