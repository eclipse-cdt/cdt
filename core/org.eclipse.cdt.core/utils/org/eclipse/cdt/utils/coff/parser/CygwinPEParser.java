/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import org.eclipse.core.runtime.IPath;


/**
 */
public class CygwinPEParser extends PEParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "Cygwin PE"; //$NON-NLS-1$
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new CygwinPEBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.EXECUTABLE;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryObject createBinaryCore(IPath path) {
		return new CygwinPEBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.CORE;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryObject createBinaryObject(IPath path) {
		return new CygwinPEBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.OBJECT;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	protected IBinaryShared createBinaryShared(IPath path) {
		return new CygwinPEBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.SHARED;
			}
		};
	}

}
