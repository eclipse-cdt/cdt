/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.editors.CDebugEditor;
import org.eclipse.cdt.debug.internal.ui.editors.DisassemblyEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 23, 2002
 */
public class ManageBreakpointRulerActionDelegate extends AbstractRulerActionDelegate
{
	static final private String ASM_EDITOR_ID = "org.eclipse.cdt.ui.editor.asm.AsmEditor"; //$NON-NLS-1$

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor( IAction callerAction, IEditorPart targetEditor )
	{
		if ( targetEditor != null )
		{
			String id = targetEditor.getSite().getId();
			if ( !id.equals( CDebugEditor.EDITOR_ID ) && 
				 !id.equals( CUIPlugin.EDITOR_ID ) && 
				 !id.equals( ASM_EDITOR_ID ) && 
				 !id.equals( DisassemblyEditor.DISASSEMBLY_EDITOR_ID ) )
				targetEditor = null;
		}
		super.setActiveEditor( callerAction, targetEditor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	public IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo )
	{
		return new ManageBreakpointRulerAction( rulerInfo, editor );
	}
}
