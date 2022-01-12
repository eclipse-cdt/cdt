/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

import junit.framework.Assert;

public class VisibilityAsserts {
	public static void assertVisibility(int expected, int actual) {
		Assert.assertEquals(visibilityName(expected), visibilityName(actual));
	}

	public static String visibilityName(int visibility) {
		switch (visibility) {
		case ICPPClassType.v_private:
			return "private";
		case ICPPClassType.v_protected:
			return "protected";
		case ICPPClassType.v_public:
			return "public";
		default:
			throw new IllegalArgumentException("Illegal visibility: " + visibility);
		}
	}
}
