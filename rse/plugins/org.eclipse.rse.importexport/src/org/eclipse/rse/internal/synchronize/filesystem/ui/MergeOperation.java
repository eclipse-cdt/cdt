/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.ui.mapping.SynchronizationOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelMergeOperation;

public class MergeOperation extends SynchronizationOperation {

	private IMergeContext context;
	
	protected MergeOperation(ISynchronizePageConfiguration configuration, Object[] elements, IMergeContext context) {
		super(configuration, elements);
		
		this.context= context;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		new ModelMergeOperation(getPart(), ((SynchronizationContext)context).getScopeManager()) {
			public boolean isPreviewRequested() {
				return false;
			}
			protected void initializeContext(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(null, 10);
				monitor.done();
			}
			protected ISynchronizationContext getContext() {
				return context;
			}
			protected void executeMerge(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(null, 100);
				if (!hasChangesOfInterest()) {
					handleNoChanges();
				} else if (isPreviewRequested()) {
					handlePreviewRequest();
				} else {
					IStatus status = ModelMergeOperation.validateMerge(getMergeContext(), monitor);
					if (!status.isOK()) {
						if (!promptToContinue(status))
							return;
					}
					status = performMerge(monitor);
					if (!status.isOK()) {
						handleMergeFailure(status);
					}
				}
				monitor.done();
			}
			private IMergeContext getMergeContext() {
				return (IMergeContext)getContext();
			}
			private boolean promptToContinue(final IStatus status) {
		    	final boolean[] result = new boolean[] { false };
		    	Runnable runnable = new Runnable() {
					public void run() {
						ErrorDialog dialog = new ErrorDialog(getShell(), TeamUIMessages.ModelMergeOperation_0, TeamUIMessages.ModelMergeOperation_1, status, IStatus.ERROR | IStatus.WARNING | IStatus.INFO) {
							protected void createButtonsForButtonBar(Composite parent) {
						        createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL,
						                false);
								createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL,
										true);
						        createDetailsButton(parent);
							}
							/* (non-Javadoc)
							 * @see org.eclipse.jface.dialogs.ErrorDialog#buttonPressed(int)
							 */
							protected void buttonPressed(int id) {
								if (id == IDialogConstants.YES_ID)
									super.buttonPressed(IDialogConstants.OK_ID);
								else if (id == IDialogConstants.NO_ID)
									super.buttonPressed(IDialogConstants.CANCEL_ID);
								super.buttonPressed(id);
							}
						};
						int code = dialog.open();
						result[0] = code == 0;
					}
				};
				getShell().getDisplay().syncExec(runnable);
				return (result[0]);
			}
		}.run(monitor);
	}



}
