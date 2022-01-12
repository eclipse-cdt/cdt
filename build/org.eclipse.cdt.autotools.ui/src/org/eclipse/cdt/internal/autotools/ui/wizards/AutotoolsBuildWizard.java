/*******************************************************************************
 * Copyright (c) 2007, 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Red Hat Inc - modification for Autotools project
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;

/**
 *
 */
public class AutotoolsBuildWizard extends AbstractCWizard {
	public static final String OTHERS_LABEL = AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.1"); //$NON-NLS-1$
	public static final String AUTOTOOLS_PROJECTTYPE_ID = "org.eclipse.linuxtools.cdt.autotools.core.projectType"; //$NON-NLS-1$

	/**
	 * @since 5.1
	 */
	public static final String EMPTY_PROJECT = AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.2"); //$NON-NLS-1$
	public static final String AUTOTOOLS_TOOLCHAIN_ID = "org.eclipse.linuxtools.cdt.autotools.core.toolChain"; //$NON-NLS-1$

	/**
	 * Creates and returns an array of items to be displayed
	 */
	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		ArrayList<EntryDescriptor> items = new ArrayList<>();

		// look for project types that have a toolchain based on the Autotools toolchain
		// and if so, add an entry for the project type.
		// Fix for bug#374026
		SortedMap<String, IProjectType> sm = ManagedBuildManager.getExtensionProjectTypeMap();
		for (Map.Entry<String, IProjectType> e : sm.entrySet()) {
			IProjectType pt = e.getValue();
			AutotoolsBuildWizardHandler h = new AutotoolsBuildWizardHandler(pt, parent, wizard);
			IToolChain[] tcs = ManagedBuildManager.getExtensionToolChains(pt);
			for (int i = 0; i < tcs.length; i++) {
				IToolChain t = tcs[i];

				IToolChain parent = t;
				while (parent.getSuperClass() != null) {
					parent = parent.getSuperClass();
				}

				if (!parent.getId().equals(AUTOTOOLS_TOOLCHAIN_ID))
					continue;

				if (t.isSystemObject())
					continue;
				if (!isValid(t, supportedOnly, wizard))
					continue;

				h.addTc(t);
			}

			if (h.getToolChainsCount() > 0)
				items.add(new EntryDescriptor(pt.getId(), null, pt.getName(), true, h, null));
		}

		return items.toArray(new EntryDescriptor[items.size()]);
	}
}
