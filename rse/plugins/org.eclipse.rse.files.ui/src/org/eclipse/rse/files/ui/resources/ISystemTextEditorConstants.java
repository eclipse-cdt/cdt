/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;

public interface ISystemTextEditorConstants {


	
	// Editor id
	public static final String SYSTEM_TEXT_EDITOR_ID	= "org.eclipse.rse.editor";
	public static final String SYSTEM_TEXT_BROWSER_ID	= "org.eclipse.rse.browser";
	
	// Identifier for profile type
	public static final String EDITOR_PROFILE_TYPE		= "editorProfileType";
	
	// key to identify file has sequence numbers	
	public static final String SEQUENCE_NUMBERS_KEY	    = "sequence_numbers_key";
	
	// key to identify that a save limit should be set
	public static final String MAX_LINE_LENGTH_KEY	    = "record_length_key";
	
	// key to identify that a source encoding should be set
	// this is used to emulate the remote encoding of the file
	public static final String SOURCE_ENCODING_KEY		= "source_encoding_key";

	// key to identify host ccsid	
	public static final String CCSID_KEY                = "ccsid_key";
	public static final String TEMP_CCSID_KEY           = "temp_ccsid_key";
	
	// key to identify logical or visual BIDI
	public static final String BIDI_LOGICAL_KEY			= "bidi_logical_key";
	
	// key to identify the local encoding
	// NOTE: DO NOT CHANGE THIS!! This is used by the IBM debugger.
	public static final String LOCAL_ENCODING_KEY		= "encoding";
}