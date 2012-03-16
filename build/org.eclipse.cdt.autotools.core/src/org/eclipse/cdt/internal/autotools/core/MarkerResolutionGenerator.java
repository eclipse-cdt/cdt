/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

	public IMarkerResolution[] getResolutions(IMarker marker) {
//		System.out.println("in marker resolution, library info is " + marker.getAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, ""));
//		String libraryInfo = marker.getAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, null);
//		if (libraryInfo != null) {
//			return new IMarkerResolution[] {new PkgconfigErrorResolution(libraryInfo)};
//		};
		return new IMarkerResolution[0];
	}

}
