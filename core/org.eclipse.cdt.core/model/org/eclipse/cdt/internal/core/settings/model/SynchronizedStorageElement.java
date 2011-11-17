/*******************************************************************************
 * Copyright (c) 2008, 2009 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of ICStorageElement which wraps another ICStorageElement
 * in synchronization.  This is similar to the Collections.synchronized
 * methods.
 *
 * If two threads structurally modify the same part of the tree,
 * then this may still fail.
 *
 * This is used by CConfigBaseDescriptor to allow multiple threads
 * to modify the same storage elements (mostly) safely.  See the associated
 * tests in the CDescriptor testsuite
 *
 * TODO best effort concurrent modification exception
 */
public class SynchronizedStorageElement implements ICStorageElement {

	private final Object fLock;
	private final ICStorageElement fEl;

	private SynchronizedStorageElement(ICStorageElement el, Object lock) {
		this.fEl = el;
		this.fLock = lock;
	}

	private SynchronizedStorageElement(ICStorageElement el) {
		this.fEl = el;
		this.fLock = this;
	}

	/**
	 * Return the original ICStorageElement stored backing this Proxy
	 * ICStorageElement
	 * @return ICStorageElement original storage element
	 */
	public ICStorageElement getOriginalElement() {
		synchronized (fLock) {
			return fEl;
		}
	}

	/**
	 * Create a synchronized storage element from an existing storage element
	 * All access to this ICStorageElement tree will be synchronized on the
	 * returned ICStorageElement object.
	 * @param el
	 * @return SynchronizedStorageElement wrapping the original ICStorageElement
	 */
	public static SynchronizedStorageElement synchronizedElement(ICStorageElement el) {
		return new SynchronizedStorageElement(el);
	}

	/**
	 * Create a synchronized storage element from an existing storage element
	 * All access to this ICStorageElement tree will be synchronized on the
	 * returned ICStorageElement object.
	 * @param el
	 * @param lock
	 * @return SynchronizedStorageElement wrapping the original ICStorageElement
	 */
	public static SynchronizedStorageElement synchronizedElement(ICStorageElement el, Object lock) {
		return new SynchronizedStorageElement(el, lock);
	}

	/**
	 * @return the lock used to synchronize this SynchronizedStorage and its children
	 */
	public Object lock() {
		return fLock;
	}


	@Override
	public void clear() {
		synchronized (fLock) {
			fEl.clear();
		}
	}

	@Override
	public ICStorageElement createChild(String name) {
		synchronized (fLock) {
			return new SynchronizedStorageElement(fEl.createChild(name), fLock);
		}
	}

	@Override
	public ICStorageElement createCopy() throws UnsupportedOperationException,
			CoreException {
		synchronized (fLock) {
			return synchronizedElement(fEl.createCopy());
		}
	}

	@Override
	public boolean equals(ICStorageElement other) {
		synchronized (fLock) {
			if (other instanceof SynchronizedStorageElement)
				other = ((SynchronizedStorageElement)other).fEl;
			return fEl.equals(other);
		}
	}

	@Override
	public String getAttribute(String name) {
		synchronized (fLock) {
			return fEl.getAttribute(name);
		}
	}

	@Override
	public String[] getAttributeNames() {
		synchronized (fLock) {
			return fEl.getAttributeNames();
		}
	}

	@Override
	public ICStorageElement[] getChildren() {
		synchronized (fLock) {
			return createSynchronizedChildren(fEl.getChildren());
		}
	}

	@Override
	public ICStorageElement[] getChildrenByName(String name) {
		synchronized (fLock) {
			return createSynchronizedChildren(fEl.getChildrenByName(name));
		}
	}

	private ICStorageElement[] createSynchronizedChildren(ICStorageElement[] children) {
		ICStorageElement[] synchChildren = new ICStorageElement[children.length];
		for (int i = 0; i < children.length; i++)
			synchChildren[i] = new SynchronizedStorageElement(children[i], fLock);
		return synchChildren;
	}

	@Override
	public String getName() {
		synchronized (fLock) {
			return fEl.getName();
		}
	}

	@Override
	public ICStorageElement getParent() {
		synchronized (fLock) {
			if (fEl.getParent() == null ||
					fEl.getParent() instanceof SynchronizedStorageElement)
				return fEl.getParent();
			return new SynchronizedStorageElement(fEl.getParent(), fLock);
		}
	}

	@Override
	public String getValue() {
		synchronized (fLock) {
			return fEl.getValue();
		}
	}

	@Override
	public boolean hasAttribute(String name) {
		synchronized (fLock) {
			return fEl.hasAttribute(name);
		}
	}

	@Override
	public boolean hasChildren() {
		synchronized (fLock) {
			return fEl.hasChildren();
		}
	}

	@Override
	public ICStorageElement importChild(ICStorageElement el)
			throws UnsupportedOperationException {
		synchronized (fLock) {
			return new SynchronizedStorageElement(el.importChild(el), fLock);
		}
	}

	@Override
	public void removeAttribute(String name) {
		synchronized (fLock) {
			fEl.removeAttribute(name);
		}
	}

	@Override
	public void removeChild(ICStorageElement el) {
		synchronized (fLock) {
			if (el instanceof SynchronizedStorageElement)
				el = ((SynchronizedStorageElement)el).fEl;
			fEl.removeChild(el);
		}
	}

	@Override
	public void setAttribute(String name, String value) {
		synchronized (fLock) {
			fEl.setAttribute(name, value);
		}
	}

	@Override
	public void setValue(String value) {
		synchronized (fLock) {
			fEl.setValue(value);
		}
	}

}
