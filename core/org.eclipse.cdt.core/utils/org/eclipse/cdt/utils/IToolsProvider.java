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

import org.eclipse.core.runtime.IPath;

/**
 */
public interface IToolsProvider {

	/**
	 * Return Addr2Line for this executable
	 * @param path, the executable
	 * @return
	 */
	Addr2line getAddr2Line(IPath path);

	/**
	 * Return CPPFilt to translate mangle names
	 * @return
	 */
	CPPFilt getCPPFilt();

	/**
	 * Return Objdump for this executable
	 * @param path, the executable
	 * @return
	 */
	Objdump getObjdump(IPath path);

}
