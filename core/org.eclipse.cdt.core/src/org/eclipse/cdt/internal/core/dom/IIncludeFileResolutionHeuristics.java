/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
