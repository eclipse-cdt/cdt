/*******************************************************************************
 * Copyright (c) 2022 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor.multipage;

import java.util.Arrays;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Opens a new {@link MultiPageEditorExample multi page editor} if the the active editor
 * is not a multi page editor, otherwise adds a new tab to the active editor.
 */
public class MultiPageEditorExampleLauncher implements IEditorLauncher {

	@Override
	public void open(IPath path) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();

		Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(path))).stream()
				.findFirst().ifPresent(file -> {
					if (activeEditor instanceof MultiPageEditorExample) {
						MultiPageEditorExample mpe = (MultiPageEditorExample) activeEditor;
						mpe.createPage(new FileEditorInput(file));
					} else {
						try {
							IDE.openEditor(page, file, MultiPageEditorExample.ID);
						} catch (PartInitException e) {
							ErrorDialog.openError(window.getShell(), "Error creating nested CEditor", null,
									e.getStatus());
						}
					}
				});
	}

}
