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
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Default external scanner info provider.
 * Runs an external command (i.e. gcc -c -v) and parses an output for scanner info. 
 * 
 * @author vhirsl
 */
public class DefaultExternalScannerInfoProvider implements IExternalScannerInfoProvider, IMarkerGenerator {
	
	private static final String EXTERNAL_SI_PROVIDER_ERROR = "DefaultExternalScannerInfoProvider.Provider_Error"; //$NON-NLS-1$
	private static final String EXTERNAL_SI_PROVIDER_CONSOLE_ID = MakeCorePlugin.getUniqueIdentifier() + ".ExternalScannerInfoProviderConsole";	//$NON-NLS-1$
	
	private IPath fWorkingDirectory;
	private IPath fCompileCommand;
	private String fCompileArguments;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider#invokeProvider(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.resources.IProject, org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo, java.lang.String[])
	 */
	public boolean invokeProvider(IProgressMonitor monitor, IProject currentProject, IScannerConfigBuilderInfo buildInfo, String[] targetSpecificOptions) {
		if (!initialize(currentProject, buildInfo)) {
			return false;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeCorePlugin.getResourceString("ExternalScannerInfoProvider.Reading_Specs"), 100); //$NON-NLS-1$
		
		try {
			IConsole console = CCorePlugin.getDefault().getConsole(EXTERNAL_SI_PROVIDER_CONSOLE_ID);
			console.start(currentProject);
			OutputStream cos = console.getOutputStream();

			// Before launching give visual cues via the monitor
			monitor.subTask(MakeCorePlugin.getResourceString("ExternalScannerInfoProvider.Reading_Specs")); //$NON-NLS-1$
			
			String errMsg = null;
			CommandLauncher launcher = new CommandLauncher();
			// Print the command for visual interaction.
			launcher.showCommand(true);

			// add file and TSO
			String[] compileArguments = prepareArguments(targetSpecificOptions);

			String ca = coligate(compileArguments);

			monitor.subTask(MakeCorePlugin.getResourceString("ExternalScannerInfoProvider.Invoking_Command")
					+ fCompileCommand.toString() + ca); //$NON-NLS-1$
			cos = new StreamMonitor(new SubProgressMonitor(monitor, 70), cos, 100);
			
			OutputStream sniffer = ScannerInfoConsoleParserFactory.getESIProviderOutputSniffer(
					cos, currentProject, buildInfo);
			Process p = launcher.execute(fCompileCommand, compileArguments, setEnvironment(launcher), fWorkingDirectory);
			if (p != null) {
				try {
					// Close the input of the Process explicitely.
					// We will never write to it.
					p.getOutputStream().close();
				} catch (IOException e) {
				}
				if (launcher.waitAndRead(sniffer, sniffer, new SubProgressMonitor(monitor, 0)) != CommandLauncher.OK) {
					errMsg = launcher.getErrorMessage();
				}
				monitor.subTask(MakeCorePlugin.getResourceString("ExternalScannerInfoProvider.Parsing_Output")); //$NON-NLS-1$
			}
			else {
				errMsg = launcher.getErrorMessage();
			}

			if (errMsg != null) {
				String errorDesc = MakeCorePlugin.getFormattedString(EXTERNAL_SI_PROVIDER_ERROR, 
						fCompileCommand.toString() + ca);
				addMarker(currentProject, -1, errorDesc, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
			}

			monitor.subTask(MakeCorePlugin.getResourceString("ExternalScannerInfoProvider.Creating_Markers")); //$NON-NLS-1$
			sniffer.close();
			cos.close();
		}
		catch (Exception e) {
			CCorePlugin.log(e);
		}
		finally {
			monitor.done();
		}
		return true;
	}

	/**
	 * @param currentProject
	 * @param buildInfo
	 * @return boolean
	 */
	private boolean initialize(IProject currentProject, IScannerConfigBuilderInfo buildInfo) {
		boolean rc = false;
		if (buildInfo.isDefaultESIProviderCmd()) {
			fWorkingDirectory = MakeCorePlugin.getWorkingDirectory();
		}
		else {
			fWorkingDirectory = currentProject.getLocation();
		}
		fCompileCommand = buildInfo.getESIProviderCommand();
		if (fCompileCommand != null) {
			fCompileArguments = buildInfo.getESIProviderArguments();
			rc = true;
		}
		return rc;
	}

	/**
	 * @param tso
	 * @return
	 */
	private String[] prepareArguments(String[] tso) {
		String[] rv = null;
		// commandArguments may have multiple arguments; tokenizing
		int nTokens = 0;
		if (fCompileArguments != null && fCompileArguments.length() > 0) {
			StringTokenizer tokenizer = new StringTokenizer(fCompileArguments, " ");//$NON-NLS-1$
			nTokens = tokenizer.countTokens();
			if (nTokens > 0) {
				rv = new String[nTokens + tso.length];
				for (int i = 0; tokenizer.hasMoreTokens(); ++i) {
					rv[i] = tokenizer.nextToken();
				}
			}
		}
		if (rv == null) {
			rv = new String[tso.length];
		}
		for (int i = 0; i < tso.length; ++i) {
			rv[nTokens + i] = tso[i];
		}
		return rv;
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
	private String[] setEnvironment(CommandLauncher launcher) {
		// Set the environmennt, some scripts may need the CWD var to be set.
		Properties props = launcher.getEnvironment();
		props.put("CWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
		props.put("PWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
		String[] env = null;
		ArrayList envList = new ArrayList();
		Enumeration names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}
		return env;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IMarkerGenerator#addMarker(org.eclipse.core.resources.IResource, int, java.lang.String, int, java.lang.String)
	 */
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		try {
			IMarker[] cur = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			if ((cur != null) && (cur.length > 0)) {
				for (int i = 0; i < cur.length; i++) {
					int line = ((Integer) cur[i].getAttribute(IMarker.LOCATION)).intValue();
					int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
					if (line == lineNumber && sev == mapMarkerSeverity(severity) && mesg.equals(errorDesc)) {
						return;
					}
				}
			}

			IMarker marker = file.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.LOCATION, lineNumber);
			marker.setAttribute(IMarker.MESSAGE, errorDesc);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(severity));
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (errorVar != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, errorVar);
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	int mapMarkerSeverity(int severity) {
		switch (severity) {
			case SEVERITY_ERROR_BUILD :
			case SEVERITY_ERROR_RESOURCE :
				return IMarker.SEVERITY_ERROR;
			case SEVERITY_INFO :
				return IMarker.SEVERITY_INFO;
			case SEVERITY_WARNING :
				return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}
}
