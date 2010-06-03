/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

/**
 * Specifies the style of part of some text source.
 */
public interface ISourceTag {
	// style codes
	final static int STYLE_None         =  0;
	final static int STYLE_Class        =  1;
	final static int STYLE_Struct       =  2;
	final static int STYLE_Union        =  3;
	final static int STYLE_Interface    =  4;
	final static int STYLE_Package      =  5;
	final static int STYLE_Function     =  6;
	final static int STYLE_ProtectedFunction = 7;
	final static int STYLE_Method       =  8;
	final static int STYLE_Exception    = 9;
	final static int STYLE_Variable     = 10;
	final static int STYLE_MemberVariable    = 11;
	final static int STYLE_Enumerator   = 12;
	final static int STYLE_Macro        = 13;
	final static int STYLE_Include      = 14;
	final static int STYLE_Undefined    = 15;
	final static int STYLE_Enumeration  = 16;
	final static int STYLE_Typedef      = 17;
	final static int STYLE_Type3        = 18;
	final static int STYLE_Type4        = 19;
	final static int STYLE_Type5        = 20;
	final static int STYLE_File         = 21;
	final static int STYLE_Project      = 22;
	final static int STYLE_IncludeContainer = 23;
	final static int STYLE_LocalVariable    = 24;
	final static int STYLE_Label        = 25;
	final static int STYLE_Record		= 26;
	final static int STYLE_TaggedType   = 27;
	final static int STYLE_Subtype		= 28;
	final static int STYLE_Warning		= 29;
	final static int STYLE_Count        = 30;
	
	/**
	 * Returns the unqualified name of the source tag. Files return their base name.
	 */
	String getName();

	/**
	 * Returns the fully qualified name of the source tag. Files return their path.
	 */
	String getQualifiedName();

	/**
	 * Returns the range of the symbol within the file.
	 */
	ISourceRange getFullRange();

	/**
	 * Returns the range of the identifier of the symbol within the file.
	 */
	ISourceRange getRangeOfIdentifier();
		
	/**
	 * Computes the style code. Style codes are language dependent. You
	 * cannot derive any information from the style-code of a symbol. It
	 * may only be used to influence the visualization of a symbol. You
	 * may select color, font or icon depending on the style code.
	 * @return the style code of the symbol
	 */
	int getStyleCode();

	/**
	 * Returns the timestamp of the file at the time the sourcetag was generated.
	 */
	long getSnapshotTime();
}
