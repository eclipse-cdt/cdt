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
 *     Anton Leherbauer (Wind River Systems)
 *     Hansruedi Patzen (IFS)
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import static org.eclipse.cdt.internal.core.ByteUtils.makeInt;
import static org.eclipse.cdt.internal.core.ByteUtils.makeShort;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.Elf.ELFhdr;
import org.eclipse.cdt.utils.elf.Elf.PHdr;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

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
				Attribute attribute = getAttribute(hints, path);

				if (attribute != null) {
					switch (attribute.getType()) {
					case Attribute.ELF_TYPE_EXE:
						binary = createBinaryExecutable(path);
						break;

					case Attribute.ELF_TYPE_SHLIB:
						binary = hasInterpProgramHeader(hints, path) ? createBinaryExecutable(path)
								: createBinaryShared(path);
						break;

					case Attribute.ELF_TYPE_OBJ:
						binary = createBinaryObject(path);
						break;

					case Attribute.ELF_TYPE_CORE:
						binary = createBinaryCore(path);
						break;
					}
					if (binary instanceof ElfBinaryObject) {
						((ElfBinaryObject) binary).setElfAttributes(attribute);
					}
				}
			} catch (IOException e) {
				if (hints == null) {
					try {
						binary = createBinaryArchive(path);
					} catch (IOException e2) {
						CCorePlugin.log(e); // log original exception
						throw e2;
					}
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

	private static Elf.Attribute getAttribute(byte[] hints, IPath path) throws IOException {
		if (Elf.isElfHeader(hints)) {
			try {
				return Elf.getAttributes(hints);
			} catch (EOFException eof) {
				// continue, the array was to small.
			}
		}
		return Elf.getAttributes(path.toOSString());
	}

	private static boolean hasInterpProgramHeader(byte[] hints, IPath path) throws IOException {
		if (Elf.isElfHeader(hints)) {
			int e_phentsizeOffset = 0;
			int e_phnumOffset = 0;
			int e_ehsizeOffset = 0;
			switch (hints[ELFhdr.EI_CLASS]) {
			case ELFhdr.ELFCLASS32:
				e_ehsizeOffset = 0x28;
				e_phentsizeOffset = 0x2A;
				e_phnumOffset = 0x2C;
				break;
			case ELFhdr.ELFCLASS64:
				e_ehsizeOffset = 0x34;
				e_phentsizeOffset = 0x36;
				e_phnumOffset = 0x38;
				break;
			default:
				CCorePlugin.log(IStatus.WARNING, "Unknown ELF header class in file: " + path.toOSString()); //$NON-NLS-1$
				return false;
			}
			if (e_phnumOffset + 2 < hints.length) {
				boolean isle = (hints[ELFhdr.EI_DATA] == ELFhdr.ELFDATA2LSB);
				short e_phentsize = makeShort(hints, e_phentsizeOffset, isle);
				short e_phnum = makeShort(hints, e_phnumOffset, isle);
				short e_ehsize = makeShort(hints, e_ehsizeOffset, isle);
				int lastProgramHeaderOffset = e_ehsize + (e_phnum - 1) * e_phentsize;
				for (int i = e_ehsize; i < Math.min(lastProgramHeaderOffset, hints.length) + 4; i += e_phentsize) {
					if (makeInt(hints, i, isle) == PHdr.PT_INTERP) {
						return true;
					}
				}
				/* See if we checked every program header type */
				if (lastProgramHeaderOffset + 4 < hints.length) {
					return false;
				}
			}
		}

		try {
			/* No PHdr.PT_INTERP found in the hints meaning we need to read the file itself */
			return Arrays.stream(getPHdrs(path)).anyMatch(phdr -> phdr.p_type == PHdr.PT_INTERP);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	private static PHdr[] getPHdrs(IPath path) throws IOException {
		try (Elf elf = new Elf(path.toOSString())) {
			return elf.getPHdrs();
		}
	}
}
