/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Node location inside of a macro expansion.
 * @since 5.0
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTMacroExpansionLocation extends IASTNodeLocation {

	/**
	 * Returns the expansion node enclosing this location. This will be the outermost
	 * macro expansion that can actually be found in the code.
	 */
	public IASTPreprocessorMacroExpansion getExpansion();

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
}
