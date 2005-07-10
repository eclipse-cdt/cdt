/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;

/**
 * @since 2.0
 */
public class ManagedMakeProject implements ICOwner {

	/**
	 * Zero-argument constructor to fulfill the contract for 
	 * implementation classes supplied via an extension point 
	 */
	public ManagedMakeProject() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICOwner#configure(org.eclipse.cdt.core.ICDescriptor)
	 */
	public void configure(ICDescriptor cproject) throws CoreException {
		cproject.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		cproject.remove(CCorePlugin.BUILDER_MODEL_ID);
		cproject.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
		
		updateIndexers(cproject);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICOwner#update(org.eclipse.cdt.core.ICDescriptor, java.lang.String)
	 */
	public void update(ICDescriptor cproject, String extensionID) throws CoreException {
		if (extensionID.equals(CCorePlugin.BINARY_PARSER_UNIQ_ID)) {
			updateBinaryParsers(cproject);
		}
				
		if (extensionID.equals(CCorePlugin.INDEXER_UNIQ_ID)) {
			updateIndexers(cproject);
		}
	}
	
	private void updateBinaryParsers(ICDescriptor cDescriptor) throws CoreException {
		IManagedBuildInfo buildInfo = null;
		String[] ids = null;
		IProject project = cDescriptor.getProject();

		// If we cannot get the build information, it may be due to the fact that the 
		// build information is yet to be created, due to a synchronization issue
		// Don't do anything now to the binary parsers because there is nothing meaningful to do.
		// This routine should be invoked later, when the required build information is available		
		if (!ManagedBuildManager.canGetBuildInfo(project)) return;

		buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo != null) {
			IConfiguration config = buildInfo.getDefaultConfiguration();
			if (config == null && buildInfo.getManagedProject() != null) {
				IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
				if (configs != null && configs.length > 0)
					config = configs[0];
			}
			if (config != null) {
				//  Get the values from the current configuration
				IToolChain toolChain = config.getToolChain();
				if (toolChain != null) {
					ITargetPlatform targPlatform = toolChain.getTargetPlatform();
					if (targPlatform != null) {
						ids = targPlatform.getBinaryParserList();
					}
				}
			}
		}
		
		cDescriptor.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
		if (ids != null) {
			for (int i = 0; i < ids.length; i++) {
				cDescriptor.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, ids[i]);
			}
		}
	}
	
	private void updateIndexers(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
		Preferences corePrefs = CCorePlugin.getDefault().getPluginPreferences();
		String id = corePrefs.getString(CCorePlugin.PREF_INDEXER);
		if (id != null && id.length() != 0) {
			String[] ids = parseStringToArray(id);
			for (int i = 0; i < ids.length; i++) {
				cDescriptor.create(CCorePlugin.INDEXER_UNIQ_ID, ids[i]);
			}
		}
	}
	
	private String[] parseStringToArray(String syms) {
		if (syms != null && syms.length() > 0) {
			StringTokenizer tok = new StringTokenizer(syms, ";"); //$NON-NLS-1$
			ArrayList list = new ArrayList(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		return new String[0];
	}
}
