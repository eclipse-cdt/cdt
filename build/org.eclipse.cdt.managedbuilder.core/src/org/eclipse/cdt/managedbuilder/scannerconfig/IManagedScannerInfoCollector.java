/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.scannerconfig;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.core.resources.IProject;

/**
 * Interface that a colelctor of compiler information must implement.
 * @since 2.0
 */
public interface IManagedScannerInfoCollector extends IScannerInfoCollector {
	/**
	 * Answers a map of collected defines that the the compiler uses by default. 
	 * The symbols are defined in the map as a (macro, value) pair as follows 
	 * <p><p><code>-DFOO</code> will be stored as ("FOO","")
	 * <p><code>-DFOO=BAR</code> will be stored as ("FOO","BAR")
	 * <p><p>Duplicates will not be stored in the map and any whitespaces in 
	 * the macro or value will be trimmed out.
	 *  
	 * @return a <code>Map</code> of defined symbols and values
	 */
	public Map getDefinedSymbols();
	
	
	/**
	 * Answers a <code>List</code> of unique built-in includes paths that have been 
	 * collected for the receiver. The paths are stored as <code>String</code> in the proper 
	 * format for the host tools. 
	 * 
	 * @return a <code>List</code> of built-in compiler include search paths.  
	 */
	public List getIncludePaths();
	
	/**
	 * Sets the <code>IProject</code> for the receiver.
	 * 
	 * @param project
	 */
	public void setProject(IProject project);
}
