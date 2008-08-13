package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Xuan Chen        (IBM)    - [222263] Need to provide a PropertySet Adapter for System Team View (cleanup some use action stuff)
 * Kevin Doyle 		(IBM)	 - [222829] MoveUp/Down Broken in Work with User Actions Dialog
 *******************************************************************************/
/**
 * @author coulthar
 *
 * Constants used throughout the User Defined Action framework.
 */
public interface ISystemUDAConstants {
	
	public static final String USER_DEFINED_ACTION_PROPRERTY_SET_PREFIX = "UserActoins."; //$NON-NLS-1$
	
	public static final String USER_DEFINED_ACTION_PROPRERTY_SET_Name = "User Actions"; //$NON-NLS-1$
	
	static final String UDA_ROOT_ATTR = "uda_root"; //$NON-NLS-1$
	static final String FILETYPES_ROOT = "FileTypes"; //$NON-NLS-1$
	static final String ACTIONS_ROOT = "Actions"; //$NON-NLS-1$
	static final String ACTION_FILETYPES = "User Action File Types"; //$NON-NLS-1$
	
	/**
	 * The name of the xml tag for domain tags.
	 * Domains are used to partition actions. 
	 * Eg, you might have "Folder" and "File" domains
	 *   or for iSeries "Object" and "Member" domains
	 */
	public static final String XE_DOMAIN = "Domain"; //$NON-NLS-1$
	
	/**
	 * The name of the xml attribute of domain tags which
	 *  identifies the domain type. Its values will be
	 *  an untranslated name like "Object" or "Folder".
	 */
	public static final String XE_DOMTYPE = "DomainType"; //$NON-NLS-1$
	
	/**
	 * The name of the xml attribute of domain tags which
	 *  identifies the domain name. Its values will be
	 *  a translated name like "Object" or "Folder".
	 */
	public static final String XE_DOMNAME = "name"; //$NON-NLS-1$
	
	/**
	 * The name of the attribute we consistently use to store an element's name
	 */
	public static final String NAME_ATTR = "name"; //$NON-NLS-1$
	
	/**
	 * The type of the attribute we consistently use to store an element's name
	 */
	public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
	
	/**
	 * The name of the attribute we consistently use to store an element's original IBM-supplied name
	 */
	public static final String ORIGINAL_NAME_ATTR = "OriginalName"; //$NON-NLS-1$
	
	/**
	 * The position in the list of other user actions a user action should show up in
	 */
	public static final String ORDER_ATTR = "Order"; //$NON-NLS-1$
	
	/**
	 * The name of the attribute we consistently use to store a release number
	 */
	public static final String RELEASE_ATTR = "release"; //$NON-NLS-1$
	
	public static final String RELEASE_VALUE = "7.5"; //$NON-NLS-1$
}
