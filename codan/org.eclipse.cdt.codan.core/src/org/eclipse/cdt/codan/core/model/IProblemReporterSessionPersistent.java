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
 * persistent, ex. markers. Also this object has context of checker and
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
	 * Delete all problems associated with session resource and session checker.
	 * If "all" is true also delete all problems associated with workspace (and
	 * session checker)
	 * 
	 */
	public void deleteProblems(boolean all);

	/**
	 * Notify that session is started
	 */
	public void start();

	/**
	 * Notify that session is
	 * ended
	 */
	public void done();

	IChecker getChecker();

	IResource getResource();

	/**
	 * Create an instance of the object.This is a bit ugly since implemented has
	 * to combine
	 * object itself and factory to this object.
	 * 
	 * @param resource
	 * @param checker
	 * @return
	 * @since 2.0
	 */
	public IProblemReporterSessionPersistent createReporter(IResource resource,
			IChecker checker);
}