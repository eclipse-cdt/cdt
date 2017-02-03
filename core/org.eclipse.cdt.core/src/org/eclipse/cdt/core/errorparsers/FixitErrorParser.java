/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API
 *******************************************************************************/
package org.eclipse.cdt.core.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.errorparsers.FixitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @since 6.3
 */
public class FixitErrorParser extends RegexErrorParser {
	
	private final static Pattern fixit = Pattern.compile("fix-it:\"(.*)\":\\{(.*-.*)\\}:\"(.*)\""); //$NON-NLS-1$

	public FixitErrorParser (String id, String name) {
		super(id, name);
	}
	
	public FixitErrorParser () {
		super();
	}
	
	/**
	 * Parse a line of build output and register errors/warnings/infos for
	 * Problems view in internal list of {@link ErrorParserManager}.
	 *
	 * @param line - line of the input
	 * @param epManager - error parsers manager
	 * @return true if error parser recognized and accepted line, false otherwise
	 */
	@Override
	public boolean processLine(String line, ErrorParserManager epManager) {
		Matcher m = fixit.matcher(line);
		if (m.matches()) {
			IFile f = epManager.findFileName(m.group(1));
			try {
				IMarker[] markers = f.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
				if (markers.length > 0) {
					IMarker lastMarker = markers[markers.length - 1];
					FixitManager.getInstance().addMarker(lastMarker, m.group(2), m.group(3));
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return true;
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
			that.addPattern((RegexErrorPattern)pattern.clone());
		}
		return that;
	}
}
