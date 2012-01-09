/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.BinaryFile;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ElfBinaryArchive extends BinaryFile implements IBinaryArchive {

	private ArrayList<IBinaryObject> children;

	public ElfBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p, IBinaryFile.ARCHIVE);
		new AR(p.toOSString()).dispose(); // check file type
		children = new ArrayList<IBinaryObject>(5);
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
				IBinaryObject[] bobjs= createArchiveMembers(headers);
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

	protected IBinaryObject[] createArchiveMembers(ARHeader[] headers) {
		IBinaryObject[] result= new IBinaryObject[headers.length];
		for (int i = 0; i < headers.length; i++) {
			result[i]= new ElfBinaryObject(getBinaryParser(), getPath(), headers[i]);
		}
		return result;
	}
	
	/**
	 * @deprecated use {@link ElfBinaryArchive#createArchiveMembers(ARHeader[])} 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated
	protected void addArchiveMembers(ARHeader[] headers, ArrayList children) {
		IBinaryObject[] bobjs= createArchiveMembers(headers);
		children.addAll(Arrays.asList(bobjs));
	}
}
