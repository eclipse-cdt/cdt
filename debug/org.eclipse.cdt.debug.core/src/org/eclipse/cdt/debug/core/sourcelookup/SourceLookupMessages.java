/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Jonah Graham (Kichwa Coders) - Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.osgi.util.NLS;

class SourceLookupMessages extends NLS {
	public static String MappingSourceContainer_0;
	public static String AbsolutePathSourceContainer_0;
	public static String ProgramRelativePathSourceContainer_0;
	public static String SourceSubstitutePathContainer_0;

	static {
		NLS.initializeMessages(SourceLookupMessages.class.getName(), SourceLookupMessages.class);
	}

	private SourceLookupMessages() {
		// Do not instantiate
	}
}
