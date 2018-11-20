/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/

package org.eclipse.cdt.make.ui.wizards;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.ReferenceBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 *
 * @deprecated as of CDT 4.0. This option page was used for New Project Wizard
 * for 3.X style projects.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class MakeProjectWizardOptionPage extends NewCProjectWizardOptionPage {
	MakeWizardOptionBlock makeWizardBlock;

	public class MakeWizardOptionBlock extends MakeProjectOptionBlock {
		IndexerBlock indexBlock;

		public MakeWizardOptionBlock(ICOptionContainer parent) {
			super(parent);
		}

		@Override
		protected void addTabs() {
			addTab(new ReferenceBlock());
			super.addTabs();
			addTab(indexBlock = new IndexerBlock());
		}

		public void setupHelpContextIds() {
			List<ICOptionPage> pages = getOptionPages();

			Iterator<ICOptionPage> iter = pages.iterator();
			for (int i = 0; i < 6 && iter.hasNext(); i++) {
				ICOptionPage page = iter.next();

				String id = null;
				switch (i) {
				case 0:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_PROJECTS_TAB;
					break;
				case 1:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_MAKEBUILDER_TAB;
					break;
				case 2:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_ERRORPARSER_TAB;
					break;
				case 3:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_BINARYPARSER_TAB;
					break;
				case 4:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_DISCOVERY_TAB;
					break;
				case 5:
					id = IMakeHelpContextIds.MAKE_PROJ_WIZ_INDEXER_TAB;
					break;
				}
				MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(page.getControl(), id);
			}
		}
	}

	public MakeProjectWizardOptionPage(String title, String description) {
		super("MakeProjectSettingsPage"); //$NON-NLS-1$
		setTitle(title);
		setDescription(description);
	}

	@Override
	protected TabFolderOptionBlock createOptionBlock() {
		return (makeWizardBlock = new MakeWizardOptionBlock(this));
	}

	@Override
	public IProject getProject() {
		return ((NewCProjectWizard) getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreference()
	 */
	@Override
	public Preferences getPreferences() {
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}

	public boolean isIndexerEnabled() {
		//    isIndexEnabled() * @deprecated always returns false
		//	  return makeWizardBlock.indexBlock.isIndexEnabled();
		return false;
	}

	public void setupHelpContextIds() {
		makeWizardBlock.setupHelpContextIds();
	}
}
