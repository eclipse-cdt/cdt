/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICBreakpoint;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Presents a custom properties dialog to configure the attibutes of 
 * a C/C++ breakpoint from the ruler popup menu of a text editor.
 * 
 * @since Aug 29, 2002
 */
public class CBreakpointPropertiesRulerAction extends AbstractBreakpointRulerAction
{
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public CBreakpointPropertiesRulerAction( ITextEditor editor, IVerticalRulerInfo info )
	{
		setInfo( info );
		setTextEditor( editor );
		setText( "Breakpoint &Properties..." );
	}

	/**
	 * @see Action#run()
	 */
	public void run()
	{
		if ( getBreakpoint() != null )
		{
			Dialog d = new CBreakpointPropertiesDialog( getTextEditor().getEditorSite().getShell(), (ICBreakpoint)getBreakpoint() );
			d.open();
		}
	}

	/**
	 * @see IUpdate#update()
	 */
	public void update()
	{
		setBreakpoint( determineBreakpoint() );
		if ( getBreakpoint() == null || !( getBreakpoint() instanceof ICBreakpoint ) )
		{
			setBreakpoint( null );
			setEnabled( false );
			return;
		}
		setEnabled( true );
	}
}
