/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.IMarkerGenerator;
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
    private Vector fDirectoryStack;
    private IMarkerGenerator fMarkerGenerator;
    private ArrayList fErrors;

    /**
     * 
     */
    public AbstractGCCBOPConsoleParserUtility(IProject project, IPath workingDirectory,
                                              IMarkerGenerator markerGenerator) {
        fDirectoryStack = new Vector();
        fErrors = new ArrayList();
        this.project = project;
        fBaseDirectory = project.getLocation();
        if (workingDirectory != null) {
            pushDirectory(workingDirectory);
        }
    }

    /**
     * @return Returns the fBaseDirectory.
     */
    protected IPath getBaseDirectory() {
        return fBaseDirectory;
    }
    /**
     * @return Returns the fDirectoryStack.
     */
    protected Vector getDirectoryStack() {
        return fDirectoryStack;
    }
    /**
     * @return Returns the fErrors.
     */
    protected ArrayList getErrors() {
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
            return (IPath) fDirectoryStack.lastElement();
        }
        // Fallback to the Project Location
        // FIXME: if the build did not start in the Project ?
        return fBaseDirectory;
    }

    protected void pushDirectory(IPath dir) {
        if (dir != null) {
            IPath pwd = null;
            if (fBaseDirectory.isPrefixOf(dir)) {
                pwd = dir.removeFirstSegments(fBaseDirectory.segmentCount());
            } else {
                // check if it is a cygpath
                if (dir.toString().startsWith("/cygdrive/")) {  //$NON-NLS-1$
                    char driveLetter = dir.toString().charAt(10);
                    driveLetter = (Character.isLowerCase(driveLetter)) ? Character.toUpperCase(driveLetter) : driveLetter;
                    StringBuffer buf = new StringBuffer();
                    buf.append(driveLetter);
                    buf.append(':');
                    String drive = buf.toString();
                    pwd = dir.removeFirstSegments(2);
                    pwd = pwd.setDevice(drive);
                    pwd = pwd.makeAbsolute();
                }
                else {
                    pwd = dir;
                }
            }
            fDirectoryStack.addElement(pwd);
        }
    }

    protected IPath popDirectory() {
        int i = getDirectoryLevel();
        if (i != 0) {
            IPath dir = (IPath) fDirectoryStack.lastElement();
            fDirectoryStack.removeElementAt(i - 1);
            return dir;
        }
        return new Path("");    //$NON-NLS-1$
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
        for (Iterator iter = fErrors.iterator(); iter.hasNext(); ) {
            Problem problem = (Problem) iter.next();
            if (problem.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
                reset = true;
            }
            if (problem.file == null) {
                fMarkerGenerator.addMarker(
                    project,
                    problem.lineNumber,
                    problem.description,
                    problem.severity,
                    problem.variableName);
            } else {
                fMarkerGenerator.addMarker(
                    problem.file,
                    problem.lineNumber,
                    problem.description,
                    problem.severity,
                    problem.variableName);
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
