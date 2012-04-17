/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
