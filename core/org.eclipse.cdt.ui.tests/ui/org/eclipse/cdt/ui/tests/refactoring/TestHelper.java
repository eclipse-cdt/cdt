/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
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
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

public class TestHelper {

	public static String unifyNewLines(String code) {
		String replacement = System.getProperty("line.separator"); //$NON-NLS-1$
		return code.replaceAll("(\n)|(\r\n)", replacement); //$NON-NLS-1$
	}
}
