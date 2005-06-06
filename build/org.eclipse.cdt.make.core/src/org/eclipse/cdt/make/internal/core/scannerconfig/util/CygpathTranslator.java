/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
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
    private static final String CYGPATH_ERROR_MESSAGE = "CygpathTranslator.NotAvailableErrorMessage"; //$NON-NLS-1$
    private CygPath cygPath = null;
    private boolean isAvailable = false;
    
    public CygpathTranslator(IProject project) {
        SCMarkerGenerator scMarkerGenerator = new SCMarkerGenerator();
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
                cygPath = new CygPath("cygpath"); //$NON-NLS-1$
                isAvailable = true;
            }
        }
        catch (CoreException e) {
        }
        catch (IOException e) {
            isAvailable = false;
            scMarkerGenerator = new SCMarkerGenerator();
            scMarkerGenerator.addMarker(project, -1, 
                    MakeMessages.getString(CYGPATH_ERROR_MESSAGE),
                    IMarkerGenerator.SEVERITY_WARNING, null);
        }
        if (isAvailable) {
            // remove problem markers
            scMarkerGenerator.removeMarker(project, -1, 
                    MakeMessages.getString(CYGPATH_ERROR_MESSAGE),
                    IMarkerGenerator.SEVERITY_WARNING, null);
        }
    }
    
    /**
     * @param sumIncludes
     * @return
     */
    public static List translateIncludePaths(IProject project, List sumIncludes) {
        CygpathTranslator cygpath = new CygpathTranslator(project);
        if (cygpath.cygPath == null) return sumIncludes;
        
        List translatedIncludePaths = new ArrayList();
        for (Iterator i = sumIncludes.iterator(); i.hasNext(); ) {
            String includePath = (String) i.next();
            IPath realPath = new Path(includePath);
            if (realPath.toFile().exists()) {
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
                }
                if (!translatedPath.equals(includePath)) {
                    // Check if the translated path exists
                    IPath transPath = new Path(translatedPath);
                    if (transPath.toFile().exists()) {
                        translatedIncludePaths.add(transPath.toPortableString());
                    }
                    else {
                        // TODO VMIR for now add even if it does not exist
                        translatedIncludePaths.add(translatedPath);
                    }
                }
                else {
                    // TODO VMIR for now add even if it does not exist
                    translatedIncludePaths.add(translatedPath);
                }
            }
        }
        cygpath.cygPath.dispose();
        return translatedIncludePaths;
    }

}
