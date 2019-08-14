/*******************************************************************************
 * Copyright (c) 2000, 2019 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - Initial PEParser class
 *******************************************************************************/

package org.eclipse.cdt.utils.coff.parser;

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.coff.PE64;
import org.eclipse.cdt.utils.coff.PE64.Attribute;
import org.eclipse.cdt.utils.coff.PEConstants;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEParser64 extends AbstractCExtension implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		try {
			PE64.Attribute attribute = null;
			if (hints != null && hints.length > 0) {
				try {
					attribute = PE64.getAttribute(hints);
				} catch (EOFException e) {
					// continue to try
				}
			}
			// the hints may have to small, keep on trying.
			if (attribute == null) {
				attribute = PE64.getAttribute(path.toOSString());
			}

			if (attribute != null) {
				switch (attribute.getType()) {
				case Attribute.PE_TYPE_EXE:
					binary = createBinaryExecutable(path);
					break;

				case Attribute.PE_TYPE_SHLIB:
					binary = createBinaryShared(path);
					break;

				case Attribute.PE_TYPE_OBJ:
					binary = createBinaryObject(path);
					break;

				case Attribute.PE_TYPE_CORE:
					binary = createBinaryCore(path);
					break;
				}
			}
		} catch (IOException e) {
			// Is it an Archive?
			binary = createBinaryArchive(path);
		}

		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	@Override
	public String getFormat() {
		return "PE"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	@Override
	public boolean isBinary(byte[] array, IPath path) {
		boolean isBin = PE64.isExeHeader(array) || AR.isARHeader(array);
		// It maybe an object file try the known machine types.
		if (!isBin && array.length > 1) {
			int f_magic = (((array[1] & 0xff) << 8) | (array[0] & 0xff));
			switch (f_magic) {
			case PEConstants.IMAGE_FILE_MACHINE_ALPHA:
			case PEConstants.IMAGE_FILE_MACHINE_ARM:
			case PEConstants.IMAGE_FILE_MACHINE_ALPHA64:
			case PEConstants.IMAGE_FILE_MACHINE_I386:
			case PEConstants.IMAGE_FILE_MACHINE_IA64:
			case PEConstants.IMAGE_FILE_MACHINE_M68K:
			case PEConstants.IMAGE_FILE_MACHINE_MIPS16:
			case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU:
			case PEConstants.IMAGE_FILE_MACHINE_MIPSFPU16:
			case PEConstants.IMAGE_FILE_MACHINE_POWERPC:
			case PEConstants.IMAGE_FILE_MACHINE_R3000:
			case PEConstants.IMAGE_FILE_MACHINE_R4000:
			case PEConstants.IMAGE_FILE_MACHINE_R10000:
			case PEConstants.IMAGE_FILE_MACHINE_SH3:
			case PEConstants.IMAGE_FILE_MACHINE_SH4:
			case PEConstants.IMAGE_FILE_MACHINE_THUMB:
			case PEConstants.IMAGE_FILE_MACHINE_ARM2:
				// Ok;
				isBin = true;
				break;
			}
		}
		return isBin;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getHintBufferSize()
	 */
	@Override
	public int getHintBufferSize() {
		return 512;
	}

	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new PEBinaryExecutable64(this, path);
	}

	protected IBinaryObject createBinaryCore(IPath path) {
		return new PEBinaryObject64(this, path, IBinaryFile.CORE);
	}

	protected IBinaryObject createBinaryObject(IPath path) {
		return new PEBinaryObject64(this, path, IBinaryFile.OBJECT);
	}

	protected IBinaryShared createBinaryShared(IPath path) {
		return new PEBinaryShared64(this, path);
	}

	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new PEBinaryArchive64(this, path);
	}

}
