/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
	 * A counter of the number of time clients have asked for this working copy.
	 * It is set to 1, if the working copy is not managed. When destroyed, this
	 * counter is set to 0. Once destroyed, this working copy cannot be opened
	 * and non-handle info can not be accessed. This is never true if this
	 * translation unit is not a working copy.
	 */
	protected int useCount = 1;

	/**
	 * Creates a working copy of this element.
	 */
	public WorkingCopy(ICElement parent, IFile file, String id, IBufferFactory bufferFactory) {
		this(parent, file, id, bufferFactory, null);
	}

	public WorkingCopy(ICElement parent, IFile file, String id, IBufferFactory bufferFactory,
			IProblemRequestor requestor) {
		super(parent, file, id);
		this.bufferFactory = bufferFactory == null ? getBufferManager() : bufferFactory;
		problemRequestor = requestor;
	}

	public WorkingCopy(ICElement parent, URI uri, String id, IBufferFactory bufferFactory) {
		super(parent, uri, id);
		this.bufferFactory = bufferFactory == null ? getBufferManager() : bufferFactory;
	}

	@Override
	public void commit(boolean force, IProgressMonitor monitor) throws CModelException {
		ITranslationUnit original = this.getOriginalElement();
		if (original.exists()) {
			CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this, force);
			op.runOperation(monitor);
		} else {
			IBuffer buffer = getBuffer();
			String contents = buffer.getContents();
			if (contents == null)
				return;

			try {
				IFile originalRes = (IFile) original.getResource();
				String encoding = null;
				try {
					encoding = originalRes.getCharset();
				} catch (CoreException e) {
					// Use no encoding.
				}
				byte[] bytes = encoding == null ? contents.getBytes() : contents.getBytes(encoding);
				ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
				if (originalRes.exists()) {
					int updateFlags = force ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY;
					originalRes.setContents(stream, updateFlags, monitor);
				} else {
					originalRes.create(stream, force, monitor);
				}
			} catch (IOException e) {
				throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
			} catch (CoreException e) {
				throw new CModelException(e);
			}
		}
	}

	@Override
	public void destroy() {
		if (--this.useCount > 0) {
			return;
		}
		try {
			problemRequestor = null;
			DestroyWorkingCopyOperation op = new DestroyWorkingCopyOperation(this);
			op.runOperation(null);
		} catch (CModelException e) {
			// Do nothing.
		}
	}

	@Override
	public boolean exists() {
		// Working copy always exists in the model until it is destroyed.
		return this.useCount != 0;
	}

	/**
	 * Returns custom buffer factory
	 */
	@Override
	public IBufferFactory getBufferFactory() {
		return this.bufferFactory;
	}

	/**
	 * Working copies must be identical to be equal.
	 *
	 * @see Object#equals
	 */
	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	/**
	 * Returns the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element.
	 *
	 * @param workingCopyElement the specified working copy element
	 * @return the original element the specified working copy element was created from,
	 * or <code>null</code> if this is not a working copy element
	 */
	@Override
	public ICElement getOriginal(ICElement workingCopyElement) {
		// It has to come from the same workingCopy, meaning ours.
		if (workingCopyElement instanceof ISourceReference) {
			ITranslationUnit wunit = ((ISourceReference) workingCopyElement).getTranslationUnit();
			if (!wunit.equals(this)) {
				return null;
			}
		} else {
			return null;
		}
		ITranslationUnit tu = getOriginalElement();
		if (tu == null) {
			return null; // oops !!
		}

		// Look for it.
		ICElement element = workingCopyElement;
		ArrayList<ICElement> children = new ArrayList<>();
		while (element != null && element.getElementType() != ICElement.C_UNIT) {
			children.add(element);
			element = element.getParent();
		}
		ICElement current = tu;
		for (int i = children.size(); --i >= 0;) {
			ICElement child = children.get(i);
			if (current instanceof IParent) {
				try {
					ICElement[] celems = ((IParent) current).getChildren();
					current = null;
					for (int j = 0; j < celems.length; ++j) {
						if (celems[j].getElementName().equals(child.getElementName())
								&& celems[j].getElementType() == child.getElementType()) {
							current = celems[j];
							break;
						}
					}
				} catch (CModelException e) {
					current = null;
				}
			} else {
				current = null;
			}
		}
		return current;
	}

	@Override
	public ITranslationUnit getOriginalElement() {
		IFile file = getFile();
		if (file != null) {
			return new TranslationUnit(getParent(), getFile(), getContentTypeId());
		}
		return new ExternalTranslationUnit(getParent(), getLocationURI(), getContentTypeId());
	}

	@Override
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IProblemRequestor requestor) {
		return this;
	}

	@Override
	public IWorkingCopy getWorkingCopy() {
		return this;
	}

	@Override
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor) {
		return this;
	}

	@Override
	public boolean isBasedOn(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return false;
		}
		if (this.useCount == 0) {
			return false;
		}
		try {
			// If resource got deleted, then getModificationStamp() will answer
			// IResource.NULL_STAMP, which is always different from the cached timestamp.
			return ((TranslationUnitInfo) getElementInfo()).fTimestamp == ((IFile) resource).getModificationStamp();
		} catch (CModelException e) {
			return false;
		}
	}

	@Override
	public boolean isWorkingCopy() {
		return true;
	}

	/**
	 * @see ITranslationUnit
	 * @see IWorkingCopy
	 *
	 * @exception CModelException attempting to open a read only element for
	 *     something other than navigation, or if this is a working copy being
	 *     opened after it has been destroyed.
	 */
	@Override
	public void open(IProgressMonitor monitor) throws CModelException {
		if (this.useCount == 0) { // was destroyed
			throw newNotPresentException();
		}
		super.open(monitor);
		//if (monitor != null && monitor.isCanceled()) return;
		//if (this.problemRequestor != null && this.problemRequestor.isActive()) {
		//	this.problemRequestor.beginReporting();
		//	TranslationUnitProblemFinder.process(this, this.problemRequestor, monitor);
		//	this.problemRequestor.endReporting();
		//}
	}

	@Override
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {
		if (this.useCount == 0)
			throw newNotPresentException();

		// Create buffer - working copies may use custom buffer factory.
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null)
			return null;

		// Set the buffer source if needed.
		if (buffer.getContents() == null) {
			ITranslationUnit original = this.getOriginalElement();
			IBuffer originalBuffer = null;
			try {
				originalBuffer = original.getBuffer();
			} catch (CModelException e) {
				// Original element does not exist: create an empty working copy.
				if (!e.getCModelStatus().doesNotExist()) {
					throw e;
				}
			}
			if (originalBuffer != null) {
				char[] originalContents = originalBuffer.getCharacters();
				if (originalContents != null) {
					buffer.setContents(originalContents.clone());
				}
			} else {
				// Initialize buffer.
				buffer.setContents(new char[0]);
			}
		}

		// Add buffer to buffer cache.
		this.getBufferManager().addBuffer(buffer);

		// Listen to buffer changes.
		buffer.addBufferChangedListener(this);

		return buffer;
	}

	@Override
	public IMarker[] reconcile() throws CModelException {
		reconcile(false, null);
		return null;
	}

	@Override
	public void reconcile(boolean forceProblemDetection, IProgressMonitor monitor) throws CModelException {
		reconcile(false, forceProblemDetection, monitor);
	}

	@Override
	public void restore() throws CModelException {
		if (this.useCount == 0)
			throw newNotPresentException(); // Was destroyed.

		TranslationUnit original = (TranslationUnit) getOriginalElement();
		IBuffer buffer = this.getBuffer();
		if (buffer == null)
			return;
		buffer.setContents(original.getContents());
		updateTimeStamp(original);
		makeConsistent(null);
	}

	@Override
	public void save(IProgressMonitor pm, boolean force) throws CModelException {
		if (isReadOnly()) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
		}
		// Compute fine-grain deltas in case the working copy is being reconciled already
		// (if not it would miss one iteration of deltas).
		this.reconcile();
	}

	protected void updateTimeStamp(TranslationUnit original) throws CModelException {
		long timeStamp = ((IFile) original.getResource()).getModificationStamp();
		if (timeStamp == IResource.NULL_STAMP) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.INVALID_RESOURCE));
		}
		((TranslationUnitInfo) getElementInfo()).fTimestamp = timeStamp;
	}

	@Override
	public IASTTranslationUnit reconcile(boolean computeAST, boolean forceProblemDetection, IProgressMonitor monitor)
			throws CModelException {
		if (this.useCount == 0)
			throw newNotPresentException(); // Was destroyed.

		ReconcileWorkingCopyOperation op = new ReconcileWorkingCopyOperation(this, computeAST, forceProblemDetection);
		op.runOperation(monitor);
		return op.fAST;
	}
}
