/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Andrew Ferguson (Symbian)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import java.util.Properties;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;


/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMIndexer {

	public void setProject(ICProject project);
	public ICProject getProject();
		
	/**
	 * Return the unique ID of type of this indexer
	 * @return the unique ID of type of this indexer
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
	public IPDOMIndexerTask createTask(ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed);
}
