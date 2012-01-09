/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private static final int ADD_DEPENDENCIES = IIndexManager.ADD_DEPENDENCIES;
	private static final int ADD_DEPENDENT = IIndexManager.ADD_DEPENDENT;
	private static final int SKIP_PROVIDED = IIndexManager.SKIP_PROVIDED;
	private static final int ADD_EXTENSION_FRAGMENTS = IIndexManager.ADD_EXTENSION_FRAGMENTS;

	private PDOMManager fPDOMManager;

	public IndexFactory(PDOMManager manager) {
		fPDOMManager= manager;
	}

	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		projects = ArrayUtil.removeNulls(ICProject.class, projects);

		boolean addDependencies= (options & ADD_DEPENDENCIES) != 0;
		boolean addDependent= (options & ADD_DEPENDENT) != 0;
		boolean skipProvided= (options & SKIP_PROVIDED) != 0;
		boolean addExtensionFragments= (options & ADD_EXTENSION_FRAGMENTS) != 0;

		HashMap<IProject, Integer> map= new HashMap<IProject, Integer>();
		Collection<ICProject> selectedProjects= getProjects(projects, addDependencies, addDependent,
				map, new Integer(1));

		HashMap<String, IIndexFragment> fragments= new LinkedHashMap<String, IIndexFragment>();
		for (ICProject cproject : selectedProjects) {
			IIndexFragment pdom= fPDOMManager.getPDOM(cproject);
			if (pdom != null) {
				safeAddFragment(fragments, pdom);

				if (!skipProvided) {
					safeAddProvidedFragments(cproject, fragments, addExtensionFragments);
				}
			}
		}
		if (fragments.isEmpty()) {
			return EmptyCIndex.INSTANCE;
		}

		int primaryFragmentCount= fragments.size();

		if (!addDependencies) {
			projects= selectedProjects.toArray(new ICProject[selectedProjects.size()]);
			selectedProjects.clear();
			// Don't clear the map, so projects are not selected again.
			selectedProjects= getProjects(projects, true, false, map, new Integer(2));
			for (ICProject cproject : selectedProjects) {
				IIndexFragment pdom= fPDOMManager.getPDOM(cproject);
				safeAddFragment(fragments, pdom);

				if (!skipProvided) {
					safeAddProvidedFragments(cproject, fragments, addExtensionFragments);
				}
			}
		}

		Collection<IIndexFragment> pdoms= fragments.values();
		return new CIndex(pdoms.toArray(new IIndexFragment[pdoms.size()]), primaryFragmentCount);
	}

	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
		Map<String, IIndexFragment> readOnlyFrag= new LinkedHashMap<String, IIndexFragment>();
		IWritableIndexFragment pdom= (IWritableIndexFragment) fPDOMManager.getPDOM(project);
		if (pdom == null) {
			throw new CoreException(CCorePlugin.createStatus(
					NLS.bind(Messages.IndexFactory_errorNoSuchPDOM0, project.getElementName())));
		}
		safeAddProvidedFragments(project, readOnlyFrag, false);

		Collection<ICProject> selectedProjects= getProjects(new ICProject[] {project}, true, false,
				new HashMap<IProject, Integer>(), new Integer(1));
		selectedProjects.remove(project);

		for (ICProject cproject : selectedProjects) {
			safeAddFragment(readOnlyFrag, fPDOMManager.getPDOM(cproject));
		}

		Collection<IIndexFragment> readOnlyFragments= readOnlyFrag.values();
		return new WritableCIndex(pdom, readOnlyFragments.toArray(new IIndexFragment[readOnlyFragments.size()]));
	}

	private Collection<ICProject> getProjects(ICProject[] projects, boolean addDependencies,
			boolean addDependent, HashMap<IProject, Integer> map, Integer markWith) {
		List<IProject> projectsToSearch= new ArrayList<IProject>();

		for (ICProject cproject : projects) {
			IProject project= cproject.getProject();
			checkAddProject(project, map, projectsToSearch, markWith);
			projectsToSearch.add(project);
		}

		if (addDependencies || addDependent) {
			for (int i= 0; i < projectsToSearch.size(); i++) {
				IProject project= projectsToSearch.get(i);
				IProject[] nextLevel;
				try {
					if (addDependencies) {
						nextLevel = project.getReferencedProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
					if (addDependent) {
						nextLevel= project.getReferencingProjects();
						for (int j = 0; j < nextLevel.length; j++) {
							checkAddProject(nextLevel[j], map, projectsToSearch, markWith);
						}
					}
				} catch (CoreException e) {
					// silently ignore
					map.put(project, new Integer(0));
				}
			}
		}

		CoreModel cm= CoreModel.getDefault();
		Collection<ICProject> result= new ArrayList<ICProject>();
		for (Map.Entry<IProject, Integer> entry : map.entrySet()) {
			if (entry.getValue() == markWith) {
				ICProject cproject= cm.create(entry.getKey());
				if (cproject != null) {
					result.add(cproject);
				}
			}
		}
		return result;
	}

	private void checkAddProject(IProject project, HashMap<IProject, Integer> map,
			List<IProject> projectsToSearch, Integer markWith) {
		if (map.get(project) == null) {
			if (project.isOpen()) {
				map.put(project, markWith);
				projectsToSearch.add(project);
			} else {
				map.put(project, new Integer(0));
			}
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
					String id= fragment.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
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
	 * @param includeNonPDOMFragments
	 */
	private void safeAddProvidedFragments(ICProject cproject, Map<String, IIndexFragment> fragments,
			boolean includeNonPDOMFragments) {
		ICProjectDescription pd= CoreModel.getDefault().getProjectDescription(cproject.getProject(), false);
		if (pd != null) {
			IndexProviderManager ipm = CCoreInternals.getPDOMManager().getIndexProviderManager();
			ICConfigurationDescription cfg= pd.getDefaultSettingConfiguration();
			if (cfg != null) {
				try {
					IIndexFragment[] pFragments= ipm.getProvidedIndexFragments(cfg, includeNonPDOMFragments);
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
