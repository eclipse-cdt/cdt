/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An image location explains how a name made it into the translation unit.
 * @since 5.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImageLocation extends IASTFileLocation {
	/**
	 * The image is part of the code that has not been modified by the preprocessor.
	 */
	final int REGULAR_CODE = 1;
	/**
	 * The image is part of a macro definition and was introduced by some macro expansion.
	 */
	final int MACRO_DEFINITION = 2;
	/**
	 * The image is part of an argument of an explicit macro expansion.
	 */
	final int ARGUMENT_TO_MACRO_EXPANSION = 3;

	/**
	 * Returns the kind of image-location, one of {@link #REGULAR_CODE}, {@link #MACRO_DEFINITION}
	 * or {@link #ARGUMENT_TO_MACRO_EXPANSION}.
	 */
	public int getLocationKind();
}
