/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.CModelException;

/**
 * An operation created as a result of a call to JavaCore.run(IWorkspaceRunnable, IProgressMonitor)
 * that encapsulates a user defined IWorkspaceRunnable.
 */
public class BatchOperation extends CModelOperation {
	protected IWorkspaceRunnable runnable;
	public BatchOperation(IWorkspaceRunnable runnable) {
		this.runnable = runnable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#executeOperation()
	 */
	protected void executeOperation() throws CModelException {
		try {
			this.runnable.run(fMonitor);
		} catch (CoreException ce) {
			if (ce instanceof CModelException) {
				throw (CModelException)ce;
			}
			if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
				Throwable e= ce.getStatus().getException();
				if (e instanceof CModelException) {
					throw (CModelException) e;
				}
			}
			throw new CModelException(ce);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#verify()
	 */
	protected ICModelStatus verify() {
		// cannot verify user defined operation
		return CModelStatus.VERIFIED_OK;
	}

	
}
