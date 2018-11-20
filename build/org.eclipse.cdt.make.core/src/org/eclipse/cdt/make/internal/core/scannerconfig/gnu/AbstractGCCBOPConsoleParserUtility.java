/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Common utilities for GCC build output console parsers
 *
 * @author vhirsl
 */
public abstract class AbstractGCCBOPConsoleParserUtility {
	private IProject project;
	private IPath fBaseDirectory;
	private Vector<IPath> fDirectoryStack;
	private IMarkerGenerator fMarkerGenerator;
	private ArrayList<Problem> fErrors;

	/**
	 *
	 */
	public AbstractGCCBOPConsoleParserUtility(IProject project, IPath workingDirectory,
			IMarkerGenerator markerGenerator) {
		fDirectoryStack = new Vector<>();
		fErrors = new ArrayList<>();
		this.project = project;
		fBaseDirectory = new Path(EFSExtensionManager.getDefault().getPathFromURI(project.getLocationURI()));
		if (workingDirectory != null) {
			pushDirectory(workingDirectory);
		}
	}

	/**
	 * @return Returns the fBaseDirectory.
	 */
	public IPath getBaseDirectory() {
		return fBaseDirectory;
	}

	/**
	 * @return Returns the fDirectoryStack.
	 */
	protected Vector<IPath> getDirectoryStack() {
		return fDirectoryStack;
	}

	/**
	 * @return Returns the fErrors.
	 */
	protected ArrayList<Problem> getErrors() {
		return fErrors;
	}

	/**
	 * @return Returns the fMarkerGenerator.
	 */
	protected IMarkerGenerator getMarkerGenerator() {
		return fMarkerGenerator;
	}

	/**
	 * @return Returns the project.
	 */
	protected IProject getProject() {
		return project;
	}

	public IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return fDirectoryStack.lastElement();
		}
		// Fallback to the Project Location
		// FIXME: if the build did not start in the Project ?
		return fBaseDirectory;
	}

	protected void pushDirectory(IPath dir) {
		if (dir != null) {
			IPath pwd = null;
			if (fBaseDirectory != null && fBaseDirectory.isPrefixOf(dir)) {
				pwd = dir.removeFirstSegments(fBaseDirectory.segmentCount());
			} else {
				// check if it is a cygpath
				pwd = convertCygpath(dir);
			}
			fDirectoryStack.addElement(pwd);
		}
	}

	public static IPath convertCygpath(IPath path) {
		if (path.segmentCount() > 1 && path.segment(0).equals("cygdrive")) { //$NON-NLS-1$
			StringBuilder buf = new StringBuilder(2);
			buf.append(Character.toUpperCase(path.segment(1).charAt(0)));
			buf.append(':');
			path = path.removeFirstSegments(2);
			path = path.setDevice(buf.toString());
			path = path.makeAbsolute();
		}
		return path;
	}

	protected IPath popDirectory() {
		int i = getDirectoryLevel();
		if (i != 0) {
			IPath dir = fDirectoryStack.lastElement();
			fDirectoryStack.removeElementAt(i - 1);
			return dir;
		}
		return new Path(""); //$NON-NLS-1$
	}

	protected int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	public void changeMakeDirectory(String dir, int dirLevel, boolean enterDir) {
		if (enterDir) {
			/* Sometimes make screws up the output, so
			 * "leave" events can't be seen.  Double-check level
			 * here.
			 */
			for (int parseLevel = getDirectoryLevel(); dirLevel < parseLevel; parseLevel = getDirectoryLevel()) {
				popDirectory();
			}
			pushDirectory(new Path(dir));
		} else {
			popDirectory();
			/* Could check to see if they match */
		}
	}

	public boolean reportProblems() {
		boolean reset = false;
		for (Iterator<Problem> iter = fErrors.iterator(); iter.hasNext();) {
			Problem problem = iter.next();
			if (problem.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
				reset = true;
			}
			if (problem.file == null) {
				fMarkerGenerator.addMarker(new ProblemMarkerInfo(project, problem.lineNumber, problem.description,
						problem.severity, problem.variableName));
			} else {
				fMarkerGenerator.addMarker(new ProblemMarkerInfo(problem.file, problem.lineNumber, problem.description,
						problem.severity, problem.variableName));
			}
		}
		fErrors.clear();
		return reset;
	}

	protected class Problem {
		protected IResource file;
		protected int lineNumber;
		protected String description;
		protected int severity;
		protected String variableName;

		public Problem(IResource file, int lineNumber, String desciption, int severity, String variableName) {
			this.file = file;
			this.lineNumber = lineNumber;
			this.description = desciption;
			this.severity = severity;
			this.variableName = variableName;
		}
	}

	/**
	 * Called by the console line parsers to generate a problem marker.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		// No need to collect markers if marker generator is not present
		if (fMarkerGenerator != null) {
			Problem problem = new Problem(file, lineNumber, desc, severity, varName);
			fErrors.add(problem);
		}
	}
}
