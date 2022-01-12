/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.osgi.util.NLS;

class SourceLookupMessages extends NLS {
	public static String MappingSourceContainer_0;
	public static String AbsolutePathSourceContainer_0;
	public static String ProgramRelativePathSourceContainer_0;

	static {
		NLS.initializeMessages(SourceLookupMessages.class.getName(), SourceLookupMessages.class);
	}

	private SourceLookupMessages() {
		// Do not instantiate
	}
}
