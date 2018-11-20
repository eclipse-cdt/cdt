/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;

/**
 * @deprecated This class is obsolete but it is there just in case it might be used with old style projects.
 */
@Deprecated
public class MakeProject implements ICOwner {

	@Override
	public void configure(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		cDescriptor.remove(CCorePlugin.BUILDER_MODEL_ID);
		updateBinaryParsers(cDescriptor);
		updateIndexers(cDescriptor);
	}

	@Override
	public void update(ICDescriptor cDescriptor, String extensionID) throws CoreException {
		if (extensionID.equals(CCorePlugin.BINARY_PARSER_UNIQ_ID)) {
			updateBinaryParsers(cDescriptor);
		}

		if (extensionID.equals(CCorePlugin.INDEXER_UNIQ_ID)) {
			updateIndexers(cDescriptor);
		}
	}

	private void updateBinaryParsers(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
		Preferences makePrefs = MakeCorePlugin.getDefault().getPluginPreferences();
		String ids = makePrefs.getString(CCorePlugin.PREF_BINARY_PARSER);
		if (ids != null && ids.length() != 0) {
			for (String id : parseStringToArray(ids)) {
				cDescriptor.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, id);
			}
		}
	}

	private void updateIndexers(ICDescriptor cDescriptor) throws CoreException {
		cDescriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
		Preferences corePrefs = CCorePlugin.getDefault().getPluginPreferences();
		String ids = corePrefs.getString(CCorePlugin.PREF_INDEXER);
		if (ids != null && ids.length() != 0) {
			for (String id : parseStringToArray(ids)) {
				cDescriptor.create(CCorePlugin.INDEXER_UNIQ_ID, id);
			}
		}
	}

	private String[] parseStringToArray(String syms) {
		if (syms != null && syms.length() > 0) {
			StringTokenizer tok = new StringTokenizer(syms, ";"); //$NON-NLS-1$
			ArrayList<String> list = new ArrayList<>(tok.countTokens());
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return list.toArray(new String[list.size()]);
		}
		return new String[0];
	}
}
