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
		setText( CDebugUIPlugin.getResourceString("internal.ui.actions.EnableDisableBreakpointRulerAction.Enable_Breakpoint") ); //$NON-NLS-1$
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
									   CDebugUIPlugin.getResourceString("internal.ui.actions.EnableDisableBreakpointRulerAction.Enabling_disabling_breakpoints"),  //$NON-NLS-1$
									   CDebugUIPlugin.getResourceString("internal.ui.actions.EnableDisableBreakpointRulerAction.Exceptions_occured_enabling_disabling_breakpoint"),  //$NON-NLS-1$
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
			setText( enabled ? CDebugUIPlugin.getResourceString("internal.ui.actions.EnableDisableBreakpointRulerAction.Disable_breakpoint") : "&Enable Breakpoint" ); //$NON-NLS-1$
		}
		catch( CoreException ce )
		{
			CDebugUIPlugin.log( ce );
		}
	}
}
