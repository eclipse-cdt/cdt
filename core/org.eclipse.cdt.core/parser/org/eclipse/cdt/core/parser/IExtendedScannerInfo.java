/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon, IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * Extension to {@link IScannerInfo}, allows for providing additional preprocessor options.
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
	 * <p>
	 * In order to suppress the use of the directory of the current file (side effect of gcc option
	 * -I-) you can pass '-' as one of the include paths. Other than that, the '-' will not have an
	 * effect, in particular it will not split the include path as the -I- option would do. 
	 */
	public String [] getLocalIncludePath();
}
