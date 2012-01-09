/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.IOException;

import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public class DefaultGnuToolFactory implements IGnuToolFactory {
	protected ICExtension fExtension;
	
	/**
	 * 
	 */
	public DefaultGnuToolFactory(ICExtension ext) {
		fExtension = ext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IGnuToolProvider#getAddr2line(org.eclipse.core.runtime.IPath)
	 */
	@Override
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
	@Override
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
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IGnuToolProvider#getObjdump(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public NM getNM(IPath path) {
		IPath nmPath = getNMPath();
		String nmArgs = getNMArgs();
		NM nm = null;
		if (nmPath != null && !nmPath.isEmpty()) {
			try {
				nm = new NM(nmPath.toOSString(), nmArgs, path.toOSString());
			} catch (IOException e1) {
			}
		}
		return nm;
	}

	protected IPath getAddr2linePath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value =  ref.getExtensionData("addr2line"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "addr2line"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getObjdumpPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value =  ref.getExtensionData("objdump"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "objdump"; //$NON-NLS-1$
		}
		return new Path(value);
	}
	
	protected String getObjdumpArgs() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value =  ref.getExtensionData("objdumpArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}
	
	protected IPath getCPPFiltPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "c++filt"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getStripPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("strip"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "strip"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected IPath getNMPath() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value = ref.getExtensionData("nm"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = "nm"; //$NON-NLS-1$
		}
		return new Path(value);
	}

	protected String getNMArgs() {
		ICConfigExtensionReference ref = fExtension.getConfigExtensionReference();
		String value =  ref.getExtensionData("nmArgs"); //$NON-NLS-1$
		if (value == null || value.length() == 0) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}
}
