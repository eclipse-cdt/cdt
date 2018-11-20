/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Enumeration;
import java.util.Map;

import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IBufferChangedListener;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class Openable extends Parent implements IOpenable {
	protected IResource resource;

	public Openable(ICElement parent, IPath path, int type) {
		// Check if the file is under the workspace.
		this(parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path), path.lastSegment(), type);
	}

	public Openable(ICElement parent, IResource resource, int type) {
		this(parent, resource, resource.getName(), type);
	}

	public Openable(ICElement parent, IResource res, String name, int type) {
		super(parent, name, type);
		resource = res;
	}

	@Override
	public IResource getResource() {
		return resource;
	}

	/**
	 * The buffer associated with this element has changed. Registers
	 * this element as being out of synch with its buffer's contents.
	 * If the buffer has been closed, this element is set as NOT out of
	 * synch with the contents.
	 *
	 * @see IBufferChangedListener
	 */
	@Override
	public void bufferChanged(BufferChangedEvent event) {
		if (event.getBuffer().isClosed()) {
			CModelManager.getDefault().getElementsOutOfSynchWithBuffers().remove(this);
			getBufferManager().removeBuffer(event.getBuffer());
		} else {
			CModelManager.getDefault().getElementsOutOfSynchWithBuffers().put(this, this);
		}
	}

	/**
	 * Builds this element's structure and properties in the given
	 * info object, based on this element's current contents (reuse buffer
	 * contents if this element has an open buffer, or resource contents
	 * if this element does not have an open buffer). Children
	 * are placed in the given newElements table (note, this element
	 * has already been placed in the newElements table). Returns true
	 * if successful, or false if an error is encountered while determining
	 * the structure of this element.
	 */
	protected abstract boolean buildStructure(OpenableInfo info, IProgressMonitor pm,
			Map<ICElement, CElementInfo> newElements, IResource underlyingResource) throws CModelException;

	/**
	 * Closes the buffer associated with this element, if any.
	 */
	protected void closeBuffer() {
		if (!hasBuffer())
			return; // nothing to do
		IBuffer buffer = null;
		buffer = getBufferManager().getBuffer(this);
		if (buffer != null) {
			buffer.close();
			buffer.removeBufferChangedListener(this);
		}
	}

	/**
	 * Does any necessary cleanup when this element is being closed.
	 */
	@Override
	protected void closing(Object info) throws CModelException {
		closeBuffer();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 */
	@Override
	public IBuffer getBuffer() throws CModelException {
		if (hasBuffer()) {
			// Ensure element is open.
			if (!isOpen()) {
				getElementInfo();
			}
			IBuffer buffer = getBufferManager().getBuffer(this);
			if (buffer == null) {
				// Try to (re)open a buffer.
				buffer = openBuffer(null);
			}
			return buffer;
		}
		return null;
	}

	/**
	 * Returns the buffer factory to use for creating new buffers.
	 */
	public IBufferFactory getBufferFactory() {
		return getBufferManager().getDefaultBufferFactory();
	}

	/**
	 * Returns the buffer manager for this element.
	 */
	protected BufferManager getBufferManager() {
		return BufferManager.getDefaultBufferManager();
	}

	/**
	 * Returns true if this element may have an associated source buffer,
	 * otherwise false. Subclasses must override as required.
	 */
	protected boolean hasBuffer() {
		return false;
	}

	@Override
	public boolean hasUnsavedChanges() throws CModelException {
		if (isReadOnly() || !isOpen()) {
			return false;
		}
		IBuffer buf = this.getBuffer();
		if (buf != null && buf.hasUnsavedChanges()) {
			return true;
		}
		// For roots and projects must check open buffers to see
		// if they have an child with unsaved changes.
		if (fType == C_MODEL || fType == C_PROJECT) {
			Enumeration<IBuffer> openBuffers = getBufferManager().getOpenBuffers();
			while (openBuffers.hasMoreElements()) {
				IBuffer buffer = openBuffers.nextElement();
				if (buffer.hasUnsavedChanges()) {
					ICElement owner = (ICElement) buffer.getOwner();
					if (isAncestorOf(owner)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Subclasses must override as required.
	 *
	 * @see org.eclipse.cdt.core.model.IOpenable#isConsistent()
	 */
	@Override
	public boolean isConsistent() throws CModelException {
		return true;
	}

	@Override
	public boolean isOpen() {
		return CModelManager.getDefault().getInfo(this) != null;
	}

	@Override
	public boolean isStructureKnown() throws CModelException {
		CElementInfo info = (CElementInfo) CModelManager.getDefault().getInfo(this);
		return info != null && info.isStructureKnown();
	}

	/**
	 * Returns true if this represents a source element. Openable source elements have
	 * an associated buffer created when they are opened.
	 */
	protected boolean isSourceElement() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#makeConsistent(IProgressMonitor)
	 */
	@Override
	public void makeConsistent(IProgressMonitor pm) throws CModelException {
		makeConsistent(pm, false);
	}

	@Override
	public void makeConsistent(IProgressMonitor monitor, boolean forced) throws CModelException {
		// Only translation units can be inconsistent.
		// Other Openables cannot be inconsistent so default is to do nothing.
	}

	@Override
	public void open(IProgressMonitor pm) throws CModelException {
		getElementInfo(pm);
	}

	/**
	 * Opens a buffer on the contents of this element, and returns
	 * the buffer, or returns <code>null</code> if opening fails.
	 * By default, do nothing - subclasses that have buffers
	 * must override as required.
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {
		return null;
	}

	/**
	 * Open the parent element if necessary.
	 */
	protected void openParent(CElementInfo childInfo, Map<ICElement, CElementInfo> newElements, IProgressMonitor pm)
			throws CModelException {
		Openable openableParent = (Openable) getOpenableParent();
		if (openableParent != null && !openableParent.isOpen()) {
			openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
		}
	}

	@Override
	protected void generateInfos(CElementInfo info, Map<ICElement, CElementInfo> newElements, IProgressMonitor monitor)
			throws CModelException {
		if (CModelManager.VERBOSE) {
			System.out.println("OPENING Element (" + Thread.currentThread() + "): " + this); //$NON-NLS-1$//$NON-NLS-2$
		}

		// Open the parent if necessary.
		openParent(info, newElements, monitor);
		if (monitor != null && monitor.isCanceled())
			return;

		// Put the info before building the structure so that questions to the handle behave as if
		// the element existed (case of compilation units becoming working copies).
		newElements.put(this, info);

		// Build the structure of the Openable (this will open the buffer if needed).
		try {
			OpenableInfo openableInfo = (OpenableInfo) info;
			boolean isStructureKnown = buildStructure(openableInfo, monitor, newElements, getResource());
			openableInfo.setIsStructureKnown(isStructureKnown);
		} catch (CModelException e) {
			newElements.remove(this);
			throw e;
		}

		// Remove out of sync buffer for this element.
		CModelManager.getDefault().getElementsOutOfSynchWithBuffers().remove(this);
	}

	@Override
	public void save(IProgressMonitor pm, boolean force) throws CModelException {
		IResource res = getResource();
		if (res != null) {
			ResourceAttributes attributes = res.getResourceAttributes();
			if (attributes != null && attributes.isReadOnly()) {
				throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
			}
		}
		// Check also the underlying resource.
		if (isReadOnly()) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
		}
		IBuffer buf = getBuffer();
		if (buf != null) {
			buf.save(pm, force);
			this.makeConsistent(pm); // update the element info of this element
		}
	}

	/**
	 * Finds enclosing package fragment root if any.
	 */
	public SourceRoot getSourceRoot() {
		ICElement current = this;
		do {
			if (current instanceof SourceRoot)
				return (SourceRoot) current;
			current = current.getParent();
		} while (current != null);
		return null;
	}
}
