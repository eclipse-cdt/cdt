/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.actions;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The delegate for the "Automatically Load Symbols" action.
 */
public class SetAutoSolibActionDelegate implements IViewActionDelegate, ISelectionListener, IPartListener {

	private IViewPart fView = null;

	private IAction fAction;

	private IStatus fStatus = null;

	/**
	 * Constructor for SetAutoSolibActionDelegate.
	 */
	public SetAutoSolibActionDelegate() {
		super();
	}

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
		if ( part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) ) {
			update( getAction() );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

			public void run() {
				try {
					doAction( DebugUITools.getDebugContext() );
					setStatus( null );
				}
				catch( DebugException e ) {
					setStatus( e.getStatus() );
				}
			}
		} );
		if ( getStatus() != null && !getStatus().isOK() ) {
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) {
				CDebugUIPlugin.errorDialog( getErrorDialogMessage(), getStatus() );
			}
			else {
				CDebugUIPlugin.log( getStatus() );
			}
		}
		update( action );
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
			IAdaptable element = DebugUITools.getDebugContext();
			action.setEnabled( getEnableStateForSelection( element ) );
			action.setChecked( getCheckStateForSelection( element ) );
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

	protected void dispose() {
		if ( getView() != null ) {
			getView().getViewSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
			getView().getViewSite().getPage().removePartListener( this );
		}
	}

	protected boolean getCheckStateForSelection( IAdaptable element ) {
		SharedLibraryManager slm = getSharedLibraryManager( element );
		Target target = getTarget(element);
		if ( slm != null  && target != null) {
			try {
				return slm.isAutoLoadSymbols(target);
			} catch( CDIException e ) {
			}
		}
		return false;
	}

	protected boolean getEnableStateForSelection( IAdaptable element ) {
		return (element instanceof IDebugElement && ((IDebugElement)element).getDebugTarget().isSuspended() && getSharedLibraryManager( element ) != null);
	}

	protected String getStatusMessage() {
		return ActionMessages.getString( "SetAutoSolibActionDelegate.0" ); //$NON-NLS-1$
	}

	protected String getErrorDialogMessage() {
		return ActionMessages.getString( "SetAutoSolibActionDelegate.1" ); //$NON-NLS-1$
	}

	protected void setStatus( IStatus status ) {
		fStatus = status;
	}

	protected IStatus getStatus() {
		return fStatus;
	}

	protected void doAction( IAdaptable element ) throws DebugException {
		if ( getView() == null )
			return;
		SharedLibraryManager slm = getSharedLibraryManager( element );
		Target target = getTarget(element);
		if ( slm != null && target != null && getAction() != null ) {
			try {
				slm.setAutoLoadSymbols( target, getAction().isChecked() );
			}
			catch( CDIException e ) {
				getAction().setChecked( !getAction().isChecked() );
				throw new DebugException( new Status( IStatus.ERROR, MIPlugin.getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, e.getMessage(), null ) );
			}
		}
	}

	private SharedLibraryManager getSharedLibraryManager( IAdaptable element ) {
		if ( element != null ) {
			ICDISession session = (ICDISession)element.getAdapter( ICDISession.class );
			if ( session instanceof Session )
				return ((Session)session).getSharedLibraryManager();
		}
		return null;
	}

	private Target getTarget( IAdaptable element ) {
		if (element != null) {
			ICDITarget target = (ICDITarget)element.getAdapter( ICDITarget.class );
			if (target instanceof Target) {
				return (Target)target;
			}
		}
		return null;
	}
}
