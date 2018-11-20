/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @deprecated, use IASTMacroExpansionLocation instead
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IASTMacroExpansion extends IASTNodeLocation {
	/**
	 * The macro definition used for the expansion
	 */
	public IASTPreprocessorMacroDefinition getMacroDefinition();

	/**
	 * The macro reference for the explicit macro expansion containing this expansion.
	 * @since 5.0
	 */
	public IASTName getMacroReference();

	/**
	 * Returns an offset within the macro-expansion. The offset can be used to compare
	 * nodes within the same macro-expansion. However, it does not serve as an offset
	 * into a file.
	 */
	@Override
	public int getNodeOffset();

	/**
	 * Returns the length of this location. The length can be used to compare this location
	 * with others from within the same macro-expansion. However, the length does not neccessarily
	 * relate to a length in terms of characters.
	 */
	@Override
	public int getNodeLength();

	/**
	 * The source locations for for the macro expansion. These are the locations
	 * where the expansion in question occurred and was replaced.
	 *
	 * @deprecated use {@link IASTNodeLocation#asFileLocation()}.
	 */
	@Deprecated
	public IASTNodeLocation[] getExpansionLocations();
}
