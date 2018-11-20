/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		//		System.out.println("in marker resolution, library info is " + marker.getAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, ""));
		//		String libraryInfo = marker.getAttribute(IAutotoolsMarker.MARKER_LIBRARY_INFO, null);
		//		if (libraryInfo != null) {
		//			return new IMarkerResolution[] {new PkgconfigErrorResolution(libraryInfo)};
		//		};
		return new IMarkerResolution[0];
	}

}
