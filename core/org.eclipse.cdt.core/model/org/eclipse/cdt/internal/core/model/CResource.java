package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICResource;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class CResource extends Parent implements ICResource {
	
	public CResource (ICElement parent, IPath path, int type) {
		// Check if the file is under the workspace.
		this (parent, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation (path),
			path.lastSegment(), type);
	}

	public CResource (ICElement parent, IResource resource, int type) {
		this (parent, resource, resource.getName(), type);
	}
	
	public CResource (ICElement parent, IResource resource, String name, int type) {
		super (parent, resource, name, type);
	}

	public IResource getUnderlyingResource() throws CModelException {
		return resource;
	}

	public IResource getResource() throws CModelException {
		return resource;
	}
	
	protected abstract CElementInfo createElementInfo ();
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
	protected void buildStructure(CResourceInfo info, IProgressMonitor monitor) throws CModelException {

		if (monitor != null && monitor.isCanceled()) return;
	
		// remove existing (old) infos
		removeInfo();
		HashMap newElements = new HashMap(11);
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
	protected void closeBuffer(CFileInfo info) {
		if (!hasBuffer()) return; // nothing to do
		IBuffer buffer = null;
		buffer = getBufferManager().getBuffer(this);
		if (buffer != null) {
			buffer.close();
			buffer.removeBufferChangedListener(this);
		}
	}
	/**
	 * Derived classes may override.
	 */
	protected boolean generateInfos(CResourceInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException{
		return false;
	}
	/**
	 * @see org.eclipse.cdt.core.model.ICOpenable#getBuffer()
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
	 * @see org.eclipse.cdt.core.model.ICOpenable#hasUnsavedChanges()
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
		if (fType == C_ROOT ||
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
	 * @see org.eclipse.cdt.core.model.ICOpenable#isConsistent()
	 */
	public boolean isConsistent() throws CModelException {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICOpenable#isOpen()
	 */	
	public boolean isOpen() {
		synchronized(CModelManager.getDefault()){
			return CModelManager.getDefault().getInfo(this) != null;
		}
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
	 * @see org.eclipse.cdt.core.model.ICOpenable#makeConsistent(IProgressMonitor)
	 */
	public void makeConsistent(IProgressMonitor pm) throws CModelException {
		if (!isConsistent()) {
			buildStructure((CFileInfo)getElementInfo(), pm);
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICOpenable#open(IProgressMonitor)
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

		CResource openableParent = (CResource)getOpenableParent();
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
		try {
			
			// 1) Parent must be open - open the parent if necessary
			openParent(pm);

			// 2) create the new element info and open a buffer if needed
			CResourceInfo info = (CResourceInfo) createElementInfo();
			IResource resource = getResource();
			if (resource != null && isSourceElement()) {
				this.openBuffer(pm);
			} 

			// 3) build the structure of the openable
			buildStructure(info, pm);
		
			// if any problems occuring openning the element, ensure that it's info
			// does not remain in the cache	(some elements, pre-cache their info
			// as they are being opened).
		} catch (CModelException e) {
			CModelManager.getDefault().removeInfo(this);
			throw e;
		}
	}

	/**
	 * @see org.eclipse.cdt.core.model.ICOpenable#save(IProgressMonitor, boolean)
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
		
}
