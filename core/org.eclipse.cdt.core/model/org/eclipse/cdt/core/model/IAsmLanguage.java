/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * This is an optional extension interface to {@link ILanguage} which allows
 * an assembly language variant to expose certain syntax characteristics.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 *
 * @since 5.0
 */
public interface IAsmLanguage {

	/**
	 * Get the set of valid line comment characters defined for this assembly variant.
	 *
	 * @return an array line comment characters
	 */
	char[] getLineCommentCharacters();

	/**
	 * Get the line separator character defined for this assembly variant.
	 * The line separator character is used to split physical lines into logical lines.
	 * <code>'\0'</code> means that no line separator character is defined.
	 *
	 * @return the line separator character or <code>'\0'</code>
	 */
	char getLineSeparatorCharacter();

	/**
	 * Get the set of assembler directives defined for this variant.
	 *
	 * @return an array of keywords
	 */
	String[] getDirectiveKeywords();

	/**
	 * Get the preprocessor keywords (directives) defined for this variant.
	 *
	 * @return an array of keywords
	 */
	String[] getPreprocessorKeywords();

}
