/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Violaine Batthish (IBM) - bug 270013
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

/**
 * OpenIncludeAction
 */
public class OpenIncludeAction extends Action {
	ISelectionProvider fSelectionProvider;

	/**
	 * Constructor.
	 */
	public OpenIncludeAction(ISelectionProvider provider) {
		super(MakeUIPlugin.getResourceString("OpenIncludeAction.title")); //$NON-NLS-1$
		setDescription(MakeUIPlugin.getResourceString("OpenIncludeAction.description")); //$NON-NLS-1$
		setToolTipText(MakeUIPlugin.getResourceString("OpenIncludeAction.tooltip")); //$NON-NLS-1$
		fSelectionProvider = provider;
	}

	@Override
	public void run() {
		IInclude[] includes = getIncludeDirective(fSelectionProvider.getSelection());
		if (includes != null) {
			for (IInclude include : includes) {
				for (IDirective directive : include.getDirectives()) {
					try {
						openInEditor(directive);
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	public static IEditorPart openInEditor(IDirective directive) throws PartInitException {
		try {
			URI uri = directive.getMakefile().getFileURI();
			IFileStore store = EFS.getStore(uri);

			IFile[] file = MakeUIPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (file.length > 0 && file[0] != null) {
				IWorkbenchPage p = MakeUIPlugin.getActivePage();
				if (p != null) {
					IEditorPart editorPart = IDE.openEditor(p, file[0], true);
					if (editorPart instanceof MakefileEditor) {
						((MakefileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			} else {
				// External file
				IEditorInput input = new FileStoreEditorInput(store);
				IWorkbenchPage p = MakeUIPlugin.getActivePage();
				if (p != null) {
					String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
					IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
					if (editorPart instanceof MakefileEditor) {
						((MakefileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	IInclude[] getIncludeDirective(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			@SuppressWarnings("unchecked")
			List<Object> list = ((IStructuredSelection) sel).toList();
			if (list.size() > 0) {
				List<IInclude> includes = new ArrayList<>(list.size());
				for (Object element : list) {
					if (element instanceof IInclude) {
						includes.add((IInclude) element);
					}
				}
				return includes.toArray(new IInclude[includes.size()]);
			}
		}
		return null;
	}

	public boolean canActionBeAdded(ISelection selection) {
		IInclude[] includes = getIncludeDirective(selection);
		return includes != null && includes.length != 0;
	}
}
