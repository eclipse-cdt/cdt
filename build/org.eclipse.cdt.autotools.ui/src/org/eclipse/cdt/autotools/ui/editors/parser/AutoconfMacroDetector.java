/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc., (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *    Ed Swartz (Nokia) - refactoring
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

import java.util.regex.Pattern;

/**
 *
 */
public class AutoconfMacroDetector implements IAutoconfMacroDetector {

	private static final Pattern AUTOCONF_MACRO_PATTERN = Pattern.compile("PKG_.*|AC_.*|AM_.*|m4.*"); //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.autotools.core.ui.editors.parser.IAutoconfMacroDetector#isMacroIdentifier(java.lang.String)
	 */
	public boolean isMacroIdentifier(String name) {
		return AUTOCONF_MACRO_PATTERN.matcher(name).matches();
	}

}
