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


import java.util.Enumeration;

import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.internal.core.util.LRUCache;
import org.eclipse.cdt.internal.core.util.OverflowingLRUCache;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 * 
 * This class is similar to the JDT BufferManager class
 */

public class BufferManager implements IBufferFactory {

	/**
	 * An LRU cache of <code>IBuffers</code>.
	 */
	public class BufferCache extends OverflowingLRUCache {
		/**
		 * Constructs a new buffer cache of the given size.
		 */
		public BufferCache(int size) {
			super(size);
		}
		/**
		 * Constructs a new buffer cache of the given size.
		 */
		public BufferCache(int size, int overflow) {
			super(size, overflow);
		}
		/**
		 * Returns true if the buffer is successfully closed and
		 * removed from the cache, otherwise false.
		 *
		 * <p>NOTE: this triggers an external removal of this buffer
		 * by closing the buffer.
		 */
		protected boolean close(LRUCacheEntry entry) {
			IBuffer buffer= (IBuffer) entry._fValue;
			if (buffer.hasUnsavedChanges()) {
				return false;
			}
			buffer.close();
			return true;
		}
		/**
		 * Returns a new instance of the reciever.
		 */
		protected LRUCache newInstance(int size, int overflow) {
			return new BufferCache(size, overflow);
		}
	}

	protected static BufferManager DEFAULT_BUFFER_MANAGER;

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache openBuffers = new BufferCache(60);

	/**
	 * Creates a new buffer manager.
	 */
	public BufferManager() {
	}
	/**
	 * Adds a buffer to the table of open buffers.
	 */
	protected void addBuffer(IBuffer buffer) {
		openBuffers.put(buffer.getOwner(), buffer);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IBufferFactory#createBuffer(org.eclipse.cdt.core.model.IOpenable)
	 */
	public IBuffer createBuffer(IOpenable owner) {
		ICElement element = (ICElement)owner;

		IResource resource = element.getResource();
		return 
			new Buffer(
				resource instanceof IFile ? (IFile)resource : null, 
				owner, 
				element.isReadOnly());
	}
	
	/**
	 * Returns the open buffer associated with the given owner,
	 * or <code>null</code> if the owner does not have an open
	 * buffer associated with it.
	 */
	public IBuffer getBuffer(IOpenable owner) {
		return (IBuffer)openBuffers.get(owner);
	}
	/**
	 * Returns the default buffer factory.
	 */
	public IBufferFactory getDefaultBufferFactory() {
		return this;
	}
	/**
	 * Returns the default buffer manager.
	 */
	public synchronized static BufferManager getDefaultBufferManager() {
		if (DEFAULT_BUFFER_MANAGER == null) {
			DEFAULT_BUFFER_MANAGER = new BufferManager();
		}
		return DEFAULT_BUFFER_MANAGER;
	}
	/**
	 * Returns an enumeration of all open buffers.
	 * <p> 
	 * The <code>Enumeration</code> answered is thread safe.
	 *
	 * @see OverflowingLRUCache
	 * @return Enumeration of IBuffer
	 */
	public Enumeration getOpenBuffers() {
		synchronized (openBuffers) {
			openBuffers.shrink();
			return openBuffers.elements();
		}
	}
	
	
	/**
	 * Removes a buffer from the table of open buffers.
	 */
	protected void removeBuffer(IBuffer buffer) {
		openBuffers.remove(buffer.getOwner());
	}

}
