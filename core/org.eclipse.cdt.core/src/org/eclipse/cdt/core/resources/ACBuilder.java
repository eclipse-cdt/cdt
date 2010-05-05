/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

public abstract class ACBuilder extends IncrementalProjectBuilder implements IMarkerGenerator {

	private static final String PREF_BUILD_ALL_CONFIGS = "build.all.configs.enabled"; //$NON-NLS-1$
	private static final String PREF_BUILD_CONFIGS_RESOURCE_CHANGES = "build.proj.ref.configs.enabled"; //$NON-NLS-1$
	private static final Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();

	private static final String CONTENTS_CONFIGURATION_IDS = "org.eclipse.cdt.make.core.configurationIds"; //$NON-NLS-1$
	/** @since 5.2 */ // set to true to print build events on the console in debug mode
	protected static final boolean DEBUG_EVENTS = false;
	/**
	 * Constructor for ACBuilder
	 */
	public ACBuilder() {
		super();
	}

	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		ProblemMarkerInfo problemMarkerInfo = new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar, null);
		addMarker(problemMarkerInfo);
	}

		/*
		 * callback from Output Parser
		 */
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		try {
			IResource markerResource = problemMarkerInfo.file ;
			if (markerResource==null)  {
				markerResource = getProject();
			}
			IMarker[] cur = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
			/*
			 * Try to find matching markers and don't put in duplicates
			 */
			String externalLocation = null;
			if (problemMarkerInfo.externalPath != null && ! problemMarkerInfo.externalPath.isEmpty()) {
				externalLocation = problemMarkerInfo.externalPath.toOSString();
			}
			if ((cur != null) && (cur.length > 0)) {
				for (IMarker element : cur) {
					int line = ((Integer) element.getAttribute(IMarker.LINE_NUMBER)).intValue();
					int sev = ((Integer) element.getAttribute(IMarker.SEVERITY)).intValue();
					String mesg = (String) element.getAttribute(IMarker.MESSAGE);
					String extloc = (String) element.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
					if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity) && mesg.equals(problemMarkerInfo.description)) {
						if (extloc==externalLocation || (extloc!=null && extloc.equals(externalLocation))) {
							return;
						}
					}
				}
			}
			
			IMarker marker = markerResource.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, -1);
			marker.setAttribute(IMarker.CHAR_END, -1);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (externalLocation != null) {
				try {
					URI uri = new URI(externalLocation);
					if (uri.getScheme()!=null) {
						marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, externalLocation);
						String locationText = NLS.bind(CCorePlugin.getResourceString("ACBuilder.ProblemsView.Location"), //$NON-NLS-1$
								problemMarkerInfo.lineNumber, externalLocation);
						marker.setAttribute(IMarker.LOCATION, locationText);
					}
				} catch (URISyntaxException e) {
					// Just ignore those which cannot be open by editor
				}
			} else if (problemMarkerInfo.lineNumber==0){
				marker.setAttribute(IMarker.LOCATION, " "); //$NON-NLS-1$
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
	
	public static boolean needAllConfigBuild() {
		return prefs.getBoolean(PREF_BUILD_ALL_CONFIGS);
	}
	
	public static void setAllConfigBuild(boolean enable) {
		prefs.setValue(PREF_BUILD_ALL_CONFIGS, enable);		
	}
	
	/**
	 * Preference for building configurations only when there are resource changes within Eclipse or
	 * when there are changes in its references.
	 * @return true if configurations will be build when project resource changes within Eclipse 
	 *         false otherwise
	 * @since 5.1
	 */
	public static boolean buildConfigResourceChanges() {
		//bug 219337
		return prefs.getBoolean(PREF_BUILD_CONFIGS_RESOURCE_CHANGES);
	}
	
	/**
	 * Preference for building configurations only when there are resource changes within Eclipse or
	 * when there are changes in its references.
	 * @param enable
	 * @since 5.1
	 */
	public static void setBuildConfigResourceChanges(boolean enable) {
		prefs.setValue(PREF_BUILD_CONFIGS_RESOURCE_CHANGES, enable);		
	}
	
	@SuppressWarnings("nls")
	private static String kindToString(int kind) {
		return (kind==IncrementalProjectBuilder.AUTO_BUILD ? "AUTO_BUILD"
				: kind==IncrementalProjectBuilder.CLEAN_BUILD ? "CLEAN_BUILD"
				: kind==IncrementalProjectBuilder.FULL_BUILD ? "FULL_BUILD"
				: kind==IncrementalProjectBuilder.INCREMENTAL_BUILD ? "INCREMENTAL_BUILD"
				: "[unknown kind]")+"="+kind;
	}
	
	@SuppressWarnings("nls")
	private String cfgIdToNames(String strIds) {
		IProject project = getProject();
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		if (prjDesc==null)
			return strIds;
		
		if (strIds==null)
			return "Active=" + prjDesc.getActiveConfiguration().getName();
		
		String[] ids = strIds.split("\\|");
		String names="";
		for (String id : ids) {
			ICConfigurationDescription cfgDesc = prjDesc.getConfigurationById(id);
			String name;
			if (cfgDesc!=null)
				name = cfgDesc.getName();
			else
				name = id;
			
			if (names.length()>0)
				names=names+",";
			names = names + name;
		}
		if (names.equals(""))
			return strIds;
		return names;
	}

	/**
	 * For debugging purpose only. Prints events on the debug console.
	 * 
	 * @since 5.2
	 */
	@SuppressWarnings("nls")
	protected void printEvent(int kind, Map<String, String> args) {
		if (DEBUG_EVENTS) {
			String ids = args!=null ? args.get(CONTENTS_CONFIGURATION_IDS) : null;
			System.out.println("t"+Thread.currentThread().getId()+": "
					+ kindToString(kind)
					+ ", " +  getProject()
					+ "[" + cfgIdToNames(ids) +"]"
					+ ", " + this.getClass().getSimpleName()
				);
		}
	}
	
	@Override
	// This method is overridden with no purpose but to track events in debug mode
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);
	}

   /**
	* Default ACBuilder shouldn't require locking the workspace during a CDT Project build.
	*
	* Note this may have a detrimental effect on #getDelta().  Derived builders which rely
	* on #getDelta(...) being accurate should return a WorkspaceRoot scheduling rule.
	* @since 5.2
	*/
	@Override
	@SuppressWarnings("rawtypes")
	public ISchedulingRule getRule(int trigger, Map args) {
		return null;
	}

}
