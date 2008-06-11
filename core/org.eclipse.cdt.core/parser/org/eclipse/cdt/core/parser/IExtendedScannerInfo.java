/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public interface IExtendedScannerInfo extends IScannerInfo {

	/**
	 * Return an array of files which will be preprocessed before parsing the translation-unit in order
	 * to populate the macro-dictionary.
	 */
	public String [] getMacroFiles();

	/**
	 * Return an array of files that will be parsed before parsing the translation-unit as if the these
	 * files were included using include directives.
	 */
	public String [] getIncludeFiles();

	/**
	 * Return an array of paths that is searched after the current directory, when an include directive 
	 * with double-quotes is processed.
	 */
	public String [] getLocalIncludePath();
}
