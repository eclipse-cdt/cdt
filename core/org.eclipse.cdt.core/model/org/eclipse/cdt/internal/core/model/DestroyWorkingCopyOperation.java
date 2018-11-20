/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;

/**
 * Destroys a working copy (remove it from its cache if it is shared)
 * and signal its removal through a delta.
 */
public class DestroyWorkingCopyOperation extends CModelOperation {

	public DestroyWorkingCopyOperation(ICElement workingCopy) {
		super(new ICElement[] { workingCopy });
	}

	/**
	 * @exception CModelException if setting the source
	 * 	of the original translation unit fails
	 */
	@Override
	protected void executeOperation() throws CModelException {
		WorkingCopy workingCopy = getWorkingCopy();
		workingCopy.close();

		// If original element is not on classpath flush it from the cache.
		ITranslationUnit originalElement = workingCopy.getOriginalElement();
		if (!workingCopy.getParent().exists()) {
			originalElement.close();
		}

		// Remove working copy from the cache if it is shared.
		IWorkingCopy wc = CModelManager.getDefault().removeSharedWorkingCopy(workingCopy.bufferFactory,
				originalElement);
		if (wc != null) {
			//System.out.println("Destroying shared working copy " + workingCopy.toStringWithAncestors());//$NON-NLS-1$
			//CModelManager.getDefault().fire(delta, ElementChangedEvent.POST_RECONCILE);
		}

		// Report C deltas
		CElementDelta delta = new CElementDelta(this.getCModel());
		delta.removed(workingCopy);
		addDelta(delta);
		removeReconcileDelta(workingCopy);
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected WorkingCopy getWorkingCopy() {
		return (WorkingCopy) getElementToProcess();
	}

	/**
	 * @see CModelOperation#isReadOnly
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}
}
