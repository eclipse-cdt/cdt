/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
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
	 * Main method that checker should implement that actually detects errors
	 * 
	 * @param resource
	 *        - resource to run on
	 * @return true if framework should traverse children of the resource and
	 *         run this checkers on them again
	 */
	boolean processResource(IResource resource);

	/**
	 * Implement this method to trim down type of resource you are interested
	 * in, usually it will be c/c++ files only. This method should be
	 * independent from current user preferences.
	 * 
	 * @param resource
	 *        - resource to run on
	 * @return - true if checker should be run on this resource
	 */
	boolean enabledInContext(IResource resource);

	/**
	 * Checker must implement this method to determine if it can run in editor
	 * "as you type". Checker must be really light weight to run in this mode.
	 * If it returns true, checker must also implement
	 * {@link IRunnableInEditorChecker}.
	 * Checker should return false if check is non-trivial and takes a long
	 * time.
	 * 
	 * @return true if need to be run in editor as user types, and false
	 *         otherwise
	 */
	boolean runInEditor();
}
