/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Red Hat Inc. - Fix Bug 406711
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.autotools.ui.ErrorParserBlock;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.newui.CDTHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("deprecation")
public class NewAutotoolsProjectOptionPage extends NewCProjectWizardOptionPage {

	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.projectOptionsPage"; //$NON-NLS-1$

	public static class ManagedWizardOptionBlock extends ManagedProjectOptionBlock {

		NewAutotoolsProjectOptionPage parent;

		public ManagedWizardOptionBlock(NewAutotoolsProjectOptionPage parentPage) {
			super(parentPage);
			parent = parentPage;
		}

		public static class AutotoolsReferenceBlock extends ReferenceBlock {
			AutotoolsReferenceBlock() {
				super();
			}

			@Override
			public void performApply(IProgressMonitor monitor) {
				try {
					super.performApply(monitor);
				} catch (RuntimeException e) {
					// TODO: Fix ReferenceBlock not to generate NullPointerException
					//       when no projects are referenced
				}
			}
		}

		public void updateProjectTypeProperties() {
		}

		@Override
		protected void addTabs() {
			addTab(new AutotoolsReferenceBlock());
			// Bug 406711 - Remove the IndexerBlock as this causes an exception to occur
			//             because the project handle isn't set up.  It is not needed.
		}

		public void setupHelpContextIds() {
			List<ICOptionPage> pages = getOptionPages();

			Iterator<ICOptionPage> iter = pages.iterator();
			for (int i = 0; i < 3 && iter.hasNext(); i++) {
				ICOptionPage page = iter.next();

				String id = null;
				if (page instanceof ReferenceBlock) {
					id = CDTHelpContextIds.MAN_PROJ_WIZ_PROJECTS_TAB;
				} else if (page instanceof ErrorParserBlock) {
					id = CDTHelpContextIds.MAN_PROJ_WIZ_ERRORPARSERS_TAB;
				}
				PlatformUI.getWorkbench().getHelpSystem().setHelp(page.getControl(), id);
			}
		}
	}

	protected ManagedWizardOptionBlock optionBlock;

	/**
	 * @param pageName
	 */
	public NewAutotoolsProjectOptionPage(String pageName) {
		super(pageName);
		optionBlock = new ManagedWizardOptionBlock(this);
	}

	@Override
	protected TabFolderOptionBlock createOptionBlock() {
		return optionBlock;
	}

	@Override
	public IProject getProject() {
		if (getWizard() instanceof ConvertToAutotoolsProjectWizard)
			return ((ConvertToAutotoolsProjectWizard) getWizard()).getProject();
		return null;
	}

	@Override
	public Preferences getPreferences() {
		return ManagedBuilderUIPlugin.getDefault().getPluginPreferences();
	}

	public void updateProjectTypeProperties() {
		//  Update the error parser list
		optionBlock.updateProjectTypeProperties();
	}

	public void setupHelpContextIds() {
		optionBlock.setupHelpContextIds();
	}

	@Override
	public IWizardPage getNextPage() {
		return MBSCustomPageManager.getNextPage(PAGE_ID); // get first custom page, if any
	}

}
