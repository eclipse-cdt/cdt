/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.movetype;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String MoveType;
	public static String DeclarationMustBeSelected;
	public static String TooManyDeclarationsSelected;
	public static String NoTypeSelected;
	public static String NotInsideFunction;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
