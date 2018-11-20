/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API
 *******************************************************************************/
package org.eclipse.cdt.core.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.ProblemMarkerFilterManager;
import org.eclipse.cdt.internal.errorparsers.FixitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @since 6.3
 */
public class FixitErrorParser extends RegexErrorParser {

	private final static Pattern fixit = Pattern.compile("fix-it:\"(.*)\":\\{(.*-.*)\\}:\"(.*)\""); //$NON-NLS-1$

	public FixitErrorParser(String id, String name) {
		super(id, name);
	}

	public FixitErrorParser() {
		super();
	}

	@Override
	public boolean processLine(String line, ErrorParserManager epManager) {
		Matcher m = fixit.matcher(line);
		if (m.matches()) {
			IProject project = null;
			IFile f = epManager.findFileName(m.group(1));
			if (f != null) {
				project = f.getProject();
				try {
					ProblemMarkerInfo info = ProblemMarkerFilterManager.getInstance().getLastProblemMarker(f);
					String externalLocation = null;
					if (info.externalPath != null && !info.externalPath.isEmpty()) {
						externalLocation = info.externalPath.toOSString();
					}

					// Try to find matching marker to tie to fix-it
					IMarker[] markers = f.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
					for (IMarker marker : markers) {
						int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
						int sev = marker.getAttribute(IMarker.SEVERITY, -1);
						String msg = (String) marker.getAttribute(IMarker.MESSAGE);
						if (lineNumber == info.lineNumber && sev == info.severity && msg.equals(info.description)) {
							String extloc = (String) marker
									.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
							if (extloc == null || extloc.equals(externalLocation)) {
								if (project == null || project.equals(info.file.getProject())) {
									FixitManager.getInstance().addMarker(marker, m.group(2), m.group(3));
									return true;
								}
								String source = (String) marker.getAttribute(IMarker.SOURCE_ID);
								if (project.getName().equals(source)) {
									FixitManager.getInstance().addMarker(marker, m.group(2), m.group(3));
									return true;
								}
							}
						}
					}
				} catch (CoreException | NumberFormatException e) {
					CCorePlugin.log(e);
				}
				return true;
			}
		}
		return super.processLine(line, epManager);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		FixitErrorParser that = new FixitErrorParser(getId(), getName());
		for (RegexErrorPattern pattern : getPatterns()) {
			that.addPattern((RegexErrorPattern) pattern.clone());
		}
		return that;
	}
}
