/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

/**
 * @author Bogdan Gheorghe
 * 
 * IIndexStorage must be implemented by all indexStorage providers
 */
public interface IIndexStorage {
	
	//Indexer that use this indexer storage
	public ICDTIndexer[] getIndexers();
	
	//Get path variables that are used 
	public String[] getPathVariables();
	public void resolvePathVariables();
	
	//Merge functionality for the storage
	public void merge();
	public boolean canMergeWith(IIndexStorage storage);
}
