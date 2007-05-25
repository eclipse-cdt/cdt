/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

public interface ISystemTextEditorConstants {


	
	// Editor id
	public static final String SYSTEM_TEXT_EDITOR_ID	= "org.eclipse.rse.editor"; //$NON-NLS-1$
	public static final String SYSTEM_TEXT_BROWSER_ID	= "org.eclipse.rse.browser"; //$NON-NLS-1$
	
	// Identifier for profile type
	public static final String EDITOR_PROFILE_TYPE		= "editorProfileType"; //$NON-NLS-1$
	
	// key to identify file has sequence numbers	
	public static final String SEQUENCE_NUMBERS_KEY	    = "sequence_numbers_key"; //$NON-NLS-1$
	
	// key to identify that a save limit should be set
	public static final String MAX_LINE_LENGTH_KEY	    = "record_length_key"; //$NON-NLS-1$
	
	// key to identify that a source encoding should be set
	// this is used to emulate the remote encoding of the file
	public static final String SOURCE_ENCODING_KEY		= "source_encoding_key"; //$NON-NLS-1$

	// key to identify host ccsid	
	public static final String CCSID_KEY                = "ccsid_key"; //$NON-NLS-1$
	public static final String TEMP_CCSID_KEY           = "temp_ccsid_key"; //$NON-NLS-1$
	
	// key to identify logical or visual BIDI
	public static final String BIDI_LOGICAL_KEY			= "bidi_logical_key"; //$NON-NLS-1$
	
	// key to identify the local encoding
	// NOTE: DO NOT CHANGE THIS!! This is used by the IBM debugger.
	public static final String LOCAL_ENCODING_KEY		= "encoding"; //$NON-NLS-1$
}