/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Enter type comment.
 * 
 * @since Apr 2, 2003
 */
public class ManageFunctionBreakpointActionDelegate extends ActionDelegate
													implements IObjectActionDelegate
{
//	private IFunction fFunction = null;
	private ICElement fElement = null;

	/**
	 * 
	 */
	public ManageFunctionBreakpointActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action )
	{
		if ( getMethod() != null )
			manageBreakpoint( getMethod() );
		else if ( getFunction() != null )
			manageBreakpoint( getFunction() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICElement )
			{
				boolean enabled = enablesFor( (ICElement)element );
				action.setEnabled( enabled );
				if ( enabled )
				{
					setElement( (ICElement)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setElement( null );
	}

	public ICElement getElement()
	{
		return fElement;
	}

	public void setElement( ICElement element )
	{
		fElement = element;
	}

	private boolean enablesFor( ICElement element )
	{
		// for now
		return true;
	}
	
	private void manageBreakpoint( IFunction function )
	{
//		try
//		{
//			ICFunctionBreakpoint breakpoint = CDebugModel.functionBreakpointExists( function );
//			if ( breakpoint != null )
//			{
//				DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
//			}
//			else
//			{
//				CDebugModel.createFunctionBreakpoint( function, true, 0, "", true ); //$NON-NLS-1$
//			}
//		}
//		catch( CoreException e )
//		{
//			CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.ManageFunctionBreakpointActionDelegate.Cannot_add_breakpoint"), e ); //$NON-NLS-1$
//		}
	}
	
	private IFunction getFunction()
	{
		return ( getElement() != null ) ? (IFunction)getElement().getAdapter( IFunction.class ) : null;
	}

	private void manageBreakpoint( IMethod method )
	{
//		try
//		{
//			ICFunctionBreakpoint breakpoint = CDebugModel.methodBreakpointExists( method );
//			if ( breakpoint != null )
//			{
//				DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
//			}
//			else
//			{
//				CDebugModel.createMethodBreakpoint( method, true, 0, "", true ); //$NON-NLS-1$
//			}
//		}
//		catch( CoreException e )
//		{
//			CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.ManageFunctionBreakpointActionDelegate.Cannot_add_breakpoint"), e ); //$NON-NLS-1$
//		}
	}
	
	private IMethod getMethod()
	{
		return ( getElement() != null ) ? (IMethod)getElement().getAdapter( IMethod.class ) : null;
	}
}
