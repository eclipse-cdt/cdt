/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial PEBinaryObject class
 *     Space Codesign Systems - Support for 64 bit executables
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PE64;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEBinaryObject64 extends PEBinaryObject {

	public PEBinaryObject64(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
	}

	public PEBinaryObject64(IBinaryParser parser, IPath p, int type) {
		super(parser, p, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(PE64.class)) {
			try {
				if (header != null) {
					return (T) new PE64(getPath().toOSString(), header.getObjectDataOffset());
				}
				return (T) new PE64(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			PE64 pe = getAdapter(PE64.class);
			if (pe != null) {
				return (T) pe.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}

	@Override
	protected PE getPE() throws IOException {
		if (header != null) {
			return new PE64(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new PE64(getPath().toOSString());
	}
}
