/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.internal.core.scannerconfig.ConsoleOutputSniffer;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * New default external scanner info provider of type 'open'
 * 
 * @author vhirsl
 */
public class DefaultSIFileReader implements IExternalScannerInfoProvider {
    private static final String EXTERNAL_SI_PROVIDER_CONSOLE_ID = MakeCorePlugin.getUniqueIdentifier() + ".ExternalScannerInfoProviderConsole"; //$NON-NLS-1$

    private long fileSize = 0;
    
    private SCMarkerGenerator markerGenerator = new SCMarkerGenerator();

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider#invokeProvider(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.resources.IResource, java.lang.String, org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2)
     */
    public boolean invokeProvider(IProgressMonitor monitor,
                                  IResource resource, 
                                  String providerId, 
                                  IScannerConfigBuilderInfo2 buildInfo,
                                  IScannerInfoCollector collector) {
        boolean rc = false;
        IProject project = resource.getProject();
        // input
        BufferedReader reader = getStreamReader(buildInfo.getBuildOutputFilePath());
        if (reader == null)
            return rc;
        // output
        IConsole console = CCorePlugin.getDefault().getConsole(EXTERNAL_SI_PROVIDER_CONSOLE_ID);
        console.start(project);
        OutputStream ostream, cos;
        try {
            ostream = console.getOutputStream();
        }
        catch (CoreException e) {
            ostream = null;
        }
        
        // get build location
        IPath buildDirectory = null;
        try {
            IMakeBuilderInfo makeInfo = MakeCorePlugin.createBuildInfo(project, MakeBuilder.BUILDER_ID);
            if (!makeInfo.getBuildLocation().isEmpty()) {
                IResource res = project.getParent().findMember(makeInfo.getBuildLocation());
                if (res instanceof IContainer && res.exists()) {
                    buildDirectory = res.getLocation();
                }
            }
            if (buildDirectory == null) {
                buildDirectory = project.getLocation();
            }
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
        
		ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.
                getMakeBuilderOutputSniffer(ostream, null, project, buildDirectory, buildInfo, markerGenerator, collector);
		if (sniffer != null) {
			ostream = (sniffer == null ? null : sniffer.getOutputStream());
		}
        
		rc = readFileToOutputStream(monitor, reader, ostream);
        
        return rc;
	}

    /**
     * @param inputFileName
     * @return
     */
    private BufferedReader getStreamReader(String inputFileName) {
        BufferedReader reader = null;
        try {
            fileSize = new File(inputFileName).length();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName)));
        } catch (FileNotFoundException e) {
            MakeCorePlugin.log(e);
        }
        return reader;
    }

    /**
     * Precondition: Neither input nor output are null
     * @param monitor
     * @return
     */
    private boolean readFileToOutputStream(IProgressMonitor monitor, BufferedReader reader, OutputStream ostream) {
        final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
        monitor.beginTask("Reading build output ...", (int)((fileSize == 0) ? 10000 : fileSize)); //$NON-NLS-1$
        // check if build output file exists
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (monitor.isCanceled()) {
                    return false;
                }

                line += lineSeparator;
                byte[] bytes = line.getBytes();
                ostream.write(bytes);
                monitor.worked(bytes.length);
            }
        } catch (IOException e) {
            MakeCorePlugin.log(e);
        } finally {
            try {
                ostream.flush();
            } catch (IOException e) {
                MakeCorePlugin.log(e);
            }
            try {
                ostream.close();
            } catch (IOException e) {
                MakeCorePlugin.log(e);
            }
        }
        monitor.done();
        return true;
    }

}
