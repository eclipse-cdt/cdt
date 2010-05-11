/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.KVStringPair;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SCDOptionsEnum;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.xlc.core.scannerconfig.util.XLCCommandDSC;
import org.eclipse.cdt.utils.FileSystemUtilityManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author crecoskie
 *
 */
public class XLCBuildOutputParserUtility {
    protected class Problem {
        protected String description;
        protected IResource file;
        protected int lineNumber;
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
    public static IPath convertCygpath(IPath path) {
    	if (path.segmentCount() > 1 && path.segment(0).equals("cygdrive")) { //$NON-NLS-1$
            StringBuffer buf = new StringBuffer(2);
            buf.append(Character.toUpperCase(path.segment(1).charAt(0)));
            buf.append(':');
            path = path.removeFirstSegments(2);
            path = path.setDevice(buf.toString());
            path = path.makeAbsolute();
        }
    	return path;
	}
    private List commandsList2;
    private int commandsN = 0;
    private List compiledFileList;

    private Map directoryCommandListMap;

    private IPath fBaseDirectory;
    private String fDefaultMacroDefinitionValue= "1"; //$NON-NLS-1$
    private Vector<IPath> fDirectoryStack;
    private ArrayList<Problem> fErrors;
    private int filesN = 0;

    private IMarkerGenerator fMarkerGenerator;

    private IProject project;

    private int workingDirsN = 0;
    
	/*
	 * For tracking the location of files being compiled
	 */
	private Map<String, IFile> fFilesInProject;
	private List<String> fCollectedFiles;
	private List<String> fNameConflicts;

	/**
     * 
     */
    public XLCBuildOutputParserUtility(IProject project, IPath workingDirectory,
                                              IMarkerGenerator markerGenerator) {
        fDirectoryStack = new Vector<IPath>();
        fErrors = new ArrayList<Problem>();
        this.project = project;
        fBaseDirectory = getPathForResource(project);
        if (workingDirectory != null) {
            pushDirectory(workingDirectory);
        }
    }

	private IPath getPathForResource(IResource resource) {
		// TODO: when the file system utility stuff is in, this will have to call it to get the path
		// for now, get the path from the URI
		URI locationURI = resource.getLocationURI();
		IPath path = new Path(locationURI.getPath());
		return path;
	}

    /**
     * Adds a mapping filename, generic_command
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile(String longFileName, String genericCommand) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);
        
        String workingDir = getWorkingDirectory().toString();
        List directoryCommandList = (List) directoryCommandListMap.get(workingDir);
        if (directoryCommandList == null) {
            directoryCommandList = new ArrayList();
            directoryCommandListMap.put(workingDir, directoryCommandList);
            ++workingDirsN;
        }
        Map command21FileListMap = null;
        for (Iterator i = directoryCommandList.iterator(); i.hasNext(); ) {
            command21FileListMap = (Map) i.next();
            List fileList = (List) command21FileListMap.get(genericCommand);
            if (fileList != null) {
                if (!fileList.contains(longFileName)) {
                    fileList.add(longFileName);
                    ++filesN;
                }
                return;
            }
        }
        command21FileListMap = new HashMap(1);
        directoryCommandList.add(command21FileListMap);
        ++commandsN;
        List fileList = new ArrayList();
        command21FileListMap.put(genericCommand, fileList);
        fileList.add(longFileName);
        ++filesN;
    }

    /**
     * Adds a mapping command line -> file, this time without a dir
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile2(String longFileName, String genericLine) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);

        String[] tokens = genericLine.split("\\s+"); //$NON-NLS-1$
        CCommandDSC command = getNewCCommandDSC(tokens, 0, false); // assume .c file type
        int index = commandsList2.indexOf(command);
        if (index == -1) {
            commandsList2.add(command);
            ++commandsN;
        }
        else {
            command = (CCommandDSC) commandsList2.get(index);
        }
//        // add a file
//        command.addFile(longFileName);
//        ++filesN;
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

    /**
     * 
     */
    void generateReport() {
        TraceUtil.metricsTrace("Stats for directory ", //$NON-NLS-1$
                   "Generic command: '", "' applicable for:",  //$NON-NLS-1$ //$NON-NLS-2$
                   directoryCommandListMap);
        TraceUtil.summaryTrace("Discovery summary", workingDirsN, commandsN, filesN); //$NON-NLS-1$
    }
    
    /**
     * @param filePath : String
     * @return filePath : IPath - not <code>null</code>
     */
    public IPath getAbsolutePath(String filePath) {
        IPath pFilePath;
        if (filePath.startsWith("/")) { //$NON-NLS-1$
        	return convertCygpath(new Path(filePath));
        }
        else if (filePath.startsWith("\\") || //$NON-NLS-1$
            (!filePath.startsWith(".") && //$NON-NLS-1$
             filePath.length() > 2 && filePath.charAt(1) == ':' && 
             (filePath.charAt(2) == '\\' || filePath.charAt(2) == '/'))) {
            // absolute path
            pFilePath = new Path(filePath);
        }
        else {
            // relative path
            IPath cwd = getWorkingDirectory();
            if (!cwd.isAbsolute()) {
                cwd = getBaseDirectory().append(cwd);
            }
            if (filePath.startsWith("`pwd`")) { //$NON-NLS-1$
            	if (filePath.length() > 5 && (filePath.charAt(5) == '/' || filePath.charAt(5) == '\\')) {
            		filePath = filePath.substring(6);
            	}
            	else {
            		filePath = filePath.substring(5);
            	}
            }
            pFilePath = cwd.append(filePath);
        }
        return pFilePath;
    }
    /**
     * @return Returns the fBaseDirectory.
     */
    public IPath getBaseDirectory() {
        return fBaseDirectory;
    }
    
    /**
     * Returns all CCommandDSC collected so far.
     * Currently this list is not filled, so it will always return an empty list.
     * @return List of CCommandDSC
     */
    public List getCCommandDSCList() {
        return new ArrayList(commandsList2);
    }
    
    protected int getDirectoryLevel() {
        return fDirectoryStack.size();
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
     * @param genericLine
     * @param cppFileType
     * @return CCommandDSC compile command description 
     */
    public CCommandDSC getNewCCommandDSC(String[] tokens, final int idxOfCompilerCommand, boolean cppFileType) {
		ArrayList dirafter = new ArrayList();
		ArrayList includes = new ArrayList();
        XLCCommandDSC command = new XLCCommandDSC(cppFileType, getProject());
        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), tokens[idxOfCompilerCommand]));
        for (int i = idxOfCompilerCommand+1; i < tokens.length; ++i) {
        	String token = tokens[i];
        	//Target specific options: see GccScannerInfoConsoleParser
			if (token.startsWith("-m") ||		//$NON-NLS-1$
				token.equals("-ansi") ||		//$NON-NLS-1$
				token.equals("-posix") ||		//$NON-NLS-1$
				token.equals("-pthread") ||		//$NON-NLS-1$
				token.startsWith("-O") ||		//$NON-NLS-1$
				token.equals("-fno-inline") ||	//$NON-NLS-1$
				token.startsWith("-finline") ||	//$NON-NLS-1$
				token.equals("-fno-exceptions") ||	//$NON-NLS-1$
				token.equals("-fexceptions") ||		//$NON-NLS-1$
				token.equals("-fshort-wchar") ||	//$NON-NLS-1$
				token.equals("-fshort-double") ||	//$NON-NLS-1$
				token.equals("-fno-signed-char") ||	//$NON-NLS-1$
				token.equals("-fsigned-char") ||	//$NON-NLS-1$
				token.startsWith("-fabi-version=")	//$NON-NLS-1$
			) {		
		        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), token));
				continue;
        	}
            for (int j = SCDOptionsEnum.MIN; j <= SCDOptionsEnum.MAX; ++j) {
                final SCDOptionsEnum optionKind = SCDOptionsEnum.getSCDOptionsEnum(j);
				if (token.startsWith(optionKind.toString())) {
                    String option = token.substring(
                            optionKind.toString().length()).trim();
                    if (option.length() > 0) {
                        // ex. -I/dir
                    }
                    else if (optionKind.equals(SCDOptionsEnum.IDASH)) {
                    	for (Iterator iter=includes.iterator(); iter.hasNext(); ) {
                    		option = (String)iter.next();
                            KVStringPair pair = new KVStringPair(SCDOptionsEnum.IQUOTE.toString(), option);
                        	command.addSCOption(pair);                    		
                    	}
                    	includes = new ArrayList();
                        // -I- has no parameter
                    }
                    else {
                        // ex. -I /dir
                        // take a next token
                        if (i+1 < tokens.length && !tokens[i+1].startsWith("-")) { //$NON-NLS-1$
                            option = tokens[++i];
                        }
                        else break;
                    }
                    
                    if (option.length() > 0 && (
                            optionKind.equals(SCDOptionsEnum.INCLUDE) ||
                            optionKind.equals(SCDOptionsEnum.INCLUDE_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IMACROS_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IDIRAFTER) ||
                            optionKind.equals(SCDOptionsEnum.ISYSTEM) || 
                            optionKind.equals(SCDOptionsEnum.IQUOTE) )) {
                        option = (getAbsolutePath(option)).toString();
                    }
                    
                    if (optionKind.equals(SCDOptionsEnum.IDIRAFTER)) {
                        KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
                    	dirafter.add(pair);
                    }
                    else if (optionKind.equals(SCDOptionsEnum.INCLUDE)) {
                    	includes.add(option);
                    }
                    else { // add the pair
                    	if (optionKind.equals(SCDOptionsEnum.DEFINE)) {
                        	if (option.indexOf('=') == -1) {
                        		option += '='+ fDefaultMacroDefinitionValue;
                        	}
                    	}
                        KVStringPair pair = new KVStringPair(optionKind.toString(), option);
                    	command.addSCOption(pair);
                    }
                    break;
                }
            }
        }
        String option;
    	for (Iterator iter=includes.iterator(); iter.hasNext(); ) {
    		option = (String)iter.next();
            KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
        	command.addSCOption(pair);                    		
    	}
    	for (Iterator iter=dirafter.iterator(); iter.hasNext(); ) {
        	command.addSCOption((KVStringPair)iter.next());                    		
    	}
        return command;
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

    protected IPath popDirectory() {
        int i = getDirectoryLevel();
        if (i != 0) {
            IPath dir = fDirectoryStack.lastElement();
            fDirectoryStack.removeElementAt(i - 1);
            return dir;
        }
        return new Path("");    //$NON-NLS-1$
    }

    protected void pushDirectory(IPath dir) {
        if (dir != null) {
            IPath pwd = null;
            if (fBaseDirectory != null && fBaseDirectory.isPrefixOf(dir)) {
                pwd = dir.removeFirstSegments(fBaseDirectory.segmentCount());
            } else {
                // check if it is a cygpath
            	pwd= convertCygpath(dir);
            }
            fDirectoryStack.addElement(pwd);
        }
    }

	public boolean reportProblems() {
        boolean reset = false;
        for (Iterator<Problem> iter = fErrors.iterator(); iter.hasNext(); ) {
            Problem problem = iter.next();
            if (problem.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
                reset = true;
            }
            if (problem.file == null) {
                fMarkerGenerator.addMarker(new ProblemMarkerInfo(
                    project,
                    problem.lineNumber,
                    problem.description,
                    problem.severity,
                    problem.variableName));
            } else {
                fMarkerGenerator.addMarker(new ProblemMarkerInfo(
                    problem.file,
                    problem.lineNumber,
                    problem.description,
                    problem.severity,
                    problem.variableName));
            }
        }
        fErrors.clear();
        return reset;
    }

    public void setDefaultMacroDefinitionValue(String val) {
    	if (val != null) {
    		fDefaultMacroDefinitionValue= val;
    	}
	}
    
    public String getDefaultMacroDefinitionValue() {
    	return fDefaultMacroDefinitionValue;
    }
    
	public String normalizePath(String path) {
		int column = path.indexOf(':');
		if (column > 0) {
			char driveLetter = path.charAt(column - 1);
			if (Character.isLowerCase(driveLetter)) {
				StringBuffer sb = new StringBuffer();
				if (column - 1 > 0) {
					sb.append(path.substring(0, column-1));
				}
				sb.append(Character.toUpperCase(driveLetter));
				sb.append(path.substring(column));
				path = sb.toString();
			}
		}
		if (path.indexOf('.') == -1 || path.equals(".")) {	//$NON-NLS-1$
			return (new Path(path)).toString();	// convert separators to '/'
		}
		// lose "./" segments since they confuse the Path normalization
		StringBuffer buf = new StringBuffer(path);
		int len = buf.length();
		StringBuffer newBuf = new StringBuffer(buf.length());
		int scp = 0; // starting copy point
		int ssp = 0;	// starting search point
		int sdot;
		boolean validPrefix;
		while (ssp < len && (sdot = buf.indexOf(".", ssp)) != -1) {	//$NON-NLS-1$
			validPrefix = false;
			int ddot = buf.indexOf("..", ssp);//$NON-NLS-1$
			if (sdot < ddot || ddot == -1) {
				newBuf.append(buf.substring(scp, sdot));
				scp = sdot;
				ssp = sdot + 1;
				if (ssp < len) {
					if (sdot == 0 || buf.charAt(sdot - 1) == '/' || buf.charAt(sdot - 1) == '\\') {
						validPrefix = true;
					}
					char nextChar = buf.charAt(ssp);
					if (validPrefix && nextChar == '/') {
						++ssp;
						scp = ssp;
					}
					else if (validPrefix && nextChar == '\\') {
						++ssp;
						if (ssp < len - 1 && buf.charAt(ssp) == '\\') {
							++ssp;
						}
						scp = ssp;
					}
					else {
						// no path delimiter, must be '.' inside the path
						scp = ssp - 1;
					}
				}
			}
			else if (sdot == ddot) {
				ssp = sdot + 2;
			}
		}
		newBuf.append(buf.substring(scp, len));
					 
		IPath orgPath = new Path(newBuf.toString());
		return orgPath.toString();
	}

	
	/**
	 * Called by the console line parsers to find a file with a given name.
	 * @param fileName
	 * @return IFile or null
	 */
	public IFile findFile(String fileName) {
		IFile file = findFilePath(fileName);
		if (file == null) {
			// Try the project's map.
			file = findFileName(fileName);
			if (file != null) {
				// If there is a conflict then try all files in the project.
				if (isConflictingName(fileName)) {
					file = null;
					
					// Create a problem marker
					final String error = MakeMessages.getString("ConsoleParser.Ambiguous_Filepath_Error_Message"); //$NON-NLS-1$
					TraceUtil.outputError(error, fileName);
					generateMarker(getProject(), -1, error+fileName, IMarkerGenerator.SEVERITY_WARNING, null);
				}
			}
		}
		return file;
	}
	
	/**
	 * @param filePath
	 * @return
	 */
	protected IFile findFilePath(String filePath) {
		IPath path = null;
		IPath fp = new Path(filePath);
		if (fp.isAbsolute()) {
			if (getBaseDirectory().isPrefixOf(fp)) {
				int segments = getBaseDirectory().matchingFirstSegments(fp);
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

	/**
	 * @param fileName
	 * @return
	 */
	protected IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return (IFile) fFilesInProject.get(path.lastSegment());
	}

	protected IFile findFileInWorkspace(IPath path) {
		IFile file = null;
		if (path.isAbsolute()) {
			IWorkspaceRoot root = getProject().getWorkspace().getRoot();

			// construct a URI, based on the project's locationURI, that points
			// to the given path
			URI projectURI = project.getLocationURI();

			URI newURI = FileSystemUtilityManager.getDefault().replacePath(projectURI, path.toString());

			IFile[] files = root.findFilesForLocationURI(newURI);

			for (int i = 0; i < files.length; i++) {
				if (files[i].getProject().equals(getProject())) {
					file = files[i];
					break;
				}
			}

		} else {
			file = getProject().getFile(path);
		}
		return file;
	}

	protected void collectFiles(IContainer parent, List result) {
		try {
			IResource[] resources = parent.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					result.add(resource);
				} else if (resource instanceof IContainer) {
					collectFiles((IContainer) resource, result);
				}
			}
		} catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
	}

	protected boolean isConflictingName(String fileName) {
		IPath path = new Path(fileName);
		return fNameConflicts.contains(path.lastSegment());
	}

	public List translateRelativePaths(IFile file, String fileName, List includes) {
		List translatedIncludes = new ArrayList(includes.size());
		for (Iterator i = includes.iterator(); i.hasNext(); ) {
			String include = (String) i.next();
			IPath includePath = new Path(include);
			if (!includePath.isAbsolute() && !includePath.isUNC()) {	// do not translate UNC paths
				// First try the current working directory
				IPath cwd = getWorkingDirectory();
				if (!cwd.isAbsolute()) {
					cwd = getBaseDirectory().append(cwd);
				}
				
				IPath filePath = new Path(fileName);
				if (!filePath.isAbsolute()) {
					// check if the cwd is the right one
					// appending fileName to cwd should yield file path
					filePath = cwd.append(fileName);
				}
				if (!filePath.toString().equalsIgnoreCase(file.getLocation().toString())) {
					// must be the cwd is wrong
					// check if file name starts with ".."
					if (fileName.startsWith("..")) {	//$NON-NLS-1$
						// probably multiple choices for cwd, hopeless
						final String error = MakeMessages.getString("ConsoleParser.Working_Directory_Error_Message"); //$NON-NLS-1$
						TraceUtil.outputError(error, fileName);
						generateMarker(file, -1, error,	 IMarkerGenerator.SEVERITY_WARNING, fileName);				
						break;
					}
					else {
						// remove common segments at the end 
						IPath tPath = new Path(fileName);
						if (fileName.startsWith(".")) {	//$NON-NLS-1$
							tPath = tPath.removeFirstSegments(1);
						}
						// get the file path from the file
						filePath = file.getLocation();
						IPath lastFileSegment = filePath.removeFirstSegments(filePath.segmentCount() - tPath.segmentCount());
						if (lastFileSegment.matchingFirstSegments(tPath) == tPath.segmentCount()) {
							cwd = filePath.removeLastSegments(tPath.segmentCount());
						}
					}
				}
				
				IPath candidatePath = cwd.append(includePath);
				File dir = candidatePath.toFile();
				include = candidatePath.toString();
				if (!dir.exists()) {
					final String error = MakeMessages.getString("ConsoleParser.Nonexistent_Include_Path_Error_Message"); //$NON-NLS-1$
					TraceUtil.outputError(error, include);
//					generateMarker(file, -1, error+include, IMarkerGenerator.SEVERITY_WARNING, fileName);				
				}
			}
			// TODO VMIR for now add unresolved paths as well
			translatedIncludes.add(include);
		}
		return translatedIncludes;
	}
}
