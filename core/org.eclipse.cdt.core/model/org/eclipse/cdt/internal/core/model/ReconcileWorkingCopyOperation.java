/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Reconcile a working copy and signal the changes through a delta.
 */
public class ReconcileWorkingCopyOperation extends CModelOperation {
		
	boolean forceProblemDetection;
	boolean fComputeAST;
	IASTTranslationUnit fAST;
	
	public ReconcileWorkingCopyOperation(ICElement workingCopy, boolean forceProblemDetection) {
		this(workingCopy, false, forceProblemDetection);
	}

	public ReconcileWorkingCopyOperation(ICElement workingCopy, boolean computeAST, boolean forceProblemDetection) {
		super(new ICElement[] {workingCopy});
		fComputeAST= computeAST;
		this.forceProblemDetection = forceProblemDetection;
	}

	/**
	 * @exception CModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	@Override
	protected void executeOperation() throws CModelException {
		if (fMonitor != null){
			if (fMonitor.isCanceled())
				throw new OperationCanceledException();
			fMonitor.beginTask("element.reconciling", 10); //$NON-NLS-1$
		}
	
		WorkingCopy workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		CElementDeltaBuilder deltaBuilder = null;
	
		try {
			if (!wasConsistent || forceProblemDetection || fComputeAST) {
				// create the delta builder (this remembers the current content of the tu)
				deltaBuilder = new CElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				fAST= workingCopy.makeConsistent(fComputeAST, fMonitor);

				deltaBuilder.buildDeltas();

				// register the deltas
				if (deltaBuilder.delta != null) {
					if (!wasConsistent || forceProblemDetection || deltaBuilder.delta.getAffectedChildren().length > 0) {
						addReconcileDelta(workingCopy, deltaBuilder.delta);
					}
				}
			}
			if (fMonitor != null) fMonitor.worked(2);
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
	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
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
