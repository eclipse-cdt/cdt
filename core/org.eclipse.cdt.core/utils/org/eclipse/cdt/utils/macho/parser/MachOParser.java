/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.macho.parser;
 
import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.ToolsProvider;
import org.eclipse.cdt.utils.macho.AR;
import org.eclipse.cdt.utils.macho.MachO;
import org.eclipse.cdt.utils.macho.MachO.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class MachOParser extends ToolsProvider implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}


	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		try {
			MachO.Attribute attribute = null;
			if (hints != null && hints.length > 0) {
				try {
					attribute = MachO.getAttributes(hints);
				} catch (EOFException eof) {
					// continue, the array was to small.
				}
			}

			//Take a second run at it if the data array failed. 			
 			if(attribute == null) {
				attribute = MachO.getAttributes(path.toOSString());
 			}

			if (attribute != null) {
				switch (attribute.getType()) {
					case Attribute.MACHO_TYPE_EXE :
						binary = createBinaryExecutable(path);
						break;

					case Attribute.MACHO_TYPE_SHLIB :
						binary = createBinaryShared(path);
						break;

					case Attribute.MACHO_TYPE_OBJ :
						binary = createBinaryObject(path);
						break;

					case Attribute.MACHO_TYPE_CORE :
						binary = createBinaryCore(path);
						break;
				}
			}
		} catch (IOException e) {
			binary = createBinaryArchive(path);
		}
		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "MACHO"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] array, IPath path) {
		return MachO.isMachOHeader(array) || AR.isARHeader(array);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBufferSize()
	 */
	public int getHintBufferSize() {
		return 128;
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new BinaryArchive(this, path);
	}

	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new MachOBinaryObject(this, path);
	}

	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new MachOBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.macho.parser.MachOBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.EXECUTABLE;
			}
		};
	}

	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new MachOBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.macho.parser.MachOBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.SHARED;
			}
		};
	}

	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new MachOBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.macho.parser.MachOBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.CORE;
			}
		};
	}
}
