/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;

/**
 * Reconcile a working copy and signal the changes through a delta.
 */
public class ReconcileWorkingCopyOperation extends CModelOperation {
		
	boolean forceProblemDetection;
	
	public ReconcileWorkingCopyOperation(ICElement workingCopy, boolean forceProblemDetection) {
		super(new ICElement[] {workingCopy});
		this.forceProblemDetection = forceProblemDetection;
	}
	/**
	 * @exception CModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws CModelException {
		if (fMonitor != null){
			if (fMonitor.isCanceled()) return;
			fMonitor.beginTask("element.reconciling", 10); //$NON-NLS-1$
		}
	
		WorkingCopy workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		CElementDeltaBuilder deltaBuilder = null;
	
		try {
			// create the delta builder (this remembers the current content of the cu)
			if (!wasConsistent){
				deltaBuilder = new CElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				workingCopy.makeConsistent(fMonitor);
				deltaBuilder.buildDeltas();

				// register the deltas
				if (deltaBuilder != null){
					if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
						addReconcileDelta(workingCopy, deltaBuilder.delta);
					}
				}

			}
	
			if (fMonitor != null) fMonitor.worked(2);
			
			// force problem detection? - if structure was consistent
			if (forceProblemDetection && wasConsistent){
				if (fMonitor != null && fMonitor.isCanceled()) return;		
			}
			
		} finally {
			if (fMonitor != null) fMonitor.done();
		}
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected WorkingCopy getWorkingCopy() {
		return (WorkingCopy)getElementToProcess();
	}
	/**
	 * @see CModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}

	protected ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		WorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy.useCount == 0) {
			return new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, workingCopy); //was destroyed
		}
		return status;
	}


}
