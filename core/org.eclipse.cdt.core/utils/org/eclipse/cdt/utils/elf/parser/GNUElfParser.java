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

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * GNUElfParser
 */
public class GNUElfParser extends ElfParser implements IGnuToolFactory {

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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IGnuToolProvider#getAddr2line(org.eclipse.core.runtime.IPath)
	 */
	public Addr2line getAddr2line(IPath path) {
		IPath addr2LinePath = getAddr2linePath();
		Addr2line addr2line = null;
		if (addr2LinePath != null && !addr2LinePath.isEmpty()) {
			try {
				addr2line = new Addr2line(addr2LinePath.toOSString(), path.toOSString());
			} catch (IOException e1) {
			}
		}
		return addr2line;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IGnuToolProvider#getCPPFilt()
	 */
	public CPPFilt getCPPFilt() {
		IPath cppFiltPath = getCPPFiltPath();
		CPPFilt cppfilt = null;
		if (cppFiltPath != null && ! cppFiltPath.isEmpty()) {
			try {
				cppfilt = new CPPFilt(cppFiltPath.toOSString());
			} catch (IOException e2) {
			}
		}
		return cppfilt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IGnuToolProvider#getObjdump(org.eclipse.core.runtime.IPath)
	 */
	public Objdump getObjdump(IPath path) {
		IPath objdumpPath = getObjdumpPath();
		String objdumpArgs = getObjdumpArgs();
		Objdump objdump = null;
		if (objdumpPath != null && !objdumpPath.isEmpty()) {
			try {
				objdump = new Objdump(objdumpPath.toOSString(), objdumpArgs, path.toOSString());
			} catch (IOException e1) {
			}
		}
		return objdump;
	}
	
	protected IPath getAddr2linePath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("addr2line"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "addr2line"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getObjdumpPath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("objdump"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "objdump"; //$NON-NLS-1$
		}
		return new Path(value);
	}
	
	protected String getObjdumpArgs() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("objdumpArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}
	
	protected IPath getCPPFiltPath() {
		ICExtensionReference ref = getExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "c++filt"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getStripPath() {
		ICExtensionReference ref = getExtensionReference();
		String value = ref.getExtensionData("strip"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "strip"; //$NON-NLS-1$
		}
		return new Path(value);
	}
	
}
