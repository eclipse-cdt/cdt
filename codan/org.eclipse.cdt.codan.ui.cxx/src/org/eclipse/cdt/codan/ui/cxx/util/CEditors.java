/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cxx.util;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Utility methods related to <code>{@link CEditor}</code>.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
@SuppressWarnings("restriction") // CEditor is internal API
public final class CEditors {
	/**
	 * Finds the current {@code CEditor}.
	 * @return the current {@code CEditor}, or {@code null} if one cannot be found.
	 */
	public static TextEditor activeCEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			IWorkbenchPage activePage = w.getActivePage();
			IEditorPart editor = activePage.getActiveEditor();
			if (editor instanceof CEditor) {
				return (CEditor) editor;
			}
		}
		return null;
	}

	private CEditors() {}
}
