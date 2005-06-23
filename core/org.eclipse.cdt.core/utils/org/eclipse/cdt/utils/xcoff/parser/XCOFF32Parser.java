/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.xcoff.parser;

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.DefaultGnuToolFactory;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.xcoff.AR;
import org.eclipse.cdt.utils.xcoff.XCoff32;
import org.eclipse.core.runtime.IPath;

/**
 * XCOFF 32bit binary parser for AIX
 * 
 * @author vhirsl
 */
public class XCOFF32Parser extends AbstractCExtension implements IBinaryParser {

	private IGnuToolFactory toolFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(byte[],
	 *      org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		if (isBinary(hints, path)) {
			try {
				XCoff32.Attribute attribute = null;
				if (hints != null && hints.length > 0) {
					try {
						attribute = XCoff32.getAttributes(hints);
					} catch (EOFException eof) {
						// continue, the array was to small.
					}
				}

				//Take a second run at it if the data array failed.
				if (attribute == null) {
					attribute = XCoff32.getAttributes(path.toOSString());
				}

				if (attribute != null) {
					switch (attribute.getType()) {
						case XCoff32.Attribute.XCOFF_TYPE_EXE :
							binary = createBinaryExecutable(path);
							break;

						case XCoff32.Attribute.XCOFF_TYPE_SHLIB :
							binary = createBinaryShared(path);
							break;

						case XCoff32.Attribute.XCOFF_TYPE_OBJ :
							binary = createBinaryObject(path);
							break;

						case XCoff32.Attribute.XCOFF_TYPE_CORE :
							binary = createBinaryCore(path);
							break;
					}
				}
			} catch (IOException e) {
				binary = createBinaryArchive(path);
			}
		}
		return binary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "XCOFF32"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[],
	 *      org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] hints, IPath path) {
		return XCoff32.isXCOFF32Header(hints) || AR.isARHeader(hints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.IBinaryParser#getHintBufferSize()
	 */
	public int getHintBufferSize() {
		return 512;
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryExecutable(IPath path) {
		return new XCOFFBinaryExecutable(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryShared createBinaryShared(IPath path) {
		return new XCOFFBinaryShared(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryObject createBinaryObject(IPath path) {
		return new XCOFFBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryObject createBinaryCore(IPath path) {
		return new XCOFFBinaryObject(this, path, IBinaryFile.CORE);
	}

	/**
	 * @param path
	 * @return @throws
	 *         IOException
	 */
	private IBinaryFile createBinaryArchive(IPath path) throws IOException {
		return new XCOFFBinaryArchive(this, path);
	}

	/**
	 * @return
	 */
	private DefaultGnuToolFactory createGNUToolFactory() {
		return new DefaultGnuToolFactory(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IGnuToolFactory.class)) {
			if (toolFactory == null) {
				toolFactory = createGNUToolFactory();
			}
			return toolFactory;
		}
		return super.getAdapter(adapter);
	}
}
