/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;

/**
 * AbstarctChecker that has extra methods to simplify adding problem
 * preferences.
 * Checker can produce several problems, but preferences are per problem.
 * Shared are not supported now.
 */
public abstract class AbstractCheckerWithProblemPreferences extends
		AbstractChecker implements ICheckerWithPreferences {
	/**
	 * Checker that actually has parameter must override this
	 */
	public void initPreferences(IProblemWorkingCopy problem) {
		// do nothing
	}

	/**
	 * Add a parameter
	 * 
	 * @param problem
	 *            - problem that has parameter
	 * @param key
	 *            - parameter key
	 * @param label
	 *            - parameter label - user visible
	 * @param defaultValue
	 *            - parameter default value
	 * @return - parameter info object
	 */
	public IProblemPreference addPreference(IProblemWorkingCopy problem,
			String key, String label, Object defaultValue) {
		MapProblemPreference map = getTopLevelPreferenceMap(problem);
		BasicProblemPreference info = new BasicProblemPreference(key, label,
				PreferenceType.typeOf(defaultValue));
		map.addChildDescriptor(info);
		setDefaultPreferenceValue(problem, key, defaultValue);
		return info;
	}

	public IProblemPreference addPreference(IProblemWorkingCopy problem,
			IProblemPreference info, Object defaultValue) {
		MapProblemPreference map = getTopLevelPreferenceMap(problem);
		map.addChildDescriptor(info);
		setDefaultPreferenceValue(problem, info.getKey(), defaultValue);
		return info;
	}

	/**
	 * @param problem
	 * @param key
	 * @param defaultValue
	 */
	protected void setDefaultPreferenceValue(IProblemWorkingCopy problem,
			String key, Object defaultValue) {
		MapProblemPreference map = getTopLevelPreferenceMap(problem);
		if (map.getChildValue(key) == null)
			map.addChildValue(key, defaultValue);
	}

	/**
	 * @param problem
	 * @return
	 */
	protected MapProblemPreference getTopLevelPreferenceMap(
			IProblemWorkingCopy problem) {
		MapProblemPreference map = (MapProblemPreference) problem
				.getPreference();
		if (map == null) {
			map = new MapProblemPreference("params", ""); //$NON-NLS-1$ //$NON-NLS-2$
			problem.setPreference(map);
		}
		return map;
	}

	/**
	 * Return value for the key in the top level preference map
	 * 
	 * @param problem
	 * @param key
	 * @return
	 */
	public Object getPreference(IProblem problem, String key) {
		return ((MapProblemPreference) problem.getPreference())
				.getChildValue(key);
	}
}
