/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * Helper class to load/save problem profile settings in persistent storage
 * 
 */
public class CodanPreferencesLoader {
	private IProblemProfile baseModel;

	/**
	 * Constructor
	 * 
	 * @param profile - problem profile to work with
	 */
	public CodanPreferencesLoader(IProblemProfile profile) {
		setInput(profile);
	}

	/**
	 * Default constructor
	 */
	public CodanPreferencesLoader() {
	}

	/**
	 * Sets the profile for this class
	 * 
	 * @param profile
	 */
	public void setInput(IProblemProfile profile) {
		baseModel = profile;
	}

	/**
	 * @return problems array from the profile
	 */
	public IProblem[] getProblems() {
		IProblem[] problems = baseModel.getProblems();
		return problems;
	}

	/**
	 * @param id
	 * @param s
	 */
	public void setProperty(String id, String s) {
		IProblem prob = baseModel.findProblem(id);
		if (!(prob instanceof IProblemWorkingCopy))
			return;
		String sevs = s;
		boolean enabled = true;
		if (sevs.startsWith("-")) { //$NON-NLS-1$
			sevs = sevs.substring(1);
			enabled = false;
		}
		((IProblemWorkingCopy) prob).setEnabled(enabled);
		CodanSeverity sev;
		try {
			sev = CodanSeverity.valueOf(sevs);
		} catch (RuntimeException e) {
			sev = CodanSeverity.Warning;
		}
		((IProblemWorkingCopy) prob).setSeverity(sev);
	}

	@Override
	public String toString() {
		return getInput().toString();
	}

	/**
	 * @return problem profile set for this class
	 */
	public IProblemProfile getInput() {
		return baseModel;
	}

	/**
	 * @param id - property id, which is the same as problem id
	 * @return get text representation of a "property" value for the given id,
	 *         which is severity name, with "-" in front of it it problem is
	 *         disabled.
	 */
	public String getProperty(String id) {
		IProblem prob = baseModel.findProblem(id);
		String enabled = prob.isEnabled() ? "" : "-"; //$NON-NLS-1$ //$NON-NLS-2$
		String severity = prob.getSeverity().toString();
		String res = enabled + severity;
		return res;
	}

	/**
	 * Takes string values from storePreferences and applies them to the problem
	 * profile
	 * 
	 * @param storePreferences
	 */
	public void load(Preferences storePreferences) {
		IProblem[] probs = getProblems();
		for (int i = 0; i < probs.length; i++) {
			String id = probs[i].getId();
			String s = storePreferences.get(id, null);
			if (s != null) {
				setProperty(id, s);
				setProblemPreferenceValues(id, storePreferences);
			}
		}
	}

	/**
	 * Takes string values of the problem preferences from storePreferences
	 * and applies them to the problem profile
	 * 
	 * @param problemId
	 * @param storePreferences
	 */
	private void setProblemPreferenceValues(String problemId,
			Preferences storePreferences) {
		IProblem prob = baseModel.findProblem(problemId);
		String prefKey = getPreferencesKey(problemId);
		if (prefKey == null)
			return;
		String exported = storePreferences.get(prefKey, null);
		if (exported != null) {
			//System.err.println(prefKey + " import " + exported);
			prob.getPreference().importValue(exported);
		}
	}

	/**
	 * Return preference node (osgi preferences) for the project
	 * 
	 * @param project
	 * @return project preferences node
	 */
	public static Preferences getProjectNode(IProject project) {
		if (!project.exists())
			return null;
		Preferences prefNode = new ProjectScope(project)
				.getNode(CodanCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;
		return prefNode;
	}

	/**
	 * Return preference node (osgi preferences) for the workspace
	 * 
	 * @return project preferences node
	 */
	public static Preferences getWorkspaceNode() {
		Preferences prefNode = new InstanceScope()
				.getNode(CodanCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;
		return prefNode;
	}

	/**
	 * Name of the preference key for the root problem preference in the osgi
	 * preferences
	 * 
	 * @param id - problem id
	 * @return top level preference id
	 */
	public String getPreferencesKey(String id) {
		IProblem prob = baseModel.findProblem(id);
		IProblemPreference pref = prob.getPreference();
		if (pref == null)
			return null;
		return id + "." + pref.getKey(); //$NON-NLS-1$
	}

	/**
	 * @param id - problem id
	 * @return - export value of root problem preference (to be saved in eclipse
	 *         preferences)
	 */
	public String getPreferencesString(String id) {
		IProblem prob = baseModel.findProblem(id);
		IProblemPreference pref = prob.getPreference();
		if (pref == null)
			return null;
		String str = pref.exportValue();
		//System.err.println(id + " set " + str);
		return str;
	}
}
