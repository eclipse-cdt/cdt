/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
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
package org.eclipse.cdt.internal.ui.refactoring.changes;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String AbstractCElementRenameChange_renaming;
	public static String CreateFileChange_create_file;
	public static String CreateFileChange_unknown_location;
	public static String CreateFileChange_file_exists;
	public static String DeleteFileChange_delete_file;
	public static String DeleteFileChange_file_does_not_exist;
	public static String RenameTranslationUnitChange_name;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate.
	private Messages() {
	}
}
