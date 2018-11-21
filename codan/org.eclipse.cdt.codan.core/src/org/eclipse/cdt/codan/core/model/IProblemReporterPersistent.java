/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

/**
 * IProblemReporterPersistent - interface to report problems, which are
 * persistent, ex. markers
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 */
public interface IProblemReporterPersistent extends IProblemReporter {
	/**
	 * Delete all problems associated with resource created by given checker
	 *
	 * @param resource
	 * @param checker
	 */
	public void deleteProblems(IResource resource, IChecker checker);

	/**
	 * Delete all problems associated with resource
	 *
	 * @param resource
	 */
	public void deleteProblems(IResource resource);

	/**
	 * Delete all persisted problems
	 */
	public void deleteAllProblems();
}