/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

/**
 * Enter type comment.
 * 
 * @since: Oct 11, 2002
 */
public class SwitchToDisassemblyActionDelegate extends AbstractListenerActionDelegate
{
	private IViewPart fViewPart = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction( Object element ) throws DebugException
	{
		if ( element != null && element instanceof CDebugElement )
		{
			ISourceMode sourceMode = (ISourceMode)((CDebugElement)element).getDebugTarget().getAdapter( ISourceMode.class );
			if ( sourceMode != null )
			{
				sourceMode.setMode( ( sourceMode.getMode() == ISourceMode.MODE_SOURCE ) ? ISourceMode.MODE_DISASSEMBLY : ISourceMode.MODE_SOURCE );
				((CDebugElement)element).fireChangeEvent( DebugEvent.CLIENT_REQUEST );
				if ( fViewPart != null && fViewPart instanceof ISelectionChangedListener )
				{
					final AbstractDebugView view = (AbstractDebugView)fViewPart;
					fViewPart.getViewSite().getShell().getDisplay().asyncExec( 
									new Runnable()
										{
											public void run()
											{
												Viewer viewer = view.getViewer();
												viewer.setSelection( viewer.getSelection() );
											}
										} );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor( Object element )
	{
		if ( element != null && element instanceof CDebugElement )
		{
			return ( ((CDebugElement)element).getDebugTarget().getAdapter( ISourceMode.class ) != null );
		}
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#enableForMultiSelection()
	 */
	protected boolean enableForMultiSelection()
	{
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage()
	{
		return "Exceptions occurred attempting to switch to disassembly/source mode.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return "Switch to disassembly/source mode failed.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle()
	{
		return "Switch to disassembly/source mode";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		super.selectionChanged( action, selection );
		boolean checked = false;
		if ( selection != null && selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element != null && element instanceof CDebugElement )
			{
				ISourceMode sourceMode =  (ISourceMode)((CDebugElement)element).getDebugTarget().getAdapter( ISourceMode.class );
				checked = ( sourceMode != null && sourceMode.getMode() == ISourceMode.MODE_DISASSEMBLY );
			}
		}
		action.setChecked( checked );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init( IViewPart view )
	{
		super.init( view );
		fViewPart = view;
	}
}
