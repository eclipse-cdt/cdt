/*******************************************************************************
 * Copyright (c) 2007, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;


import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Invoked when user right clicks on an element in the Variables or Expressions
 * view and selects 'Add Watchpoint (C/C++)'
 */
public class AddWatchpointOnVariableActionDelegate extends AddWatchpointActionDelegate implements IObjectActionDelegate {

	/** The target variable/expression */
	private ICWatchpointTarget fVar;
	
	/** The view where fVar was selected */ 
	private IWorkbenchPart fActivePart;
	
	/**
	 * Constructor
	 */
	public AddWatchpointOnVariableActionDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fActivePart = targetPart;
	}

	private static class GetSizeRequest extends CRequest implements ICWatchpointTarget.GetSizeRequest {
		int fSize = -1;
		@Override
		public int getSize() {
			return fSize;
		}
		@Override
		public void setSize(int size) {
			fSize = size;
		}
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AddWatchpointActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (fVar == null) {
			return;
		}
		
		final String expr = fVar.getExpression();
		if (expr == null) {
			assert false : "how are we getting an empty expression?"; //$NON-NLS-1$
			return;
		}

		// Getting the size of the variable/expression is an asynchronous
		// operation...or at least the API is (the CDI implementation reacts
		// synchronously)
		final ICWatchpointTarget.GetSizeRequest request = new GetSizeRequest() {
			@Override
			public void done() {
				if (isSuccess()) {
					// Now that we have the size, put up a dialog to create the watchpoint
					final int size = getSize();
					assert size > 0 : "unexpected variale/expression size"; //$NON-NLS-1$
					WorkbenchJob job = new WorkbenchJob("open watchpoint dialog") { //$NON-NLS-1$
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							AddWatchpointDialog dlg = new AddWatchpointDialog(CDebugUIPlugin.getActiveWorkbenchShell(), 
									getMemorySpaceManagement());
							dlg.setExpression(expr);
							dlg.initializeRange(false, Integer.toString(size));
							if (dlg.open() == Window.OK) {
								addWatchpoint(dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression(), dlg.getMemorySpace(), dlg.getRange());
							}
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();
				}
				else  {
					WorkbenchJob job = new WorkbenchJob("watchpoint error") { //$NON-NLS-1$
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if (fActivePart != null) {
								ErrorDialog.openError( fActivePart.getSite().getWorkbenchWindow().getShell(), ActionMessages.getString( "AddWatchpointOnVariableActionDelegate.Error_Dlg_Title" ), ActionMessages.getString( "AddWatchpointOnVariableActionDelegate.No_Element_Size" ), getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
							}
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();
				}
			}
		};
		fVar.getSize(request);
	}
	
	private class CanCreateWatchpointRequest extends CRequest implements ICWatchpointTarget.CanCreateWatchpointRequest {
		boolean fCanCreate;
		@Override
		public boolean getCanCreate() {
			return fCanCreate;
		}
		@Override
		public void setCanCreate(boolean value) {
			fCanCreate = value;
		}
	};

	/**
	 * Record the target variable/expression
	 * 
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction action, ISelection selection) {
		fVar = null;
		if (selection == null || selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		if (selection instanceof TreeSelection) {
			Object obj = ((TreeSelection)selection).getFirstElement();
			fVar = (ICWatchpointTarget)DebugPlugin.getAdapter(obj, ICWatchpointTarget.class);
			if (fVar != null) {
				final ICWatchpointTarget.CanCreateWatchpointRequest request = new CanCreateWatchpointRequest() {
					@Override
					public void done() {
						action.setEnabled(getCanCreate());
					}
				};
				fVar.canSetWatchpoint(request);
				return;
			}
			assert false : "action should not have been available for object " + obj; //$NON-NLS-1$
		}
		else if (selection instanceof StructuredSelection) {
			// Not sure why, but sometimes we get an extraneous empty StructuredSelection. Seems harmless enough
			assert ((StructuredSelection)selection).getFirstElement() == null : "action installed in unexpected type of view/part"; //$NON-NLS-1$
		}
		else {
			assert false : "action installed in unexpected type of view/part"; //$NON-NLS-1$
		}
		action.setEnabled(false);
	}
}
