/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextEditor;

/**
 * Enter type comment.
 * 
 * @since: Jan 6, 2003
 */
public class DisassemblyEditor extends AsmTextEditor
{
	/**
	 * Constructor for DisassemblyEditor.
	 */
	public DisassemblyEditor()
	{
		super();
		setDocumentProvider( CDebugUIPlugin.getDefault().getDisassemblyDocumentProvider() );
		setEditorContextMenuId("#DisassemblyEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#DisassemblyEditorRulerContext"); //$NON-NLS-1$
	}
}
