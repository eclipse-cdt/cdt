/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
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

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;


public class GNUElfBinaryArchive extends ElfBinaryArchive {

	public GNUElfBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p);
	}
	
	@Override
	protected IBinaryObject[] createArchiveMembers(ARHeader[] headers) {
		IBinaryObject[] result= new IBinaryObject[headers.length];
		for (int i = 0; i < headers.length; i++) {
			result[i] = new GNUElfBinaryObject(getBinaryParser(), getPath(), headers[i]);
		}
		return result;
	}
}
