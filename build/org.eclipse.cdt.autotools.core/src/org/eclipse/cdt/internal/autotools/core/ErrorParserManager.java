/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
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
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation  
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.internal.core.IErrorMarkeredOutputStream;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.URIUtil;

/**
 * The purpose of ErrorParserManager is to delegate the work of error parsing 
 * build output to {@link IErrorParser}s, assist in finding {@link IResource}s, and
 * help create appropriate error/warning/info markers to be displayed
 * by the Problems view.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings("restriction")
public class ErrorParserManager extends OutputStream {
	/**
	 * The list of error parsers stored in .project for 3.X projects
	 * as key/value pair with key="org.eclipse.cdt.core.errorOutputParser"
	 * @deprecated since CDT 4.0.
	 */
	
	/**
	 * Delimiter for error parsers presented in one string.
	 * @since 5.2
	 */
	public final static char ERROR_PARSER_DELIMITER = ';';

	private int nOpens;
	private int lineCounter=0;

	private final IProject fProject;
	private final MarkerGenerator fMarkerGenerator;

	private Map<String, ErrorParser> fErrorParsers;
	private ArrayList<ProblemMarkerInfo> fErrors;

	private Vector<URI> fDirectoryStack;
	private final URI fBaseDirectoryURI;

	private String previousLine;
	private OutputStream outputStream;
	private final StringBuilder currentLine = new StringBuilder();


	/**
	 * Constructor.
	 * 
	 * @param project - project being built.
	 * @param markerGenerator - marker generator able to create markers.
	 */
	public ErrorParserManager(IProject project, MarkerGenerator markerGenerator) {
		this(project, markerGenerator, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param project - project being built.
	 * @param markerGenerator - marker generator able to create markers.
	 * @param parsersIDs - array of error parsers' IDs.
	 */
	public ErrorParserManager(IProject project, MarkerGenerator markerGenerator, String[] parsersIDs) {
		this(project, project.getLocationURI(), markerGenerator, parsersIDs);
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
	public ErrorParserManager(IProject project, URI baseDirectoryURI, MarkerGenerator markerGenerator, String[] parsersIDs) {
		fProject = project;
		fMarkerGenerator = markerGenerator;
		fDirectoryStack = new Vector<URI>();
		fErrors = new ArrayList<ProblemMarkerInfo>();
		fErrorParsers = new LinkedHashMap<String, ErrorParser>();

		if (baseDirectoryURI != null)
			fBaseDirectoryURI = baseDirectoryURI;
		else
			fBaseDirectoryURI = project.getLocationURI();
	}

	public void addErrorParser(String id, ErrorParser parser) {
		fErrorParsers.put(id, parser);
	}

	/**
	 * @return current project.
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @return the current URI location where the build is being performed
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
	 * Parses the input and tries to generate error or warning markers
	 */
	private void processLine(String line) {
		String lineTrimmed = line.trim();
		lineCounter++;

		ProblemMarkerInfo marker=null;

		for (ErrorParser parser : fErrorParsers.values()) {
			ErrorParser curr = parser;
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

			boolean consume = false;
			// Protect against rough parsers who may accidentally
			// throw an exception on a line they can't handle.
			// It should not stop parsing of the rest of output.
			try {
				consume = curr.processLine(lineToParse, this);
			} catch (Exception e){
				AutotoolsPlugin.log(e);
			} finally {
				if (fErrors.size() > 0) {
					if (marker==null)
						marker = fErrors.get(0);
					fErrors.clear();
				}
			}

			if (consume)
				break;
			}
		outputLine(line, marker);
	}
	
	/** 
	 * Conditionally output line to outputStream. If stream 
	 * supports error markers, use it, otherwise use conventional stream
	 */
	private void outputLine(String line, ProblemMarkerInfo marker) {
		String l = line + "\n";  //$NON-NLS-1$
		if ( outputStream == null ) return; 
		try {
			if (marker != null) {
				if (outputStream instanceof IErrorMarkeredOutputStream) {
					IErrorMarkeredOutputStream mos = (IErrorMarkeredOutputStream)outputStream;
					mos.write(l, marker);
				}
			}
			byte[] b = l.getBytes();
			outputStream.write(b, 0, b.length);			
		} catch (IOException e) {
			AutotoolsPlugin.log(e);
		}
	}

	/**
	 * @return counter counting processed lines of output
	 * @since 5.2
	 */
	public int getLineCounter() {
		return lineCounter;
	}


	/**
	 * Add marker to the list of error markers.
	 * 
	 * @param file - resource to add the new marker.
	 * @param lineNumber - line number of the error.
	 * @param desc - description of the error.
	 * @param severity - severity of the error.
	 * @param varName - variable name.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName,
			AutotoolsProblemMarkerInfo.Type type) {
		generateExternalMarker(file, lineNumber, desc, severity, varName, null, null, type);
	}

	/**
	 * Add marker to the list of error markers.
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
	public void generateExternalMarker(IResource file, int lineNumber, String desc, int severity, 
			String varName, IPath externalPath, String libraryInfo, AutotoolsProblemMarkerInfo.Type type) {
		AutotoolsProblemMarkerInfo problemMarkerInfo = 
			new AutotoolsProblemMarkerInfo(file, lineNumber, desc, severity, varName, externalPath, libraryInfo, type);
		addProblemMarker(problemMarkerInfo);
	}

	/**
	 * Add the given marker to the list of error markers.
	 * 
	 * @param problemMarkerInfo - The marker to be added 
	 */
	public void addProblemMarker(AutotoolsProblemMarkerInfo problemMarkerInfo){
		fErrors.add(problemMarkerInfo.getMarker());
		fMarkerGenerator.addMarker(problemMarkerInfo);
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
	 * Note: you have to close this stream explicitly
	 * don't rely on ErrorParserManager.close(). 
	 * @param os - output stream
	 */
	public void setOutputStream(OutputStream os) {
		outputStream = os;
	}

	/**
	 * Method getOutputStream. 
	 * Note: you have to close this stream explicitly
	 * don't rely on ErrorParserManager.close(). 
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() {
		nOpens++;
		return this;
	}

	/**
	 * @see java.io.OutputStream#close()
	 * Note: don't rely on this method to close underlying OutputStream, 
	 * close it explicitly 
	 */
	@Override
	public synchronized void close() throws IOException {
		if (nOpens > 0 && --nOpens == 0) {
			checkLine(true);
			fDirectoryStack.removeAllElements();
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
	}

	// This method examines contents of currentLine buffer
	// if it contains whole line this line is checked by error
	// parsers (processLine method). 
	// If flush is true rest of line is checked by error parsers.
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
//		try {
			URI baseURI = getWorkingDirectoryURI();
			String uriString = path.toString();

			// On Windows "C:/folder/" -> "/C:/folder/"
			if (path.isAbsolute() && uriString.charAt(0) != IPath.SEPARATOR)
			    uriString = IPath.SEPARATOR + uriString;
			
			return EFSExtensionManager.getDefault().createNewURIFromPath(baseURI, uriString);
	}



	/**
	 * @param ids - array of error parser IDs
	 * @return error parser IDs delimited with error parser delimiter ";"
	 * @since 5.2
	 */
	public static String toDelimitedString(String[] ids) {
		String result=""; //$NON-NLS-1$
		for (String id : ids) {
			if (result.length()==0) {
				result = id;
			} else {
				result += ERROR_PARSER_DELIMITER + id;
			}
		}
		return result;
	}
}
