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

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.xcoff.AR;
import org.eclipse.cdt.utils.xcoff.XCoff32;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * XCOFF 32bit binary parser for AIX
 * 
 * @author vhirsl
 */
public class XCOFF32Parser extends AbstractCExtension implements IBinaryParser, IGnuToolFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		if (isBinary(hints, path)) {
			try {
				XCoff32.Attribute attribute = null;
				if (hints != null && hints.length > 0) {
					try {
						attribute = XCoff32.getAttributes(hints);
					} catch (EOFException eof) {
						// continue, the array was to small.
					}
				}
	
				//Take a second run at it if the data array failed. 			
	 			if(attribute == null) {
					attribute = XCoff32.getAttributes(path.toOSString());
	 			}
	
				if (attribute != null) {
					switch (attribute.getType()) {
						case XCoff32.Attribute.XCOFF_TYPE_EXE :
							binary = createBinaryExecutable(path);
							break;
	
						case XCoff32.Attribute.XCOFF_TYPE_SHLIB :
							binary = createBinaryShared(path);
							break;
	
						case XCoff32.Attribute.XCOFF_TYPE_OBJ :
							binary = createBinaryObject(path);
							break;
	
						case XCoff32.Attribute.XCOFF_TYPE_CORE :
							binary = createBinaryCore(path);
							break;
					}
				}
			} catch (IOException e) {
				binary = createBinaryArchive(path);
			}
		}
		return binary;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "XCOFF32"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] hints, IPath path) {
		return XCoff32.isXCOFF32Header(hints) || AR.isARHeader(hints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getHintBufferSize()
	 */
	public int getHintBufferSize() {
		return 512;
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryExecutable(IPath path) {
		return new XCOFFBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
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
	private IBinaryFile createBinaryShared(IPath path) {
		return new XCOFFBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
			 */
			public int getType() {
				return IBinaryFile.SHARED;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryObject(IPath path) {
		return new XCOFFBinaryObject(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryFile createBinaryCore(IPath path) {
		return new XCOFFBinaryObject(this, path) {
			/* (non-Javadoc)
			 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
			 */
			public int getType() {
				return IBinaryFile.CORE;
			}
		};
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private IBinaryFile createBinaryArchive(IPath path) throws IOException {
		return new BinaryArchive(this, path);
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
