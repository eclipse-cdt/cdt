/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 * Gerhard Schaber (Wind River Systems) - bug 210125
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeBuilderUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
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

    @Override
	public boolean invokeProvider(IProgressMonitor monitor, IResource resource,
    		String providerId, IScannerConfigBuilderInfo2 buildInfo,
    		IScannerInfoCollector collector) {
    	return invokeProvider(monitor, resource, new InfoContext(resource.getProject()), providerId, buildInfo, collector, null);
    }

    @Override
	public boolean invokeProvider(IProgressMonitor monitor,
                                  IResource resource,
                                  InfoContext context,
                                  String providerId,
                                  IScannerConfigBuilderInfo2 buildInfo,
                                  IScannerInfoCollector collector,
                                  Properties env) {
        boolean rc = false;
        IProject project = resource.getProject();
        // input
        BufferedReader reader = getStreamReader(buildInfo.getBuildOutputFilePath());
        if (reader == null)
            return rc;

        try {
	        // output
	        IConsole console = CCorePlugin.getDefault().getConsole(EXTERNAL_SI_PROVIDER_CONSOLE_ID);
	        console.start(project);
	        OutputStream ostream;
	        try {
	            ostream = console.getOutputStream();
	        }
	        catch (CoreException e) {
	            ostream = null;
	        }

	        // get build location
	        IPath buildDirectory = MakeBuilderUtil.getBuildDirectory(project, MakeBuilder.BUILDER_ID);

			ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.
	                getMakeBuilderOutputSniffer(ostream, null, project, context, buildDirectory, buildInfo, markerGenerator, collector);
			if (sniffer != null) {
				ostream = sniffer.getOutputStream();
			}

			if (ostream != null) {
				rc = readFileToOutputStream(monitor, reader, ostream);
			}
        } finally {
			try {
				reader.close();
			} catch (IOException e) {
	            MakeCorePlugin.log(e);
			}
        }
        return rc;
	}

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
