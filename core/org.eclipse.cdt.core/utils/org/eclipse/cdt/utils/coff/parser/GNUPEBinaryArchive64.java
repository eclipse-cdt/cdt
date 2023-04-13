/*******************************************************************************
 * Copyright (c) 2004, 2023 Space Codesign Systems and others.
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
 *     QNX Software Systems - initial CygwinPEBinaryArchive class
 *     John Dallaway - Initial GNUPEBinaryArchive64 class (#361)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;

/** @since 8.2 */
public class GNUPEBinaryArchive64 extends PEBinaryArchive64 {

	public GNUPEBinaryArchive64(PEParser64 parser, IPath path) throws IOException {
		super(parser, path);
	}

	@Override
	protected void addArchiveMembers(ARHeader[] headers, ArrayList<IBinaryObject> children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new GNUPEBinaryObject64(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}

}
