/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
