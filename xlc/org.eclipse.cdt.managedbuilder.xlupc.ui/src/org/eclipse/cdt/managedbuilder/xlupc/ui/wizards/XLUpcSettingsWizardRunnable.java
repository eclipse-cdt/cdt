/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlupc.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.xlc.ui.wizards.XLCSettingsWizardRunnable;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.wizard.IWizardPage;


public class XLUpcSettingsWizardRunnable extends XLCSettingsWizardRunnable {


	// now add UPC language mapping to the project
	private static final String[] CONTENT_TYPE_IDS = { CCorePlugin.CONTENT_TYPE_CHEADER, CCorePlugin.CONTENT_TYPE_CSOURCE };

	public XLUpcSettingsWizardRunnable() {
		pageId = XLUpcSettingsWizardPage.PAGE_ID;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		// get a handle to the wizard
		IWizardPage[] pages = MBSCustomPageManager.getPages();

		if (pages != null && pages.length > 0) {

			CDTCommonProjectWizard wizard = (CDTCommonProjectWizard) pages[0].getWizard();
			IProject project = wizard.getLastProject();
			LanguageManager langManager = LanguageManager.getInstance();

			try {
				ProjectLanguageConfiguration langConfig = langManager.getLanguageConfiguration(project);

				ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, false);
				ICConfigurationDescription configDescription = projectDescription.getActiveConfiguration();

				IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

				for(String id : CONTENT_TYPE_IDS) {
					if(contentTypeManager.getContentType(id) != null) {
						langConfig.addContentTypeMapping(configDescription, id, UPCLanguage.ID);
					}
				}

				langManager.storeLanguageMappingConfiguration(project, new IContentType[0]);

			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
}
