/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.utils.xcoff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.xcoff.AR;
import org.eclipse.cdt.utils.xcoff.XCoff32;
import org.eclipse.core.runtime.IPath;

/**
 * A member of an XCOFF32 archive
 * 
 * @author vhirsl
 */
public class ARMember extends XCOFFBinaryObject {
	AR.MemberHeader header;

	/**
	 * @param parser
	 * @param path
	 */
	public ARMember(IBinaryParser parser, IPath path, AR.MemberHeader header) {
		super(parser, path);
		this.header = header;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (path != null && header != null) {
			try {
				stream = new ByteArrayInputStream(header.getObjectData());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getShortName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.xcoff.parser.XCOFFBinaryObject#addSymbols(org.eclipse.cdt.utils.xcoff.XCoff32.Symbol[], byte[], org.eclipse.cdt.utils.Addr2line, org.eclipse.cdt.utils.CPPFilt, org.eclipse.cdt.utils.CygPath, java.util.List)
	 */
	protected void addSymbols(XCoff32.Symbol[] peSyms, byte[] table, List list) {
		CPPFilt cppfilt = getCPPFilt();
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isVariable()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 /*||
					!Character.isJavaIdentifierStart(name.charAt(0))*/) {
					continue;
				}
				if (cppfilt != null) {
					try {
						name = cppfilt.getFunction(name);
					} catch (IOException e1) {
						cppfilt = null;
					}
				}
				Symbol sym = new Symbol(this, name, peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE, peSyms[i].n_value, 1);

				list.add(sym);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.xcoff.parser.XCOFFBinaryObject#getXCoff32()
	 */
	protected XCoff32 getXCoff32() throws IOException {
		if (header != null) {
			return header.getXCoff();
		}
		throw new IOException(CCorePlugin.getResourceString("Util.exception.noFileAssociation")); //$NON-NLS-1$
	}
}
