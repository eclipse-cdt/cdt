/*
 * Copyright (c) 2014 BlackBerry Limited  and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
