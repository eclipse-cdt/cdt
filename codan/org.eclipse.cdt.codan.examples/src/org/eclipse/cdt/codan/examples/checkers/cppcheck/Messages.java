/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers.cppcheck;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String CppcheckChecker_toolName;

	static {
		// initialize resource bundle
		Class<Messages> clazz = Messages.class;
		NLS.initializeMessages(clazz.getName(), clazz);
	}

	private Messages() {
	}
}
