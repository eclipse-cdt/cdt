/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     James Blackburn (Broadcom) - Bug 247838
 *     Andrew Gvozdev (Quoin Inc)
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;

/**
 * The purpose of ErrorParserManager is to delegate the work of error parsing 
 * build output to {@link IErrorParser}s, assist in finding {@link IResource}s, and
 * help create appropriate error/warning/info markers to be displayed
 * by the Problems view.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ErrorParserManager extends OutputStream {

	private int nOpens;

	public final static String PREF_ERROR_PARSER = CCorePlugin.PLUGIN_ID + ".errorOutputParser"; //$NON-NLS-1$

	private final IProject fProject;
	private final IMarkerGenerator fMarkerGenerator;

	private Map<String, IErrorParser[]> fErrorParsers;
	private ArrayList<ProblemMarkerInfo> fErrors;

	private Vector<URI> fDirectoryStack;
	private final URI fBaseDirectoryURI;

	private String previousLine;
	private OutputStream outputStream;
	private final StringBuilder currentLine = new StringBuilder();

	private final StringBuilder scratchBuffer = new StringBuilder();

	private boolean hasErrors = false;

	private String cachedFileName = null;
	private URI cachedWorkingDirectory = null;
	private IFile cachedFile = null;

	private static boolean isCygwin = true;

	/**
	 * Constructor.
	 * 
	 * @param builder - project builder.
	 */
	public ErrorParserManager(ACBuilder builder) {
		this(builder.getProject(), builder);
	}

	/**
	 * Constructor.
	 * 
	 * @param project - project being built.
	 * @param markerGenerator - marker generator able to create markers.
	 */
	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator) {
		this(project, markerGenerator, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param project - project being built.
	 * @param markerGenerator - marker generator able to create markers.
	 * @param parsersIDs - array of error parsers' IDs.
	 */
	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator, String[] parsersIDs) {
		this(project, project.getLocationURI(), markerGenerator, parsersIDs);
	}

	/**
	 * Constructor.
	 * 
	 * @param project - project being built.
	 * @param workingDirectory - IPath location of the working directory of where the build is performed. 
	 * @param markerGenerator - marker generator able to create markers.
	 * @param parsersIDs - array of error parsers' IDs.
	 * @deprecated use {@link #ErrorParserManager(IProject, URI, IMarkerGenerator, String[])} instead
	 */
	@Deprecated
	public ErrorParserManager(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator, String[] parsersIDs) {
		this(project, (workingDirectory == null || workingDirectory.isEmpty()) ? null : org.eclipse.core.filesystem.URIUtil.toURI(workingDirectory), 
				markerGenerator, parsersIDs);
	}

	/**
	 * URI based constructor.
	 * 
	 * @param project - project being built.
	 * @param baseDirectoryURI - absolute location URI of working directory of where the build is performed. 
	 * @param markerGenerator - marker generator able to create markers.
	 * @param parsersIDs - array of error parsers' IDs.
	 * @since 5.1
	 */
	public ErrorParserManager(IProject project, URI baseDirectoryURI, IMarkerGenerator markerGenerator, String[] parsersIDs) {
		fProject = project;
		fMarkerGenerator = markerGenerator;
		fDirectoryStack = new Vector<URI>();
		fErrors = new ArrayList<ProblemMarkerInfo>();
		enableErrorParsers(parsersIDs);

		if (baseDirectoryURI != null)
			fBaseDirectoryURI = baseDirectoryURI;
		else
			fBaseDirectoryURI = project.getLocationURI();
	}

	private void enableErrorParsers(String[] parsersIDs) {
		if (parsersIDs == null) {
			parsersIDs = CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
		fErrorParsers = new LinkedHashMap<String, IErrorParser[]>(parsersIDs.length);
		for (String parsersID : parsersIDs) {
			IErrorParser[] parsers = CCorePlugin.getDefault().getErrorParser(parsersID);
			fErrorParsers.put(parsersID, parsers);
		}
	}

	/**
	 * @return current project.
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @return current working directory location where build is being performed.
	 * @deprecated use {@link #getWorkingDirectoryURI()} instead
	 */
	@Deprecated
	public IPath getWorkingDirectory() {
		return org.eclipse.core.filesystem.URIUtil.toPath(getWorkingDirectoryURI());
	}

	/**
	 * Return the current URI location where the build is being performed
	 * @since 5.1
	 */
	public URI getWorkingDirectoryURI() {
		if (!fDirectoryStack.isEmpty())
			return fDirectoryStack.lastElement();

		// Fall back to the Project Location / Build directory
		return fBaseDirectoryURI;
	}

	/**
	 * {@link #pushDirectory} and {@link #popDirectory} are used to change working directory
	 * from where file name is searched (see {@link #findFileInWorkspace}).
	 * The intention is to handle make output of commands "pushd dir" and "popd".
	 * 
	 * @param dir - another directory level to keep in stack -- corresponding to 'pushd'.
	 */
	public void pushDirectory(IPath dir) {
		if (dir != null) {
			URI uri;
			URI workingDirectoryURI = getWorkingDirectoryURI();
			if (!dir.isAbsolute())
				uri = URIUtil.append(workingDirectoryURI, dir.toString());
			else {
				uri = toURI(dir);
				if (uri == null) // Shouldn't happen; error logged
					return;
			}
			pushDirectoryURI(uri);
		}
	}

	/**
	 * {@link #pushDirectoryURI} and {@link #popDirectoryURI} are used to change working directory
	 * from where file name is searched (see {@link #findFileInWorkspace}).
	 * The intention is to handle make output of commands "pushd dir" and "popd".
	 * 
	 * @param dir - another directory level to keep in stack -- corresponding to 'pushd'.
	 * @since 5.1
	 */
	public void pushDirectoryURI(URI dir) {
		if (dir != null) {
			if (dir.isAbsolute())
				fDirectoryStack.addElement(dir);
			else
				fDirectoryStack.addElement(URIUtil.makeAbsolute(dir, getWorkingDirectoryURI()));
		}
	}

	/**
	 * {@link #pushDirectory} and {@link #popDirectory} are used to change working directory
	 * from where file name is searched (see {@link #findFileInWorkspace}).
	 * The intention is to handle make output of commands "pushd" and "popd".
	 * 
	 * @return previous build directory location corresponding 'popd' command.
	 * @deprecated use {@link #popDirectoryURI()} instead
	 */
	@Deprecated
	public IPath popDirectory() {
		return org.eclipse.core.filesystem.URIUtil.toPath(popDirectoryURI());
	}

	/**
	 * {@link #pushDirectoryURI(URI)} and {@link #popDirectoryURI()} are used to change working directory
	 * from where file name is searched (see {@link #findFileInWorkspace(IPath)}).
	 * The intention is to handle make output of commands "pushd" and "popd".
	 * 
	 * @return previous build directory location corresponding 'popd' command.
	 * @since 5.1
	 */
	public URI popDirectoryURI() {
		int i = fDirectoryStack.size();
		if (i != 0) {
			URI dir = fDirectoryStack.lastElement();
			fDirectoryStack.removeElementAt(i - 1);
			return dir;
		}
		return fBaseDirectoryURI;
	}

	/**
	 * @return number of directories in the stack.
	 */
	public int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	/**
	 * This function used to populate member fFilesInProject which is not necessary
	 * anymore. Now {@link ResourceLookup} is used for search and not collection of files
	 * kept by {@code ErrorParserManager}.
	 * @param parent - project.
	 * @param result - resulting collection of files.
	 * 
	 * @deprecated Use {@link #findFileName} for searches.
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
	 * Returns the file with the given name if that file can be uniquely identified.
	 * Otherwise returns {@code null}.
	 *
	 * @param fileName - file name could be plain file name, absolute path or partial path
	 * @return - file in the workspace or {@code null}.
	 */
	public IFile findFileName(String fileName) {
		if (fileName.equals(cachedFileName) && cachedWorkingDirectory != null && 
				org.eclipse.core.filesystem.URIUtil.equals(getWorkingDirectoryURI(), cachedWorkingDirectory))
			return cachedFile;

		IPath path = new Path(fileName);

		// Try to find exact match. If path is not absolute - searching in working directory.
		IFile file = findFileInWorkspace(path);

		// Try to find best match considering known partial path
		if (file==null) {
			IProject[] prjs = new IProject[] { fProject };
			IFile[] files = ResourceLookup.findFilesByName(path, prjs, false);
			if (files.length == 0)
				files = ResourceLookup.findFilesByName(path, prjs, /* ignoreCase */ true);
			if (files.length == 0) {
				prjs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				files = ResourceLookup.findFilesByName(path, prjs, false);
				if (files.length == 0)
					files = ResourceLookup.findFilesByName(path, prjs, /* ignoreCase */ true);
			}
			if (files.length == 1)
				file = files[0];
		}

		// Could be cygwin path
		if (file==null && isCygwin && path.isAbsolute()) {
			file = findCygwinFile(fileName);
		}

		cachedFileName = fileName;
		cachedWorkingDirectory = getWorkingDirectoryURI();
		cachedFile = file;
		return file;
	}

	/**
	 * Find exact match in the workspace. If path is not absolute search is done in working directory.
	 * 
	 * @param path - file path.
	 * @return - file in the workspace or {@code null} if such a file doesn't exist
	 */
	protected IFile findFileInWorkspace(IPath path) {
		URI uri;
		if (!path.isAbsolute())
			uri = URIUtil.append(getWorkingDirectoryURI(), path.toString());
		else {
			uri = toURI(path);
			if (uri == null) // Shouldn't happen; error logged
				return null;
		}
		return findFileInWorkspace(uri);
	}

	/**
	 * Find exact match in the workspace. If path is not absolute search is done in the current working directory.
	 * 
	 * @param uri - absolute or relative URI to resolve.
	 * @return - file in the workspace or {@code null} if such a file doesn't exist
	 * @since 5.1
	 */
	protected IFile findFileInWorkspace(URI uri) {
		if (!uri.isAbsolute())
			uri = URIUtil.makeAbsolute(uri, getWorkingDirectoryURI());

		IFile f = ResourceLookup.selectFileForLocationURI(uri, fProject);
		if (f != null && f.isAccessible())
			return f;
		return null;
	}

	/**
	 * @param fileName - file name.
	 * @return {@code true} if the project contains more than one file with the given name.
	 * 
	 * @deprecated Use {@link #findFileName} for searches.
	 */
	@Deprecated
	public boolean isConflictingName(String fileName) {
		return ResourceLookup.findFilesByName(new Path(fileName), new IProject[] {fProject}, false).length > 1;
	}

	/**
	 * Called by the error parsers to find an IFile for a given
	 * external filesystem 'location'
	 * 
	 * @param filePath - file path.
	 * @return IFile representing the external location, or null if one 
	 *         couldn't be found.
	 * 
	 * @deprecated Use {@link #findFileName} for searches.
	 */
	@Deprecated
	public IFile findFilePath(String filePath) {
		IPath path = new Path(filePath);
		IFile file = findFileInWorkspace(path);

		// That didn't work, see if it is a cygpath
		if (file == null && isCygwin) {
			file = findCygwinFile(filePath);
		}

		return (file != null && file.exists()) ? file : null;
	}

	private IFile findCygwinFile(String filePath) {
		IFile file=null;
		IPath path;
		CygPath cygpath = null;
		try {
			cygpath = new CygPath();
			path = new Path(cygpath.getFileName(filePath));
			file = findFileInWorkspace(path);
		} catch (UnsupportedOperationException e) {
			isCygwin = false;
		} catch (Exception e) {
		}
		finally {
			if (cygpath != null)
				cygpath.dispose();
		}
		return file;
	}

	/**
	 * Add marker to the list of error markers.
	 * Markers are actually added in the end of processing in {@link #reportProblems()}.
	 * 
	 * @param file - resource to add the new marker.
	 * @param lineNumber - line number of the error.
	 * @param desc - description of the error.
	 * @param severity - severity of the error.
	 * @param varName - variable name.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		generateExternalMarker(file, lineNumber, desc, severity, varName, null);
	}

	/**
	 * Add marker to the list of error markers.
	 * Markers are actually added in the end of processing in {@link #reportProblems()}.
	 * 
	 * @param file - resource to add the new marker.
	 * @param lineNumber - line number of the error.
	 * @param desc - description of the error.
	 * @param severity - severity of the error, one of
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 * @param varName - variable name.
	 * @param externalPath - external path pointing to a file outside the workspace.
	 */
	public void generateExternalMarker(IResource file, int lineNumber, String desc, int severity, String varName, IPath externalPath) {
		ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, desc, severity, varName, externalPath);
		fErrors.add(problemMarkerInfo);
		if (severity == IMarkerGenerator.SEVERITY_ERROR_RESOURCE)
			hasErrors = true;
	}

	/**
	 * Called by the error parsers.
	 * @return the previous line, save in the working buffer.
	 */
	public String getPreviousLine() {
		return new String((previousLine) == null ? "" : previousLine); //$NON-NLS-1$
	}

	/**
	 * Method setOutputStream.
	 * @param os - output stream
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

	/**
	 * Create actual markers from the list of collected problems.
	 * 
	 * @return {@code true} if detected a problem indicating that build failed.
	 *         The semantics of the return code is inconsistent. As far as build is concerned
	 *         there is no difference between errors
	 *         {@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE} and
	 *         {@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 */
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
     * Converts a location {@link IPath} to an {@link URI}. Contrary to
     * {@link URIUtil#toURI(IPath)} this method does not assume that the path belongs
     * to local file system.
     *
     * The returned URI uses the scheme and authority of the current working directory
     * as returned by {@link #getWorkingDirectoryURI()}
     *
	 * @param path - the path to convert to URI.
	 * @return URI
	 * @since 5.1
	 */
	private URI toURI(IPath path) {
		try {
			URI baseURI = getWorkingDirectoryURI();
			String uriString = path.toString();

			// On Windows "C:/folder/" -> "/C:/folder/"
			if (path.isAbsolute() && uriString.charAt(0) != IPath.SEPARATOR)
			    uriString = IPath.SEPARATOR + uriString;

			return new URI(baseURI.getScheme(), baseURI.getUserInfo(),
					       baseURI.getHost(), baseURI.getPort(),
					       uriString, null, null);
		} catch (URISyntaxException e) {
			String message = "Problem converting path to URI [" + path.toString() + "]";  //$NON-NLS-1$//$NON-NLS-2$
			CCorePlugin.log(message, e);
		}
		return null;
	}

	/**
	 * @return scratch buffer.
	 * @deprecated Use IErrorParser2 interface to handle multiline messages rather than scratch buffer.
	 */
	@Deprecated
	public String getScratchBuffer() {
		return scratchBuffer.toString();
	}

	/**
	 * @param line - input line.
	 * @deprecated Use IErrorParser2 interface to handle multiline messages rather than scratch buffer.
	 */
	@Deprecated
	public void appendToScratchBuffer(String line) {
		scratchBuffer.append(line);
	}

	/**
	 * @deprecated Use IErrorParser2 interface to handle multiline messages rather than scratch buffer.
	 */
	@Deprecated
	public void clearScratchBuffer() {
		scratchBuffer.setLength(0);
	}

	/**
	 * @return {@code true} if errors attributed to resources detected
	 * 
	 * @deprecated The semantics of this function is inconsistent. As far as build is concerned
	 *         there is no difference between errors
	 *         {@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE} and
	 *         {@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 */
	@Deprecated
	public boolean hasErrors() {
		return hasErrors;
	}
}
