package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.IBufferChangedListener;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class Openable extends Parent implements IOpenable, IBufferChangedListener {

	protected IResource resource;	

	public Openable (ICElement parent, IPath path, int type) {
		// Check if the file is under the workspace.
		this (parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation (path),
			path.lastSegment(), type);
	}

	public Openable (ICElement parent, IResource resource, int type) {
		this (parent, resource, resource.getName(), type);
	}
	
	public Openable (ICElement parent, IResource res, String name, int type) {
		super (parent, name, type);
		resource = res;
	}

	public IResource getResource()  {
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
	public void bufferChanged(BufferChangedEvent event) {
		if (event.getBuffer().isClosed()) {
			CModelManager.getDefault().getElementsOutOfSynchWithBuffers().remove(this);
			getBufferManager().removeBuffer(event.getBuffer());
		} else {
			CModelManager.getDefault().getElementsOutOfSynchWithBuffers().put(this, this);
		}
	}	
	/**
	 * Updates the info objects for this element and all of its children by
	 * removing the current infos, generating new infos, and then placing
	 * the new infos into the C Model cache tables.
	 */
	protected void buildStructure(OpenableInfo info, HashMap newElements, IProgressMonitor monitor) throws CModelException {

		if (monitor != null && monitor.isCanceled()) return;
	
		// remove existing (old) infos
		removeInfo();
		info.setIsStructureKnown(generateInfos(info, monitor, newElements, getResource()));
		CModelManager.getDefault().getElementsOutOfSynchWithBuffers().remove(this);
		for (Iterator iter = newElements.keySet().iterator(); iter.hasNext();) {
			ICElement key = (ICElement) iter.next();
			Object value = newElements.get(key);
			CModelManager.getDefault().putInfo(key, value);
		}
		
		// add the info for this at the end, to ensure that a getInfo cannot reply null in case the LRU cache needs
		// to be flushed. Might lead to performance issues.
		CModelManager.getDefault().putInfo(this, info);	
	}

	/**
	 * Close the buffer associated with this element, if any.
	 */
	protected void closeBuffer(OpenableInfo info) {
		if (!hasBuffer()) return; // nothing to do
		IBuffer buffer = null;
		buffer = getBufferManager().getBuffer(this);
		if (buffer != null) {
			buffer.close();
			buffer.removeBufferChangedListener(this);
		}
	}

	/**
	 * This element is being closed.  Do any necessary cleanup.
	 */
	protected void closing(Object info) throws CModelException {
		if (info instanceof OpenableInfo) {
			closeBuffer((OpenableInfo)info);
		} else {
			closeBuffer(null);
		}
	}


	/**
	 * Builds this element's structure and properties in the given
	 * info object, based on this element's current contents (i.e. buffer
	 * contents if this element has an open buffer, or resource contents
	 * if this element does not have an open buffer). Children
	 * are placed in the given newElements table (note, this element
	 * has already been placed in the newElements table). Returns true
	 * if successful, or false if an error is encountered while determining
	 * the structure of this element.
	 */
	protected abstract boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException;

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws CModelException {
		if (hasBuffer()) {
			// ensure element is open
			if (!isOpen()) {
				getElementInfo();
			}
			IBuffer buffer = getBufferManager().getBuffer(this);
			if (buffer == null) {
				// try to (re)open a buffer
				buffer = openBuffer(null);
			}
			return buffer;
		} else {
			return null;
		}
	}

	/**
	 * Answers the buffer factory to use for creating new buffers
	 */
	public IBufferFactory getBufferFactory(){
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
	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() throws CModelException{
	
		if (isReadOnly() || !isOpen()) {
			return false;
		}
		IBuffer buf = this.getBuffer();
		if (buf != null && buf.hasUnsavedChanges()) {
			return true;
		}
		// for roots and projects must check open buffers
		// to see if they have an child with unsaved changes
		if (fType == C_MODEL ||
			fType == C_PROJECT) {
			Enumeration openBuffers= getBufferManager().getOpenBuffers();
			while (openBuffers.hasMoreElements()) {
				IBuffer buffer= (IBuffer)openBuffers.nextElement();
				if (buffer.hasUnsavedChanges()) {
					ICElement owner= (ICElement)buffer.getOwner();
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
	public boolean isConsistent() throws CModelException {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#isOpen()
	 */	
	public boolean isOpen() {
		return CModelManager.getDefault().getInfo(this) != null;
	}

	/**
	 * Returns true if this represents a source element.
	 * Openable source elements have an associated buffer created
	 * when they are opened.
	 */
	protected boolean isSourceElement() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#makeConsistent(IProgressMonitor)
	 */
	public void makeConsistent(IProgressMonitor pm) throws CModelException {
		makeConsistent(pm, false);
	}
	
	public void makeConsistent(IProgressMonitor pm, boolean forced) throws CModelException {
		if (!isConsistent() || forced) {
			CModelManager manager = CModelManager.getDefault();
			boolean hadTemporaryCache = manager.hasTemporaryCache();
			try {
				HashMap newElements = manager.getTemporaryCache();
				buildStructure((OpenableInfo)getElementInfo(), newElements, pm);
			} finally {
				if (!hadTemporaryCache) {
					manager.resetTemporaryCache();
				}				
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#open(IProgressMonitor)
	 */
	public void open(IProgressMonitor pm) throws CModelException {
		if (!isOpen()) {
			this.openWhenClosed(pm);
		}
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
	 * 	Open the parent element if necessary
	 * 
	 */
	protected void openParent(IProgressMonitor pm) throws CModelException {

		Openable openableParent = (Openable)getOpenableParent();
		if (openableParent != null) {
			if (!openableParent.isOpen()){
				openableParent.openWhenClosed(pm);
			}
		}
	}

	/**
	 * Open a <code>IOpenable</code> that is known to be closed (no check for
	 * <code>isOpen()</code>).
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		try {
			HashMap newElements = manager.getTemporaryCache();
			// 1) Parent must be open - open the parent if necessary
			openParent(pm);

			// 2) create the new element info and open a buffer if needed
			OpenableInfo info = (OpenableInfo) createElementInfo();
			IResource resource = getResource();
			if (resource != null && isSourceElement()) {
				this.openBuffer(pm);
			} 

			// 3) build the structure of the openable
			buildStructure(info, newElements, pm);

			//if (!hadTemporaryCache) {
			//	manager.putInfos(this, newElements);
			//}

			// if any problems occuring openning the element, ensure that it's info
			// does not remain in the cache	(some elements, pre-cache their info
			// as they are being opened).
		} catch (CModelException e) {
			CModelManager.getDefault().removeInfo(this);
			throw e;
		} finally {
			if (!hadTemporaryCache) {
				manager.resetTemporaryCache();
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#save(IProgressMonitor, boolean)
	 */
	public void save(IProgressMonitor pm, boolean force) throws CModelException {
		if (isReadOnly() || this.getResource().isReadOnly()) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
		}
		IBuffer buf = getBuffer();
		if (buf != null) { 
			buf.save(pm, force);
			this.makeConsistent(pm); // update the element info of this element
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	protected CElementInfo createElementInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof Openable) {
			IResource otherRes = ((Openable)o).getResource();
			IResource res = this.getResource();
			if (otherRes != null && res != null) {
				return otherRes.equals(res);
			}
		}
		return super.equals(o);
	}
}
