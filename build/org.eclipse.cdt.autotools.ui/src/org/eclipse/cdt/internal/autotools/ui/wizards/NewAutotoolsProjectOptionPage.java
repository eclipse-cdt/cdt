/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.autotools.ui.ErrorParserBlock;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.newui.CDTHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.PlatformUI;


@SuppressWarnings("deprecation")
public class NewAutotoolsProjectOptionPage extends NewCProjectWizardOptionPage {
	
	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.projectOptionsPage"; //$NON-NLS-1$
	
	public static class ManagedWizardOptionBlock extends ManagedProjectOptionBlock {
		
		NewAutotoolsProjectOptionPage parent;
		IndexerBlock indexBlock;
		

		public ManagedWizardOptionBlock(NewAutotoolsProjectOptionPage parentPage) {
			super(parentPage);
			parent = parentPage;
		}

		public class AutotoolsReferenceBlock extends ReferenceBlock {
			AutotoolsReferenceBlock() {
				super();
			}
			
			public void performApply(IProgressMonitor monitor) throws CoreException {
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

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
		 */
		protected void addTabs() {
			addTab(new AutotoolsReferenceBlock());
			// NOTE: The setting of error parsers is commented out here
			//       because they need to be set per-configuration.
			//       The other tabs on this page are per-project.
			//       Error parsers can be selected per configuration in the 
			//        project properties
			//errorParsers = new ErrorParserBlock();
			//addTab(errorParsers);
			addTab(indexBlock = new IndexerBlock());
		}
		
		public void setupHelpContextIds(){
			List<ICOptionPage> pages = getOptionPages();
			
			Iterator<ICOptionPage> iter = pages.iterator();
			for( int i = 0; i < 3 && iter.hasNext(); i++ ) {
				ICOptionPage page = (ICOptionPage) iter.next();
				
				String id = null;
				if (page instanceof ReferenceBlock) {
					id = CDTHelpContextIds.MAN_PROJ_WIZ_PROJECTS_TAB;
				} else if (page instanceof ErrorParserBlock) {
					id = CDTHelpContextIds.MAN_PROJ_WIZ_ERRORPARSERS_TAB;
				} else if (page instanceof IndexerBlock) {
					id = CDTHelpContextIds.MAN_PROJ_WIZ_INDEXER_TAB;
				}
				PlatformUI.getWorkbench().getHelpSystem().setHelp(page.getControl(), id);
			}
		}
	}
	
	protected ManagedWizardOptionBlock optionBlock;
	protected NewCProjectWizard parentWizard;

	/**
	 * @param pageName
	 */
	public NewAutotoolsProjectOptionPage(String pageName, NewCProjectWizard parentWizard) {
		super(pageName);
		this.parentWizard = parentWizard;
		optionBlock = new ManagedWizardOptionBlock(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage#createOptionBlock()
	 */
	protected TabFolderOptionBlock createOptionBlock() {
		return optionBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		if (getWizard() instanceof ConvertToAutotoolsProjectWizard)
			return ((ConvertToAutotoolsProjectWizard)getWizard()).getProject();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferenceStore()
	 */
	public Preferences getPreferences() {
		return ManagedBuilderUIPlugin.getDefault().getPluginPreferences();
	}
	
	public void updateProjectTypeProperties() {
		//  Update the error parser list
		optionBlock.updateProjectTypeProperties();
	}
	
	public void setupHelpContextIds(){
		optionBlock.setupHelpContextIds();
	}
	
	public IWizardPage getNextPage()
	{
		return MBSCustomPageManager.getNextPage(PAGE_ID); // get first custom page, if any
	}
	
}
