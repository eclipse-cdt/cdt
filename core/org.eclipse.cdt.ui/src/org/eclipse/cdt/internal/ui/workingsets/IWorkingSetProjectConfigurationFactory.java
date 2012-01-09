/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.workingsets.WorkspaceSnapshot.ProjectState;

/**
 * Protocol for a factory of {@link IWorkingSetProjectConfiguration}s. Factories are {@linkplain Registry
 * registered} against project natures.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public interface IWorkingSetProjectConfigurationFactory {
	/**
	 * Queries my factory ID. The ID is persisted in the working set configuration data so that the same
	 * factory can be used to reconstruct project configurations when loading the working set configurations.
	 * 
	 * @return my unique identifier
	 */
	String getID();

	/**
	 * Creates a new project configuration element.
	 * 
	 * @param parent
	 *            the working set configuration that owns the new project configuration
	 * @param project
	 *            the workspace project for which to create the configuration
	 * 
	 * @return the new project configuration
	 */
	IWorkingSetProjectConfiguration createProjectConfiguration(IWorkingSetConfiguration parent,
			IProject project);

	/**
	 * Creates a UI controller to support editing the specified project configuration snapshot, which should
	 * have been obtained from a configuration that I previously
	 * {@linkplain #createProjectConfiguration(org.eclipse.cdt.internal.ui.workingsets.IWorkingSetConfiguration, IProject)
	 * created}, myself.
	 * 
	 * @param config
	 *            a project configuration snapshot that I created
	 * 
	 * @return a suitable controller for it. Must not be <code>null</code>
	 */
	IWorkingSetProjectConfigurationController createProjectConfigurationController(
			IWorkingSetProjectConfiguration.ISnapshot config);

	/**
	 * Creates a snapshot of the configuration state of a project in the workspace. This may capture
	 * additional build meta-data beyond just the "active configuration."
	 * 
	 * @param project
	 *            a project to capture in a {@link WorkspaceSnapshot}
	 * @param desc
	 *            the project description, from which to capture the initial configuration data
	 * 
	 * @return the project state capture. Must not be <code>null</code>
	 */
	WorkspaceSnapshot.ProjectState createProjectState(IProject project, ICProjectDescription desc);

	//
	// Nested types
	//

	/**
	 * A registry of {@linkplain IWorkingSetProjectConfigurationFactory project configuration factories}
	 * contributed on the <tt>org.eclipse.cdt.ui.workingSetConfigurations</tt> extension point.
	 * 
	 * @author Christian W. Damus (cdamus)
	 * 
	 * @since 6.0
	 */
	class Registry {
		private static final String EXT_PT_ID = "workingSetConfigurations"; //$NON-NLS-1$
		private static final String E_FACTORY = "projectConfigurationFactory"; //$NON-NLS-1$
		private static final String E_NATURE = "projectNature"; //$NON-NLS-1$
		private static final String A_ID = "id"; //$NON-NLS-1$
		private static final String A_CLASS = "class"; //$NON-NLS-1$

		/**
		 * The shared project configuration factory registry.
		 */
		public static Registry INSTANCE = new Registry();

		private final IWorkingSetProjectConfigurationFactory defaultFactory = new Default();
		private final Map<String, IWorkingSetProjectConfigurationFactory> factoriesByID = new java.util.HashMap<String, IWorkingSetProjectConfigurationFactory>();
		private final Map<String, IWorkingSetProjectConfigurationFactory> factoriesByNature = new java.util.HashMap<String, IWorkingSetProjectConfigurationFactory>();

		private Map<String, Set<String>> projectNaturePartOrdering;

		private Registry() {
			super();

			projectNaturePartOrdering = computeProjectNaturePartOrdering();

			loadExtensions();
		}

		public IWorkingSetProjectConfigurationFactory getFactory(String id) {
			IWorkingSetProjectConfigurationFactory result = get(factoriesByID, id);

			if (result == null) {
				result = defaultFactory;
			}

			return result;
		}

		public IWorkingSetProjectConfigurationFactory getFactory(IProject project) {
			IWorkingSetProjectConfigurationFactory result = null;

			for (String nature : getPartOrderedNatureIDs(project)) {
				result = get(factoriesByNature, nature);

				if (result != null) {
					break;
				}
			}

			return result;
		}

		private IWorkingSetProjectConfigurationFactory get(
				Map<?, IWorkingSetProjectConfigurationFactory> map, Object key) {
			return map.get(key);
		}

		private String[] getPartOrderedNatureIDs(IProject project) {
			String[] result;

			try {
				result = project.getDescription().getNatureIds();
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
				result = new String[0];
			}

			if (result.length > 0) {
				Arrays.sort(result, new Comparator<String>() {
					@Override
					public int compare(String nature1, String nature2) {
						Set<String> required1 = projectNaturePartOrdering.get(nature1);
						Set<String> required2 = projectNaturePartOrdering.get(nature2);

						if (required1.contains(nature2)) {
							return -1; // required1 precedes required2
						} else if (required2.contains(nature1)) {
							return +1; // required2 precedes required1
						} else if (nature1.startsWith("org.eclipse.cdt.") //$NON-NLS-1$
								&& !nature2.startsWith("org.eclipse.cdt.")) { //$NON-NLS-1$
							return +1; // lower priority to CDT natures
						} else if (nature2.startsWith("org.eclipse.cdt.") //$NON-NLS-1$
								&& !nature1.startsWith("org.eclipse.cdt.")) { //$NON-NLS-1$
							return -1; // lower priority to CDT natures
						}

						return 0; // not partially comparable
					}
				});
			}

			return result;
		}

		private Map<String, Set<String>> computeProjectNaturePartOrdering() {
			Map<String, Set<String>> result = new java.util.HashMap<String, Set<String>>();

			// first pass to populate the map with immediate requireds
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			for (IProjectNatureDescriptor next : ws.getNatureDescriptors()) {
				result.put(next.getNatureId(), new java.util.HashSet<String>(Arrays.asList(next
						.getRequiredNatureIds())));
			}

			// now, iterate to add transitive requireds
			boolean loopAgain;
			do {
				loopAgain = false;

				for (Map.Entry<String, Set<String>> next : result.entrySet()) {
					Set<String> requireds = next.getValue();
					Set<String> newRequireds = new java.util.HashSet<String>(requireds);

					boolean changed = false;

					for (String required : requireds) {
						changed |= newRequireds.addAll(result.get(required));
					}

					if (changed) {
						loopAgain = true;
						next.setValue(newRequireds);
					}
				}
			} while (loopAgain);

			return result;
		}

		private void loadExtensions() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			for (IExtension ext : registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, EXT_PT_ID).getExtensions()) {
				for (IConfigurationElement element : ext.getConfigurationElements()) {
					if (E_FACTORY.equals(element.getName())) {
						try {
							Descriptor desc = new Descriptor(element);

							synchronized (factoriesByID) {
								factoriesByID.put(desc.getID(), desc);
							}

							synchronized (factoriesByNature) {
								for (IConfigurationElement nature : element.getChildren(E_NATURE)) {
									String natureID = nature.getAttribute(A_ID);

									if (natureID != null) {
										factoriesByNature.put(natureID, desc);
									} else {
										CUIPlugin.log(NLS.bind(
												WorkingSetMessages.WSProjConfigFactory_noNatureID, ext
														.getContributor().getName()), null);
									}
								}
							}
						} catch (CoreException e) {
							CUIPlugin.log(e.getStatus());
						}
					}
				}
			}
		}

		//
		// Nested classes
		//

		/**
		 * A self-resolving descriptor for lazy instantiation of a factory.
		 */
		private class Descriptor implements IWorkingSetProjectConfigurationFactory {
			private final IConfigurationElement extension;
			private final String id;

			Descriptor(IConfigurationElement extension) throws CoreException {
				this.extension = extension;
				id = extension.getAttribute(A_ID);

				if (id == null) {
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, NLS.bind(
							WorkingSetMessages.WSProjConfigFactory_noFactoryID, extension.getContributor()
									.getName())));
				}
			}

			@Override
			public String getID() {
				return id;
			}

			@Override
			public IWorkingSetProjectConfiguration createProjectConfiguration(
					IWorkingSetConfiguration parent, IProject project) {
				return resolve().createProjectConfiguration(parent, project);
			}

			@Override
			public IWorkingSetProjectConfigurationController createProjectConfigurationController(
					IWorkingSetProjectConfiguration.ISnapshot config) {

				return resolve().createProjectConfigurationController(config);
			}

			@Override
			public ProjectState createProjectState(IProject project, ICProjectDescription desc) {
				return resolve().createProjectState(project, desc);
			}

			private IWorkingSetProjectConfigurationFactory resolve() {
				IWorkingSetProjectConfigurationFactory result = null;

				try {
					result = (IWorkingSetProjectConfigurationFactory) extension
							.createExecutableExtension(A_CLASS);
				} catch (ClassCastException e) {
					CUIPlugin.log(NLS.bind(WorkingSetMessages.WSProjConfigFactory_badFactory, extension
							.getContributor().getName()), e);
				} catch (CoreException e) {
					CUIPlugin.log(new MultiStatus(CUIPlugin.PLUGIN_ID, 0, new IStatus[] { e.getStatus() },
							WorkingSetMessages.WSProjConfigFactory_factoryFailed, null));
				}

				if (result == null) {
					result = defaultFactory;
				}

				// replace the descriptor in the maps
				synchronized (factoriesByID) {
					factoriesByID.put(getID(), result);
				}
				synchronized (factoriesByNature) {
					for (Map.Entry<String, IWorkingSetProjectConfigurationFactory> next : factoriesByNature
							.entrySet()) {
						if (next.getValue().getID().equals(getID())) {
							next.setValue(result);
						}
					}
				}

				return result;
			}
		}

		/**
		 * The default project configuration factory. Clients may extend this class to implement custom
		 * factories for their project natures.
		 * 
		 * @author Christian W. Damus (cdamus)
		 * 
		 * @since 6.0
		 */
		public static class Default implements IWorkingSetProjectConfigurationFactory, IExecutableExtension {

			private String id;

			public Default() {
				super();
			}

			@Override
			public IWorkingSetProjectConfiguration createProjectConfiguration(
					IWorkingSetConfiguration parent, IProject project) {

				WorkingSetProjectConfiguration result = createProjectConfiguration(parent);
				result.setProjectName(project.getName());
				return result;
			}

			protected WorkingSetProjectConfiguration createProjectConfiguration(
					IWorkingSetConfiguration parent) {
				return new WorkingSetProjectConfiguration(parent);
			}

			@Override
			public IWorkingSetProjectConfigurationController createProjectConfigurationController(
					IWorkingSetProjectConfiguration.ISnapshot config) {

				return new ProjectConfigurationController(config);
			}

			@Override
			public ProjectState createProjectState(IProject project, ICProjectDescription desc) {
				return new WorkspaceSnapshot.ProjectState(project, desc);
			}

			@Override
			public String getID() {
				return id;
			}

			@Override
			public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
					throws CoreException {

				this.id = config.getAttribute(A_ID);
			}

		}
	}
}
