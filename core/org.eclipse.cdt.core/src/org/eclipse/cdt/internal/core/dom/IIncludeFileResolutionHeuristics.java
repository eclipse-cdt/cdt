/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * Abstract base class for heuristic include file resolution.
 */
public interface IIncludeFileResolutionHeuristics {

	/**
	 * Attempt to find a file for the given include without using an include search path.
	 * @param include the include as provided in the directive
	 * @param currentFile the file the inclusion belongs to.
	 * @return a location for the inclusion or null.
	 */
	String findInclusion(String include, String currentFile);
}
