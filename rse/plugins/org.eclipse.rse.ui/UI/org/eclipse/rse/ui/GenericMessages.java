/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui;

import org.eclipse.osgi.util.NLS;

public class GenericMessages extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.ui.GenericMessages";//$NON-NLS-1$

	public static String ResourceNavigator_goto;
	
	public static String RefreshAction_text;
	public static String RefreshAction_toolTip;
	public static String RefreshAction_progressMessage;
	public static String RefreshAction_problemTitle;
	public static String RefreshAction_problemMessage;
	public static String RefreshAction_locationDeletedMessage;
	public static String RefreshAction_dialogTitle;

	public static String TransferOperation_message;
	public static String RSESubSystemOperation_message;
	public static String RSESubSystemOperation_Connect_message;
	public static String RSESubSystemOperation_Disconnect_message;
	public static String RSESubSystemOperation_Get_properties_message;
	public static String RSESubSystemOperation_Get_property_message;
	public static String RSESubSystemOperation_Resolve_filter_strings_message;
	public static String RSESubSystemOperation_Set_properties_message;
	public static String RSESubSystemOperation_Set_property_message;
	public static String RSESubSystemOperation_Notifying_registry_message;

	// ==============================================================================
	// Property Pages
	// ==============================================================================
	public static String PropertyDialog_messageTitle;
	public static String PropertyDialog_noPropertyMessage;
	public static String PropertyDialog_propertyMessage;
	
	// ==============================================================================
	// Editor Framework
	// ==============================================================================
	public static String EditorManager_saveResourcesMessage;
	public static String EditorManager_saveResourcesTitle;
	
	public static String TypesFiltering_title;
	public static String TypesFiltering_message;
	public static String TypesFiltering_otherExtensions;
	public static String TypesFiltering_typeDelimiter;


	public static String FileExtension_extensionEmptyMessage;
	public static String FileExtension_fileNameInvalidMessage;
	public static String FilteredPreferenceDialog_PreferenceSaveFailed;
	
	public static String RSEQuery_task;
		
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, GenericMessages.class);
	}
}