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
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
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
	
	private static final String EXTERNAL_SI_PROVIDER_ERROR = "ExternalScannerInfoProvider.Provider_Error"; //$NON-NLS-1$
	private static final String EXTERNAL_SI_PROVIDER_CONSOLE_ID = MakeCorePlugin.getUniqueIdentifier() + ".ExternalScannerInfoProviderConsole";	//$NON-NLS-1$
	private static final String LANG_ENV_VAR = "LANG";
	
	private IPath fWorkingDirectory;
	private IPath fCompileCommand;
	private String[] fCompileArguments;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider#invokeProvider(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.resources.IProject, org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo, java.util.List, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector)
	 */
	public boolean invokeProvider(IProgressMonitor monitor,
								  IProject currentProject,
								  IScannerConfigBuilderInfo buildInfo,
								  List targetSpecificOptions,
								  IScannerInfoCollector collector) {
		if (targetSpecificOptions == null) {
			targetSpecificOptions = new ArrayList();
		}
		if (!initialize(currentProject, buildInfo)) {
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
			CommandLauncher launcher = new CommandLauncher();
			// Print the command for visual interaction.
			launcher.showCommand(true);

			// add file and TSO
			String[] compileArguments = fCompileArguments;
			if (buildInfo.isDefaultESIProviderCmd()) {
				// consider TSO only if default command
				compileArguments = prepareArguments(targetSpecificOptions);
			}

			String ca = coligate(compileArguments);

			monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Invoking_Command")  //$NON-NLS-1$
					+ fCompileCommand.toString() + ca);
			cos = new StreamMonitor(new SubProgressMonitor(monitor, 70), cos, 100);
			
			OutputStream sniffer = ScannerInfoConsoleParserFactory.getESIProviderOutputSniffer(
					cos, currentProject, buildInfo, collector);
			TraceUtil.outputTrace("Default provider is executing command:", fCompileCommand.toString() + ca, ""); //$NON-NLS-1$ //$NON-NLS-2$
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
				monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Parsing_Output")); //$NON-NLS-1$
			}
			else {
				errMsg = launcher.getErrorMessage();
			}

			if (errMsg != null) {
				String errorDesc = MakeMessages.getFormattedString(EXTERNAL_SI_PROVIDER_ERROR, 
						fCompileCommand.toString() + ca);
				addMarker(currentProject, -1, errorDesc, IMarkerGenerator.SEVERITY_WARNING, null);
			}

			monitor.subTask(MakeMessages.getString("ExternalScannerInfoProvider.Creating_Markers")); //$NON-NLS-1$
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
		
		fWorkingDirectory = currentProject.getLocation();
		String targetFile = "dummy";	//$NON-NLS-1$
		try {
			if (currentProject.hasNature(CCProjectNature.CC_NATURE_ID)) {
				targetFile = GCCScannerConfigUtil.CPP_SPECS_FILE;
			}
			else if (currentProject.hasNature(CProjectNature.C_NATURE_ID)) {
				targetFile = GCCScannerConfigUtil.C_SPECS_FILE;
			}
		} catch (CoreException e) {
			//TODO VMIR better error handling
			MakeCorePlugin.log(e.getStatus());
		}
		IPath path2File = MakeCorePlugin.getWorkingDirectory().append(targetFile);
		if (!path2File.toFile().exists()) {
			GCCScannerConfigUtil.createSpecs();
		}
		fCompileCommand = buildInfo.getESIProviderCommand();
		if (fCompileCommand != null) {
			fCompileArguments = ScannerConfigUtil.tokenizeStringWithQuotes(buildInfo.getESIProviderArguments(), "\"");//$NON-NLS-1$
			for (int i = 0; i < fCompileArguments.length; ++i) {
				fCompileArguments[i] = fCompileArguments[i].replaceAll("\\$\\{plugin_state_location\\}",	//$NON-NLS-1$ 
						MakeCorePlugin.getWorkingDirectory().toString());
				fCompileArguments[i] = fCompileArguments[i].replaceAll("\\$\\{specs_file\\}", targetFile);	//$NON-NLS-1$
			}
			rc = true;
		}
		return rc;
	}

	/**
	 * @param tso - target specific options
	 * @return
	 */
	private String[] prepareArguments(List tso) {
		String[] rv = null;
		// commandArguments may have multiple arguments; tokenizing
		int nTokens = 0;
		if (fCompileArguments != null && fCompileArguments.length > 0) {
			nTokens = fCompileArguments.length;
			rv = new String[nTokens + tso.size()];
			System.arraycopy(fCompileArguments, 0, rv, 0, nTokens);
		}
		else {
			rv = new String[tso.size()];
		}
		for (int i = 0; i < tso.size(); ++i) {
			rv[nTokens + i] = (String) tso.get(i);
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
		// On POSIX (Linux, UNIX) systems reset LANG variable to English with UTF-8 encoding
		// since GNU compilers can handle only UTF-8 characters. English language is chosen
		// beacuse GNU compilers inconsistently handle different locales when generating
		// output of the 'gcc -v' command. Include paths with locale characters will be
		// handled properly regardless of the language as long as the encoding is set to UTF-8.
		if (props.containsKey(LANG_ENV_VAR)) {
			props.put(LANG_ENV_VAR, "en_US.UTF-8"); //$NON-NLS-1$
		}
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
