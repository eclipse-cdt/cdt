/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     James Blackburn (Broadcom) - Bug 247838
 *     Andrew Gvozdev (Quoin Inc)
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ErrorParserManager extends OutputStream {

	private int nOpens;

	private final static String OLD_PREF_ERROR_PARSER = "errorOutputParser"; //$NON-NLS-1$
	public final static String PREF_ERROR_PARSER = CCorePlugin.PLUGIN_ID + ".errorOutputParser"; //$NON-NLS-1$

	private final IProject fProject;
	private final IMarkerGenerator fMarkerGenerator;

	private Map<String, IErrorParser[]> fErrorParsers;
	private ArrayList<ProblemMarkerInfo> fErrors;

	private Vector<IPath> fDirectoryStack;
	private IPath fBaseDirectory;

	private String previousLine;
	private OutputStream outputStream;
	private final StringBuilder currentLine = new StringBuilder();

	private final StringBuilder scratchBuffer = new StringBuilder();

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
			fErrorParsers = new LinkedHashMap<String, IErrorParser[]>(parsersIDs.length);
			for (String parsersID : parsersIDs) {
				IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parsersID);
				fErrorParsers.put(parsersID, parsers);
			}
		}
		fMarkerGenerator = markerGenerator;
		initErrorParserManager(workingDirectory);
	}

	private void initErrorParserManager(IPath workingDirectory) {
		fDirectoryStack = new Vector<IPath>();
		fErrors = new ArrayList<ProblemMarkerInfo>();

		fBaseDirectory = (workingDirectory == null || workingDirectory.isEmpty()) ? fProject.getLocation() : workingDirectory;
	}

	public IProject getProject() {
		return fProject;
	}

	public IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return fDirectoryStack.lastElement();
		}
		// Fall back to the Project Location
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
			IPath dir = fDirectoryStack.lastElement();
			fDirectoryStack.removeElementAt(i - 1);
			return dir;
		}
		return new Path(""); //$NON-NLS-1$
	}

	public int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	private void enableAllParsers() {
		fErrorParsers = new LinkedHashMap<String, IErrorParser[]>();
		String[] parserIDs = CCorePlugin.getDefault().getAllErrorParsersIDs();
		for (String parserID : parserIDs) {
			IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parserID);
			fErrorParsers.put(parserID, parsers);
		}
		if (fErrorParsers.size() == 0) {
			CCorePlugin.getDefault().getPluginPreferences().setValue(OLD_PREF_ERROR_PARSER, ""); // remove old prefs //$NON-NLS-1$
		}
	}

	/**
	 * This function used to populate member fFilesInProject which is not necessary
	 * anymore. Now {@link ResourceLookup} is used for search and not collection of files
	 * kept by {@code ErrorParserManager}.
	 * 
	 * Use {@link #findFileName} and {@link #findFilePath} for searches.
	 * @deprecated
	 */
	@Deprecated
	protected void collectFiles(IProject parent, final List<IResource> result) {
		try {
			parent.accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) {
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


		String lineTrimmed = line.trim();

		for (IErrorParser[] parsers : fErrorParsers.values()) {
			for (IErrorParser curr : parsers) {
				int types = IErrorParser2.NONE;
				if (curr instanceof IErrorParser2) {
					types = ((IErrorParser2) curr).getProcessLineBehaviour();
				}
				if ((types & IErrorParser2.KEEP_LONGLINES) == 0) {
					// long lines are not given to parsers, unless it wants it
					if (lineTrimmed.length() > 1000)
						continue;
				}
				// standard behavior (pre 5.1) is to trim the line
				String lineToParse = lineTrimmed;
				if ((types & IErrorParser2.KEEP_UNTRIMMED) !=0 ) {
					// untrimmed lines
					lineToParse = line;
				}
				// Protect against rough parsers who may accidentally
				// throw an exception on a line they can't handle.
				// It should not stop parsing of the rest of output.
				try {
					if (curr.processLine(lineToParse, this)) {
						return;
					}
				} catch (Exception e){
					String message = "Error parsing line [" + lineToParse + "]";  //$NON-NLS-1$//$NON-NLS-2$
					CCorePlugin.log(message, e);
				}
			}
		}
	}

	/**
	 * Returns the project file with the given name if that file can be uniquely identified.
	 * Otherwise returns <code>null</code>.
	 */
    public IFile findFileName(String fileName) {
    	IProject[] prjs = new IProject[] {fProject};
    	IPath path = new Path(fileName);
    	IFile[] files = ResourceLookup.findFilesByName(path, prjs, false);
    	if (files.length == 0)
			files = ResourceLookup.findFilesByName(path, prjs, true);
    	if (files.length == 1)
			return files[0];
    	return null;
	}

	protected IFile findFileInWorkspace(IPath path) {
		if (!path.isAbsolute()) {
			path = getWorkingDirectory().append(path);
		}
		return ResourceLookup.selectFileForLocation(path, fProject);
	}

	/**
	 * Use {@link #findFileName} and {@link #findFilePath} for searches.
	 * 
	 * Returns <code>true</code> if the project contains more than one file with the given name.
	 * @deprecated
	 */
	@Deprecated
	public boolean isConflictingName(String fileName) {
		return ResourceLookup.findFilesByName(new Path(fileName), new IProject[] {fProject}, false).length > 1;
	}

	/**
	 * Called by the error parsers to find an IFile for a given
	 * external filesystem 'location'
	 * 
	 * @return IFile representing the external location, or null if one 
	 *               couldn't be found.
	 */
	public IFile findFilePath(String filePath) {
		IPath path = new Path(filePath);
		IFile file = findFileInWorkspace(path);

		// That didn't work, see if it is a cygpath
		if (file == null) {
			CygPath cygpath = null;
			try {
				cygpath = new CygPath();
				path = new Path(cygpath.getFileName(filePath));
				if (fBaseDirectory.isPrefixOf(path)) {
					int segments = fBaseDirectory.matchingFirstSegments(path);
					path = path.removeFirstSegments(segments).setDevice(null);
				}
				file = findFileInWorkspace(path);
			} catch (Exception e) {
			}
			finally {
				if (cygpath != null)
					cygpath.dispose();
			}
		}

		return (file != null && file.exists()) ? file : null;
	}

	/**
	 * Called by the error parsers.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		generateExternalMarker(file, lineNumber, desc, severity, varName, null);
	}

	/**
	 * Called by the error parsers for external problem markers
	 */
	public void generateExternalMarker(IResource file, int lineNumber, String desc, int severity, String varName, IPath externalPath) {
		ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, desc, severity, varName, externalPath);
		fErrors.add(problemMarkerInfo);
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
	 * @param os
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
	@Override
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
	@Override
	public void flush() throws IOException {
		if (outputStream != null)
			outputStream.flush();
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		currentLine.append((char) b);
		checkLine(false);
		if (outputStream != null)
			outputStream.write(b);
	}

	@Override
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
			String line = buffer.substring(0, i);
			// get rid of any trailing '\r'
			if (line.endsWith("\r"))  //$NON-NLS-1$
				line=line.substring(0,line.length()-1);
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
			for (ProblemMarkerInfo problemMarkerInfo : fErrors) {
				if (problemMarkerInfo.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
					reset = true;
				}
				fMarkerGenerator.addMarker(problemMarkerInfo);
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
