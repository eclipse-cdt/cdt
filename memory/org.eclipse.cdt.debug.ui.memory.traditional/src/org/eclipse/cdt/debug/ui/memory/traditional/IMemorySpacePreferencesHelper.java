/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Initial API
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.util.Map;

/**
 * @since 1.4
 */
public interface IMemorySpacePreferencesHelper {

	/**
	   * Updates the list of known memory space ids. For each new memory space,
	   * a preference is set, assigning it a distinct background color, from 
	   * a pool. Ids that are already known will be ignored.
	   * @param ids an array of memory spaces ids, for the current platform.
	   */
	void updateMemorySpaces(String[] ids);

	/**
	   * @return the preference store key used to lookup the default color for a 
	   * given memory space id
	   */
	String getMemorySpaceKey(String id);

	/**
	   * @return a map of each known memory space key to corresponding label entries
	   */
	Map<String, String> getMemorySpaceLabels();

	/**
	   * @return a map of each known memory space key to corresponding csv representation of an RGB color
	   */
	Map<String, String> getMemorySpaceDefaultColors();
}