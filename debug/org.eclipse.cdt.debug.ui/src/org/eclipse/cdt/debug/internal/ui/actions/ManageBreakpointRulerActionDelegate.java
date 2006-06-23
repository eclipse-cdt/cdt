/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class ManageBreakpointRulerActionDelegate extends AbstractRulerActionDelegate {

	private ToggleBreakpointRulerAction fTargetAction;
	private IEditorPart fActiveEditor;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	public IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo ) {
		fTargetAction = new ToggleBreakpointRulerAction( editor, rulerInfo );
		return fTargetAction;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor( IAction callerAction, IEditorPart targetEditor ) {
		if ( fActiveEditor != null ) {
			if ( fTargetAction != null ) {
				fTargetAction.dispose();
				fTargetAction = null;
			}
		}
		fActiveEditor = targetEditor;
		super.setActiveEditor( callerAction, targetEditor );
	}
}
