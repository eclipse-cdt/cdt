/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 17, 2002
 */
public class AddExpressionActionDelegate extends AbstractEditorActionDelegate
{
	/**
	 * Constructor for AddExpressionActionDelegate.
	 */
	public AddExpressionActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		String text = getSelectedText();
		ExpressionDialog dlg = new ExpressionDialog( getShell(), text );
		if ( dlg.open() != Window.OK )
			return;
		createExpression( dlg.getExpression() );
	}

	protected String getSelectedText()
	{
		ISelection selection = getTargetSelection();
		if ( selection != null && selection instanceof ITextSelection )
		{
			return ((ITextSelection)selection).getText().trim();
		}
		return ""; //$NON-NLS-1$
	}

	protected Shell getShell()
	{
		return ( getTargetPart() != null ) ? 
				getTargetPart().getSite().getShell() : CDebugUIPlugin.getActiveWorkbenchShell();
	}
	
	private void createExpression( final String text )
	{
		final Display display = CDebugUIPlugin.getStandardDisplay();
		if ( display.isDisposed() )
		{
			return;
		}
		display.asyncExec( new Runnable()
								{
									public void run()
									{
										try
										{
											IExpression expression = CDebugModel.createExpression( getDebugTarget(), text );
											DebugPlugin.getDefault().getExpressionManager().addExpression( expression );
											showExpressionView();
										}
										catch( DebugException e )
										{
											CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.AddExpressionActionDelegate.Evaluation_of_expression_failed"), e ); //$NON-NLS-1$
										}
									}
								} );
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		IDebugTarget target = null;
		if ( part != null && part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection instanceof IStructuredSelection )
			{
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IDebugElement )
				{
					IDebugTarget target1 = ((IDebugElement)element).getDebugTarget();
					if ( target1 != null && target1 instanceof ICExpressionEvaluator )
					{
						target = target1;
					}
				}
			}
			setDebugTarget( target );
			update();
		}
	}
	
	protected void initializeDebugTarget()
	{
		setDebugTarget( null );
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context != null && context instanceof IDebugElement )
		{
			IDebugTarget target = ((IDebugElement)context).getDebugTarget();
			if ( target != null && target instanceof ICExpressionEvaluator )
			{
				setDebugTarget( target );
			}			
		}
	}

	/**
	 * Make the expression view visible or open one if required.
	 * 
	 */
	protected void showExpressionView()
	{
		IWorkbenchPage page = CDebugUIPlugin.getActivePage();
		if ( page != null )
		{
			IViewPart part = page.findView( IDebugUIConstants.ID_EXPRESSION_VIEW );
			if ( part == null )
			{
				try
				{
					page.showView( IDebugUIConstants.ID_EXPRESSION_VIEW );
				}
				catch( PartInitException e )
				{
					CDebugUIPlugin.log( e.getStatus() );
				}
			}
			else
			{
				page.bringToTop( part );
			}
		}
	}
}
