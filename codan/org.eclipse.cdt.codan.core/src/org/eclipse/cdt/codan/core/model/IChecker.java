/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Interface that checker must implement (through extending directly or
 * indirectly {@link AbstractChecker}.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *              Extend {@link AbstractChecker} class instead.
 */
public interface IChecker {
	/**
	 * Called before processing a resource.
	 *
	 * @param resource the resource that is about to be processed.
	 * @since 2.0
	 */
	void before(IResource resource);

	/**
	 * Main method that checker should implement that actually detects errors
	 *
	 * @param resource the resource to run on.
	 * @param context container object for sharing data between different checkers
	 * 		operating on the resource.
	 * @return true if framework should traverse children of the resource and
	 *      run this checkers on them again.
	 * @throws OperationCanceledException if the checker was interrupted.
	 * @since 2.0
	 */
	boolean processResource(IResource resource, ICheckerInvocationContext context) throws OperationCanceledException;

	/**
	 * Called after processing a resource.
	 *
	 * @param resource the resource that has been processed.
	 * @since 2.0
	 */
	void after(IResource resource);

	/**
	 * @return the problem reporter.
	 * @since 2.0
	 */
	IProblemReporter getProblemReporter();

	/**
	 * Checker must implement this method to determine if it can run in editor
	 * "as you type". Checker must be really light weight to run in this mode.
	 * If it returns true, checker must also implement
	 * {@link IRunnableInEditorChecker}.
	 * Checker should return false if check is non-trivial and takes a long
	 * time.
	 *
	 * @return true if need to be run in editor as user types, and false otherwise
	 */
	boolean runInEditor();
}
