/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Arrays;
import java.util.List;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * A delegate for the "Add Globals" action.
 */
public class AddGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate, ISelectionListener, IPartListener {

	private IGlobalVariableDescriptor[] fGlobals;

	private IViewPart fView = null;

	private IAction fAction;

	private IStructuredSelection fSelection;

	private IStatus fStatus = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init( IViewPart view ) {
		fView = view;
		view.getSite().getPage().addPartListener( this );
		view.getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( part != null && part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) ) {
			if ( selection instanceof IStructuredSelection ) {
				setSelection( (IStructuredSelection)selection );
			}
			else {
				setSelection( null );
			}
			update( getAction() );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		final IStructuredSelection selection = getSelection();
		if ( selection != null && selection.size() != 1 )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				try {
					doAction( selection.getFirstElement() );
					setStatus( null );
				}
				catch( DebugException e ) {
					setStatus( e.getStatus() );
				}
			}
		} );
		IStatus status = getStatus();
		if ( status != null && !status.isOK() ) {
			if ( status.isMultiStatus() ) {
				status = new MultiStatus( status.getPlugin(), status.getCode(), status.getChildren(), ActionMessages.getString( "AddGlobalsActionDelegate.Error(s)_occured_adding_globals_1" ), status.getException() ); //$NON-NLS-1$
			}
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) {
				CDebugUIPlugin.errorDialog( getErrorDialogMessage(), status );
			}
			else {
				CDebugUIPlugin.log( status );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		setAction( action );
		if ( getView() != null ) {
			update( action );
		}
	}

	protected void update( IAction action ) {
		if ( action != null ) {
			action.setEnabled( getEnableStateForSelection( getSelection() ) );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part ) {
		if ( part.equals( getView() ) ) {
			dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part ) {
	}

	protected IViewPart getView() {
		return fView;
	}

	protected void setView( IViewPart viewPart ) {
		fView = viewPart;
	}

	protected void setAction( IAction action ) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}

	private void setSelection( IStructuredSelection selection ) {
		fSelection = selection;
	}

	private IStructuredSelection getSelection() {
		return fSelection;
	}

	public void dispose() {
		if ( getView() != null ) {
			getView().getViewSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
			getView().getViewSite().getPage().removePartListener( this );
		}
	}

	protected boolean getEnableStateForSelection( IStructuredSelection selection ) {
		if ( selection == null || selection.size() != 1 ) {
			return false;
		}
		Object element = selection.getFirstElement();
		return (element != null && element instanceof IDebugElement && ((IDebugElement)element).getDebugTarget().getAdapter( IExecFileInfo.class ) != null);
	}

	private ListSelectionDialog createDialog() {
		return new ListSelectionDialog( getView().getSite().getShell(), fGlobals, new IStructuredContentProvider() {

			public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
			}

			public void dispose() {
			}

			public Object[] getElements( Object parent ) {
				return getGlobals();
			}
		}, new LabelProvider() {

			public String getText( Object element ) {
				if ( element instanceof IGlobalVariableDescriptor ) {
					String path = ""; //$NON-NLS-1$
					if ( ((IGlobalVariableDescriptor)element).getPath() != null ) {
						path = ((IGlobalVariableDescriptor)element).getPath().toString();
						int index = path.lastIndexOf( '/' );
						if ( index != -1 )
							path = path.substring( index + 1 );
					}
					return (path.length() > 0 ? ('\'' + path + "\'::") : "") + ((IGlobalVariableDescriptor)element).getName(); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return null;
			}
		}, ActionMessages.getString( "AddGlobalsActionDelegate.0" ) ); //$NON-NLS-1$
	}

	protected IGlobalVariableDescriptor[] getGlobals() {
		return fGlobals;
	}

	protected void doAction( Object element ) throws DebugException {
		if ( getView() == null )
			return;
		if ( element != null && element instanceof IDebugElement ) {
			IExecFileInfo info = (IExecFileInfo)((IDebugElement)element).getDebugTarget().getAdapter( IExecFileInfo.class );
			ICGlobalVariableManager gvm = (ICGlobalVariableManager)((IDebugElement)element).getDebugTarget().getAdapter( ICGlobalVariableManager.class );
			if ( info != null && gvm != null ) {
				fGlobals = info.getGlobals();
				ListSelectionDialog dlg = createDialog();
				dlg.setInitialSelections( gvm.getDescriptors() );
				if ( dlg.open() == Window.OK ) {
					List list = Arrays.asList( dlg.getResult() );
					IGlobalVariableDescriptor[] selections = (IGlobalVariableDescriptor[])list.toArray( new IGlobalVariableDescriptor[list.size()] );
					gvm.addGlobals( selections );
				}
			}
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "AddGlobalsActionDelegate.1" ); //$NON-NLS-1$
	}

	protected void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected IStatus getStatus() {
		return fStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		super.init( action );
		Object element = DebugUITools.getDebugContext();
		setSelection( (element != null) ? new StructuredSelection( element ) : new StructuredSelection() );
		update( action );
	}
}
