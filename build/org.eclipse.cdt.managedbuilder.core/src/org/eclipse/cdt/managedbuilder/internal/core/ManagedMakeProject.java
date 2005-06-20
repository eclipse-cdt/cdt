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

/**
 * @since 2.0
 */
public class ManagedMakeProject implements ICOwner {

	/**
	 * Zero-argument constructor to fulfill the contract for 
	 * implementation calsses supplied via an extension point 
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
	public void update(ICDescriptor cproject, String extensionID)
			throws CoreException {
		
		if (extensionID.equals(CCorePlugin.INDEXER_UNIQ_ID)) {
			updateIndexers(cproject);
		}

	}
	
	private void updateBinaryParsers(ICDescriptor cproject) throws CoreException {
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
