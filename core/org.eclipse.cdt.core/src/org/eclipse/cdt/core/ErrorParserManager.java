package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.internal.errorparsers.GASErrorParser;
import org.eclipse.cdt.internal.errorparsers.GCCErrorParser;
import org.eclipse.cdt.internal.errorparsers.GLDErrorParser;
import org.eclipse.cdt.internal.errorparsers.MakeErrorParser;
import org.eclipse.cdt.internal.errorparsers.VCErrorParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ErrorParserManager extends OutputStream {
	private int nOpens;

	private static String PREF_ERROR_PARSER = "errorOutputParser";

	private IProject fProject;
	private IMarkerGenerator fMarkerGenerator;
	private Map fFilesInProject;
	private List fNameConflicts;

	private ArrayList fErrorParsers;
	private ArrayList fErrors;

	private Vector fDirectoryStack;
	private IPath fBaseDirectory;

	private String previousLine;
	private OutputStream outputStream;
	private StringBuffer currentLine = new StringBuffer();

	public ErrorParserManager(ACBuilder builder) {
		this(builder.getProject(), builder);
	}

	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator) {
		fProject = project;
		fErrorParsers = new ArrayList();
		fMarkerGenerator = markerGenerator;
		readPreferences();
		initParser();
	}

	private void initParser() {
		fFilesInProject = new HashMap();
		fNameConflicts = new ArrayList();
		fDirectoryStack = new Vector();
		fErrors = new ArrayList();

		// prepare file lists
		fFilesInProject.clear();
		fNameConflicts.clear();

		List collectedFiles = new ArrayList();
		fBaseDirectory = fProject.getLocation();
		collectFiles(fProject, collectedFiles);

		for (int i = 0; i < collectedFiles.size(); i++) {
			IFile curr = (IFile) collectedFiles.get(i);
			Object existing = fFilesInProject.put(curr.getName(), curr);
			if (existing != null) {
				fNameConflicts.add(curr.getName());
			}
		}
	}

	public IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return (IPath) fDirectoryStack.lastElement();
		}
		return new Path("");
	}

	public void pushDirectory(IPath dir) {
		if (dir != null) {
			IPath pwd = null;
			if (fBaseDirectory.isPrefixOf(dir)) {
				int segments = fBaseDirectory.matchingFirstSegments(dir);
				pwd = dir.removeFirstSegments(segments);
			}
			else {
				pwd = dir;
			}
			fDirectoryStack.addElement(pwd);
		}
	}

	public IPath popDirectory() {
		int i = fDirectoryStack.size();
		IPath dir = (IPath) fDirectoryStack.lastElement();
		if (i != 0) {
			fDirectoryStack.removeElementAt(i - 1);
		}
		return dir;
	}

	public int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	protected void addParser(IErrorParser parser) {
		fErrorParsers.add(parser);
	}

	private void readPreferences() {
		fErrorParsers.clear();
		String parserNames = CCorePlugin.getDefault().getPluginPreferences().getString(PREF_ERROR_PARSER);
		if (parserNames != null && parserNames.length() > 0) {
			StringTokenizer tok = new StringTokenizer(parserNames, ";");
			while (tok.hasMoreElements()) {
				String clName = tok.nextToken();
				try {
					IErrorParser parser = (IErrorParser) getClass().forName(clName).newInstance();
					fErrorParsers.add(parser);
				}
				catch (ClassNotFoundException e) {
					// not found
					CCorePlugin.log(e);
				}
				catch (InstantiationException e) {
					CCorePlugin.log(e);
				}
				catch (IllegalAccessException e) {
					CCorePlugin.log(e);
				}
				catch (ClassCastException e) {
					CCorePlugin.log(e);
				}
			}
		}
		if (fErrorParsers.size() == 0) {
			initErrorParsersArray(fErrorParsers);
		}
		savePreferences();
	}

	private void initErrorParsersArray(List errorParsers) {
		errorParsers.add(new VCErrorParser());
		errorParsers.add(new GCCErrorParser());
		errorParsers.add(new GLDErrorParser());
		errorParsers.add(new GASErrorParser());
		errorParsers.add(new MakeErrorParser());
	}

	private void savePreferences() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < fErrorParsers.size(); i++) {
			buf.append(fErrorParsers.get(i).getClass().getName());
			buf.append(';');
		}
		CCorePlugin.getDefault().getPluginPreferences().setValue(PREF_ERROR_PARSER, buf.toString());
	}

	protected void collectFiles(IContainer parent, List result) {
		try {
			IResource[] resources = parent.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					result.add(resource);
				}
				else if (resource instanceof IContainer) {
					collectFiles((IContainer) resource, result);
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	/**
	 * Parses the input and try to generate error or warning markers
	 */
	public void parse(String output) {
		BufferedReader rd = new BufferedReader(new StringReader(output));
		try {
			String line = rd.readLine();
			while (line != null) {
				processLine(line);
				previousLine = line;
				line = rd.readLine();
			}
		}
		catch (IOException e) {
			CCorePlugin.log(e);
		}
		finally {
			try {
				rd.close();
			}
			catch (IOException e) {
			}
		}

		fDirectoryStack.removeAllElements();
		fBaseDirectory = null;
	}

	private void processLine(String line) {
		int top = fErrorParsers.size() - 1;
		int i = top;
		do {
			IErrorParser curr = (IErrorParser) fErrorParsers.get(i);
			if (curr.processLine(line, this)) {
				if (i != top) {
					// move to top
					Object used = fErrorParsers.remove(i);
					fErrorParsers.add(used);
					savePreferences();
				}
				return;
			}
			i--;
		}
		while (i >= 0);
	}

	/**
	 * Called by the error parsers.
	 */
	public IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return (IFile) fFilesInProject.get(path.lastSegment());
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
			}
			else {
				path = fp;
			}
		}
		else {
			path = (IPath) getWorkingDirectory().append(filePath);
		}
		return (IFile) fProject.getFile(path);
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
	}

	/**
	 * Called by the error parsers.  Return the previous line, save in the working buffer.
	 */
	public String getPreviousLine() {
		return new String((previousLine) == null ? "" : previousLine);
	}

	/**
	 * Method setOutputStream.
	 * @param cos
	 */
	public void setOutputStream(OutputStream os) {
		outputStream = os;
	}

	/**
	 * Method getInputStream.
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
			fDirectoryStack.removeAllElements();
			fBaseDirectory = null;
			outputStream.close();
		}
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		outputStream.flush();
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		currentLine.append((char) b);
		checkLine();
		outputStream.write(b);
	}

	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		}
		else if (off != 0 || (len < 0) || (len > b.length)) {
			throw new IndexOutOfBoundsException();
		}
		else if (len == 0) {
			return;
		}
		currentLine.append(new String(b, 0, len));
		checkLine();
		outputStream.write(b, off, len);
	}

	private void checkLine() {
		String line = currentLine.toString();
		if (line.endsWith("\n")) {
			processLine(line);
			previousLine = line;
			currentLine.setLength(0);
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
				}
				else {
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
}
