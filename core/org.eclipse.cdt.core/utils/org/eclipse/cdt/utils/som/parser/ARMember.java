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
package org.eclipse.cdt.utils.som.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.som.AR;
import org.eclipse.cdt.utils.som.SOM;
import org.eclipse.core.runtime.IPath;

/**
 * A member of a SOM archive
 * 
 * @author vhirsl
 */
public class ARMember extends SOMBinaryObject {
	private AR.ARHeader header;
	
	/**
	 * @param parser
	 * @param path
	 */
	public ARMember(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path);
		this.header = header;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.som.parser.SOMBinaryObject#addSymbols(org.eclipse.cdt.utils.som.SOM.Symbol[], byte[], org.eclipse.cdt.utils.Addr2line, org.eclipse.cdt.utils.CPPFilt, org.eclipse.cdt.utils.CygPath, java.util.List)
	 */
	protected void addSymbols(SOM.Symbol[] peSyms, byte[] table, List list) {
		CPPFilt cppfilt = getCPPFilt();
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isVariable()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 || 
				    !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				if (cppfilt != null) {
					try {
						name = cppfilt.getFunction(name);
					} catch (IOException e1) {
						cppfilt = null;
					}
				}
				Symbol sym = new Symbol(this, name, peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE, new Addr32(peSyms[i].symbol_value), 1);

				list.add(sym);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.som.parser.SOMBinaryObject#getSOM()
	 */
	protected SOM getSOM() throws IOException {
		if (header != null) {
			return header.getSOM();
		}
		throw new IOException(CCorePlugin.getResourceString("Util.exception.noFileAssociation")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getContents()
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return ""; //$NON-NLS-1$
	}
}
