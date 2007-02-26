/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.core;

import org.eclipse.core.runtime.IAdaptable;
import org.osgi.framework.Bundle;

/**
 * Interface for a system type. Constants are defined for various system types.
 * These constants are kept in sync with definitions in plugin.xml.
 * 
 * This interface is not intended to be implemented by clients.
 */
public interface IRSESystemType extends IAdaptable {

	/**
	 * Linux system type, "Linux".
	 */
	public static final String SYSTEMTYPE_LINUX = "Linux"; //$NON-NLS-1$

	/**
	 * Power Linux type, "Power Linux".
	 */
	public static final String SYSTEMTYPE_POWER_LINUX = "Power Linux"; //$NON-NLS-1$

	/**
	 * Power Linux type, "zSeries Linux".
	 */
	public static final String SYSTEMTYPE_ZSERIES_LINUX = "zSeries Linux"; //$NON-NLS-1$

	/**
	 * Unix system type, "Unix".
	 */
	public static final String SYSTEMTYPE_UNIX = "Unix"; //$NON-NLS-1$

	/**
	 * AIX system type, "AIX".
	 */
	public static final String SYSTEMTYPE_AIX = "AIX"; //$NON-NLS-1$

	/**
	 * PASE system type, "PASE".
	 */
	public static final String SYSTEMTYPE_PASE = "PASE"; //$NON-NLS-1$

	/**
	 * iSeries system type, "iSeries".
	 */
	public static final String SYSTEMTYPE_ISERIES = "iSeries"; //$NON-NLS-1$

	/**
	 * Local system type, "Local".
	 */
	public static final String SYSTEMTYPE_LOCAL = "Local"; //$NON-NLS-1$

	/**
	 * z/OS system type, "z/OS".
	 */
	public static final String SYSTEMTYPE_ZSERIES = "z/OS"; //$NON-NLS-1$

	/**
	 * Windows system type, "Windows".
	 */
	public static final String SYSTEMTYPE_WINDOWS = "Windows"; //$NON-NLS-1$

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
}