/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

/**
 * IProblemReporterPersistent - interface to report problems, which are
 * persistent, e.g. markers. Also this object has context of checker and
 * current resource, which allows to manage markers better - i.e. instead of
 * deleting replace them when needed, and queue markers for insertion instead
 * of add right away.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 *
 * @since 2.0
 */
public interface IProblemReporterSessionPersistent extends IProblemReporter {
	/**
	 * Deletes all problems associated with session resource and session checker.
	 *
	 * @param all If <code>true</code> the method also deletes all problems associated with
	 * workspace (and session checker).
	 */
	public void deleteProblems(boolean all);

	/**
	 * Notifies that session is started.
	 */
	public void start();

	/**
	 * Notifies that session is ended.
	 */
	public void done();

	IChecker getChecker();

	IResource getResource();

	/**
	 * Creates a problem reporter.
	 *
	 * @param resource
	 * @param checker
	 * @return the created reporter
	 * @since 2.0
	 */
	public IProblemReporterSessionPersistent createReporter(IResource resource, IChecker checker);
}