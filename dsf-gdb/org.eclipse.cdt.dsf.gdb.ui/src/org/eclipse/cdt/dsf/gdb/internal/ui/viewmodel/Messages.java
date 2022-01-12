/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	static {
		// initialize resource bundle
		NLS.initializeMessages(/*Messages.class.getName()*/"org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.Message", //$NON-NLS-1$
				Messages.class);
	}

	private Messages() {
	}

	public static String Internal_Error;
	public static String More_Children;
}
