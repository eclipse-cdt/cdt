/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.Checkers;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.LaunchModeProblemPreference;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemCategory;
import org.eclipse.cdt.codan.internal.core.model.ProblemProfile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * Implementation of checker registry interface
 */
public class CheckersRegistry implements Iterable<IChecker>, ICheckersRegistry {
	private static final String NAME_ATTR = "name"; //$NON-NLS-1$
	private static final String ID_ATTR = "id"; //$NON-NLS-1$
	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
	private static final String CHECKERS_EXTENSION_POINT_NAME = "checkers"; //$NON-NLS-1$
	private static final String CHECKER_ENABLEMENT_EXTENSION_POINT_NAME = "checkerEnablement"; //$NON-NLS-1$
	private static final String CHECKER_ELEMENT = "checker"; //$NON-NLS-1$
	private static final String PROBLEM_ELEMENT = "problem"; //$NON-NLS-1$
	private static final String CATEGORY_ELEMENT = "category"; //$NON-NLS-1$
	private static final Object DEFAULT = "DEFAULT"; //$NON-NLS-1$
	public static final String CLONE_SUFFIX = ".COPY"; //$NON-NLS-1$
	private Collection<IChecker> checkers = new ArrayList<IChecker>();
	private static CheckersRegistry instance;
	private static boolean initialized = false;
	private final Map<Object, IProblemProfile> profiles = new HashMap<Object, IProblemProfile>();
	private final Map<IChecker, Collection<IProblem>> problemList = new HashMap<IChecker, Collection<IProblem>>();
	private final Map<String, IChecker> problemCheckerMapping = new HashMap<String, IChecker>();
	private final List<ICheckerEnablementVerifier> checkerEnablementVerifiers = new ArrayList<ICheckerEnablementVerifier>();

	private CheckersRegistry() {
		instance = this;
		profiles.put(DEFAULT, new ProblemProfile(DEFAULT));
		readCheckersRegistry();
		readCheckerEnablementVerifier();
		initialized = true;
	}

	private void readCheckersRegistry() {
		IExtensionPoint ep = getExtensionPoint(CHECKERS_EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		IConfigurationElement[] elements = ep.getConfigurationElements();
		// process categories
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			processCategories(configurationElement);
		}
		// process shared problems
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			processProblem(configurationElement);
		}
		// process checkers
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			processChecker(configurationElement);
		}
		// init parameters for checkers with parameters
		for (Iterator<IChecker> iterator = problemList.keySet().iterator(); iterator.hasNext();) {
			IChecker c = iterator.next();
			if (c instanceof ICheckerWithPreferences) {
				Collection<IProblem> list = problemList.get(c);
				for (Iterator<IProblem> iterator2 = list.iterator(); iterator2.hasNext();) {
					IProblem p = iterator2.next();
					if (p instanceof IProblemWorkingCopy) {
						try {
							((ICheckerWithPreferences) c).initPreferences((IProblemWorkingCopy) p);
						} catch (Throwable t) {
							CodanCorePlugin.log(t);
						}
					}
				}
			}
		}
	}

	/**
	 * @param configurationElement
	 */
	private void processCategories(IConfigurationElement configurationElement) {
		if (configurationElement.getName().equals(CATEGORY_ELEMENT)) {
			String id = getAtt(configurationElement, ID_ATTR);
			if (id == null)
				return;
			String name = getAtt(configurationElement, NAME_ATTR);
			if (name == null)
				return;
			CodanProblemCategory cat = new CodanProblemCategory(id, name);
			String category = getAtt(configurationElement, "parentCategory", false); //$NON-NLS-1$
			addCategory(cat, category);
		}
	}

	/**
	 * @param configurationElement
	 */
	private void processChecker(IConfigurationElement configurationElement) {
		try {
			if (configurationElement.getName().equals(CHECKER_ELEMENT)) {
				String id = getAtt(configurationElement, ID_ATTR);
				if (id == null)
					return;
				String name = getAtt(configurationElement, NAME_ATTR, false);
				if (name == null)
					name = id;
				IChecker checkerObj = null;
				try {
					Object checker = configurationElement.createExecutableExtension(CLASS_ATTR);
					checkerObj = (IChecker) checker;
					addChecker(checkerObj);
				} catch (CoreException e) {
					CodanCorePlugin.log(e);
					return;
				}
				boolean hasRef = false;
				IConfigurationElement[] children2 = configurationElement.getChildren(PROBLEM_ELEMENT);
				if (children2 != null) {
					for (IConfigurationElement ref : children2) {
						IProblem p = processProblem(ref);
						addRefProblem(checkerObj, p);
						hasRef = true;
					}
				}
				IConfigurationElement[] children1 = configurationElement.getChildren("problemRef"); //$NON-NLS-1$
				if (children1 != null) {
					for (IConfigurationElement ref : children1) {
						hasRef = true;
						IProblem p = getDefaultProfile().findProblem(ref.getAttribute("refId")); //$NON-NLS-1$
						addRefProblem(checkerObj, p);
					}
				}
				if (!hasRef) {
					CodanProblem p = new CodanProblem(id, name);
					addProblem(p, null);
					addRefProblem(checkerObj, p);
				}
			}
		} catch (Throwable e) {
			CodanCorePlugin.log(e);
		}
	}

	/**
	 * @param configurationElement
	 * @return
	 */
	private CodanProblem processProblem(IConfigurationElement configurationElement) {
		if (configurationElement.getName().equals(PROBLEM_ELEMENT)) {
			String id = getAtt(configurationElement, ID_ATTR);
			if (id == null)
				return null;
			String name = getAtt(configurationElement, NAME_ATTR);
			if (name == null)
				name = id;
			CodanProblem p = new CodanProblem(id, name);
			String category = getAtt(configurationElement, "category", false); //$NON-NLS-1$
			if (category == null)
				category = "org.eclipse.cdt.codan.core.categories.ProgrammingProblems"; //$NON-NLS-1$
			String enab = getAtt(configurationElement, "defaultEnabled", false); //$NON-NLS-1$
			String sev = getAtt(configurationElement, "defaultSeverity", false); //$NON-NLS-1$
			String patt = getAtt(configurationElement, "messagePattern", false); //$NON-NLS-1$
			String desc = getAtt(configurationElement, "description", false); //$NON-NLS-1$
			String markerType = getAtt(configurationElement, "markerType", false); //$NON-NLS-1$
			String smultiple = getAtt(configurationElement, "multiple", false); //$NON-NLS-1$
			if (enab != null) {
				p.setEnabled(Boolean.valueOf(enab));
			}
			if (sev != null) {
				CodanSeverity cSev = CodanSeverity.valueOf(sev);
				if (cSev != null)
					p.setSeverity(cSev);
			}
			if (patt != null) {
				p.setMessagePattern(patt);
			}
			if (markerType != null) {
				p.setMarkerType(markerType);
			}
			p.setDescription(desc);
			if (smultiple != null) {
				p.setMultiple(Boolean.valueOf(smultiple));
			}
			addProblem(p, category);
			return p;
		}
		return null;
	}

	private static String getAtt(IConfigurationElement configurationElement, String name) {
		return getAtt(configurationElement, name, true);
	}

	private static String getAtt(IConfigurationElement configurationElement, String name, boolean req) {
		String elementValue = configurationElement.getAttribute(name);
		if (elementValue == null && req)
			CodanCorePlugin.log("Extension " //$NON-NLS-1$
					+ configurationElement.getDeclaringExtension().getUniqueIdentifier()
					+ " missing required attribute: " + configurationElement.getName() //$NON-NLS-1$
					+ "." + name); //$NON-NLS-1$
		return elementValue;
	}

	private void readCheckerEnablementVerifier() {
		IExtensionPoint ep = getExtensionPoint(CHECKER_ENABLEMENT_EXTENSION_POINT_NAME);
		for (IConfigurationElement ce : ep.getConfigurationElements()) {
			try {
				checkerEnablementVerifiers.add((ICheckerEnablementVerifier) ce.createExecutableExtension(CLASS_ATTR));
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
	}

	private IExtensionPoint getExtensionPoint(String extensionPointName) {
		return Platform.getExtensionRegistry().getExtensionPoint(CodanCorePlugin.PLUGIN_ID, extensionPointName);
	}

	@Override
	public Iterator<IChecker> iterator() {
		return checkers.iterator();
	}

	/**
	 * @return the singleton checkers registry
	 */
	public static synchronized CheckersRegistry getInstance() {
		if (instance == null)
			return new CheckersRegistry();
		if (!initialized)
			throw new IllegalStateException("Registry is not initialized"); //$NON-NLS-1$
		return instance;
	}

	@Override
	public void addChecker(IChecker checker) {
		checkers.add(checker);
	}

	@Override
	public void addProblem(IProblem p, String category) {
		IProblemCategory cat = getDefaultProfile().findCategory(category);
		if (cat == null)
			cat = getDefaultProfile().getRoot();
		((ProblemProfile) getDefaultProfile()).addProblem(p, cat);
	}

	@Override
	public void addCategory(IProblemCategory p, String category) {
		IProblemCategory cat = getDefaultProfile().findCategory(category);
		if (cat == null)
			cat = getDefaultProfile().getRoot();
		((ProblemProfile) getDefaultProfile()).addCategory(p, cat);
	}

	@Override
	public void addRefProblem(IChecker c, IProblem p) {
		Collection<IProblem> plist = problemList.get(c);
		if (plist == null) {
			plist = new ArrayList<IProblem>();
			problemList.put(c, plist);
		}
		plist.add(p);
		problemCheckerMapping.put(p.getId(), c);
	}

	/**
	 * Returns the checker associated with a problem.
	 * @param problem the given problem.
	 * @return the checker associated with a problem.
	 */
	public IChecker getCheckerForProblem(IProblem problem) {
		return problemCheckerMapping.get(problem.getId());
	}

	/**
	 * Returns list of problems registered for given checker
	 *
	 * @return collection of problems or null
	 */
	@Override
	public Collection<IProblem> getRefProblems(IChecker checker) {
		return problemList.get(checker);
	}

	@Override
	public IProblemProfile getDefaultProfile() {
		return profiles.get(DEFAULT);
	}

	@Override
	public IProblemProfile getWorkspaceProfile() {
		IProblemProfile wp = profiles.get(ResourcesPlugin.getWorkspace());
		if (wp == null) {
			wp = (IProblemProfile) getDefaultProfile().clone();
			((ProblemProfile) wp).setResource(ResourcesPlugin.getWorkspace());
			// load default values
			CodanPreferencesLoader loader = new CodanPreferencesLoader(wp);
	    	Preferences[] preferences = {
					InstanceScope.INSTANCE.getNode(CodanCorePlugin.PLUGIN_ID),
					ConfigurationScope.INSTANCE.getNode(CodanCorePlugin.PLUGIN_ID),
					DefaultScope.INSTANCE.getNode(CodanCorePlugin.PLUGIN_ID),
				};
			loader.load(preferences);
			profiles.put(ResourcesPlugin.getWorkspace(), wp);
		}
		return wp;
	}

	@Override
	public void updateProfile(IResource element, IProblemProfile profile) {
		// Updating profile can invalidate all cached profiles
		IProblemProfile defaultProfile = getDefaultProfile();
		profiles.clear();
		profiles.put(DEFAULT, defaultProfile);
		if (profile != null && element != null)
			profiles.put(element, profile);
	}

	@Override
	public IProblemProfile getResourceProfile(IResource element) {
		IProblemProfile prof = profiles.get(element);
		if (prof == null) {
			if (element instanceof IProject) {
				prof = (IProblemProfile) getWorkspaceProfile().clone();
				((ProblemProfile) prof).setResource(element);
				// Load default values
				CodanPreferencesLoader loader = new CodanPreferencesLoader(prof);
				Preferences projectNode = CodanPreferencesLoader.getProjectNode((IProject) element);
				boolean useWorkspace = projectNode.getBoolean(PreferenceConstants.P_USE_PARENT, true);
				if (!useWorkspace) {
					loader.load(projectNode);
				}
				profiles.put(element, prof);
			} else if (element.getParent() != null) {
				prof = getResourceProfile(element.getParent());
			} else {
				prof = getResourceProfile(element.getProject());
			}
		}
		return prof;
	}

	@Override
	public IProblemProfile getResourceProfileWorkingCopy(IResource element) {
		IProblemProfile prof = (IProblemProfile) getResourceProfile(element).clone();
		return prof;
	}

	/**
	 * Tests if a checker needs to run in a specific launch mode.
	 *
	 * @param checker
	 * @param resource
	 * @param mode
	 * @return <code>true</code> if the checker should run.
	 */
	public boolean isCheckerEnabled(IChecker checker, IResource resource, CheckerLaunchMode mode) {
		if (resource.getType() != IResource.FILE) {
			return false;
		}

		if (mode == CheckerLaunchMode.RUN_AS_YOU_TYPE && !Checkers.canCheckerRunAsYouType(checker)) {
			return false;
		}
		for (ICheckerEnablementVerifier verifier : checkerEnablementVerifiers) {
			if (!verifier.isCheckerEnabled(checker, resource, mode)) {
				return false;
			}
		}

		IProblemProfile resourceProfile = getResourceProfile(resource);
		Collection<IProblem> refProblems = getRefProblems(checker);
		for (IProblem p : refProblems) {
			// We need to check problem enablement in a particular profile.
			IProblem problem = resourceProfile.findProblem(p.getId());
			if (problem == null)
				throw new IllegalArgumentException(p.getId() + " is not registered"); //$NON-NLS-1$
			if (!problem.isEnabled())
				return false;
			if (checker instanceof AbstractCheckerWithProblemPreferences) {
				LaunchModeProblemPreference pref =
						((AbstractCheckerWithProblemPreferences) checker).getLaunchModePreference(problem);
				if (pref.isRunningInMode(mode)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the number of checkers
	 */
	public int getCheckersSize() {
		return checkers.size();
	}

	/**
	 * Create a replicated problem - it has same check and same initial values
	 * as original but user can modify it further
	 *
	 * @param problem
	 * @param profile
	 */
	public void replicateProblem(IProblem problem, IProblemProfile profile) {
		CodanProblem x = (CodanProblem) problem.clone();
		x.setId(getNextCloneId(problem, profile));
		((ProblemProfile) profile).addProblem(x, problem.getParentCategory());
	}

	/**
	 * @param problem
	 * @param profile
	 * @return
	 */
	private String getNextCloneId(IProblem problem, IProblemProfile profile) {
		IProblem[] problems = profile.getProblems();
		String prefix = problem.getId() + CLONE_SUFFIX;
		int max = 0;
		for (int i = 0; i < problems.length; i++) {
			IProblem x = problems[i];
			if (x.getId().startsWith(prefix)) {
				int num = 0;
				try {
					num = Integer.parseInt(x.getId().substring(prefix.length()));
				} catch (NumberFormatException e) {
					// well...
				}
				if (max < num)
					max = num;
			}
		}
		max++;
		return prefix + max;
	}
}
