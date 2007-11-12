/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 * @since 5.0
 * 
 * An image location explains how a name made it into the translation unit. 
 */
public interface IASTImageLocation extends IASTFileLocation {
	
	/**
	 * The image is part of the code that has not been modified by the preprocessor.
	 */
	final int REGULAR_CODE= 1;
	/** 
	 * The image is part of a macro definition and was introduced by some macro expansion.
	 */
	final int MACRO_DEFINITION= 2;
	/**
	 * The image is part of an argument of an explicit macro expansion.
	 */
	final int ARGUMENT_TO_MACRO_EXPANSION= 3;

	/**
	 * Returns the kind of image-location, one of {@link #REGULAR_CODE}, {@link #MACRO_DEFINITION} or
	 * {@link #ARGUMENT_TO_MACRO_EXPANSION}.
	 */
	public int getLocationKind();
}
