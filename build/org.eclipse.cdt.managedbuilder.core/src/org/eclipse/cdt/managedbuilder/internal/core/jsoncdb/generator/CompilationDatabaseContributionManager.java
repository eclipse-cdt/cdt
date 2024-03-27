/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core.jsoncdb.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.jsoncdb.CompilationDatabaseInformation;
import org.eclipse.cdt.managedbuilder.core.jsoncdb.ICompilationDatabaseContributor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;

/* package */ final class CompilationDatabaseContributionManager {
	private static final String ATTRIB_RUNNER = "runner"; //$NON-NLS-1$
	private static final String ATTRIB_TOOLCHAIN_ID = "toolchainID"; //$NON-NLS-1$
	private static final String ID_COMPILATIONDATABASE = "compilationDatabase"; //$NON-NLS-1$
	private static final String EXTENSION_ID = "compilationDatabaseContributor"; //$NON-NLS-1$
	/**
	 * Map of tool chain IDs (see {@link IToolChain#getId()} to
	 * loaded instances of {@link ICompilationDatabaseContributor}
	 */
	@NonNull
	private final Map<String, ICompilationDatabaseContributor> loadedInstances = new HashMap<>();
	/**
	 * Map of tool chain IDs (see {@link IToolChain#getId()} to
	 * extension point information for the compilationDatabaseContributor extension.
	 */
	private final Map<String, IConfigurationElement> factoryExtensions = new HashMap<>();

	private class EmptyCompilationDatabaseContributor implements ICompilationDatabaseContributor {

		@Override
		public final @NonNull List<CompilationDatabaseInformation> getAdditionalFiles(@NonNull IConfiguration config) {
			return Collections.emptyList();
		}
	}

	private static CompilationDatabaseContributionManager instance;

	private CompilationDatabaseContributionManager() {
		initalise();
	}

	public static synchronized CompilationDatabaseContributionManager getInstance() {
		if (CompilationDatabaseContributionManager.instance == null) {
			CompilationDatabaseContributionManager.instance = new CompilationDatabaseContributionManager();
		}
		return CompilationDatabaseContributionManager.instance;
	}

	private void initalise() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(
				"org.eclipse.cdt.managedbuilder.core", CompilationDatabaseContributionManager.EXTENSION_ID); //$NON-NLS-1$
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					if (CompilationDatabaseContributionManager.ID_COMPILATIONDATABASE.equals(configElement.getName())) { // $NON-NLS-1$
						String toolchainId = configElement
								.getAttribute(CompilationDatabaseContributionManager.ATTRIB_TOOLCHAIN_ID);
						String className = configElement
								.getAttribute(CompilationDatabaseContributionManager.ATTRIB_RUNNER);
						if (toolchainId != null && className != null) {
							factoryExtensions.put(toolchainId, configElement);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the Code Assist tool with the specified id
	 *
	 * @param id
	 * @return Tool extension or a list with no element if not found.
	 * @throws CoreException
	 */

	@NonNull
	public synchronized ICompilationDatabaseContributor getCompilationDatabaseContributor(
			@NonNull IConfiguration config) {
		IToolChain toolChain = config.getToolChain();
		while (toolChain != null) {
			String toolchainId = toolChain.getBaseId();
			if (loadedInstances.containsKey(toolchainId)) {
				ICompilationDatabaseContributor contributor = loadedInstances.get(toolchainId);
				Assert.isNotNull(contributor);
				return contributor;
			} else if (factoryExtensions.containsKey(toolchainId)) {
				return createCdbInstance(toolchainId);
			} else {
				toolChain = toolChain.getSuperClass();
			}
		}
		return new EmptyCompilationDatabaseContributor();
	}

	@NonNull
	private ICompilationDatabaseContributor createCdbInstance(String toolchainId) {
		IConfigurationElement ele = factoryExtensions.get(toolchainId);
		if (ele != null) {
			ICompilationDatabaseContributor loaded = null;
			try {
				loaded = (ICompilationDatabaseContributor) ele
						.createExecutableExtension(CompilationDatabaseContributionManager.ATTRIB_RUNNER);

			} catch (CoreException e) {
				Platform.getLog(getClass()).log(Status.error("Not able to create instance", e)); //$NON-NLS-1$
			}
			if (loaded == null) {
				loaded = new EmptyCompilationDatabaseContributor();
			}
			loadedInstances.put(toolchainId, loaded);
			return loaded;
		}

		return new EmptyCompilationDatabaseContributor();
	}

}
