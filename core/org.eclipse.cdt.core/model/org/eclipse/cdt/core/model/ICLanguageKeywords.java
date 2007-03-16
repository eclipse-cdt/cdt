/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * This is an optional extension interface to {@link ILanguage} which allows
 * a C/C++ language variant to expose the set of keywords it defines. 
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface ICLanguageKeywords {

	/**
	 * Get the keywords defined for this language, excluding bult-in types.
	 * 
	 * @return an array of keywords, never <code>null</code>
	 */
	public abstract String[] getKeywords();

	/**
	 * Get the built-in type names defined for this language.
	 * 
	 * @return an array of names, never <code>null</code>
	 */
	public abstract String[] getBuiltinTypes();

	/**
	 * Get the preprocessor keywords (directives) defined for this language.
	 * 
	 * @return an array of keywords, never <code>null</code>
	 */
	public abstract String[] getPreprocessorKeywords();

}
