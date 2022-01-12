/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.FileScopeProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType;
import org.eclipse.cdt.codan.core.param.LaunchModeProblemPreference;
import org.eclipse.cdt.codan.core.param.ListProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.core.param.SuppressionCommentProblemPreference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * AbstractChecker that has extra methods to simplify adding problem preferences.
 * Checker can produce several problems, but preferences are per problem.
 * Sharing preferences between problems is not supported now.
 */
public abstract class AbstractCheckerWithProblemPreferences extends AbstractChecker implements ICheckerWithPreferences {
	/**
	 * Checker that actually has parameter must override this
	 */
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		getTopLevelPreference(problem); // initialize
		getLaunchModePreference(problem).enableInLaunchModes(CheckerLaunchMode.RUN_AS_YOU_TYPE,
				CheckerLaunchMode.RUN_ON_DEMAND, CheckerLaunchMode.RUN_ON_FULL_BUILD,
				CheckerLaunchMode.RUN_ON_INC_BUILD);
		getSuppressionCommentPreference(problem)
				.setValue(SuppressionCommentProblemPreference.generateDefaultComment(problem));
	}

	/**
	 * Scope preference - special preference that all file checkers should have,
	 * it allows user to include/exclude files for this specific problem.
	 *
	 * @param problem - problem for which scope preference is need
	 * @return scope problem preference, null if not defined
	 */
	public FileScopeProblemPreference getScopePreference(IProblem problem) {
		return getTopLevelPreference(problem).getScopePreference();
	}

	/**
	 * @param problem - problem for which preference is extracted
	 * @return launch mode preference
	 * @since 2.0
	 */
	public LaunchModeProblemPreference getLaunchModePreference(IProblem problem) {
		return getTopLevelPreference(problem).getLaunchModePreference();
	}

	/**
	 * @param problem - problem for which preference is extracted
	 * @return suppression comment preference
	 * @since 4.0
	 */
	public SuppressionCommentProblemPreference getSuppressionCommentPreference(IProblem problem) {
		return getTopLevelPreference(problem).getSuppressionCommentPreference();
	}

	/**
	 * User can scope out some resources for this checker. Checker can use this
	 * call to test if it should run on this resource at all or not. Test should
	 * be done within processResource method.
	 * This test uses user "scope" preference for the all problems that this
	 * checker can produce.
	 *
	 * @param res - resource to test on
	 * @return true if checker should report problems, fails otherwise.
	 */
	public boolean shouldProduceProblems(IResource res) {
		Collection<IProblem> refProblems = getRuntime().getCheckersRegistry().getRefProblems(this);
		for (Iterator<IProblem> iterator = refProblems.iterator(); iterator.hasNext();) {
			IProblem checkerProblem = iterator.next();
			if (shouldProduceProblem(getProblemById(checkerProblem.getId(), res), res.getFullPath()))
				return true;
		}
		return false;
	}

	/**
	 * User can scope out some resources for this checker. Checker can use this
	 * call to test if it should run on this resource at all or produce
	 * a specific problem on this resource. Test should
	 * be done within processResource method not in enabledInContext, or just
	 * before printing of a problem.
	 * This test uses user "scope" preference for the given problem. If scope is
	 * not defined preference it returns true.
	 *
	 * @param problem - problem to test for
	 * @param resource - resource to test on
	 *
	 * @return true if problem should be report for given resource, fails
	 *         otherwise.
	 */
	public boolean shouldProduceProblem(IProblem problem, IPath resource) {
		FileScopeProblemPreference scope = getScopePreference(problem);
		if (scope == null)
			return true;
		return scope.isInScope(resource);
	}

	@Override
	public void reportProblem(String problemId, IProblemLocation loc, Object... args) {
		reportProblem(getProblemById(problemId, loc.getFile()), loc, args);
	}

	/**
	 * Reports a problem
	 *
	 * @param pr - problem (kind) instance
	 * @param loc - problem location
	 * @param args - extra problem arguments
	 *
	 * @since 2.0
	 */
	public void reportProblem(IProblem pr, IProblemLocation loc, Object... args) {
		if (shouldProduceProblem(pr, loc, args))
			super.reportProblem(pr.getId(), loc, args);
	}

	/**
	 * Checks if problem should be reported, this implementation only checks
	 * suppression by scope, but subclass should override,
	 * to implement any other filtering, such as suppression by filter or by comment.
	 * Call super to check for scope suppression as well.
	 *
	 * @param problem - problem kind
	 * @param loc - location
	 * @param args - arguments
	 * @since 4.0
	 */
	protected boolean shouldProduceProblem(IProblem problem, IProblemLocation loc, Object... args) {
		return shouldProduceProblem(problem, loc.getFile().getFullPath());
	}

	/**
	 * Adds a parameter
	 *
	 * @param problem
	 *        - problem that has parameter
	 * @param key
	 *        - parameter key
	 * @param label
	 *        - parameter label - user visible
	 * @param defaultValue
	 *        - parameter default value
	 * @return - parameter info object
	 */
	public IProblemPreference addPreference(IProblemWorkingCopy problem, String key, String label,
			Object defaultValue) {
		MapProblemPreference map = getTopLevelPreference(problem);
		BasicProblemPreference info = new BasicProblemPreference(key, label, PreferenceType.typeOf(defaultValue));
		map.addChildDescriptor(info);
		setDefaultPreferenceValue(problem, key, defaultValue);
		return info;
	}

	/**
	 * Adds preference of type list of strings, list is empty by default.
	 *
	 * @param problem
	 *        - problem
	 * @param key
	 *        - preference key
	 * @param label
	 *        - preference label
	 * @param itemLabel
	 * @return preference instance of of the list, can be used to add default
	 *         values or set different element type
	 *
	 */
	public ListProblemPreference addListPreference(IProblemWorkingCopy problem, String key, String label,
			String itemLabel) {
		MapProblemPreference map = getTopLevelPreference(problem);
		ListProblemPreference list = new ListProblemPreference(key, label);
		list.setChildDescriptor(new BasicProblemPreference(ListProblemPreference.COMMON_DESCRIPTOR_KEY, itemLabel,
				PreferenceType.TYPE_STRING));
		return (ListProblemPreference) map.addChildDescriptor(list);
	}

	/**
	 * Adds preference for the given problem with default value
	 *
	 * @param problem
	 * @param pref - preference
	 * @param defaultValue - default value of the preference
	 * @return added preference
	 */
	public IProblemPreference addPreference(IProblemWorkingCopy problem, IProblemPreference pref, Object defaultValue) {
		MapProblemPreference map = getTopLevelPreference(problem);
		String key = pref.getKey();
		pref = map.addChildDescriptor(pref);
		setDefaultPreferenceValue(problem, key, defaultValue);
		return pref;
	}

	/**
	 * Convenience method for setting default preference value for checker that
	 * uses "map" as top level problem preference.
	 *
	 * @param problem - problem for which to set default value for a preference
	 * @param key - preference key
	 * @param defaultValue - value of preference to be set
	 */
	protected void setDefaultPreferenceValue(IProblemWorkingCopy problem, String key, Object defaultValue) {
		MapProblemPreference map = getTopLevelPreference(problem);
		if (map.getChildValue(key) == null)
			map.setChildValue(key, defaultValue);
	}

	/**
	 * Returns "map" problem preference for a give problem, if problem
	 * has preference different than a map, it will throw ClassCastException.
	 * If top level preference does not exist create a map preference with name
	 * "params"
	 * and return it.
	 *
	 * @param problem
	 * @return top level preference if it is a map
	 * @since 2.0
	 */
	public RootProblemPreference getTopLevelPreference(IProblem problem) {
		RootProblemPreference map = (RootProblemPreference) problem.getPreference();
		if (map == null) {
			map = new RootProblemPreference();
			if (problem instanceof IProblemWorkingCopy) {
				((IProblemWorkingCopy) problem).setPreference(map);
			}
		}
		return map;
	}

	/**
	 * Returns value of the preference for the key in the top level
	 * preference map for the given problem
	 *
	 * @param problem - problem for which to get the preference
	 * @param key - preference key
	 * @return value of the preference
	 */
	public Object getPreference(IProblem problem, String key) {
		return ((MapProblemPreference) problem.getPreference()).getChildValue(key);
	}

	/**
	 * @param arg - actual problem argument
	 * @param problem - problem kind
	 * @param exceptionListParamId - parameter id of the parameter representing
	 *        exception list for the given argumnet
	 * @return true if argument matches of the names in the exception list
	 * @since 2.0
	 */
	public boolean isFilteredArg(String arg, IProblem problem, String exceptionListParamId) {
		Object[] arr = (Object[]) getPreference(problem, exceptionListParamId);
		for (int i = 0; i < arr.length; i++) {
			String str = (String) arr[i];
			if (arg.equals(str))
				return true;
		}
		return false;
	}
}
