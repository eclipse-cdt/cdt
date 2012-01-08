/*******************************************************************************
 * Copyright (c) 2009, 2011 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.ICodanDisposable;
import org.eclipse.core.resources.IResource;

/**
 * Implementation of ICheckerInvocationContext.
 * This class is thread-safe.
 */
public class CheckerInvocationContext implements ICheckerInvocationContext {
	private final IResource resource;
	private final Map<Class<?>, Object> objectStorage;

	/**
	 * @param resource the resource this context is associated with.
	 */
	public CheckerInvocationContext(IResource resource) {
		this.resource = resource;
		objectStorage = new HashMap<Class<?>, Object>();
	}

	@Override
	public IResource getResource() {
		return resource;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(Class<T> objectClass) {
		T object = (T) objectStorage.get(objectClass);
		if (object != null)
			return object;
		for (Map.Entry<Class<?>, Object> entry : objectStorage.entrySet()) {
			if (objectClass.isAssignableFrom(entry.getKey()))
				return (T) entry.getValue();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see ICheckerInvocationContext#add(Object)
	 */
	@Override
	public synchronized <T extends ICodanDisposable> void add(T object) {
		Object old = objectStorage.put(object.getClass(), object);
		if (old != null && object != old) {
			objectStorage.put(old.getClass(), old);  // Restore old value.
			throw new IllegalArgumentException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see IDisposableCache#dispose()
	 */
	@Override
	public void dispose() {
		for (Map.Entry<Class<?>, Object> entry : objectStorage.entrySet()) {
			Object obj = entry.getValue();
			if (obj instanceof ICodanDisposable) {
				((ICodanDisposable) obj).dispose();
			}
		}
		objectStorage.clear();
	}
}
