/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
 
/**
 * A resume at line action that can be contributed to a an editor. The action
 * will perform the "resume at line" operation for editors that provide
 * an appropriate <code>IResumeAtLineTarget</code> adapter.
 */
public class ResumeAtLineActionDelegate implements IEditorActionDelegate, IViewActionDelegate, IActionDelegate2 {

	private IWorkbenchPart fActivePart = null;

	private IResumeAtLineTarget fPartTarget = null;

	private IAction fAction = null;

	private ISelectionListener fSelectionListener = new DebugSelectionListener();

	protected ISuspendResume fTargetElement = null;

	class DebugSelectionListener implements ISelectionListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
		 */
		public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
			fTargetElement = null;
			if ( selection instanceof IStructuredSelection ) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if ( ss.size() == 1 ) {
					Object object = ss.getFirstElement();
					if ( object instanceof ISuspendResume ) {
						fTargetElement = (ISuspendResume)object;
					}
				}
			}
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
		init( action );
		bindTo( targetEditor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action ) {
		this.fAction = action;
		if ( action != null ) {
			action.setText( ActionMessages.getString( "ResumeAtLineActionDelegate.0" ) ); //$NON-NLS-1$
			action.setImageDescriptor( CDebugImages.DESC_LCL_RESUME_AT_LINE );
			action.setDisabledImageDescriptor( CDebugImages.DESC_LCL_RESUME_AT_LINE_DISABLED );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fActivePart.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
		fActivePart = null;
		fPartTarget = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent( IAction action, Event event ) {
		run( action );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		if ( fPartTarget != null && fTargetElement != null ) {
			try {
				fPartTarget.resumeAtLine( fActivePart, fActivePart.getSite().getSelectionProvider().getSelection(), fTargetElement );
			}
			catch( CoreException e ) {
				ErrorDialog.openError( fActivePart.getSite().getWorkbenchWindow().getShell(), ActionMessages.getString( "ResumeAtLineActionDelegate.1" ), ActionMessages.getString( "ResumeAtLineActionDelegate.2" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		this.fAction = action;
		update();
	}

	protected void update() {
		if ( fAction == null ) {
			return;
		}
		boolean enabled = false;
		if ( fPartTarget != null && fTargetElement != null ) {
			IWorkbenchPartSite site = fActivePart.getSite();
			if ( site != null ) {
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				if ( selectionProvider != null ) {
					ISelection selection = selectionProvider.getSelection();
					enabled = fTargetElement.isSuspended() && fPartTarget.canResumeAtLine( fActivePart, selection, fTargetElement );
				}
			}
		}
		fAction.setEnabled( enabled );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		bindTo( view );
	}

	/**
	 * Binds this action to operate on the given part's run to line adapter.
	 */
	private void bindTo( IWorkbenchPart part ) {
		fActivePart = part;
		if ( part != null ) {
			part.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
			fPartTarget = (IResumeAtLineTarget)part.getAdapter( IResumeAtLineTarget.class );
			if ( fPartTarget == null ) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				// TODO: we could restrict loading to cases when the debugging context is on
				if ( adapterManager.hasAdapter( part, IResumeAtLineTarget.class.getName() ) ) {
					fPartTarget = (IResumeAtLineTarget)adapterManager.loadAdapter( part, IResumeAtLineTarget.class.getName() );
				}
			}
			// Force the selection update
			ISelection selection = part.getSite().getWorkbenchWindow().getSelectionService().getSelection( IDebugUIConstants.ID_DEBUG_VIEW );
			fSelectionListener.selectionChanged( part, selection );
		}
		update();		
	}
}
