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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.FileScopeProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;
import org.eclipse.cdt.codan.core.param.ListProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

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
		// by default add file scope preference
		addPreference(problem, new FileScopeProblemPreference(), null);
	}

	/**
	 * @param problem
	 * @return
	 */
	public FileScopeProblemPreference getScopePreference(IProblem problem) {
		FileScopeProblemPreference scope = (FileScopeProblemPreference) getTopLevelPreferenceMap(
				problem).getChildDescriptor(FileScopeProblemPreference.KEY);
		return scope;
	}

	/**
	 * User can scope out some resources for this checker. Checker can use this
	 * call to test if it should run on this resource at all or not. Test should
	 * be done within processResource method not in enabledInContext.
	 * 
	 * @param res
	 * @return
	 */
	public boolean shouldProduceProblems(IResource res) {
		Collection<IProblem> refProblems = getRuntime().getChechersRegistry()
				.getRefProblems(this);
		for (Iterator<IProblem> iterator = refProblems.iterator(); iterator
				.hasNext();) {
			IProblem checkerProblem = iterator.next();
			if (shouldProduceProblem(
					getProblemById(checkerProblem.getId(), res),
					res.getLocation()))
				return true;
		}
		return false;
	}

	public boolean shouldProduceProblem(IProblem problem, IPath resource) {
		FileScopeProblemPreference scope = getScopePreference(problem);
		if (scope == null)
			return true;
		return scope.isInScope(resource);
	}

	@Override
	public void reportProblem(String problemId, IProblemLocation loc,
			Object... args) {
		if (shouldProduceProblem(getProblemById(problemId, loc.getFile()), loc
				.getFile().getLocation()))
			super.reportProblem(problemId, loc, args);
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

	/**
	 * Add preference of type list with default string type, list is empty by
	 * default
	 * 
	 * @param problem
	 *            - problem
	 * @param key
	 *            - preference key
	 * @param label
	 *            - preference label
	 * @return preference instance of of the list, can be used to add default
	 *         values or set different element type
	 * 
	 */
	public ListProblemPreference addListPreference(IProblemWorkingCopy problem,
			String key, String label, String itemLabel) {
		MapProblemPreference map = getTopLevelPreferenceMap(problem);
		ListProblemPreference list = new ListProblemPreference(key, label);
		list.setChildDescriptor(new BasicProblemPreference(
				ListProblemPreference.COMMON_DESCRIPTOR_KEY, itemLabel,
				PreferenceType.TYPE_STRING));
		return (ListProblemPreference) map.addChildDescriptor(list);
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
			map.setChildValue(key, defaultValue);
	}

	/**
	 * @param problem
	 * @return
	 */
	protected MapProblemPreference getTopLevelPreferenceMap(IProblem problem) {
		MapProblemPreference map = (MapProblemPreference) problem
				.getPreference();
		if (map == null) {
			map = new MapProblemPreference("params", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (problem instanceof IProblemWorkingCopy) {
				((IProblemWorkingCopy) problem).setPreference(map);
			}
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
