/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;


public class GNUElfBinaryArchive extends ElfBinaryArchive {

	/**
	 * @param parser
	 * @param p
	 * @throws IOException
	 */
	public GNUElfBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.BinaryArchive#addArchiveMembers(org.eclipse.cdt.utils.elf.AR.ARHeader[], java.util.ArrayList)
	 */
	protected void addArchiveMembers(ARHeader[] headers, ArrayList children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new GNUElfBinaryObject(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}

}
