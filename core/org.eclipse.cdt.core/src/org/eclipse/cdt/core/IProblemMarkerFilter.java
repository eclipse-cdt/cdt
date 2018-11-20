/*
 * Copyright (c) 2014 BlackBerry Limited  and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.core;

/**
 * The purpose of IProblemMarkerFilter is to provide filtering function for problem markers.
 * ProblemMarkerFilter extension point are required to implements this interface.
 *
 * @since 5.6
 */
public interface IProblemMarkerFilter {

	/**
	 * Decide if a problem marker should be reported or ignored.
	 *
	 * @param markerInfo description of the problem marker that is going to be reported
	 * @return true if markers should be reported, false if should be ignored
	 */
	boolean acceptMarker(ProblemMarkerInfo markerInfo);
}
