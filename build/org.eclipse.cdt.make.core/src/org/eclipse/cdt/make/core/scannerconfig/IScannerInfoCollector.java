/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;

/**
 * Interface for scanner info collector.
 * Used by scanner info console parsers.
 * 
 * @author vhirsl
 */
public interface IScannerInfoCollector {
	// for a list of target specific options i.e. -pthread, -ansi, -no_
	public static Integer TARGET_SPECIFIC_OPTION = new Integer(1) ; 
	public static Integer IMACROS = new Integer(2);
	public static Integer COMPILER_VERSION_INFO = new Integer(3);
	
	/**
	 * Contribute to resource's scanner configuration
	 * 
	 * @param resource
	 * @param includes
	 * @param symbols
	 * @param extraInfo - a map of key - list pairs, where key is the type of extra info
	 * i.e. target specific options or imacros commands,...
	 */
	public void contributeToScannerConfig(IResource resource,
										  List includes,
										  List symbols,
										  Map extraInfo);
}