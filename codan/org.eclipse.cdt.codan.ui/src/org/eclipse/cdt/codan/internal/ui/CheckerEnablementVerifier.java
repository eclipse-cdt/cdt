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
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.internal.core.ICheckerEnablementVerifier;
import org.eclipse.cdt.codan.ui.CodanEditorUtility;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Default implementation of <code>{@link ICheckerEnablementVerifier}</code>.
 */
public class CheckerEnablementVerifier implements ICheckerEnablementVerifier {
	@Override
	public boolean isCheckerEnabled(IChecker checker, IResource resource, CheckerLaunchMode mode) {
		if (mode != CheckerLaunchMode.RUN_ON_FILE_SAVE) {
			return true;
		}
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			IWorkbenchPage page = window.getActivePage();
			for (IEditorReference reference : page.getEditorReferences()) {
				IEditorPart editor = reference.getEditor(false);
				if (!CodanEditorUtility.isResourceOpenInEditor(resource, editor)) {
					continue;
				}
				if (editor instanceof TextEditor) {
					TextEditor textEditor = (TextEditor) editor;
					return !textEditor.isDirty();
				} 
			}
		}
		return false;
	}
}
