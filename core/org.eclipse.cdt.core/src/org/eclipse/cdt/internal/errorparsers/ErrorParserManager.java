package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.ACBuilder;

public class ErrorParserManager {
	
	private static String PREF_ERROR_PARSER= "errorOutputParser";
	
	private IProject fProject;
	private IMarkerGenerator fMarkerGenerator;
	private Map fFilesInProject;
	private List fNameConflicts;
	
	private ArrayList fErrorParsers;
	
	private Vector fDirectoryStack;
	private IPath fBaseDirectory;

	private String previousLine;
	
	static String SEPARATOR = System.getProperty("file.separator");

	public ErrorParserManager(ACBuilder builder) {
		this(builder.getProject(), builder);
	}

	public ErrorParserManager(IProject project, IMarkerGenerator markerGenerator) {
		fProject= project;
		fMarkerGenerator= markerGenerator;
		fFilesInProject= new HashMap();
		fNameConflicts= new ArrayList();
		fErrorParsers= new ArrayList();
		fDirectoryStack = new Vector();
		fBaseDirectory = null;
		readPreferences();
	}
	
	public IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return (IPath)fDirectoryStack.lastElement();
		}
		return new Path("");
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
		IPath dir = (IPath)fDirectoryStack.lastElement();
		if (i != 0) {
			fDirectoryStack.removeElementAt(i-1);
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
		String parserNames= CCorePlugin.getDefault().getPluginPreferences().getString(PREF_ERROR_PARSER);
		if (parserNames != null && parserNames.length() > 0) {
			StringTokenizer tok= new StringTokenizer(parserNames, ";");
			while (tok.hasMoreElements()) {
				String clName= tok.nextToken();
				try {
					IErrorParser parser= (IErrorParser)getClass().forName(clName).newInstance();
					fErrorParsers.add(parser);
				} catch (ClassNotFoundException e) {
					// not found
					CCorePlugin.log(e);
				} catch (InstantiationException e) {
					CCorePlugin.log(e);
				} catch (IllegalAccessException e) {
					CCorePlugin.log(e);
				} catch (ClassCastException e) {
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
		errorParsers.add (new GLDErrorParser ());
		errorParsers.add (new GASErrorParser ());
		errorParsers.add (new MakeErrorParser ());
	}	
	
	
	private void savePreferences() {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < fErrorParsers.size(); i++) {
			buf.append(fErrorParsers.get(i).getClass().getName());
			buf.append(';');
		}
		CCorePlugin.getDefault().getPluginPreferences().setValue(PREF_ERROR_PARSER, buf.toString());
	}
	
	protected void collectFiles(IContainer parent, List result) {
		try {
			IResource[] resources= parent.members();
			for (int i= 0; i < resources.length; i++) {
				IResource resource= resources[i];			
				if (resource instanceof IFile) {
					result.add(resource);
				} else if (resource instanceof IContainer) {
					collectFiles((IContainer)resource, result);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}
	
	/**
	 * Parses the input and try to generate error or warning markers
	 */
	public void parse(String output) {
		// prepare file lists
		fFilesInProject.clear();
		fNameConflicts.clear();
		
		List collectedFiles= new ArrayList();		
		fBaseDirectory = fProject.getLocation();
		collectFiles(fProject, collectedFiles);
		
		for (int i= 0; i < collectedFiles.size(); i++) {
			IFile curr= (IFile)collectedFiles.get(i);
			Object existing= fFilesInProject.put(curr.getName(), curr);
			if (existing != null) {
				fNameConflicts.add(curr.getName());
			}
		}
		
		BufferedReader rd= new BufferedReader(new StringReader(output));
		try {
			String line= rd.readLine();
			while (line != null) {
				processLine(line);
				previousLine = line;
				line= rd.readLine();
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		} finally {
			try { rd.close(); } catch (IOException e) {}
		}
			
		fFilesInProject.clear();
		fNameConflicts.clear();
		fDirectoryStack.removeAllElements();		
		fBaseDirectory = null;
	}
	
	private void processLine(String line) {
		int top= fErrorParsers.size()-1;
		int i= top;
		do {
			IErrorParser curr= (IErrorParser)fErrorParsers.get(i);
			if (curr.processLine(line, this)) {
				if (i != top) {
					// move to top
					Object used= fErrorParsers.remove(i);
					fErrorParsers.add(used);
					savePreferences();
				}				
				return;
			}
			i--;
		} while (i >= 0);
	}
	
	/**
	 * Called by the error parsers.
	 */
	public IFile findFileName(String fileName) {
		IPath path= new Path(fileName);
		return (IFile)fFilesInProject.get(path.lastSegment());
	}


	/**
	 * Called by the error parsers.
	 */	
	public boolean isConflictingName(String fileName) {
		IPath path= new Path(fileName);
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
			path = (IPath)getWorkingDirectory().append(filePath);
		}	
		return (IFile)fProject.getFile(path);
	}

	/**
	 * Called by the error parsers.
	 */	
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		if (file == null) {
			fMarkerGenerator.addMarker (fProject, lineNumber, desc, severity, varName);

		} else {
			fMarkerGenerator.addMarker(file, lineNumber, desc, severity, varName);
		}
	}		

	/**
	 * Called by the error parsers.  Return the previous line, save in the working buffer.
	 */
	public String getPreviousLine() {
		return new String ((previousLine) == null ? "" : previousLine);
	}

	/**
	 * Called by the error parsers.  Overload in Makebuilder.
	 */
	public IPath getBuildCommand() {
		return new Path("");
	}

}
