/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 29, 2002
 */
public class CBreakpointPropertiesRulerActionDelegate extends AbstractRulerActionDelegate
{
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	protected IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo )
	{
		return new CBreakpointPropertiesRulerAction( editor, rulerInfo );
	}
}
