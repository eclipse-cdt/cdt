/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * OpenIncludeAction
 */
public class OpenIncludeAction extends Action {

	ISelectionProvider fSelectionProvider;

	/**
	 * 
	 */
	public OpenIncludeAction(ISelectionProvider provider) {
		super(MakeUIPlugin.getResourceString("OpenIncludeAction.title")); //$NON-NLS-1$
		setDescription(MakeUIPlugin.getResourceString("OpenIncludeAction.description")); //$NON-NLS-1$
		setToolTipText(MakeUIPlugin.getResourceString("OpenIncludeAction.tooltip")); //$NON-NLS-1$
		fSelectionProvider= provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IInclude[] includes= getIncludeDirective(fSelectionProvider.getSelection());
		if (includes != null) {
			for (int i = 0; i < includes.length; ++i) {
				IDirective[] directives = includes[i].getDirectives();
				for (int j = 0; j < directives.length; ++j) {
					try {
						openInEditor(directives[j]);
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	private static IEditorPart openInEditor(IDirective directive) throws PartInitException {
		String filename = directive.getFileName();
		IPath path = new Path(filename);
		IFile file = MakeUIPlugin.getWorkspace().getRoot().getFileForLocation(path);
		if (file != null) {
			IWorkbenchPage p = MakeUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = IDE.openEditor(p, file, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor)editorPart).setSelection(directive, true);
				}
				return editorPart;
			}
		} else {
			// External file
			IStorage storage = new FileStorage(path);
			IStorageEditorInput input = new ExternalEditorInput(storage);
			IWorkbenchPage p = MakeUIPlugin.getActivePage();
			if (p != null) {
				String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
				IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
				if (editorPart instanceof MakefileEditor) {
					((MakefileEditor)editorPart).setSelection(directive, true);
				}
				return editorPart;
			}
			
		}
		return null;
	}

	IInclude[] getIncludeDirective(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() > 0) {
				List includes = new ArrayList(list.size());
				for (int i = 0; i < list.size(); ++i) {
					Object element= list.get(i);
					if (element instanceof IInclude) {
						includes.add(element);
					}
				}
				return (IInclude[]) includes.toArray(new IInclude[includes.size()]);
			}
		}
		return null;
	}

	public boolean canActionBeAdded(ISelection selection) {
		return getIncludeDirective(selection) != null;
	}
}
