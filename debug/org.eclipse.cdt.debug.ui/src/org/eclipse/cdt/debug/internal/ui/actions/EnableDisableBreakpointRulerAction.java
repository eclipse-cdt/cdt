package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;

public class EnableDisableBreakpointRulerAction extends AbstractBreakpointRulerAction
{
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointRulerAction( ITextEditor editor, IVerticalRulerInfo info )
	{
		setInfo( info );
		setTextEditor( editor );
		setText( "&Enable Breakpoint" );
	}

	/**
	 * @see Action#run()
	 */
	public void run()
	{
		if (getBreakpoint() != null)
		{
			try
			{
				getBreakpoint().setEnabled( !getBreakpoint().isEnabled() );
			}
			catch (CoreException e)
			{
				ErrorDialog.openError( getTextEditor().getEditorSite().getShell(), 
									   "Enabling/disabling breakpoints", 
									   "Exceptions occurred enabling disabling the breakpoint", 
									   e.getStatus() );
			}
		}
	}

	/**
	 * @see IUpdate#update()
	 */
	public void update()
	{
		setBreakpoint(determineBreakpoint());
		if ( getBreakpoint() == null )
		{
			setEnabled( false );
			return;
		}
		setEnabled( true );
		try
		{
			boolean enabled = getBreakpoint().isEnabled();
			setText( enabled ? "&Disable Breakpoint" : "&Enable Breakpoint" );
		}
		catch( CoreException ce )
		{
			CDebugUIPlugin.log( ce );
		}
	}
}
