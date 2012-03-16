/*******************************************************************************
 * Copyright (c) 2006, 2010 Siemens AG.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 * Red Hat Inc. - Modified for use with autotools plug-in
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AutotoolsProblemMarkerInfo {

	public static enum Type{PACKAGE, HEADER, PROG, FILE, GENERIC}

	private ProblemMarkerInfo marker;

	public AutotoolsProblemMarkerInfo(IResource file, String description, int severity, String name, Type type) {
		this(file, -1, description, severity, null, null, name, type);
	}

	public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, Type type) {
		this(file, lineNumber, description, severity, variableName, null, null, type);
	}

	public AutotoolsProblemMarkerInfo(IResource file, int lineNumber, String description, int severity, String variableName, 
			IPath externalPath, String libraryInfo, Type type) {
		this.marker = new ProblemMarkerInfo(file, lineNumber, description, severity, variableName, externalPath);

		marker.setAttribute(IAutotoolsMarker.MARKER_PROBLEM_TYPE, type.name());
		marker.setAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, libraryInfo);

		marker.setType (IAutotoolsMarker.AUTOTOOLS_PROBLEM_MARKER);
	}

	public ProblemMarkerInfo getMarker() {
		return marker;
	}

	public String getProblemType() {
		return marker.getAttribute(IAutotoolsMarker.MARKER_PROBLEM_TYPE);
	}

	public String getLibraryInfo(){
		return marker.getAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO);
	}

}