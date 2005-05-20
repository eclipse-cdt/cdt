/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.cindexstorage;


public interface ICIndexStorageConstants {
	/**
	 * The signature of the index file.
	 */
	
	public static final String SIGNATURE= "INDEX FILE 0.016"; //$NON-NLS-1$
	
	/**
	 * The size of a block for a <code>Block</code>.
	 */
	public static final int BLOCK_SIZE= 8192;
	
	final public static char SEPARATOR= '/';
	
	// TODO:  Remove these constants.
	static final public char CLASS_SUFFIX = 'C';		// CLASS
	static final public char DERIVED_SUFFIX = 'D';		// DERIVED
	static final public char ENUM_SUFFIX = 'E';			// ENUM 
	static final public char FRIEND_SUFFIX = 'F';		// FRIEND 
	static final public char FWD_CLASS_SUFFIX = 'G';	// FWD_CLASS
	static final public char FWD_STRUCT_SUFFIX = 'H';	// FWD_STRUCT
	static final public char FWD_UNION_SUFFIX = 'I';	// FWD_UNION
	static final public char STRUCT_SUFFIX = 'S';		// STRUCT
	static final public char TYPEDEF_SUFFIX = 'T';		// TYPEDEF 
	static final public char UNION_SUFFIX = 'U';		// UNION 
	static final public char VAR_SUFFIX = 'V';			// VAR
	
	final static char [][] encodings = {  
	        "".toCharArray(), 				// not used 		//$NON-NLS-1$
			"type".toCharArray(), 			// TYPES            //$NON-NLS-1$
			"function".toCharArray(), 		// FUNCTIONS        //$NON-NLS-1$
			"method".toCharArray(), 		// METHODS          //$NON-NLS-1$
			"field".toCharArray(), 			// FIELDS           //$NON-NLS-1$
			"macro".toCharArray(), 			// MACROS           //$NON-NLS-1$
			"namespace".toCharArray(), 		// NAMESPACES       //$NON-NLS-1$
			"enumtor".toCharArray(), 		// ENUMERATORS      //$NON-NLS-1$
			"include" .toCharArray()		// INCLUDES	        //$NON-NLS-1$
	};

	final static char [][] encodingTypes = { 
			"".toCharArray(), 		// not used			   //$NON-NLS-1$
			"Decl/".toCharArray(), 	// DECLARATIONS        //$NON-NLS-1$
			"Ref/".toCharArray(), 	// REFERENCES          //$NON-NLS-1$
			"Defn/".toCharArray() 	// DEFINTIONS          //$NON-NLS-1$
	};

	final static char[] typeConstants = { ' ', // not used
			'C', // CLASS            
			'S', // STRUCT          
			'U', // UNION            
			'E', // ENUM             
			'V', // VAR              
			'T', // TYPEDEF          
			'D', // DERIVED          
			'F', // FRIEND           
			'G', // FWD_CLASS        
			'H', // FWD_STRUCT       
			'I'  // FWD_UNION
	};
	
	final static String[] typeConstantNames = { "", // not used //$NON-NLS-1$
		"Class", //$NON-NLS-1$
		"Struct", //$NON-NLS-1$
		"Union", //$NON-NLS-1$
		"Enum", //$NON-NLS-1$
		"Variable", //$NON-NLS-1$
		"Typedef", //$NON-NLS-1$
		"Derived", //$NON-NLS-1$
		"Friend", //$NON-NLS-1$
		"FWD Class", //$NON-NLS-1$
		"FWD Struct", //$NON-NLS-1$
		"FWD Union" //$NON-NLS-1$
};
	
	final static String[] allSpecifiers = {
		"private", 		// private        //$NON-NLS-1$
		"public", 		// public         //$NON-NLS-1$
		"protected", 	// protected      //$NON-NLS-1$
		"const", 		// const          //$NON-NLS-1$
		"volatile", 	// volatile       //$NON-NLS-1$
		"static", 		// static         //$NON-NLS-1$
		"extern", 		// extern         //$NON-NLS-1$
		"inline", 		// inline         //$NON-NLS-1$
		"virtual", 		// virtual        //$NON-NLS-1$
		"pure virtual", // pure virtual   //$NON-NLS-1$
		"explicit", 	// explicit       //$NON-NLS-1$
		"auto", 		// auto           //$NON-NLS-1$
		"register",	 	// register       //$NON-NLS-1$
		"mutable" 		// mutable        //$NON-NLS-1$
	};
	

	
}
