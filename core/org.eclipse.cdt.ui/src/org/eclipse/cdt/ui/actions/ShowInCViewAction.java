/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * This class will open the C/C++ Projects view and highlight the
 * selected resource matching the current resouce being edited in
 * the C/C++ Editor.  It uses the IShowInSource/IShowInTarget to 
 * accomplish this task so as to provide some additional portability
 * and future proofing.
 */
public class ShowInCViewAction extends Action implements IUpdate {
	private ITextEditor fEditor;
	final String CVIEW_ID = "org.eclipse.cdt.ui.CView";

	public ShowInCViewAction() {
		this(null);
	}
	
	public ShowInCViewAction(ITextEditor editor) {	
		super(CEditorMessages.getString("ShowInCView.label"));		 //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("ShowInCView.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("ShowInCView.description")); //$NON-NLS-1$

		fEditor= editor;
		//WorkbenchHelp.setHelp(this,	new Object[] { IJavaHelpContextIds.ADD_IMPORT_ON_SELECTION_ACTION });	
	}

	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		if(fEditor == null) {
			return;
		}
		
		//Locate a source and a target for us to use
		IShowInTarget showInTarget;
		IShowInSource showInSource;
		try {
			IWorkbenchPage page = fEditor.getEditorSite().getWorkbenchWindow().getActivePage();
			IWorkbenchPart part = page.showView(CVIEW_ID);
			if(part instanceof IShowInTarget) {
				showInTarget = (IShowInTarget)part;
			} else {
				showInTarget = (IShowInTarget)part.getAdapter(IShowInTarget.class);
			}

			if(fEditor instanceof IShowInSource) {
				showInSource = (IShowInSource)fEditor;			
			} else {
				showInSource = (IShowInSource)fEditor.getAdapter(IShowInSource.class);
			}
		} catch(Exception ex) {
			return;
		}
		
		if(showInTarget == null || showInSource == null) {
			return;
		}
		
		//Now go ahead and show it (assuming that you can!)
		showInTarget.show(showInSource.getShowInContext());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
	}
}

