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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
 
/**
 * Global retargettable resume at line action.
 */
public class RetargetResumeAtLineAction extends RetargetAction {

	private ISelectionListener fSelectionListener = new DebugSelectionListener();

	private ISuspendResume fTargetElement = null;

	class DebugSelectionListener implements ISelectionListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
		 */
		public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
			setTargetElement( null );
			if ( selection instanceof IStructuredSelection ) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if ( ss.size() == 1 ) {
					Object object = ss.getFirstElement();
					if ( object instanceof ISuspendResume ) {
						setTargetElement( (ISuspendResume)object );
					}
				}
			}
			update();
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init( IWorkbenchWindow window ) {
		super.init( window );
		window.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.RetargetAction#performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected void performAction( Object target, ISelection selection, IWorkbenchPart part ) throws CoreException {
		((IResumeAtLineTarget)target).resumeAtLine( part, selection, getTargetElement() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.RetargetAction#getAdapterClass()
	 */
	protected Class getAdapterClass() {
		return IResumeAtLineTarget.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.RetargetAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	protected boolean canPerformAction( Object target, ISelection selection, IWorkbenchPart part ) {
		return getTargetElement() != null && ((IResumeAtLineTarget)target).canResumeAtLine( part, selection, getTargetElement() );
	}

	protected ISuspendResume getTargetElement() {
		return fTargetElement;
	}

	protected void setTargetElement( ISuspendResume targetElement ) {
		fTargetElement = targetElement;
	}
}
