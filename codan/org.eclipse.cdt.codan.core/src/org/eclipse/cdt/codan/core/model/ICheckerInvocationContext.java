/*******************************************************************************
 * Copyright (c) 2009, 2011 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

/**
 * Context object that can be used to store data shared between different
 * checkers operating on the same resource. The context and all objects stored
 * in it are disposed of at the end of processing of a resource. May store
 * objects of arbitrary types but only a single instance per type.
 * <p>
 * Implementations of this interface are guaranteed to be thread-safe.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 2.0
 */
public interface ICheckerInvocationContext extends ICodanDisposable {
	/**
	 * @return the resource this context is associated with.
	 */
	IResource getResource();

	/**
	 * Returns the object of the given type. Lookup by an interface or a superclass
	 * is also possible, but in case when there are multiple objects implementing
	 * the interface, an arbitrary one will be returned.
	 *
	 * @param <T> the type of the object.
	 * @param objectClass the class of the object to retrieve.
	 * @return the object of the given type, or <code>null</code> if not present in the context.
	 */
	public <T> T get(Class<T> objectClass);

	/**
	 * Adds an object to the context. The context accepts only a single
	 * instance of each class.
	 *
	 * @param <T> the type of the object.
	 * @param object the object to add to the context.
	 * @throws IllegalArgumentException if an attempt is made to add second instance
	 * 		of the same class.
	 */
	public <T extends ICodanDisposable> void add(T object);
}
