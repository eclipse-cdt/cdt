package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Commits the contents of a working copy translation unit to its original
 * element and resource, bringing the C Model up-to-date with the current
 * contents of the working copy.
 *
 * <p>It is possible that the contents of the
 * original resource have changed since the working copy was created,
 * in which case there is an update conflict. This operation allows
 * for two settings to resolve conflict set by the <code>fForce</code> flag:<ul>
 * <li>force flag is <code>false</code> - in this case a <code>CModelException</code> 	
 * is thrown</li>
 * <li>force flag is <code>true</code> - in this case the contents of
 * 	the working copy are applied to the underlying resource even though
 * 	the working copy was created before a subsequent change in the
 * 	resource</li>
 * </ul>
 *
 * <p>The default conflict resolution setting is the force flag is <code>false</code>
 *
 * A CModelOperation exception is thrown either if the commit could not be
 * performed.
 * 
 * This class is similar to the JDT CommitWorkingCopyOperation class.
 */

public class CommitWorkingCopyOperation extends CModelOperation {
	/**
	 * Constructs an operation to commit the contents of a working copy
	 * to its original translation unit.
	 */

	public CommitWorkingCopyOperation(ITranslationUnit element, boolean force) {
		super(new ICElement[] {element}, force);
	}
	
	public ISchedulingRule getSchedulingRule() {
		IResource resource = getElementToProcess().getResource();
		IWorkspace workspace = resource.getWorkspace();
		if (resource.exists()) {
			return workspace.getRuleFactory().modifyRule(resource);
		}
		return workspace.getRuleFactory().createRule(resource);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.CModelOperation#executeOperation()
	 */
	protected void executeOperation() throws CModelException {
		try {
			beginTask("workingCopy.commit", 2); //$NON-NLS-1$
			WorkingCopy copy = (WorkingCopy)getElementToProcess();
			ITranslationUnit original = copy.getOriginalElement();
		
			
			// creates the delta builder (this remembers the content of the cu)	
			if (!original.isOpen()) {
				// force opening so that the delta builder can get the old info
				original.open(null);
			}
			CElementDeltaBuilder deltaBuilder = new CElementDeltaBuilder(original);
		
			// save the cu
			IBuffer originalBuffer = original.getBuffer();
			if (originalBuffer == null) return;
			char[] originalContents = originalBuffer.getCharacters();
			boolean hasSaved = false;
			try {
				IBuffer copyBuffer = copy.getBuffer();
				if (copyBuffer == null) return;
				originalBuffer.setContents(copyBuffer.getCharacters());
				original.save(fMonitor, fForce);
				this.hasModifiedResource = true; 
				hasSaved = true;
			} finally {
				if (!hasSaved){
					// restore original buffer contents since something went wrong
					originalBuffer.setContents(originalContents);
				}
			}
			// make sure working copy is in sync
			copy.updateTimeStamp((TranslationUnit)original);
			copy.makeConsistent(this);
			worked(1);
		
			if (deltaBuilder != null) {
				// build the deltas
				deltaBuilder.buildDeltas();
			
				// add the deltas to the list of deltas created during this operation
				if (deltaBuilder.delta != null) {
					addDelta(deltaBuilder.delta);
				}
			}
			worked(1);
		} finally {	
			done();
		}		
	}
	/**
	 * Possible failures: <ul>
	 *	<li>INVALID_ELEMENT_TYPES - the Translation unit supplied to this
	 *		operation is not a working copy
	 *  <li>ELEMENT_NOT_PRESENT - the Translation unit the working copy is
	 *		based on no longer exists.
	 *  <li>UPDATE_CONFLICT - the original Translation unit has changed since
	 *		the working copy was created and the operation specifies no force
	 *  </ul>
	 */

	public ICModelStatus verify() {
		
		IWorkingCopy wc = (IWorkingCopy) getElementToProcess();
		if (!wc.isWorkingCopy()) {
			return new CModelStatus(ICModelStatusConstants.INVALID_ELEMENT_TYPES, wc);
		}
	
		ITranslationUnit original= wc.getOriginalElement();
		IResource resource = original.getResource();
		if (!wc.isBasedOn(resource) && !fForce) {
			return new CModelStatus(ICModelStatusConstants.UPDATE_CONFLICT);
		}

		// no read-only check, since some repository adapters can change the flag on save
		// operation.	
		return CModelStatus.VERIFIED_OK;
		
	}

}
