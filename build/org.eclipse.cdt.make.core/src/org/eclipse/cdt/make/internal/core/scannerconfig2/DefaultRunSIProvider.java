/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *  Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeBuilderUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * New default external scanner info provider of type 'run'
 * 
 * @author vhirsl
 */
public class DefaultRunSIProvider implements IExternalScannerInfoProvider {
    private static final String EXTERNAL_SI_PROVIDER_ERROR = "ExternalScannerInfoProvider.Provider_Error"; //$NON-NLS-1$
    private static final String EXTERNAL_SI_PROVIDER_CONSOLE_ID = MakeCorePlugin.getUniqueIdentifier() + ".ExternalScannerInfoProviderConsole"; //$NON-NLS-1$
    private static final String LANG_ENV_VAR = "LANG"; //$NON-NLS-1$

    protected IResource resource;
    protected String providerId;
    protected IScannerConfigBuilderInfo2 buildInfo;
    protected IScannerInfoCollector collector;
    // To be initialized by a subclass
    protected IPath fWorkingDirectory;
    protected IPath fCompileCommand;
    protected String[] fCompileArguments;
    
    private SCMarkerGenerator markerGenerator = new SCMarkerGenerator();

    public boolean invokeProvider(IProgressMonitor monitor, IResource resource,
    		String providerId, IScannerConfigBuilderInfo2 buildInfo,
    		IScannerInfoCollector collector) {
    	return invokeProvider(monitor, resource, new InfoContext(resource.getProject()), providerId, buildInfo, collector, null);
    }
    
    public boolean invokeProvider(IProgressMonitor monitor,
                                  IResource resource, 
                                  InfoContext context,
                                  String providerId, 
                                  IScannerConfigBuilderInfo2 buildInfo,
                                  IScannerInfoCollector collector,
                                  Properties env) {
	    // initialize fields
        this.resource = resource;
        this.providerId = providerId;
        this.buildInfo = buildInfo;
        this.collector = collector;
        
        IProject currentProject = resource.getProject();
        // call a subclass to initialize protected fields
        if (!initialize()) {
            return false;
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask(MakeMessages.getString("ExternalScannerInfoProvider.Reading_Specs"), 100); //$NON-NLS-1$
        
        try {
            IConsole console = CCorePlugin.getDefault().getConsole(EXTERNAL_SI_PROVIDER_CONSOLE_ID);
            console.start(currentProject);
            OutputStream cos = console.getOutputStream();

            // Before launching give visual cues via the monitor
            monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Reading_Specs")); //$NON-NLS-1$
            
            String errMsg = null;
            ICommandLauncher launcher = new CommandLauncher();
            launcher.setProject(currentProject);
            // Print the command for visual interaction.
            launcher.showCommand(true);

            String[] comandLineOptions = getCommandLineOptions();
            String ca = coligate(comandLineOptions);

            monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Invoking_Command")  //$NON-NLS-1$
                    + getCommandToLaunch() + ca);
            cos = new StreamMonitor(new SubProgressMonitor(monitor, 70), cos, 100);
            
            ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getESIProviderOutputSniffer(
                    cos, cos, currentProject, context, providerId, buildInfo, collector, markerGenerator);
            OutputStream consoleOut = (sniffer == null ? cos : sniffer.getOutputStream());
            OutputStream consoleErr = (sniffer == null ? cos : sniffer.getErrorStream());
            TraceUtil.outputTrace("Default provider is executing command:", fCompileCommand.toString() + ca, ""); //$NON-NLS-1$ //$NON-NLS-2$
            Process p = launcher.execute(getCommandToLaunch(), comandLineOptions, setEnvironment(launcher, env), fWorkingDirectory, monitor);
            if (p != null) {
                try {
                    // Close the input of the Process explicitely.
                    // We will never write to it.
                    p.getOutputStream().close();
                } catch (IOException e) {
                }
                if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0)) != ICommandLauncher.OK) {
                    errMsg = launcher.getErrorMessage();
                }
                monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Parsing_Output")); //$NON-NLS-1$
            }
            else {
                errMsg = launcher.getErrorMessage();
            }

            if (errMsg != null) {
                String errorDesc = MakeMessages.getFormattedString(EXTERNAL_SI_PROVIDER_ERROR, 
                        fCompileCommand.toString() + ca);
                markerGenerator.addMarker(currentProject, -1, errorDesc, IMarkerGenerator.SEVERITY_WARNING, null);
            }

            monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Creating_Markers")); //$NON-NLS-1$
            consoleOut.close();
            consoleErr.close();
            cos.close();
        }
        catch (Exception e) {
            MakeCorePlugin.log(e);
        }
        finally {
            monitor.done();
        }
        return true;
    }
    
    protected IPath getCommandToLaunch() {
    	return fCompileCommand;
    }
    
    protected String[] getCommandLineOptions() {
        // add additional arguments
        // subclass can change default behavior
        return prepareArguments( 
                buildInfo.isUseDefaultProviderCommand(providerId));
    }
    
    /**
     * Initialization of protected fields. 
     * Subclasses are most likely to override default implementation.
     * 
     * @param currentProject
     * @return boolean
     */
    protected boolean initialize() {
    	
		IProject currProject = resource.getProject();
        //fWorkingDirectory = resource.getProject().getLocation();
		fWorkingDirectory = MakeBuilderUtil.getBuildDirectory(currProject, MakeBuilder.BUILDER_ID);
        fCompileCommand = new Path(buildInfo.getProviderRunCommand(providerId));
        fCompileArguments = ScannerConfigUtil.tokenizeStringWithQuotes(buildInfo.getProviderRunArguments(providerId), "\"");//$NON-NLS-1$
        return (fCompileCommand != null);
    }

    /**
     * Add additional arguments. For example: tso - target specific options
     * Base class implementation returns compileArguments.
     * Subclasses are most likely to override default implementation.
     * 
     * @param isDefaultCommand
     * @param collector 
     * @return
     */
    protected String[] prepareArguments(boolean isDefaultCommand) {
        return fCompileArguments;
    }

    /**
     * @param array
     * @return
     */
    private String coligate(String[] array) {
        StringBuffer sb = new StringBuffer(128);
        for (int i = 0; i < array.length; ++i) {
            sb.append(' ');
            sb.append(array[i]);
        }
        String ca = sb.toString();
        return ca;
    }

    /**
     * @param launcher
     * @return
     */
    protected String[] setEnvironment(ICommandLauncher launcher, Properties initialEnv) {
        // Set the environmennt, some scripts may need the CWD var to be set.
        Properties props = initialEnv != null ? initialEnv : launcher.getEnvironment();
        
        if (fWorkingDirectory != null) {
			props.put("CWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
			props.put("PWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
		}
        // On POSIX (Linux, UNIX) systems reset LANG variable to English with
		// UTF-8 encoding
        // since GNU compilers can handle only UTF-8 characters. English language is chosen
        // beacuse GNU compilers inconsistently handle different locales when generating
        // output of the 'gcc -v' command. Include paths with locale characters will be
        // handled properly regardless of the language as long as the encoding is set to UTF-8.
        if (props.containsKey(LANG_ENV_VAR)) {
            props.put(LANG_ENV_VAR, "en_US.UTF-8"); //$NON-NLS-1$
        }
        String[] env = null;
        ArrayList<String> envList = new ArrayList<String>();
        Enumeration<?> names = props.propertyNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String key = (String) names.nextElement();
                envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
            }
            env = envList.toArray(new String[envList.size()]);
        }
        return env;
    }

}
