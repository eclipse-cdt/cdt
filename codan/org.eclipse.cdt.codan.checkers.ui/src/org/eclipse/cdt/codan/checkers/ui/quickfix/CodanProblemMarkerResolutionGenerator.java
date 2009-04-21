/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.checkers.ui.quickfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class CodanProblemMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	public IMarkerResolution[] getResolutions(IMarker marker) {
		final Pattern patternBuildDependsAdd = Pattern.compile("Possible assignment in condition.*");
		String description = marker.getAttribute(IMarker.MESSAGE, "no message");
		Matcher matcherBuildDependsAdd = patternBuildDependsAdd.matcher(description);
		if (matcherBuildDependsAdd.matches()) {
			return new IMarkerResolution[] { new QuickFixAssignmentInCondition() };
		}
		return new IMarkerResolution[0];
	}
}