/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryArchive#addArchiveMembers(org.eclipse.cdt.utils.AR.ARHeader[],
	 *      java.util.ArrayList)
	 */
	protected void addArchiveMembers(ARHeader[] headers, ArrayList children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new CygwinPEBinaryObject(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}

}