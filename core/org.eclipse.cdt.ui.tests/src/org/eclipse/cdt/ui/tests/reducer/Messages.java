/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String RemoveFunctionBodiesRefactoring_RemoveFunctionBodies;
	public static String RemoveUnusedDeclarationsRefactoring_RemoveUnusedDeclarations;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate.
	private Messages() {
	}
}
