/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICExpressionEvaluator;
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 17, 2002
 */
public class AddExpressionActionDelegate implements IWorkbenchWindowActionDelegate, 
													IEditorActionDelegate,
													IPartListener,
													ISelectionListener,
													INullSelectionListener
{
	private IAction fAction;
	private IWorkbenchWindow fWorkbenchWindow;
	private IWorkbenchPart fTargetPart;
	private IEditorPart fTargetEditor;
	private IDebugTarget fDebugTarget = null;

	/**
	 * Constructor for AddExpressionActionDelegate.
	 */
	public AddExpressionActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
		IWorkbenchWindow win = getWorkbenchWindow();
		if ( win != null )
		{
			win.getPartService().removePartListener( this );
			win.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init( IWorkbenchWindow window )
	{
		setWorkbenchWindow( window );
		IWorkbenchPage page = window.getActivePage();
		if ( page != null )
		{
			setTargetPart( page.getActivePart() );
		}
		window.getPartService().addPartListener( this );
		window.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		initializeDebugTarget();
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
		setTargetPart( part );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part )
	{
		if ( part == getTargetPart() )
		{
			setTargetPart( null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		String text = getSelectedText();
		ExpressionDialog dlg = new ExpressionDialog( getShell(), text );
		if ( dlg.open() != Dialog.OK )
			return;
		createExpression( dlg.getExpression() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		setAction( action );
		update();
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction( IAction action )
	{
		fAction = action;
	}

	protected IWorkbenchWindow getWorkbenchWindow()
	{
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow( IWorkbenchWindow workbenchWindow )
	{
		fWorkbenchWindow = workbenchWindow;
	}
	
	protected String getSelectedText()
	{
		ISelection selection = getTargetSelection();
		if ( selection != null && selection instanceof ITextSelection )
		{
			return ((ITextSelection)selection).getText().trim();
		}
		return "";
	}

	protected void update()
	{
		IAction action = getAction();
		if ( action != null )
		{
			action.setEnabled( getDebugTarget() != null && getTargetPart() != null );
		}
	}

	protected void setEnabledState( ITextEditor editor )
	{
		if ( getAction() != null )
		{
			getAction().setEnabled( editor != null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor( IAction action, IEditorPart targetEditor )
	{
		setAction( action );
		if ( getWorkbenchWindow() == null )
		{
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			setWorkbenchWindow( window );
			if ( window != null )
			{
				window.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
			}
		}
		setTargetPart( targetEditor );
		initializeDebugTarget();
		update();
	}

	protected IWorkbenchPart getTargetPart()
	{
		return fTargetPart;
	}

	protected void setTargetPart( IWorkbenchPart part )
	{
		fTargetPart = part;
	}

	protected Shell getShell()
	{
		if ( getTargetPart() != null )
		{
			return getTargetPart().getSite().getShell();
		}
		else
		{
			return CDebugUIPlugin.getActiveWorkbenchShell();
		}
	}

	protected ISelection getTargetSelection()
	{
		IWorkbenchPart part = getTargetPart();
		if ( part != null )
		{
			ISelectionProvider provider = part.getSite().getSelectionProvider();
			if ( provider != null )
			{
				return provider.getSelection();
			}
		}
		return null;
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
										}
										catch( DebugException e )
										{
											CDebugUIPlugin.errorDialog( e.getMessage(), e );
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
		if ( part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection != null && selection instanceof IStructuredSelection )
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
	
	protected void setDebugTarget( IDebugTarget target )
	{
		fDebugTarget = target;
	}
	
	protected IDebugTarget getDebugTarget()
	{
		return fDebugTarget;
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
}
