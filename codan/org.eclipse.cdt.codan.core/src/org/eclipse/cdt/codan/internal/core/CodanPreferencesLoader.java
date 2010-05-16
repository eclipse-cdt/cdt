/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
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
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
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
	 * @param workspaceProfile
	 */
	public CodanPreferencesLoader(IProblemProfile profile) {
		setInput(profile);
	}

	/**
	 * 
	 */
	public CodanPreferencesLoader() {
	}

	public void setInput(Object model) {
		baseModel = (IProblemProfile) model;
	}

	/**
	 * @return
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getInput().toString();
	}

	/**
	 * @return
	 */
	public IProblemProfile getInput() {
		return baseModel;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getProperty(String id) {
		IProblem prob = baseModel.findProblem(id);
		if (!(prob instanceof CodanProblem))
			return null;
		String enabled = prob.isEnabled() ? "" : "-"; //$NON-NLS-1$ //$NON-NLS-2$
		String severity = prob.getSeverity().toString();
		String res = enabled + severity;
		return res;
	}

	/**
	 * @param storePreferences
	 */
	public void load(Preferences storePreferences) {
		IProblem[] probs = getProblems();
		for (int i = 0; i < probs.length; i++) {
			String id = probs[i].getId();
			String s = storePreferences.get(id, null);
			if (s != null) {
				setProperty(id, s);
				setParameterValues(id, storePreferences);
			}
		}
	}

	/**
	 * @param problemId
	 * @param storePreferences
	 */
	private void setParameterValues(String problemId,
			Preferences storePreferences) {
		IProblem prob = baseModel.findProblem(problemId);
		String prefKey = getPreferencesKey(problemId);
		if (prefKey == null)
			return;
		String exported = storePreferences.get(prefKey, null);
		if (exported != null) {
			System.err.println(prefKey + " import " + exported);
			prob.getPreference().importValue(exported);
		}
	}

	public static Preferences getProjectNode(IProject project) {
		if (!project.exists())
			return null;
		Preferences prefNode = new ProjectScope(project)
				.getNode(CodanCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;
		return prefNode;
	}

	public static Preferences getWorkspaceNode() {
		Preferences prefNode = new InstanceScope()
				.getNode(CodanCorePlugin.PLUGIN_ID);
		if (prefNode == null)
			return null;
		return prefNode;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getPreferencesKey(String id) {
		IProblem prob = baseModel.findProblem(id);
		IProblemPreference pref = prob.getPreference();
		if (pref == null)
			return null;
		return id + "." + pref.getKey(); //$NON-NLS-1$
	}

	/**
	 * @param id
	 * @return
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
