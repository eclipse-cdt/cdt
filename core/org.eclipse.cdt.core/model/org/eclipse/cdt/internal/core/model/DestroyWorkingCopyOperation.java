/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Map;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CModelException;

/**
 * Destroys a working copy (remove it from its cache if it is shared)
 * and signal its removal through a delta.
 */
public class DestroyWorkingCopyOperation extends CModelOperation {
	
	public DestroyWorkingCopyOperation(ICElement workingCopy) {
		super(new ICElement[] {workingCopy});
	}

	/**
	 * @exception CModelException if setting the source
	 * 	of the original translation unit fails
	 */
	protected void executeOperation() throws CModelException {

		WorkingCopy workingCopy = getWorkingCopy();
		workingCopy.close();
		
		// if original element is not on classpath flush it from the cache
		ICElement originalElement = workingCopy.getOriginalElement();
		if (!workingCopy.getParent().exists()) {
			((TranslationUnit)originalElement).close();
		}
		
		// remove working copy from the cache if it is shared
		CModelManager manager = CModelManager.getDefault();
		
		// In order to be shared, working copies have to denote the same compilation unit
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = manager.sharedWorkingCopies;
		
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(workingCopy.bufferFactory);
		if (perFactoryWorkingCopies != null) {
			if (perFactoryWorkingCopies.remove(originalElement) != null) {
				//System.out.println("Destroying shared working copy " + workingCopy.toStringWithAncestors());//$NON-NLS-1$
				//CModelManager.getDefault().fire(delta, ElementChangedEvent.POST_RECONCILE);
			}
		}
		
		// report C deltas
		CElementDelta delta = new CElementDelta(this.getCModel());
		delta.removed(workingCopy);
		addDelta(delta);
		removeReconcileDelta(workingCopy);
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
}
