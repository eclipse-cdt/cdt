/***************************************************************************************************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Common Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 **************************************************************************************************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class CBreakpointPropertiesRulerActionDelegate extends AbstractRulerActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(org.eclipse.ui.texteditor.ITextEditor, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	protected IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo ) {
		return new CBreakpointPropertiesRulerAction( editor, rulerInfo );
	}
}