/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
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
 * @since 4.0
 */
public interface ICLanguageKeywords {
	/**
	 * Returns the keywords defined for this language, excluding built-in types.
	 *
	 * @return an array of keywords, never <code>null</code>
	 */
	public abstract String[] getKeywords();

	/**
	 * Returns the built-in type names defined for this language.
	 *
	 * @return an array of names, never <code>null</code>
	 */
	public abstract String[] getBuiltinTypes();

	/**
	 * Returns the preprocessor keywords (directives) defined for this language.
	 *
	 * @return an array of keywords, never <code>null</code>
	 */
	public abstract String[] getPreprocessorKeywords();
}
