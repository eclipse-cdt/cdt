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
 * Interface that checker must implement. CDT Checker must be able to process a
 * resource.
 * 
 * Clients may implement and extend this interface.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *              Extend AbstractChecker class instead.
 */
public interface IChecker {
	/**
	 * Main method that checker should implement that actually detects errors
	 * 
	 * @param resource
	 *            - resource to run on
	 * @return true if need to traverse children
	 */
	boolean processResource(IResource resource);

	/**
	 * Implement this method to trim down type of resource you are interested
	 * in, usually it will be c/c++ files only
	 * 
	 * @param resource
	 *            - resource to run on
	 * @return
	 */
	boolean enabledInContext(IResource resource);

	/**
	 * Checker must implement this method to determine if it can run in editor
	 * "as you type", checker must be really light weight to run in this mode.
	 * Checker must also must implement IRunnableInEditorChecker if it returns
	 * true. Checker can return false if check is non-trivial and takes a long
	 * time.
	 * 
	 * @return true if need to be run in editor as user types, and false
	 *         otherwise
	 */
	boolean runInEditor();
}
