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

package org.eclipse.rse.persistence.dom;

public interface IRSEDOMConstants
{
	// node types
	public static final String TYPE_PROFILE = "Profile";
	public static final String TYPE_PROPERTY_SET = "PropertySet";
	public static final String TYPE_PROPERTY = "Property";
	public static final String TYPE_HOST = "Host";
	public static final String TYPE_FILTER_POOL = "FilterPool";
	public static final String TYPE_FILTER = "Filter";
	public static final String TYPE_FILTER_STRING = "FilterString";
	public static final String TYPE_FILTER_POOL_REFERENCE = "FilterPoolReference";
	public static final String TYPE_CONNECTOR_SERVICE = "ConnectorService";
	public static final String TYPE_SERVER_LAUNCHER = "ServerLauncher";
	public static final String TYPE_SUBSYSTEM = "SubSystem";
	
	// node attributes
	
	// profile attributes
	public static final String ATTRIBUTE_DEFAULT_PRIVATE="defaultPrivate";
	public static final String ATTRIBUTE_IS_ACTIVE="isActive";
	
	// subsystem attributes
	 public static final String ATTRIBUTE_HIDDEN="hidden";
	 
   	 // common attributes
	 public static final String ATTRIBUTE_NAME="name";
	 public static final String ATTRIBUTE_TYPE="type";
	 
	 // host attributes 
	public static final String ATTRIBUTE_HOSTNAME = "hostname";
	public static final String ATTRIBUTE_OFFLINE = "offline";
	public static final String ATTRIBUTE_DESCRIPTION = "description";
	public static final String ATTRIBUTE_USER_ID = "defaultUserId";
	 
	 // ConnectorService attributes
	 public static final String ATTRIBUTE_GROUP="group";
	 public static final String ATTRIBUTE_USE_SSL="useSSL";
	 
	 // Filter string attributes
	 public static final String ATTRIBUTE_STRING = "string";
	
	 // filter attributes
	 public static final String ATTRIBUTE_SUPPORTS_NESTED_FILTERS = "supportsNestedFilters";
	 public static final String ATTRIBUTE_RELATIVE_ORDER = "relativeOrder";
	 public static final String ATTRIBUTE_DEFAULT = "default";
	 public static final String ATTRIBUTE_STRING_CASE_SENSITIVE = "stringsCaseSensitive";
	 public static final String ATTRIBUTE_PROMPTABLE ="promptable";
	 public static final String ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS="supportsDuplicateFilterStrings";
	 public static final String ATTRIBUTE_NON_DELETABLE="nonDeletable";
	 public static final String ATTRIBUTE_NON_RENAMABLE="nonRenamable";
	 public static final String ATTRIBUTE_NON_CHANGEABLE="nonChangable";
	 public static final String ATTRIBUTE_STRINGS_NON_CHANGABLE="stringsNonChangable";
	 public static final String ATTRIBUTE_RELEASE="release";
	 public static final String ATTRIBUTE_SINGLE_FILTER_STRING_ONLY="singleFilterStringOnly";
	
	 // server launcher attributes
	 public static final String ATTRIBUTE_REXEC_PORT="rexecPort";
	 public static final String ATTRIBUTE_DAEMON_PORT="daemonPort";
	 public static final String ATTRIBUTE_PORT="port";
	 public static final String ATTRIBUTE_SERVER_PATH="serverPath";
	 public static final String ATTRIBUTE_SERVER_SCRIPT="serverScript";
	 public static final String ATTRIBUTE_RESTRICTED_TYPES="restrictedTypes";
	 
	public static final String ATTRIBUTE_VALUE = "value";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_OWNING_PARENT_NAME = "owningParentName";
	public static final String ATTRIBUTE_REF_ID= "refID";
	public static final String ATTRIBUTE_DELETABLE = "deletable";
	public static final String ATTRIBUTE_TRUE = "true";
	public static final String ATTRIBUTE_FALSE = "false";
}