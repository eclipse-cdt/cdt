/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils;

import java.io.IOException;

import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * ToolsProvider 
*/
public class ToolsProvider implements IToolsProvider {

	ICExtension cextension;

	public ToolsProvider(ICExtension cext) {
		cextension = cext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IToolsProvider#getAddr2Line(org.eclipse.core.runtime.IPath)
	 */
	public Addr2line getAddr2Line(IPath path) {
			IPath addr2LinePath = getAddr2LinePath();
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
	 * @see org.eclipse.cdt.utils.IToolsProvider#getCPPFilt()
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
	 * @see org.eclipse.cdt.utils.IToolsProvider#getObjdump(org.eclipse.core.runtime.IPath)
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

	ICExtensionReference getExtensionReference() {
		return cextension.getExtensionReference();
	}

	IPath getAddr2LinePath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("addr2line"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "addr2line"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	IPath getObjdumpPath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("objdump"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "objdump"; //$NON-NLS-1$
		}
		return new Path(value);
	}
	
	String getObjdumpArgs() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("objdumpArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}
	
	IPath getCPPFiltPath() {
		ICExtensionReference ref = getExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "c++filt"; //$NON-NLS-1$
		}
		return new Path(value);
	}

}
