/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Patrick Hofer - Bug 326265
 *******************************************************************************/

// this file is based on org.eclipse.cdt.internal.ui.editor.CEditorMessages

package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.osgi.util.NLS;

public final class MakefileEditorMessages extends NLS {

	public static String MakefileEditor_menu_folding;
	
	static {
		NLS.initializeMessages(MakefileEditorMessages.class.getName(), MakefileEditorMessages.class);
	}

	// Do not instantiate
	private MakefileEditorMessages() {
	}
}