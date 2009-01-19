/********************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved.
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
 * Uwe Stieber (Wind River) - Extended system type -> subsystemConfiguration association.
 * Martin Oberhuber (Wind River) - [185098] Provide constants for all well-known system types
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [218655][api] Provide SystemType enablement info in non-UI
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [226574][api] Add ISubSystemConfiguration#supportsEncoding()
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.model.SystemHostPool;
import org.osgi.framework.Bundle;

/**
 * Interface for a system type. Constants are defined for various system types.
 * These constants are kept in sync with definitions in plugin.xml.
 * <p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. System
 *           type implementations must subclass {@link AbstractRSESystemType}
 *           rather than implementing this interface directly.
 *           </p>
 */
public interface IRSESystemType extends IAdaptable {

	/**
	 * Linux system type, "Linux".
	 * @deprecated Use {@link #SYSTEMTYPE_LINUX_ID}.
	 */
	public static final String SYSTEMTYPE_LINUX = "Linux"; //$NON-NLS-1$

	/**
	 * Linux system type, "org.eclipse.rse.systemtype.linux".
	 */
	public static final String SYSTEMTYPE_LINUX_ID = "org.eclipse.rse.systemtype.linux"; //$NON-NLS-1$

	/**
	 * Power Linux type, "Power Linux".
	 * @deprecated Use {@link #SYSTEMTYPE_POWER_LINUX_ID}.
	 */
	public static final String SYSTEMTYPE_POWER_LINUX = "Power Linux"; //$NON-NLS-1$

	/**
	 * Power Linux type, "org.eclipse.rse.systemtype.linux.power".
	 */
	public static final String SYSTEMTYPE_POWER_LINUX_ID = "org.eclipse.rse.systemtype.linux.power"; //$NON-NLS-1$

	/**
	 * Power Linux type, "zSeries Linux".
	 * @deprecated Use {@link #SYSTEMTYPE_ZSERIES_LINUX_ID}.
	 */
	public static final String SYSTEMTYPE_ZSERIES_LINUX = "zSeries Linux"; //$NON-NLS-1$

	/**
	 * Power Linux type, "org.eclipse.rse.systemtype.linux.zseries".
	 */
	public static final String SYSTEMTYPE_ZSERIES_LINUX_ID = "org.eclipse.rse.systemtype.linux.zSeries"; //$NON-NLS-1$

	/**
	 * Unix system type, "Unix".
	 * @deprecated Use {@link #SYSTEMTYPE_UNIX_ID}.
	 */
	public static final String SYSTEMTYPE_UNIX = "Unix"; //$NON-NLS-1$

	/**
	 * Unix system type, "org.eclipse.rse.systemtype.unix".
	 */
	public static final String SYSTEMTYPE_UNIX_ID = "org.eclipse.rse.systemtype.unix"; //$NON-NLS-1$

	/**
	 * AIX system type, "AIX".
	 * @deprecated Use {@link #SYSTEMTYPE_AIX_ID}.
	 */
	public static final String SYSTEMTYPE_AIX = "AIX"; //$NON-NLS-1$

	/**
	 * AIX system type, "org.eclipse.rse.systemtype.aix".
	 */
	public static final String SYSTEMTYPE_AIX_ID = "org.eclipse.rse.systemtype.aix"; //$NON-NLS-1$

	/**
	 * PASE system type, "PASE".
	 * @deprecated Use {@link #SYSTEMTYPE_PASE_ID}.
	 */
	public static final String SYSTEMTYPE_PASE = "PASE"; //$NON-NLS-1$

	/**
	 * PASE system type, "org.eclipse.rse.systemtype.PASE".
	 */
	public static final String SYSTEMTYPE_PASE_ID = "org.eclipse.rse.systemtype.iseries.PASE"; //$NON-NLS-1$

	/**
	 * iSeries system type, "iSeries".
	 * @deprecated Use {@link #SYSTEMTYPE_ISERIES_ID}.
	 */
	public static final String SYSTEMTYPE_ISERIES = "iSeries"; //$NON-NLS-1$

	/**
	 * iSeries system type, "org.eclipse.rse.systemtype.iseries".
	 */
	public static final String SYSTEMTYPE_ISERIES_ID = "org.eclipse.rse.systemtype.iseries"; //$NON-NLS-1$

	/**
	 * Local system type, "Local".
	 * @deprecated Use {@link #SYSTEMTYPE_LOCAL_ID}.
	 */
	public static final String SYSTEMTYPE_LOCAL = "Local"; //$NON-NLS-1$

	/**
	 * Local system type, "org.eclipse.rse.systemtype.local".
	 */
	public static final String SYSTEMTYPE_LOCAL_ID = "org.eclipse.rse.systemtype.local"; //$NON-NLS-1$

	/**
	 * z/OS system type, "z/OS".
	 * @deprecated Use {@link #SYSTEMTYPE_ZSERIES_ID}.
	 */
	public static final String SYSTEMTYPE_ZSERIES = "z/OS"; //$NON-NLS-1$

	/**
	 * z/OS system type, "org.eclipse.rse.systemtype.zseries".
	 */
	public static final String SYSTEMTYPE_ZSERIES_ID = "org.eclipse.rse.systemtype.zseries"; //$NON-NLS-1$

	/**
	 * Windows system type, "Windows".
	 * @deprecated Use {@link #SYSTEMTYPE_WINDOWS_ID}.
	 */
	public static final String SYSTEMTYPE_WINDOWS = "Windows"; //$NON-NLS-1$

	/** Windows system type, "org.eclipse.rse.systemtype.windows". */
	public static final String SYSTEMTYPE_WINDOWS_ID = "org.eclipse.rse.systemtype.windows"; //$NON-NLS-1$

	/** Discovery system type, "org.eclipse.rse.systemtype.discovery". */
	public static final String SYSTEMTYPE_DISCOVERY_ID = "org.eclipse.rse.systemtype.discovery"; //$NON-NLS-1$
	/** FTP Only system type, "org.eclipse.rse.systemtype.ftp". */
	public static final String SYSTEMTYPE_FTP_ONLY_ID = "org.eclipse.rse.systemtype.ftp"; //$NON-NLS-1$
	/** SSH Only system type, "org.eclipse.rse.systemtype.ssh". */
	public static final String SYSTEMTYPE_SSH_ONLY_ID = "org.eclipse.rse.systemtype.ssh"; //$NON-NLS-1$
	/** Telnet Only system type, "org.eclipse.rse.systemtype.telnet". */
	public static final String SYSTEMTYPE_TELNET_ONLY_ID = "org.eclipse.rse.systemtype.telnet"; //$NON-NLS-1$

	/**
	 * System type Property Key (value: "isLocal") indicating whether
	 * a system type is declared in plugin.xml to refers to the local
	 * system.
	 * On a the local system, the following properties are expected:
	 * <ul>
	 *   <li>Subsystem Queries are fast and safe.</li>
	 *   <li>Files in the file system can be converted to java.io.File.</li>
	 * </ul>
	 * @see #testProperty(String, boolean)
	 */
	public static final String PROPERTY_IS_LOCAL = "isLocal"; //$NON-NLS-1$

	/**
	 * System type Property Key (value: "isWindows") indicating whether
	 * a system type is declared in plugin.xml to refers to a Windows
	 * system.
	 * <p>
	 * This is an "aggregate" property consisting  of several smaller
	 * properties like isCaseSensitive. In the future, we'll want more
	 * fine granular properties to check against. On a Windows system,
	 * the following properties are expected:
	 * <ul>
	 *   <li>File system is not case sensitive</li>
	 *   <li>File system has root drives</li>
	 *   <li>Symbolic links are not supported</li>
	 *   <li>"cmd" is used as the default shell, meaning that %envVar% refers to environment variables</li>
	 *   <li>Path separator is backslash (\)</li>
	 *   <li>Line end character is CRLF</li>
	 *   <li>Valid characters in file names and paths as known on Windows</li>
	 * </ul>
	 * @see #testProperty(String, boolean)
	 */
	public static final String PROPERTY_IS_WINDOWS = "isWindows"; //$NON-NLS-1$

	/**
	 * System type Property Key (value: "isCaseSensitive") indicating
	 * whether a given system type is in general case sensitive.
	 * @see #testProperty(String, boolean)
	 */
	public static final String PROPERTY_IS_CASE_SENSITIVE = "isCaseSensitive"; //$NON-NLS-1$

	/**
	 * System type Property Key (value: "supportsEncoding") indicating whether a
	 * given system type supports the user specifying an encoding to use for
	 * translating binary data to Java Unicode Strings when working on
	 * subsystems.
	 *
	 * It is up to the subsystems registered against a given system type whether
	 * they observe the system type's setting or not; the default
	 * implementations do observe it. Given that all subsystem configurations
	 * registered against a given system type do not support encodings, the
	 * corresponding RSE controls for allowing the user to change encodings will
	 * be disabled.
	 *
	 * Expected default value of this Property is "true" if not set.
	 *
	 * @see ISubSystemConfiguration#supportsEncoding(IHost)
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String PROPERTY_SUPPORTS_ENCODING = "supportsEncoding"; //$NON-NLS-1$

	/**
	 * Returns the id of the system type.
	 * @return the id of the system type
	 */
	public String getId();

	/**
	 * Returns the translatable label for use in the UI.
	 *
	 * @return The UI label or <code>null</code> if not set.
	 */
	public String getLabel();

	/**
	 * Returns the name of the system type.
	 * @return the name of the system type
	 *
	 * @deprecated Use {@link #getId()} for accessing the unique id or {@link #getLabel()} for the UI label.
	 */
	public String getName();

	/**
	 * Returns the description of the system type.
	 * @return the description of the system type
	 */
	public String getDescription();

	/**
	 * Returns the property of this system type with the given key.
	 * <code>null</code> is returned if there is no such key/value pair.
	 *
	 * @param key the name of the property to return
	 * @return the value associated with the given key or <code>null</code> if none
	 */
	public String getProperty(String key);

	/**
	 * Tests whether the given boolean property matches the expected value
	 * for this system type.
	 *
	 * Clients can use their own properties with system types, but should
	 * use reverse DNS notation to qualify their property keys (e.g.
	 * <code>com.acme.isFoobarSystem</code>. Property keys without qualifying
	 * namespace are reserved for RSE internal use.
	 *
	 * @param key the name of the property to return
	 * @param expectedValue the expected boolean value of the property.
	 * @return <code>true</code> if the Property is set on the system type and
	 *     matches the expected value. Returns <code>false</code> if the property
	 *     is not set or does not match.
	 */
	public boolean testProperty(String key, boolean expectedValue);

	/**
	 * Tests whether the system type is currently enabled.
	 *
	 * The enabled state is a dynamic property of a system type, compared to the
	 * static configuration by plugin markup. Enablement is a non-UI property,
	 * which can be set by a Product in the Preferences or modified by a user to
	 * hide certain system types.
	 * <p>
	 * Implementers of custom system types (which are registered by a
	 * SystemTypeProvider) can override this method to provide more advanced
	 * enabled checks e.g. based on license availability.
	 *
	 * @return <code>true</code> if the system type is currently enabled, or
	 *         <code>false</code> otherwise.
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean isEnabled();

	/**
	 * Tests whether the system type refers to the local system.
	 * This is a shortcut for
	 * <pre>
	 *   getId().equals(SYSTEMTYPE_LOCAL_ID) ||
	 *   || testProperty(PROPERTY_IS_LOCAL, true)
	 * </pre>
	 * See {@link #PROPERTY_IS_LOCAL} for properties expected on
	 * a Local system.
	 * Extenders (contributors of custom system types) may override.
	 * @return true if the system type refers to the local system.
	 */
	public boolean isLocal();

	/**
	 * Tests whether the system type refers to the Windows system.
	 * This is a shortcut for
	 * <pre>
	 *   getId().equals(SYSTEMTYPE_WINDOWS_ID)
	 *   || isLocal() && System.getProperty("os.name").toLowerCase().startsWith("win")
	 *   || testProperty(PROPERTY_IS_WINDOWS, true)
	 * </pre>
	 * See {@link #PROPERTY_IS_WINDOWS} for properties expected on
	 * a Windows system. This is an "aggregate" property consisting
	 * of several smaller properties like isCaseSensitive. In the
	 * future, we'll want more fine granular properties to check against.
	 * Extenders (contributors of custom system types) may override.
	 * @return true if the system type refers to a Windows system.
	 */
	public boolean isWindows();

	/**
	 * Returns the bundle which is responsible for the definition of this system type.
	 * Typically this is used as a base for searching for images and other files
	 * that are needed in presenting the system type.
	 *
	 * @return the bundle which defines this system type or <code>null</code> if none
	 */
	public Bundle getDefiningBundle();

	/**
	 * Returns a list of fully qualified known subsystem configuration id's that
	 * this system type wants to be registered against.
	 * More subsystem configurations can be added through the <tt>subsystemConfigurations</tt>
	 * extension point.
	 * <p>
	 * <b>Note:</b> The list returned here does not imply that the corresponding
	 * subsystem configurations exist. The list contains only possibilites not,
	 * requirements.
	 *
	 * @return The list of subsystem configuration id's. May be empty,
	 *         but never <code>null</code>.
	 */
	public String[] getSubsystemConfigurationIds();

	/**
	 * Creates a new <code>IHost</code> object instance. This method is
	 * called from {@link SystemHostPool#createHost(IRSESystemType, String, String, String, String, int)}.
	 *
	 * @param profile The system profile to associate with the host.
	 * @return A new <code>IHost</code> object instance.
	 */
	public IHost createNewHostInstance(ISystemProfile profile);
}