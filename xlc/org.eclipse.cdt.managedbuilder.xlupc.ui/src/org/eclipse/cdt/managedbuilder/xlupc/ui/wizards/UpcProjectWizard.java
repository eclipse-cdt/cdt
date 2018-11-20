/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlupc.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.managedbuilder.xlupc.ui.Messages;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

@SuppressWarnings({ "nls", "serial" })
public class UpcProjectWizard extends CDTCommonProjectWizard {

	private static final String ARTIFACT_TYPE_UPC_EXE = "org.eclipse.cdt.build.core.buildArtefactType.xlupc.exe";
	private static final String ARTIFACT_TYPE_UPC_EXE_DEFAULT = ARTIFACT_TYPE_UPC_EXE + ".default";

	private static final String ARTIFACT_TYPE_UPC_SO = "org.eclipse.cdt.build.core.buildArtefactType.xlupc.sharedLib";
	private static final String ARTIFACT_TYPE_UPC_SO_DEFAULT = ARTIFACT_TYPE_UPC_SO + ".default";

	private static final String ARTIFACT_TYPE_UPC_LIB = "org.eclipse.cdt.build.core.buildArtefactType.xlupc.staticLib";
	private static final String ARTIFACT_TYPE_UPC_LIB_DEFAULT = ARTIFACT_TYPE_UPC_LIB + ".default";

	private final static String PAGE_NAME = "org.eclipse.cdt.managedbuilder.xlupc.ui.mainpage";

	private static final Set<String> ALL_TYPES = new HashSet<String>() {
		{
			add(ARTIFACT_TYPE_UPC_EXE);
			add(ARTIFACT_TYPE_UPC_EXE_DEFAULT);
			add(ARTIFACT_TYPE_UPC_SO);
			add(ARTIFACT_TYPE_UPC_SO_DEFAULT);
			add(ARTIFACT_TYPE_UPC_LIB);
			add(ARTIFACT_TYPE_UPC_LIB_DEFAULT);
		}
	};

	public UpcProjectWizard() {
		super(Messages.UpcProjectWizard_0, Messages.UpcProjectWizard_1);
	}

	@Override
	public void addPages() {
		fMainPage = new CDTMainWizardPage(PAGE_NAME) {
			@Override
			public List<EntryDescriptor> filterItems(List<EntryDescriptor> items) {
				// filter out all non-UPC project types
				if (items != null) {
					Iterator<EntryDescriptor> iter = items.iterator();
					while (iter.hasNext()) {
						EntryDescriptor entryDescriptor = iter.next();
						if (!ALL_TYPES.contains(entryDescriptor.getId()))
							iter.remove();
					}
				}
				return items;
			}
		};

		fMainPage.setTitle(Messages.UpcProjectWizard_0);
		fMainPage.setDescription(Messages.UpcProjectWizard_1);
		addPage(fMainPage);
	}

	@Override
	protected IProject continueCreation(IProject prj) {
		try {
			CProjectNature.addCNature(prj, new NullProgressMonitor());
		} catch (CoreException e) {
		}
		return prj;
	}

	@Override
	public String[] getNatures() {
		return new String[] {
				CProjectNature.C_NATURE_ID/*, CCProjectNature.CC_NATURE_ID, RemoteNature.REMOTE_NATURE_ID*/ };
	}

	@Override
	public String[] getContentTypeIDs() {
		return new String[] { CCorePlugin.CONTENT_TYPE_CSOURCE, CCorePlugin.CONTENT_TYPE_CHEADER,
				UPCLanguage.UPC_CONTENT_TYPE_ID };
	}
}
