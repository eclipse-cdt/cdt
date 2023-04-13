/*******************************************************************************
 * Copyright (c) 2000, 2023 Space Codesign Systems and others.
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
 *     QNX Software Systems - Initial CygwinPEBinaryObject class
 *     John Dallaway - Initial GNUPEBinaryObject64 class (#361)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.runtime.IPath;

/** @since 8.2 */
public class GNUPEBinaryObject64 extends PEBinaryObject64 {

	public GNUPEBinaryObject64(IBinaryParser parser, IPath path, ARHeader header) {
		super(parser, path, header);
	}

	public GNUPEBinaryObject64(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	@Override
	public InputStream getContents() throws IOException {
		InputStream stream = null;
		Objdump objdump = getObjdump();
		if (objdump != null) {
			try {
				byte[] contents = objdump.getOutput();
				stream = new ByteArrayInputStream(contents);
			} catch (IOException e) {
				// Nothing
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	protected Objdump getObjdump() {
		IGnuToolFactory factory = getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getObjdump(getPath());
		}
		return null;
	}

}
