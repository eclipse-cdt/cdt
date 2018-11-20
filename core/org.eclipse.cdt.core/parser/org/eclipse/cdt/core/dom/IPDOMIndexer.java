/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.Properties;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPDOMIndexer {
	/**
	 * Sets the project for which to build the index.
	 */
	public void setProject(ICProject project);

	/**
	 * Returns the project associated with the indexer.
	 */
	public ICProject getProject();

	/**
	 * Returns the unique ID of type of this indexer
	 */
	public String getID();

	/**
	 * Returns the value of a property.
	 * @since 4.0
	 */
	public String getProperty(String key);

	/**
	 * Clients are not allowed to call this method, it is called by the framework.
	 * @since 4.0
	 */
	public void setProperties(Properties props);

	/**
	 * Clients are not allowed to call this method, it is called by the framework.
	 * Used to check whether we need to reindex a project.
	 * @since 4.0
	 */
	public boolean needsToRebuildForProperties(Properties props);

	/**
	 * Clients are not allowed to call this method, it is called by the framework.
	 * Creates a task that handles the changes.
	 * @since 4.0
	 */
	public IPDOMIndexerTask createTask(ITranslationUnit[] added, ITranslationUnit[] changed,
			ITranslationUnit[] removed);
}
