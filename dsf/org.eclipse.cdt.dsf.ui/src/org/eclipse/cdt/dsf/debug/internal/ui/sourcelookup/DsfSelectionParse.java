/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson AB - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.ui.SelectionToDeclarationJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 */
public class DsfSelectionParse {
	private TextEditor fEditor;
	private IWorkbenchPartSite fSite;

	public DsfSelectionParse(TextEditor editor) {
		fEditor = editor;
		fSite = editor.getSite();			
	}
	
	/**
	 * @return
	 */
	public IFunctionDeclaration[] resolveSelectedFunction() {
		ITextSelection textSelection = getSelectedStringFromEditor();
		if (textSelection != null) {
			SelectionToDeclarationJob job;
			try {
				job = new SelectionToDeclarationJob(fEditor, textSelection);
				job.schedule();
				job.join();
			} catch (CoreException e1) {
				DsfUIPlugin.log(e1);
				return null;
			} catch (InterruptedException e) {
				DsfUIPlugin.log(e);
				return null;
			}
			
			//fetch the result 
			return job.getSelectedFunctions();
		}
		
		return null;
	}
	
	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null) {
			sel = fSite.getSelectionProvider().getSelection();
		}

		return sel;
	}

	protected ITextSelection getSelectedStringFromEditor() {
		ISelection selection = getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		return (ITextSelection) selection;
	}
}
