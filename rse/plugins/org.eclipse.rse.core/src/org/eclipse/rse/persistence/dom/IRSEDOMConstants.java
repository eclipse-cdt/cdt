/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - add attribute name for filter type
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.persistence.dom;

/**
 * Constants used in RSE DOMs.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEDOMConstants {
	// node types
	public static final String TYPE_PROFILE = "Profile"; //$NON-NLS-1$
	public static final String TYPE_PROPERTY_SET = "PropertySet"; //$NON-NLS-1$
	public static final String TYPE_PROPERTY = "Property"; //$NON-NLS-1$
	public static final String TYPE_HOST = "Host"; //$NON-NLS-1$
	public static final String TYPE_FILTER_POOL = "FilterPool"; //$NON-NLS-1$
	public static final String TYPE_FILTER = "Filter"; //$NON-NLS-1$
	public static final String TYPE_FILTER_STRING = "FilterString"; //$NON-NLS-1$
	public static final String TYPE_FILTER_POOL_REFERENCE = "FilterPoolReference"; //$NON-NLS-1$
	public static final String TYPE_CONNECTOR_SERVICE = "ConnectorService"; //$NON-NLS-1$
	public static final String TYPE_SERVER_LAUNCHER = "ServerLauncher"; //$NON-NLS-1$
	public static final String TYPE_SUBSYSTEM = "SubSystem"; //$NON-NLS-1$

	// node attributes

	// profile attributes
	public static final String ATTRIBUTE_DEFAULT_PRIVATE = "defaultPrivate"; //$NON-NLS-1$
	public static final String ATTRIBUTE_IS_ACTIVE = "isActive"; //$NON-NLS-1$

	// subsystem attributes
	public static final String ATTRIBUTE_HIDDEN = "hidden"; //$NON-NLS-1$

	// common attributes
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	// host attributes
	public static final String ATTRIBUTE_HOSTNAME = "hostname"; //$NON-NLS-1$
	public static final String ATTRIBUTE_OFFLINE = "offline"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SYSTEM_TYPE = "systemType"; //$NON-NLS-1$

	// ConnectorService attributes
	public static final String ATTRIBUTE_GROUP = "group"; //$NON-NLS-1$
	public static final String ATTRIBUTE_USE_SSL = "useSSL"; //$NON-NLS-1$

	// Filter string attributes
	public static final String ATTRIBUTE_STRING = "string"; //$NON-NLS-1$

	// filter attributes
	public static final String ATTRIBUTE_SUPPORTS_NESTED_FILTERS = "supportsNestedFilters"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RELATIVE_ORDER = "relativeOrder"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DEFAULT = "default"; //$NON-NLS-1$
	public static final String ATTRIBUTE_STRING_CASE_SENSITIVE = "stringsCaseSensitive"; //$NON-NLS-1$
	public static final String ATTRIBUTE_PROMPTABLE = "promptable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SUPPORTS_DUPLICATE_FILTER_STRINGS = "supportsDuplicateFilterStrings"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NON_DELETABLE = "nonDeletable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NON_RENAMABLE = "nonRenamable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NON_CHANGEABLE = "nonChangable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_STRINGS_NON_CHANGABLE = "stringsNonChangable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RELEASE = "release"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SINGLE_FILTER_STRING_ONLY = "singleFilterStringOnly"; //$NON-NLS-1$
	/**
	 * A constant that specifies the filter type attribute in the DOM structure.
	 * Value "filterType".
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String ATTRIBUTE_FILTER_TYPE = "filterType"; //$NON-NLS-1$

	// server launcher attributes
	public static final String ATTRIBUTE_REXEC_PORT = "rexecPort"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DAEMON_PORT = "daemonPort"; //$NON-NLS-1$
	public static final String ATTRIBUTE_PORT = "port"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SERVER_PATH = "serverPath"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SERVER_SCRIPT = "serverScript"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RESTRICTED_TYPES = "restrictedTypes"; //$NON-NLS-1$

	public static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	public static final String ATTRIBUTE_OWNING_PARENT_NAME = "owningParentName"; //$NON-NLS-1$
	public static final String ATTRIBUTE_REF_ID = "refID"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DELETABLE = "deletable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TRUE = "true"; //$NON-NLS-1$
	public static final String ATTRIBUTE_FALSE = "false"; //$NON-NLS-1$
}
