/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line action of C editor popup menu.
 */
public class RunToLineActionDelegate extends AbstractEditorActionDelegate {

	IRunToLineTarget fRunToLineTarget;
	
	/**
	 * Constructor for RunToLineActionDelegate.
	 */
	public RunToLineActionDelegate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		if ( getTargetPart() instanceof ITextEditor ) {
			ITextSelection selection = (ITextSelection)((ITextEditor)getTargetPart()).getSelectionProvider().getSelection();
			if ( getRunToLineTarget() != null ) {
				try {
					getRunToLineTarget().runToLine( getTargetPart(), selection, getDebugTarget() );
				}
				catch( CoreException e ) {
					DebugUIPlugin.errorDialog( getTargetPart().getSite().getShell(), ActionMessages.getString( "RunToLineActionDelegate.Error_1" ), ActionMessages.getString( "RunToLineActionDelegate.Operation_failed_1" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractEditorActionDelegate#initializeDebugTarget()
	 */
	protected void initializeDebugTarget() {
		setDebugTarget( null );
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context != null && context instanceof IDebugElement ) {
			IDebugTarget target = ((IDebugElement)context).getDebugTarget();
			if ( target != null && (target instanceof IRunToLine || target instanceof IRunToAddress) ) {
				setDebugTarget( target );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart,
	 *      ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		IDebugTarget target = null;
		if ( part != null && part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) ) {
			if ( selection instanceof IStructuredSelection ) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IDebugElement ) {
					IDebugTarget target1 = ((IDebugElement)element).getDebugTarget();
					if ( target1 != null && (target1 instanceof IRunToLine || target1 instanceof IRunToAddress) ) {
						target = target1;
					}
				}
			}
			setDebugTarget( target );
			update();
		}
	}

	private IRunToLineTarget getRunToLineTarget() {
		if ( fRunToLineTarget == null ) {
			fRunToLineTarget = new RunToLineAdapter();
		}
		return fRunToLineTarget;
	}
}
