/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ErrorParserManager extends OutputStream {

	private int nOpens;

	private final static String OLD_PREF_ERROR_PARSER = "errorOutputParser"; //$NON-NLS-1$
	public final static String PREF_ERROR_PARSER = CCorePlugin.PLUGIN_ID + ".errorOutputParser"; //$NON-NLS-1$

	private IProject fProject;
	private IMarkerGenerator fMarkerGenerator;
	private Map fFilesInProject;
	private List fNameConflicts;

	private Map fErrorParsers;
	private ArrayList fErrors;

	private Vector fDirectoryStack;
	private IPath fBaseDirectory;

	private String previousLine;
	private OutputStream outputStream;
	private StringBuffer currentLine = new StringBuffer();

	private StringBuffer scratchBuffer = new StringBuffer();
	
	private boolean hasErrors = false;

	public ErrorParserManager(ACBuilder builder) {
		this(builder.getProject(), builder);
	}

	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator) {
		this(project, markerGenerator, null);
	}

	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator, String[] parsersIDs) {
		this(project, project.getLocation(), markerGenerator, parsersIDs);
	}

	public ErrorParserManager(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator, String[] parsersIDs) {
		fProject = project;
		if (parsersIDs == null) {
			enableAllParsers();
		} else {
			fErrorParsers = new LinkedHashMap(parsersIDs.length);
			for (int i = 0; i < parsersIDs.length; i++) {
				IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parsersIDs[i]);
				fErrorParsers.put(parsersIDs[i], parsers);
			}
		}
		fMarkerGenerator = markerGenerator;
		initErrorParserManager(workingDirectory);
	}

	private void initErrorParserManager(IPath workingDirectory) {
		fFilesInProject = new HashMap();
		fNameConflicts = new ArrayList();
		fDirectoryStack = new Vector();
		fErrors = new ArrayList();

		List collectedFiles = new ArrayList();
		fBaseDirectory = (workingDirectory == null || workingDirectory.isEmpty()) ? fProject.getLocation() : workingDirectory;
		collectFiles(fProject, collectedFiles);

		for (int i = 0; i < collectedFiles.size(); i++) {
			IFile file = (IFile) collectedFiles.get(i);
			Object existing = fFilesInProject.put(file.getName(), file);
			if (existing != null) {
				fNameConflicts.add(file.getName());
			}
		}
	}

	public IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return (IPath) fDirectoryStack.lastElement();
		}
		// Fallback to the Project Location
		return fBaseDirectory;
	}

	public void pushDirectory(IPath dir) {
		if (dir != null) {
			IPath pwd = null;
			if (fBaseDirectory.isPrefixOf(dir)) {
				int segments = fBaseDirectory.matchingFirstSegments(dir);
				pwd = dir.removeFirstSegments(segments);
			} else {
				pwd = dir;
			}
			fDirectoryStack.addElement(pwd);
		}
	}

	public IPath popDirectory() {
		int i = fDirectoryStack.size();
		if (i != 0) {
			IPath dir = (IPath) fDirectoryStack.lastElement();
			fDirectoryStack.removeElementAt(i - 1);
			return dir;
		}
		return new Path(""); //$NON-NLS-1$
	}

	public int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	private void enableAllParsers() {
		fErrorParsers = new LinkedHashMap();
		String[] parserIDs = CCorePlugin.getDefault().getAllErrorParsersIDs();
		for (int i = 0; i < parserIDs.length; i++) {
			IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parserIDs[i]);
			fErrorParsers.put(parserIDs[i], parsers);
		}
		if (fErrorParsers.size() == 0) {
			initErrorParsersMap();
			CCorePlugin.getDefault().getPluginPreferences().setValue(OLD_PREF_ERROR_PARSER, ""); // remove old prefs //$NON-NLS-1$
		}
	}

	private void initErrorParsersMap() {
		String[] parserIDs = CCorePlugin.getDefault().getAllErrorParsersIDs();
		for (int i = 0; i < parserIDs.length; i++) {
			IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parserIDs[i]);
			fErrorParsers.put(parserIDs[i], parsers);
		}
	}

	protected void collectFiles(IProject parent, final List result) {
		try {
			parent.accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						result.add(proxy.requestResource());
						return false;
					}
					return true;
				}
			}, IResource.NONE);
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	/**
	 * Parses the input and try to generate error or warning markers
	 */
	private void processLine(String line) {
		if (fErrorParsers.size() == 0)
			return;

		String[] parserIDs = new String[fErrorParsers.size()];
		Iterator items = fErrorParsers.keySet().iterator();
		for (int i = 0; items.hasNext(); i++) {
			parserIDs[i] = (String) items.next();
		}

		for (int i = 0; i <parserIDs.length; ++i) {
			IErrorParser[] parsers = (IErrorParser[])fErrorParsers.get(parserIDs[i]);
			for (int j = 0; j < parsers.length; j++) {
				IErrorParser curr = parsers[j];
				if (curr.processLine(line, this)) {
					return;
				}
			}
		}

// This old way of doing was trouble because it did not
// respect the ordering provide by the users.
//
//		int top = parserIDs.length - 1;
//		int i = top;
//		do {
//			IErrorParser[] parsers = (IErrorParser[]) fErrorParsers.get(parserIDs[i]);
//			for (int j = 0; j < parsers.length; j++) {
//				IErrorParser curr = parsers[j];
//				if (curr.processLine(line, this)) {
//					if (i != top) {
//						// move to top
//						Object used = fErrorParsers.remove(parserIDs[i]);
//						fErrorParsers.put(parserIDs[i], used);
//						//savePreferences();
//					}
//					return;
//				}
//			}
//			i--;
//		} while (i >= 0);
	}

	/**
	 * Called by the error parsers.
	 */
	public IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return (IFile) fFilesInProject.get(path.lastSegment());
	}

	protected IFile findFileInWorkspace(IPath path) {
		IFile file = null;
		if (path.isAbsolute()) {
			IWorkspaceRoot root = fProject.getWorkspace().getRoot();
			file =  root.getFileForLocation(path);
			// It may be a link resource so we must check it also.
			if (file == null) {
				IFile[] files = root.findFilesForLocation(path);
				for (int i = 0; i < files.length; i++) {
					if (files[i].getProject().equals(fProject)) {
						file = files[i];
						break;
					}
				}
			}

		} else {
			file = fProject.getFile(path);
		}
		return file;
	}

	/**
	 * Called by the error parsers.
	 */
	public boolean isConflictingName(String fileName) {
		IPath path = new Path(fileName);
		return fNameConflicts.contains(path.lastSegment());
	}

	/**
	 * Called by the error parsers.
	 */
	public IFile findFilePath(String filePath) {
		IPath path = null;
		IPath fp = new Path(filePath);
		if (fp.isAbsolute()) {
			if (fBaseDirectory.isPrefixOf(fp)) {
				int segments = fBaseDirectory.matchingFirstSegments(fp);
				path = fp.removeFirstSegments(segments);
			} else {
				path = fp;
			}
		} else {
			path = getWorkingDirectory().append(filePath);
		}

		IFile file = null;
		// The workspace may throw an IllegalArgumentException
		// Catch it and the parser should fallback to scan the entire project.
		try {
			file = findFileInWorkspace(path);
		} catch (Exception e) {
		}

		// We have to do another try, on Windows for cases like "TEST.C" vs "test.c"
		// We use the java.io.File canonical path.
		if (file == null || !file.exists()) {
			File f = path.toFile();
			try {
				String canon = f.getCanonicalPath();
				path = new Path(canon);
				file = findFileInWorkspace(path);
			} catch (IOException e1) {
			}
		}
		return (file != null && file.exists()) ? file : null;
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
	 * Called by the error parsers.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		Problem problem = new Problem(file, lineNumber, desc, severity, varName);
		fErrors.add(problem);
		
		if (severity == IMarkerGenerator.SEVERITY_ERROR_RESOURCE)
			hasErrors = true;
	}

	/**
	 * Called by the error parsers.  Return the previous line, save in the working buffer.
	 */
	public String getPreviousLine() {
		return new String((previousLine) == null ? "" : previousLine); //$NON-NLS-1$
	}

	/**
	 * Method setOutputStream.
	 * @param cos
	 */
	public void setOutputStream(OutputStream os) {
		outputStream = os;
	}

	/**
	 * Method getOutputStream. It has a reference count
	 * the stream must be close the same number of time this method was call.
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() {
		nOpens++;
		return this;
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (nOpens > 0 && --nOpens == 0) {
			checkLine(true);
			fDirectoryStack.removeAllElements();
			fBaseDirectory = null;
			if (outputStream != null)
				outputStream.close();
		}
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		if (outputStream != null)
			outputStream.flush();
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		currentLine.append((char) b);
		checkLine(false);
		if (outputStream != null)
			outputStream.write(b);
	}

	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off != 0 || (len < 0) || (len > b.length)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		currentLine.append(new String(b, 0, len));
		checkLine(false);
		if (outputStream != null)
			outputStream.write(b, off, len);
	}

	private void checkLine(boolean flush) {
		String buffer = currentLine.toString();
		int i = 0;
		while ((i = buffer.indexOf('\n')) != -1) {
			String line = buffer.substring(0, i).trim(); // get rid of any trailing \r
			processLine(line);
			previousLine = line;
			buffer = buffer.substring(i + 1); // skip the \n and advance
		}
		currentLine.setLength(0);
		if (flush) {
			if (buffer.length() > 0) {
				processLine(buffer);
				previousLine = buffer;
			}
		} else {
			currentLine.append(buffer);
		}
	}

	public boolean reportProblems() {
		boolean reset = false;
		if (nOpens == 0) {
			Iterator iter = fErrors.iterator();
			while (iter.hasNext()) {
				Problem problem = (Problem) iter.next();
				if (problem.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
					reset = true;
				}
				if (problem.file == null) {
					fMarkerGenerator.addMarker(
						fProject,
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
		}
		return reset;
	}

	/**
	 * 
	 */
	public String getScratchBuffer() {
		return scratchBuffer.toString();
	}

	/**
	 * @param line
	 */
	public void appendToScratchBuffer(String line) {
		scratchBuffer.append(line);
	}

	/**
	 * 
	 */
	public void clearScratchBuffer() {
		scratchBuffer.setLength(0);
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
}
