/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;

public class CygwinPEBinaryArchive extends PEBinaryArchive {

	/**
	 * @param parser
	 * @param path
	 * @throws IOException
	 */
	public CygwinPEBinaryArchive(PEParser parser, IPath path) throws IOException {
		super(parser, path);
	}

	@Override
	protected void addArchiveMembers(ARHeader[] headers, ArrayList<IBinaryObject> children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new CygwinPEBinaryObject(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}

}
