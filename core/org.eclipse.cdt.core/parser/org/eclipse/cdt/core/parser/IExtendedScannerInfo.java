/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public interface IExtendedScannerInfo extends IScannerInfo {

	/**
	 * @return
	 */
	public String [] getMacroFiles();

	/**
	 * @return
	 */
	public String [] getIncludeFiles();

	/**
     * Get local inclusions?
     * 
	 * @return
	 */
	public String [] getLocalIncludePath();
}
