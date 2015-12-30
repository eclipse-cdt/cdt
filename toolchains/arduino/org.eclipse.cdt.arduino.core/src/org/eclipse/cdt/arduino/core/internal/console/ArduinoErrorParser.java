/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public abstract class ArduinoErrorParser extends ArduinoConsoleParser {

	public static final String LINK_OFFSET = "arduino.link.offset"; //$NON-NLS-1$
	public static final String LINK_LENGTH = "arduino.link.length"; //$NON-NLS-1$

	private final Pattern errorPattern;

	public ArduinoErrorParser(String pattern, int flags, String lineQualifier) {
		super(pattern, flags, lineQualifier);
		this.errorPattern = Pattern.compile(pattern);
	}

	public ArduinoErrorParser(String pattern) {
		this(pattern, 0, null);
	}

	protected abstract String getFileName(Matcher matcher);

	protected abstract int getLineNumber(Matcher matcher);

	protected abstract String getMessage(Matcher matcher);

	protected abstract int getSeverity(Matcher matcher);

	protected abstract int getLinkOffset(Matcher matcher);

	protected abstract int getLinkLength(Matcher matcher);

	public IMarker generateMarker(IFolder buildDirectory, String text) throws CoreException {
		Matcher matcher = errorPattern.matcher(text);
		if (matcher.matches()) {
			String fileName = getFileName(matcher);

			IFile file = buildDirectory.getFile(fileName);
			if (file.exists()) {
				for (IMarker marker : file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false,
						IResource.DEPTH_ZERO)) {
					if (marker.getAttribute(IMarker.SEVERITY, -1) == getSeverity(matcher)
							&& marker.getAttribute(IMarker.LINE_NUMBER, -1) == getLineNumber(matcher)
							&& marker.getAttribute(IMarker.MESSAGE, "").equals(getMessage(matcher))) { //$NON-NLS-1$
						return marker;
					}
				}
				try {
					IMarker marker = file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
					marker.setAttribute(IMarker.MESSAGE, getMessage(matcher));
					marker.setAttribute(IMarker.SEVERITY, getSeverity(matcher));
					marker.setAttribute(IMarker.LINE_NUMBER, getLineNumber(matcher));
					marker.setAttribute(IMarker.CHAR_START, -1);
					marker.setAttribute(IMarker.CHAR_END, -1);
					marker.setAttribute(LINK_OFFSET, getLinkOffset(matcher));
					marker.setAttribute(LINK_LENGTH, getLinkLength(matcher));
					return marker;
				} catch (CoreException e) {
					Activator.log(e);
					return null;
				}
			}
		}
		return null;
	}

}
