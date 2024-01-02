/*******************************************************************************
 * Copyright (c) 2000, 2024 Space Codesign Systems and others.
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
 *     QNX Software Systems - Initial PEBinaryArchive class
 *     John Dallaway - Update for parity with ELF implementation (#630)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.cdt.utils.BinaryFile;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEBinaryArchive64 extends BinaryFile implements IBinaryArchive {

	ArrayList<IBinaryObject> children;

	public PEBinaryArchive64(PEParser64 parser, IPath path) throws IOException {
		super(parser, path, IBinaryFile.ARCHIVE);
		try (AR ar = new AR(path.toOSString())) {
			// create the object just to check file type
		}
		children = new ArrayList<>(5);
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryArchive#getObjects()
	 */
	@Override
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			AR ar = null;
			try {
				ar = new AR(getPath().toOSString());
				AR.ARHeader[] headers = ar.getHeaders();
				IBinaryObject[] bobjs = createArchiveMembers(headers);
				children.addAll(Arrays.asList(bobjs));
			} catch (IOException e) {
				//e.printStackTrace();
			}
			if (ar != null) {
				ar.dispose();
			}
			children.trimToSize();
		}
		return children.toArray(new IBinaryObject[0]);
	}

	/** @since 8.4 */
	protected IBinaryObject[] createArchiveMembers(ARHeader[] headers) {
		IBinaryObject[] result = new IBinaryObject[headers.length];
		for (int i = 0; i < headers.length; i++) {
			result[i] = new PEBinaryObject64(getBinaryParser(), getPath(), headers[i]);
		}
		return result;
	}

	/**
	 * @param headers
	 * @param children2
	 * @deprecated use {@link #createArchiveMembers(ARHeader[])}
	 */
	@Deprecated
	protected void addArchiveMembers(ARHeader[] headers, ArrayList<IBinaryObject> children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new PEBinaryObject64(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}
}
