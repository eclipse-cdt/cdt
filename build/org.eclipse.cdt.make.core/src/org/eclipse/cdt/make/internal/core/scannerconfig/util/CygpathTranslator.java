/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Hans-Erik Floryd (hef-cdt@rt-labs.com)  - http://bugs.eclipse.org/245692
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.ICygwinToolsFactroy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Use binary parser's 'cygpath' command to translate cygpaths to absolute paths.
 * 
 * @author vhirsl
 */
public class CygpathTranslator {
	/** Default Cygwin root dir */
	private static final String DEFAULT_CYGWIN_ROOT= "C:\\cygwin"; //$NON-NLS-1$
	//    private static final String CYGPATH_ERROR_MESSAGE = "CygpathTranslator.NotAvailableErrorMessage"; //$NON-NLS-1$
    private CygPath cygPath = null;
    private boolean isAvailable = false;
    
    public CygpathTranslator(IProject project) {
        try {
            ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
            for (int i = 0; i < parserRef.length; i++) {
                try {
                    IBinaryParser parser = (IBinaryParser)parserRef[i].createExtension();
                    ICygwinToolsFactroy cygwinToolFactory = (ICygwinToolsFactroy) parser.getAdapter(ICygwinToolsFactroy.class);
                    if (cygwinToolFactory != null) {
                        cygPath = cygwinToolFactory.getCygPath();
                        if (cygPath != null) {
                            isAvailable = true;
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                }
            }
            // No CygPath specified in BinaryParser page or not supported.
            // Hoping that cygpath is on the path.
            if (cygPath == null && Platform.getOS().equals(Platform.OS_WIN32)) {
            	if (new File(DEFAULT_CYGWIN_ROOT).exists()) {
                    cygPath = new CygPath(DEFAULT_CYGWIN_ROOT + "\\bin\\cygpath.exe"); //$NON-NLS-1$
            	} else {
            		cygPath = new CygPath("cygpath"); //$NON-NLS-1$
            	}
                isAvailable = cygPath.getFileName("test").equals("test"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (CoreException e) {
        }
        catch (IOException e) {
            isAvailable = false;
            // Removing markers. if cygpath isn't in your path then you aren't using cygwin.
            // Then why are we calling this....
//            scMarkerGenerator.addMarker(project, -1,
//                    MakeMessages.getString(CYGPATH_ERROR_MESSAGE),
//                    IMarkerGenerator.SEVERITY_WARNING, null);
        }
    }
    
    public static List<String> translateIncludePaths(IProject project, List<String> sumIncludes) {
    	// first check if cygpath translation is needed at all
    	boolean translationNeeded = false;
    	if (Platform.getOS().equals(Platform.OS_WIN32)) {
	    	for (Iterator<String> i = sumIncludes.iterator(); i.hasNext(); ) {
				String include = i.next();
				if (include.startsWith("/")) { //$NON-NLS-1$
					translationNeeded = true;
					break;
				}
			}
    	}
    	if (!translationNeeded) {
    		return sumIncludes;
    	}
    	
        CygpathTranslator cygpath = new CygpathTranslator(project);
        
        List<String> translatedIncludePaths = new ArrayList<String>();
        for (Iterator<String> i = sumIncludes.iterator(); i.hasNext(); ) {
            String includePath = i.next();
            IPath realPath = new Path(includePath);
            // only allow native pathes if they have a device prefix
            // to avoid matches on the current drive, e.g. /usr/bin = C:\\usr\\bin
            if (realPath.getDevice() != null && realPath.toFile().exists()) {
                translatedIncludePaths.add(includePath);
            }
            else {
                String translatedPath = includePath;
                if (cygpath.isAvailable) {
                    try {
                        translatedPath = cygpath.cygPath.getFileName(includePath);
                    }
                    catch (IOException e) {
                        TraceUtil.outputError("CygpathTranslator unable to translate path: ", includePath); //$NON-NLS-1$
                    }
                } else if (realPath.segmentCount() >= 2) {
                	// try default conversions
                	//     /cygdrive/x/ --> X:\
                	if ("cygdrive".equals(realPath.segment(0))) { //$NON-NLS-1$
                		String drive= realPath.segment(1);
                		if (drive.length() == 1) {
                			translatedPath= realPath.removeFirstSegments(2).makeAbsolute().setDevice(drive.toUpperCase() + ':').toOSString();
                		}
                	}
                }
                if (!translatedPath.equals(includePath)) {
                    // Check if the translated path exists
                    if (new File(translatedPath).exists()) {
                        translatedIncludePaths.add(translatedPath);
                    }
                    else if (cygpath.isAvailable) {
                        // TODO VMIR for now add even if it does not exist
                        translatedIncludePaths.add(translatedPath);
                    }
                    else {
                        translatedIncludePaths.add(includePath);
                    }
                }
                else {
                    // TODO VMIR for now add even if it does not exist
                    translatedIncludePaths.add(translatedPath);
                }
            }
        }
        if (cygpath.cygPath != null) {
        	cygpath.cygPath.dispose();
        }
        return translatedIncludePaths;
    }

}
