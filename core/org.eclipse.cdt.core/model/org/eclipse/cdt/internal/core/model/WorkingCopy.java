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

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementation of a working copy translation unit. A working copy maintains
 * the timestamp of the resource it was created from.
 */
public class WorkingCopy extends TranslationUnit implements IWorkingCopy {
	
	/**
	 * If set, this is the factory that will be used to create the buffer.
	 */
	protected IBufferFactory bufferFactory;
	/**
	 * A counter of the number of time clients have asked for this 
	 * working copy. It is set to 1, if the working
	 * copy is not managed. When destroyed, this counter is
	 * set to 0. Once destroyed, this working copy cannot be opened
	 * and non-handle info can not be accessed. This is
	 * never true if this translation unit is not a working copy.
	 */
	protected int useCount = 1;
	
	/**
	 * Creates a working copy of this element
	 */
	public WorkingCopy(ICElement parent, IFile file, IBufferFactory bufferFactory) {
		super(parent, file);
		this.bufferFactory = 
			bufferFactory == null ? 
				getBufferManager() :
				bufferFactory;
	}

	public WorkingCopy(ICElement parent, IPath path, IBufferFactory bufferFactory) {
		super(parent, path);
		this.bufferFactory = 
			bufferFactory == null ? 
				getBufferManager() :
				bufferFactory;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#commit(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void commit(boolean force, IProgressMonitor monitor)
		throws CModelException {
			ITranslationUnit original = (ITranslationUnit)this.getOriginalElement();
			if (original.exists()) {
				CommitWorkingCopyOperation op= new CommitWorkingCopyOperation(this, force);
				runOperation(op, monitor);
			} else {
				String contents = this.getSource();
				if (contents == null) return;
				try {
					byte[] bytes = contents.getBytes(); 
					ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
					IFile originalRes = (IFile)original.getResource();
					if (originalRes.exists()) {
						originalRes.setContents(
							stream, 
							force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
							null);
					} else {
						originalRes.create(
							stream,
							force,
							monitor);
					}
				} catch (CoreException e) {
					throw new CModelException(e);
				}
			}			
	}
	
	/**
	 * Returns a new element info for this element.
	 */
	protected CElementInfo createElementInfo() {
		return new WorkingCopyInfo(this);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#destroy()
	 */
	public void destroy() {
		if (--this.useCount > 0) {
			return;
		}
		try {
			close();
		
			// if original element is not on classpath flush it from the cache 
			ICElement originalElement = this.getOriginalElement();
			if (!this.getParent().exists()) {
				((TranslationUnit)originalElement).close();
			}
		
			// remove working copy from the cache
			CModelManager manager = CModelManager.getDefault();
		
			// In order to be shared, working copies have to denote the same compilation unit 
			// AND use the same buffer factory.
			// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
			Map sharedWorkingCopies = manager.sharedWorkingCopies;
		
			Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(this.bufferFactory);
			if (perFactoryWorkingCopies != null){
				if (perFactoryWorkingCopies.remove(originalElement) != null) {
	
					// report removed java delta
					//CElementDelta delta = new CElementDelta(this.getCoreModel());
					//delta.removed(this);
					//manager.fire(delta, CModelManager.DEFAULT_CHANGE_EVENT);
				}
			}		
		} catch (CModelException e) {
			// do nothing
		}		
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		// working copy always exists in the model until it is detroyed
		return this.useCount != 0;
	}


	/**
	 * Answers custom buffer factory
	 */
	public IBufferFactory getBufferFactory(){

		return this.bufferFactory;
	}

	/**
	 * Working copies must be identical to be equal.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		return this == o; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#getOriginalElement()
	 */
	public ITranslationUnit getOriginalElement() {
		return new TranslationUnit(getParent(), getFile());
	}

	/**
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(IProgressMonitor, IBufferFactory)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory)
		throws CModelException{
			return this;		
	}
	/**
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy()
	 */	
	public IWorkingCopy getWorkingCopy() {
		return this;
	}

	/**
	 * @see IWorkingCopy
	 */
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory){
		return this;
	}


	/**
	 * @see IWorkingCopy
	 */
	public boolean isBasedOn(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return false;
		}
		if (this.useCount == 0) {
			return false;
		}
		// if resource got deleted, then #getModificationStamp() will answer IResource.NULL_STAMP, which is always different from the cached
		// timestamp
		return ((TranslationUnitInfo) getElementInfo()).fTimestamp == ((IFile) resource).getModificationStamp();
	}
	/**
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return true;
	}

	/**
	 * @see ICFile
	 * @see IWorkingCopy
	 *
	 * @exception CModelException attempting to open a read only element for
	 * something other than navigation 	or if this is a working copy being
	 * opened after it has been destroyed.
	 */
	public void open(IProgressMonitor pm) throws CModelException {
		if (this.useCount == 0) { // was destroyed
			throw newNotPresentException();
		} else {
			super.open(pm);
		}
	}
	/**
	 * @see org.eclipse.cdt.internal.core.model.CFile#openBuffer(IProgressMonitor)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {

		if (this.useCount == 0) throw newNotPresentException();
	
		// create buffer - working copies may use custom buffer factory
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null) 
			return null;

		// set the buffer source if needed
		if (buffer.getCharacters() == null){
			ITranslationUnit original= (ITranslationUnit)this.getOriginalElement();
			IBuffer originalBuffer = null;
			try {
				originalBuffer = original.getBuffer();
			} catch (CModelException e) {
				// original element does not exist: create an empty working copy
				if (!e.getCModelStatus().doesNotExist()) {
					throw e;
				}
			}
			if (originalBuffer != null) {
				char[] originalContents = originalBuffer.getCharacters();
				if (originalContents != null) {
					buffer.setContents((char[])originalContents.clone());
				}
			} else {
				// initialize buffer
				buffer.setContents(new char[0]);
			}
		}

		// add buffer to buffer cache
		this.getBufferManager().addBuffer(buffer);

		// listen to buffer changes
		buffer.addBufferChangedListener(this);

		return buffer;	
	}
	

	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#reconcile()
	 */
	public IMarker[] reconcile() throws CModelException {
		reconcile(false, null);
		return null;
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#reconcile(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean reconcile(boolean forceProblemDetection, IProgressMonitor monitor)
		throws CModelException {
			
		boolean somethingChanged = false;
		
		if (this.useCount == 0) throw newNotPresentException(); //was destroyed

		if (monitor != null){
			if (monitor.isCanceled()) return somethingChanged;
			monitor.beginTask("element.reconciling", 10); //$NON-NLS-1$
		}

		boolean wasConsistent = isConsistent();
		CElementDeltaBuilder deltaBuilder = null;

		try {
			// create the delta builder (this remembers the current content of the cu)
			if (!wasConsistent){
				deltaBuilder = new CElementDeltaBuilder(this);
		
				// update the element infos with the content of the working copy
				this.makeConsistent(monitor);
				deltaBuilder.buildDeltas();
				somethingChanged = true;
			}

			if (monitor != null) monitor.worked(2);
	
			// force problem detection? - if structure was consistent
			if (forceProblemDetection && wasConsistent){
				if (monitor != null && monitor.isCanceled()) return somethingChanged;

				//IProblemRequestor problemRequestor = this.getProblemRequestor();
				//if (problemRequestor != null && problemRequestor.isActive()){
				//	problemRequestor.beginReporting();
				//	CompilationUnitProblemFinder.process(this, problemRequestor, monitor);
				//	problemRequestor.endReporting();
				//}
			}
	
			// fire the deltas
			//if (deltaBuilder != null){
			//	if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
			//		CModelManager.getDefault().fire(deltaBuilder.delta, ElementChangedEvent.POST_RECONCILE);
			//	}
			//}
		} finally {
			if (monitor != null) monitor.done();
		}
		return somethingChanged;	
	}
	/**
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#restore()
	 */
	public void restore() throws CModelException{
		if (this.useCount == 0) throw newNotPresentException(); //was destroyed

		TranslationUnit original = (TranslationUnit) getOriginalElement();
		IBuffer buffer = this.getBuffer();
		if (buffer == null) return;
		buffer.setContents(original.getContents());
		updateTimeStamp(original);
		makeConsistent(null);		
	}
	/**
	 * @see org.eclipse.cdt.core.model.ICFile#save(IProgressMonitor, boolean)
	 */
	public void save(IProgressMonitor pm, boolean force) throws CModelException {
		if (isReadOnly()) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
		}
		// computes fine-grain deltas in case the working copy is being reconciled already (if not it would miss one iteration of deltas).
		this.reconcile();   
	}
	/**
	 * @param original
	 * @throws CModelException
	 */
	protected void updateTimeStamp(TranslationUnit original) throws CModelException {
		long timeStamp =
			((IFile) original.getResource()).getModificationStamp();
		if (timeStamp == IResource.NULL_STAMP) {
			throw new CModelException(
				new CModelStatus(ICModelStatusConstants.INVALID_RESOURCE));
		}
		((TranslationUnitInfo) getElementInfo()).fTimestamp = timeStamp;
	}

}
