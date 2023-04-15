/*******************************************************************************
 * Copyright (c) 2005, 2023 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     John Dallaway - support GNU tool prefix lookup (#361)
 *******************************************************************************/
/*
 * Created on Jul 5, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.IPath;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGnuToolFactory {

	/** @since 8.2 */
	public static final String GNU_TOOL_PREFIX_VARIABLE = "gnu_tool_prefix"; //$NON-NLS-1$

	Addr2line getAddr2line(IPath path);

	CPPFilt getCPPFilt();

	Objdump getObjdump(IPath path);

	NM getNM(IPath path);
}
