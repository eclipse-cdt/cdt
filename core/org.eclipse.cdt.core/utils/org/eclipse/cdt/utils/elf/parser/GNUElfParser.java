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
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

/**
 * GNUElfParser
 */
public class GNUElfParser extends ElfParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "GNU ELF"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryCore(org.eclipse.core.runtime.IPath)
	 */
	protected IBinaryObject createBinaryCore(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.CORE;
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryExecutable(org.eclipse.core.runtime.IPath)
	 */
	protected IBinaryExecutable createBinaryExecutable(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.EXECUTABLE;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryObject(org.eclipse.core.runtime.IPath)
	 */
	protected IBinaryObject createBinaryObject(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.OBJECT;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfParser#createBinaryShared(org.eclipse.core.runtime.IPath)
	 */
	protected IBinaryShared createBinaryShared(IPath path) throws IOException {
		return new GNUElfBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#getType()
			 */
			public int getType() {
				return IBinaryFile.SHARED;
			}
		};
	}
}
