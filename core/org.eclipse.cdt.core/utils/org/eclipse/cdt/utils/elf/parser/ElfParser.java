/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;
 
import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ElfParser extends AbstractCExtension implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}


	@Override
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		if (hints != null && AR.isARHeader(hints)) {
			binary = createBinaryArchive(path);
		} else {
			try {
				Elf.Attribute attribute = null;
				if (hints != null && Elf.isElfHeader(hints)) {
					try {
						attribute = Elf.getAttributes(hints);
					} catch (EOFException eof) {
						// continue, the array was to small.
					}
				}
	
				//Take a second run at it if the data array failed.
	 			if(attribute == null) {
					attribute = Elf.getAttributes(path.toOSString());
	 			}
	
				if (attribute != null) {
					switch (attribute.getType()) {
						case Attribute.ELF_TYPE_EXE :
							binary = createBinaryExecutable(path);
							break;
	
						case Attribute.ELF_TYPE_SHLIB :
							binary = createBinaryShared(path);
							break;
	
						case Attribute.ELF_TYPE_OBJ :
							binary = createBinaryObject(path);
							break;
	
						case Attribute.ELF_TYPE_CORE :
							binary = createBinaryCore(path);
							break;
					}
					if (binary instanceof ElfBinaryObject) {
						((ElfBinaryObject)binary).setElfAttributes(attribute);
					}
				}
			} catch (IOException e) {
				if (hints == null) {
					binary = createBinaryArchive(path);
				}
			}
		}
		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	@Override
	public String getFormat() {
		return "ELF"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean isBinary(byte[] array, IPath path) {
		return Elf.isElfHeader(array) || AR.isARHeader(array);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBufferSize()
	 */
	@Override
	public int getHintBufferSize() {
		return 128;
	}

	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new ElfBinaryArchive(this, path);
	}

	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new ElfBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new ElfBinaryExecutable(this, path);
	}

	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new ElfBinaryShared(this, path);
	}

	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new ElfBinaryObject(this, path, IBinaryFile.CORE);
	}
}
