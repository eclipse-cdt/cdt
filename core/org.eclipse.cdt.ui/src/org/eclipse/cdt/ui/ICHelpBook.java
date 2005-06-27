/**********************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui;

/**
 * Represents the help book, that is a set of articles on some topic.
 * Such as "C functions", "Qt library", etc., provided by help provider.
 * @see ICHelpProvider
 * @since 2.1
 */
public interface ICHelpBook {
	public static final int HELP_TYPE_C = 1;
	public static final int HELP_TYPE_CPP = 2;
	public static final int HELP_TYPE_ASM = 3;

	/**
	 * returns the tytle of the Help Book
	 * @return String representing the HelpBook tytle
	 */
	String getTitle();
	
	/**
	 * gets the type of Help provided with this book that might be ine of ICHelpBook.HELP_TYPE_XXX
	 * @return one of ICHelpBook.HELP_TYPE_XXX representing the type of Provided help
	 */
	int getCHelpType();
}

