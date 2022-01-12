/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
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

	@Override
	public final IBinaryParser getBinaryParser() {
		return parser;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getPath()
	 */
	@Override
	public final IPath getPath() {
		return path;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
	 */
	@Override
	public final int getType() {
		return type;
	}

	/**
	 * @throws IOException
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getContents()
	 */
	@Override
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
