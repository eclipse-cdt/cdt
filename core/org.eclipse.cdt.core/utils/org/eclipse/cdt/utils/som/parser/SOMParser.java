/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.utils.som.parser;

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.ToolsProvider;
import org.eclipse.cdt.utils.som.AR;
import org.eclipse.cdt.utils.som.SOM;
import org.eclipse.core.runtime.IPath;

/**
 * HP-UX SOM binary parser
 * 
 * @author vhirsl
 */
public class SOMParser extends ToolsProvider implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		if (isBinary(hints, path)) {
			try {
				SOM.Attribute attribute = null;
				if (hints != null && hints.length > 0) {
					try {
						attribute = SOM.getAttributes(hints);
					} catch (EOFException eof) {
						// continue, the array was to small.
					}
				}
	
				//Take a second run at it if the data array failed. 			
	 			if(attribute == null) {
					attribute = SOM.getAttributes(path.toOSString());
	 			}
	
				if (attribute != null) {
					switch (attribute.getType()) {
						case SOM.Attribute.SOM_TYPE_EXE :
							binary = createBinaryExecutable(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_SHLIB :
							binary = createBinaryShared(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_OBJ :
							binary = createBinaryObject(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_CORE :
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "SOM"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] hints, IPath path) {
		return SOM.isSOMHeader(hints) || AR.isARHeader(hints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getHintBufferSize()
	 */
	public int getHintBufferSize() {
		return 512;	// size of file header
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryExecutable(IPath path) {
		return new SOMBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
			 */
			public int getType() {
				return IBinaryFile.EXECUTABLE;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryShared(IPath path) {
		return new SOMBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
			 */
			public int getType() {
				return IBinaryFile.SHARED;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryObject(IPath path) {
		return new SOMBinaryObject(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryCore(IPath path) {
		return new SOMBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
			 */
			public int getType() {
				return IBinaryFile.CORE;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private IBinaryFile createBinaryArchive(IPath path) throws IOException {
		return new BinaryArchive(this, path);
	}
}
