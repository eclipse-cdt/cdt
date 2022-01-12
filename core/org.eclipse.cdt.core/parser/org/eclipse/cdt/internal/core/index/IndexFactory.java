/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

import static org.eclipse.cdt.core.index.IIndexManager.ADD_DEPENDENCIES;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_DEPENDENT;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_CALL_HIERARCHY;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_SEARCH;
import static org.eclipse.cdt.core.index.IIndexManager.ADD_EXTENSION_FRAGMENTS_TYPE_HIERARCHY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;

/**
 * Class that creates indexes based on PDOMs
 * @since 4.0
 */
public class IndexFactory {
	private static final int ALL_FRAGMENT_OPTIONS = ADD_EXTENSION_FRAGMENTS_ADD_IMPORT
			| ADD_EXTENSION_FRAGMENTS_CALL_HIERARCHY | ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST
			| ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER | ADD_EXTENSION_FRAGMENTS_NAVIGATION
			| ADD_EXTENSION_FRAGMENTS_SEARCH | ADD_EXTENSION_FRAGMENTS_TYPE_HIERARCHY | ADD_EXTENSION_FRAGMENTS_EDITOR;

	private PDOMManager fPDOMManager;

	public IndexFactory(PDOMManager manager) {
		fPDOMManager = manager;
	}

	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		projects = ArrayUtil.removeNulls(ICProject.class, projects);

		boolean addDependencies = (options & ADD_DEPENDENCIES) != 0;
		boolean addDependent = (options & ADD_DEPENDENT) != 0;
		int fragmentUsage = options & ALL_FRAGMENT_OPTIONS;

		Collection<ICProject> indexProjects = getProjects(projects, addDependencies, addDependent,
				new HashSet<IProject>());

		HashMap<String, IIndexFragment> fragments = new LinkedHashMap<>();
		for (ICProject cproject : indexProjects) {
			IIndexFragment pdom = fPDOMManager.getPDOM(cproject);
			if (pdom != null) {
				safeAddFragment(fragments, pdom);
				if (fragmentUsage != 0) {
					safeAddProvidedFragments(cproject, fragments, fragmentUsage);
				}
			}
		}
		if (fragments.isEmpty()) {
			return EmptyCIndex.INSTANCE;
		}

		Collection<IIndexFragment> pdoms = fragments.values();
		return new CIndex(pdoms.toArray(new IIndexFragment[pdoms.size()]));
	}

	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
		IWritableIndexFragment pdom = (IWritableIndexFragment) fPDOMManager.getPDOM(project);
		if (pdom == null) {
			throw new CoreException(CCorePlugin
					.createStatus(NLS.bind(Messages.IndexFactory_errorNoSuchPDOM0, project.getElementName())));
		}
		return new WritableCIndex(pdom);
	}

	private Collection<ICProject> getProjects(ICProject[] projects, boolean addDependencies, boolean addDependent,
			Set<IProject> handled) {
		List<ICProject> result = new ArrayList<>();

		for (ICProject cproject : projects) {
			checkAddProject(cproject, handled, result);
		}

		if (addDependencies || addDependent) {
			final CoreModel cm = CoreModel.getDefault();
			for (int i = 0; i < result.size(); i++) {
				ICProject cproject = result.get(i);
				IProject project = cproject.getProject();
				try {
					if (addDependencies) {
						for (IProject rp : project.getReferencedProjects()) {
							checkAddProject(cm.create(rp), handled, result);
						}
					}
					if (addDependent) {
						for (IProject rp : project.getReferencingProjects()) {
							checkAddProject(cm.create(rp), handled, result);
						}
					}
				} catch (CoreException e) {
					// silently ignore
					handled.add(project);
				}
			}
		}
		return result;
	}

	private void checkAddProject(ICProject cproject, Set<IProject> handled, List<ICProject> target) {
		if (cproject != null) {
			IProject project = cproject.getProject();
			if (handled.add(project) && project.isOpen())
				target.add(cproject);
		}
	}

	/**
	 * Add an entry for the specified fragment. This copes with problems occurring when reading
	 * the fragment ID.
	 * @param id2fragment the map to add the entry to
	 * @param fragment the fragment or null (which will result in no action)
	 */
	private void safeAddFragment(Map<String, IIndexFragment> id2fragment, IIndexFragment fragment) {
		if (fragment != null) {
			try {
				fragment.acquireReadLock();
				try {
					String id = fragment.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
					id2fragment.put(id, fragment);
				} finally {
					fragment.releaseReadLock();
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} catch (InterruptedException e) {
				CCorePlugin.log(e);
			}
		}
	}

	/**
	 * Adds ID -> IIndexFragment entries to the specified Map, for fragments provided under
	 * the CIndex extension point for the specified ICProject.
	 * @param cproject The project to get the provided index fragments for.
	 * @param fragments
	 * @param usage the usage of the fragments in terms of {@link IIndexManager#ADD_EXTENSION_FRAGMENTS_ADD_IMPORT}, ...
	 */
	private void safeAddProvidedFragments(ICProject cproject, Map<String, IIndexFragment> fragments, int usage) {
		ICProjectDescription pd = CoreModel.getDefault().getProjectDescription(cproject.getProject(), false);
		if (pd != null) {
			IndexProviderManager ipm = CCoreInternals.getPDOMManager().getIndexProviderManager();
			ICConfigurationDescription cfg = pd.getDefaultSettingConfiguration();
			if (cfg != null) {
				try {
					IIndexFragment[] pFragments = ipm.getProvidedIndexFragments(cfg, usage);
					for (IIndexFragment fragment : pFragments) {
						safeAddFragment(fragments, fragment);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}
}
