/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.macho.parser;

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.macho.AR;
import org.eclipse.cdt.utils.macho.MachO64;
import org.eclipse.cdt.utils.macho.MachO64.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @since 5.2
 */
public class MachOParser64 extends AbstractCExtension implements IBinaryParser {

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
		try {
			MachO64.Attribute attribute = null;
			if (hints != null && hints.length > 0) {
				try {
					attribute = MachO64.getAttributes(hints);
				} catch (IOException eof) {
					// continue, the array was to small.
				}
			}

			//Take a second run at it if the data array failed.
			if (attribute == null) {
				attribute = MachO64.getAttributes(path.toOSString());
			}

			if (attribute != null) {
				switch (attribute.getType()) {
				case Attribute.MACHO_TYPE_EXE:
					binary = createBinaryExecutable(path);
					break;

				case Attribute.MACHO_TYPE_SHLIB:
					binary = createBinaryShared(path);
					break;

				case Attribute.MACHO_TYPE_OBJ:
					binary = createBinaryObject(path);
					break;

				case Attribute.MACHO_TYPE_CORE:
					binary = createBinaryCore(path);
					break;
				}
			}
		} catch (IOException e) {
			binary = createBinaryArchive(path);
		}
		return binary;
	}

	@Override
	public String getFormat() {
		return "MACHO"; //$NON-NLS-1$
	}

	@Override
	public boolean isBinary(byte[] array, IPath path) {
		return MachO64.isMachOHeader(array) || AR.isARHeader(array);
	}

	@Override
	public int getHintBufferSize() {
		return 128;
	}

	public CPPFilt getCPPFilt() {
		IPath cppFiltPath = getCPPFiltPath();
		CPPFilt cppfilt = null;
		if (cppFiltPath != null && !cppFiltPath.isEmpty()) {
			try {
				cppfilt = new CPPFilt(cppFiltPath.toOSString());
			} catch (IOException e2) {
			}
		}
		return cppfilt;
	}

	protected IPath getCPPFiltPath() {
		ICConfigExtensionReference ref = getConfigExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "c++filt"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new MachOBinaryArchive64(this, path);
	}

	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new MachOBinaryObject64(this, path, IBinaryFile.OBJECT);
	}

	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new MachOBinaryExecutable64(this, path);
	}

	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new MachOBinaryShared64(this, path);
	}

	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new MachOBinaryObject64(this, path, IBinaryFile.CORE);
	}
}
