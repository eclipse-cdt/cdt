/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Instance of a problem. Intermediate representation before problem become a
 * marker.
 *
 * @since 2.0
 */
public interface ICodanProblemMarker {
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * @return problem location
	 */
	public IProblemLocation getLocation();

	/**
	 * Returns problem of which type this instance is created
	 *
	 * @return problem
	 */
	public IProblem getProblem();

	/**
	 * Resource on which this problem instance is created
	 *
	 * @return resource
	 */
	public IResource getResource();

	/**
	 * Creates a maker on a resource represented by location, which attributes
	 * that this instance carries
	 *
	 * @return marker
	 * @throws CoreException
	 */
	public IMarker createMarker() throws CoreException;

	/**
	 * Create a message by applying messagePattern from a problem to a problem
	 * arguments
	 *
	 * @return message
	 */
	public String createMessage();

	/**
	 * @return problem arguments
	 */
	public Object[] getArgs();
}