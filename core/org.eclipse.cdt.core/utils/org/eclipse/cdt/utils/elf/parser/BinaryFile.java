package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public abstract class BinaryFile extends PlatformObject implements IBinaryFile {

	protected IPath path;

	public BinaryFile(IPath p) {
		path = p;
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
