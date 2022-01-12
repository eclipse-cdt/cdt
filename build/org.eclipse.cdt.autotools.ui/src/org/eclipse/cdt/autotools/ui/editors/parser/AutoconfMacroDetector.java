/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc., (c) 2015 Nokia Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *    Ed Swartz (Nokia) - refactoring
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors.parser;

import java.util.regex.Pattern;

public class AutoconfMacroDetector implements IAutoconfMacroDetector {

	private static final Pattern AUTOCONF_MACRO_PATTERN = Pattern.compile("PKG_.*|AC_.*|AM_.*|m4.*"); //$NON-NLS-1$

	@Override
	public boolean isMacroIdentifier(String name) {
		return AUTOCONF_MACRO_PATTERN.matcher(name).matches();
	}

}
