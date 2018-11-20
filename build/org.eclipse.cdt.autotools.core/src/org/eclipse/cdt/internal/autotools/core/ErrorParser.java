/*******************************************************************************
 * Copyright (c) 2010, 2016 Red Hat Inc. and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

// This class would normally extend IErrorParser and use the CDT error parser
// extension.  However, we want an extended IMarker that contains library info and
// possibly other data in the future.  The standard CDT ErrorParserManager doesn't allow
// us to pass an extended ProblemMarkerInfo, so we are forced to have our own mechanism
// which is similar to the CDT one.
public class ErrorParser extends MarkerGenerator implements IErrorParser {
	public static final String ID = AutotoolsPlugin.PLUGIN_ID + ".errorParser"; //$NON-NLS-1$
	private Pattern pkgconfigError = Pattern
			.compile(".*?(configure:\\s+error:\\s+Package requirements\\s+\\((.*?)\\)\\s+were not met).*"); //$NON-NLS-1$
	private Pattern genconfigError = Pattern.compile(".*?configure:\\s+error:\\s+(.*)"); //$NON-NLS-1$
	private Pattern checkingFail = Pattern.compile("checking for (.*)\\.\\.\\. no"); //$NON-NLS-1$

	private Pattern changingConfigDirectory = Pattern.compile("Configuring in (.*)"); //$NON-NLS-1$

	private IPath buildDir;
	private IPath sourcePath;
	private IProject project;

	public ErrorParser() {
	}

	public ErrorParser(IPath sourcePath, IPath buildPath) {
		this.buildDir = buildPath;
		this.sourcePath = sourcePath;
	}

	@Override
	public boolean processLine(String line, org.eclipse.cdt.core.ErrorParserManager eoParser) {

		if (this.project == null)
			this.project = eoParser.getProject();

		if (this.buildDir == null)
			this.buildDir = new Path(eoParser.getWorkingDirectoryURI().getPath());

		if (this.sourcePath == null)
			this.sourcePath = eoParser.getProject().getLocation();

		AutotoolsProblemMarkerInfo marker = processLine(line);
		if (marker != null) {
			// Check to see if addProblemMarker exists.
			try {
				Method method = eoParser.getClass().getMethod("addProblemMarker", ProblemMarkerInfo.class);
				try {
					method.invoke(eoParser, marker);
					return true;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} catch (SecurityException | NoSuchMethodException e) {
				return false;
			}
		}
		return false;
	}

	public boolean processLine(String line, ErrorParserManager eoParser) {
		if (this.project == null)
			this.project = eoParser.getProject();

		AutotoolsProblemMarkerInfo marker = processLine(line);
		if (marker != null) {
			eoParser.addProblemMarker(marker);
			return true;
		}
		return false;
	}

	public AutotoolsProblemMarkerInfo processLine(String line) {
		Matcher m;

		m = changingConfigDirectory.matcher(line);
		if (m.matches()) {
			// set configuration directory.
			this.buildDir = this.buildDir.append(m.group(1));
			this.sourcePath = this.sourcePath.append(m.group(1));
			return null;
		}

		m = pkgconfigError.matcher(line);
		if (m.matches()) {
			return new AutotoolsProblemMarkerInfo(getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null, null,
					m.group(2), AutotoolsProblemMarkerInfo.Type.PACKAGE);
		}

		m = genconfigError.matcher(line);
		if (m.matches()) {
			return new AutotoolsProblemMarkerInfo(getProject(), -1, m.group(1), SEVERITY_ERROR_BUILD, null,
					AutotoolsProblemMarkerInfo.Type.GENERIC);
		}

		m = checkingFail.matcher(line);
		if (m.matches()) {
			// We know that there is a 'checking for ...' fail.
			// Find the log file containing this check
			AutotoolsProblemMarkerInfo.Type type = getCheckType(m.group(1));
			if (type != null)
				return new AutotoolsProblemMarkerInfo(getProject(), "Missing " + type + " " + m.group(1), SEVERITY_INFO,
						m.group(1), type);
		}

		return null;
	}

	/**
	 * Given the name of the filed check object, look for it in the log file
	 * file and then examine the configure script to figure out what the type of
	 * the check was.
	 *
	 * @param name
	 * @return
	 */
	private AutotoolsProblemMarkerInfo.Type getCheckType(String name) {
		int lineNumber = getErrorConfigLineNumber(name);

		// now open configure file.
		File file = new File(sourcePath + "/configure");
		// If the log file is not present there is nothing we can do.
		if (!file.exists())
			return null;

		try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {

			// look for something like:
			// if test "${ac_cv_prog_WINDRES+set}" = set; then :
			Pattern errorPattern = Pattern.compile(".*ac_cv_([a-z]*)_.*"); //$NON-NLS-1$

			// skip to the line
			String line = reader.readLine();
			for (int i = 0; i < lineNumber + 10 && line != null; i++) {
				if (i < lineNumber) {
					line = reader.readLine();
					continue;
				}
				Matcher m = errorPattern.matcher(line);
				if (m.matches()) {
					String typeString = m.group(1);
					if (typeString.equals("prog"))
						return AutotoolsProblemMarkerInfo.Type.PROG;
					if (typeString.equals("header"))
						return AutotoolsProblemMarkerInfo.Type.HEADER;
					if (typeString.equals("file"))
						return AutotoolsProblemMarkerInfo.Type.FILE;
					if (typeString.equals("lib"))
						return AutotoolsProblemMarkerInfo.Type.LIB;

					return null;
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	/**
	 * Check the log file for the check for the given name and return the line
	 * number in configure where the check occurs.
	 *
	 * @param name
	 * @return
	 */
	private int getErrorConfigLineNumber(String name) {
		File file = new File(buildDir + "/config.log");
		// If the log file is not present there is nothing we can do.
		if (!file.exists())
			return -1;
		try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
			Pattern errorPattern = Pattern.compile("configure:(\\d+): checking for " + name); //$NON-NLS-1$
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m = errorPattern.matcher(line);
				if (m.matches()) {
					return Integer.parseInt(m.group(1));
				}
			}
		} catch (IOException e) {
			return -1;
		}
		return -1;
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

}
