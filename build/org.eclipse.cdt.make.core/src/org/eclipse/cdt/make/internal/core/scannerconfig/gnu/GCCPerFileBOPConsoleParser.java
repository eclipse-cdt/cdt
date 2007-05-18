/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;


/**
 * GCC per file build output parser
 * 
 * @author vhirsl
 */
public class GCCPerFileBOPConsoleParser extends AbstractGCCBOPConsoleParser {
    private final static String[] FILE_EXTENSIONS = {
        ".c", ".cc", ".cpp", ".cxx", ".C", ".CC", ".CPP", ".CXX" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    };
    private final static List FILE_EXTENSIONS_LIST = Arrays.asList(FILE_EXTENSIONS);
    
    private String[] compilerInvocation;
    private GCCPerFileBOPConsoleParserUtility fUtil;
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new GCCPerFileBOPConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        super.startup(project, collector);
        
        // check additional compiler commands from extension point manifest
        compilerInvocation = getCompilerCommands();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#getUtility()
     */
    protected AbstractGCCBOPConsoleParserUtility getUtility() {
        return fUtil;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#processSingleLine(java.lang.String)
     */
    protected boolean processSingleLine(String line) {
        boolean rc = false;
        // GCC C/C++ compiler invocation 
        int compilerInvocationIndex = -1;
        for (int cii = 0; cii < compilerInvocation.length; ++cii) {
            compilerInvocationIndex = line.indexOf(compilerInvocation[cii]);
            if (compilerInvocationIndex != -1)
                break;
        }
        if (compilerInvocationIndex == -1)
            return rc;

        // split and unquote all segments; supports build command such as 
        // sh -c 'gcc -g -O0 -I"includemath" -I "include abc" -Iincludeprint -c impl/testmath.c'
        ArrayList split = splitLine(line, compilerInvocationIndex);

        // get the position of the compiler command in the build command
        for (compilerInvocationIndex=0; compilerInvocationIndex<split.size(); compilerInvocationIndex++) {
	        String command = (String)split.get(compilerInvocationIndex);
	        // verify that it is compiler invocation
	        int cii2 = -1;
	        for (int cii = 0; cii < compilerInvocation.length; ++cii) {
	        	cii2 = command.indexOf(compilerInvocation[cii]);
	        	if (cii2 >= 0)
	                break;
	        }
        	if (cii2 >= 0)
                break;
        }    
	    if (compilerInvocationIndex >= split.size()) {
            TraceUtil.outputTrace("Error identifying compiler command", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        // find a file name
        int extensionsIndex = -1;
        boolean found = false;
        String filePath = null;
        for (int i = compilerInvocationIndex+1; i < split.size(); ++i) {
        	String segment = (String)split.get(i);
            int k = segment.lastIndexOf('.');
            if (k != -1 && (segment.length() - k < 5)) {
                String fileExtension = segment.substring(k);
                extensionsIndex = FILE_EXTENSIONS_LIST.indexOf(fileExtension);
                if (extensionsIndex != -1) {
                    filePath = segment;
                    found = true;
                    break;
                }
            }
        }
//              for (int j = 0; j < FILE_EXTENSIONS.length; ++j) {
//                  if (split[i].endsWith(FILE_EXTENSIONS[j])) {
//                      filePath = split[i];
//                      extensionsIndex = j;
//                      found = true;
//                      break;
//                  }
//              }
//              if (found) break;
        if (!found) {
            TraceUtil.outputTrace("Error identifying file name :1", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        // sanity check
        if (filePath.indexOf(FILE_EXTENSIONS[extensionsIndex]) == -1) {
            TraceUtil.outputTrace("Error identifying file name :2", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        if (fUtil != null) {
            IPath pFilePath = fUtil.getAbsolutePath(filePath);
            String shortFileName = pFilePath.removeFileExtension().lastSegment();

            // generalize occurances of the file name
            for (int i = 0; i < split.size(); i++) {
				String token = (String)split.get(i);
				if (token.equals("-include")) { //$NON-NLS-1$
					++i;
				}
				else if (token.equals("-imacros")) { //$NON-NLS-1$
					++i;
				}
				else if (token.equals(filePath)) {
					split.set(i, "LONG_NAME"); //$NON-NLS-1$
				}
				else if (token.startsWith(shortFileName)) {
					split.set(i, token.replaceFirst(shortFileName, "SHORT_NAME")); //$NON-NLS-1$
				}
			}
            
            CCommandDSC cmd = fUtil.getNewCCommandDSC((String[])split.toArray(new String[split.size()]), extensionsIndex > 0);
            IPath baseDirectory = fUtil.getBaseDirectory();
            if (baseDirectory.isPrefixOf(pFilePath)) {
	            List cmdList = new ArrayList();
	            cmdList.add(cmd);
	            Map sc = new HashMap(1);
	            sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);

				IPath relPath = pFilePath.removeFirstSegments(baseDirectory.segmentCount());
				//Note: We add the scannerconfig even if the resource doesnt actually
				//exist below this project (which may happen when reading existing
				//build logs, because resources can be created as part of the build
				//and may not exist at the time of analyzing the config but re-built
				//later on.
				//if (getProject().exists(relPath)) {
	            IFile file = getProject().getFile(relPath);
	            getCollector().contributeToScannerConfig(file, sc);
            } else {
            	//TODO limiting to pathes below this project means not being
            	//able to work with linked resources. Linked resources could
            	//be checked through IWorkspaceRoot.findFilesForLocation().
            	TraceUtil.outputError("Build command for file outside project: "+pFilePath.toString(), line); //$NON-NLS-1$
            }
            // fUtil.addGenericCommandForFile2(longFileName, genericLine);
        }
        return rc;
    }

    /**
     * Splits and unquotes all compiler command segments; supports build command such as 
     *    sh -c 'gcc -g -O0 -I"includemath" -I "include abc" -Iincludeprint -c impl/testmath.c'
     */
    private ArrayList splitLine(String line, int compilerInvocationIndex) {
        ArrayList split = new ArrayList();
        boolean bSingleQuotes = false;
        boolean bIgnoreSingleQuotes = false;
        boolean bDoubleQuotes = false;
        boolean bIgnoreDoubleQuotes = false;
        char[] chars = line.toCharArray();
        int charPos = 0;
        int length = line.length();
        boolean quit = false;
        boolean acceptExtraSingleQuote = false;
        boolean acceptExtraDoubleQuote = false;

        // eat whitespace
        while (charPos < length) {
        	char ch = chars[charPos];
        	if (!Character.isWhitespace(ch)) {
        		break;
        	}
        	charPos++;
        }
        // read token
        while (charPos<length && !quit) {
	        int startPos = -1;
	        int endPos = -1;
	        while (charPos<length && !quit) {
	        	char ch = chars[charPos];
	        	if (ch == '\'') {
	        		// ignore quotes before the actual compiler command (the command itself including its options
	        		// could be within quotes--in this case we nevertheless want to split the compiler command into segments)
	        		if (charPos <= compilerInvocationIndex) {
	        			bIgnoreSingleQuotes = !bIgnoreSingleQuotes;
	        		}
	        		else {
	        			if (bIgnoreSingleQuotes) {
	        				bIgnoreSingleQuotes = false;
	    	        		if (startPos >= 0) {
	    	        			endPos = charPos;  // end of a token
	    	        		}
    	        			quit = true;  // quit after closed quote containing the actual compiler command
	        			}
	        			else {
	        				bSingleQuotes = !bSingleQuotes;
	        			}
	        		}
// do split token here: allow -DMYKEY='MYVALUE' or-DMYKEY=\'MYVALUE\' 
	        		if (startPos >= 0) {
	        			char prevch = charPos > 0 ? chars[charPos-1] : '\0';
	        			if (acceptExtraSingleQuote) {
	        				acceptExtraSingleQuote = false;
	        			}
	        			else if (prevch != '=' && prevch != '\\') {
	        				endPos = charPos;  // end of a token
	        			}
	        			else {
	        				acceptExtraSingleQuote = true;
	        			}
	        		}
	        	}
	        	else if (ch == '"') {
	        		// ignore quotes before the actual compiler command (the command itself including its options
	        		// could be within quotes--in this case we nevertheless want to split the compiler command into segments)
	        		if (charPos <= compilerInvocationIndex) {
	        			bIgnoreDoubleQuotes = !bIgnoreDoubleQuotes;
	        		}
	        		else {
	        			if (bIgnoreDoubleQuotes) {
	        				bIgnoreDoubleQuotes = false;
	    	        		if (startPos >= 0) {
	    	        			endPos = charPos;  // end of a token
	    	        		}
    	        			quit = true;  // quit after closed quote containing the actual compiler command
	        			}
	        			else {
	    	        		bDoubleQuotes = !bDoubleQuotes;
	        			}
	        		}
// do split token here: allow -DMYKEY="MYVALUE" or-DMYKEY=\"MYVALUE\" 
	        		if (startPos >= 0) {
	        			char prevch = charPos > 0 ? chars[charPos-1] : '\0';
	        			if (acceptExtraDoubleQuote) {
	        				acceptExtraDoubleQuote = false;
	        			}
	        			else if (prevch != '=' && prevch != '\\') {
	        				endPos = charPos;  // end of a token
	        			}
	        			else {
	        				acceptExtraDoubleQuote = true;
	        			}
	        		}
	        	}
	        	else if (Character.isWhitespace(ch) || ch == ';') {
	        		if (startPos < 0 && (bSingleQuotes || bDoubleQuotes)) {
	        			startPos = charPos;
	        		}
	        		else if (startPos >= 0 && !bSingleQuotes && !bDoubleQuotes) {
	        			endPos = charPos;  // end of a token
	        		}
	        	}
	        	else {  // a valid character, starts or continues a token
	        		if (startPos < 0) {
	        			startPos = charPos;
	        		}
	        		if (charPos == length-1) {
	        			endPos = charPos+1;   // end of token
	        		}
	        	}
	        	charPos++;
	        	// a complete token has been found
	        	if (startPos >= 0 && endPos > startPos) {
	        		break;
	        	}
	        }
	    	if (startPos >= 0 && endPos >= 0 && startPos >= compilerInvocationIndex) {
	    		split.add(line.substring(startPos, endPos));
	    	}
        }
        return split;
    }
}
