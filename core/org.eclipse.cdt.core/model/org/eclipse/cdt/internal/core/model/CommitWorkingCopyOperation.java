/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
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
			WorkingCopy wc = (WorkingCopy)getElementToProcess();
			ITranslationUnit tu = wc.getOriginalElement();
		
			
			// creates the delta builder (this remembers the content of the cu)	
			if (!tu.isOpen()) {
				// force opening so that the delta builder can get the old info
				tu.open(null);
			}
			CElementDeltaBuilder deltaBuilder = new CElementDeltaBuilder(tu);
		
			// save the translation unit
            boolean hasSaved = false;
            IBuffer tuBuffer = tu.getBuffer();
            IBuffer wcBuffer = wc.getBuffer();
            if (wcBuffer == null || tuBuffer == null) {
                return;
            }
            ITextFileBuffer tuFileBuffer= null;
            ITextFileBuffer wcFileBuffer= null;
            if (tuBuffer instanceof IAdaptable) {
                tuFileBuffer= (ITextFileBuffer) ((IAdaptable) tuBuffer).getAdapter(ITextFileBuffer.class);
            }
            if (wcBuffer instanceof IAdaptable) {
                wcFileBuffer= (ITextFileBuffer) ((IAdaptable) wcBuffer).getAdapter(ITextFileBuffer.class);
            }
            
            if (wcFileBuffer != null) {
                if (wcFileBuffer.equals(tuFileBuffer)) {
                    // working on the same buffer, saving the translation unit does the trick.
                    tu.save(fMonitor, fForce);
                    hasSaved= true;
                }
                else {
                    if (wcFileBuffer.getLocation().equals(tu.getPath())) {
                        char[] originalContents = tuBuffer.getCharacters();
                        try {
                            // save the file buffer of the working copy.
                            wcFileBuffer.commit(fMonitor, fForce);
                            // change the buffer of the translation unit.
                            tuBuffer.setContents(wcBuffer.getCharacters());
                            tu.makeConsistent(null);
                            hasSaved= true;
                        } catch (CoreException e) {
                            tuBuffer.setContents(originalContents);
                            throw new CModelException(e);
                        }
                    }
                }
            }
                
            if (!hasSaved) {
                char[] originalContents = tuBuffer.getCharacters();
                try {
                    tuBuffer.setContents(wcBuffer.getCharacters());
                    tu.save(fMonitor, fForce);
                } catch (CModelException e) {
                    tuBuffer.setContents(originalContents);
                    throw e;
                }
            }
            this.hasModifiedResource = true; 
            // make sure working copy is in sync
            wc.updateTimeStamp((TranslationUnit)tu);
            wc.makeConsistent(this);

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
