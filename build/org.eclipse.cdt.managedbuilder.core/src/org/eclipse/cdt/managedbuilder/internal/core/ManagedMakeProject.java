/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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

package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;

/**
 * @deprecated This class is obsolete but it is there just in case it might be used with old style projects.
 *
 * @since 2.0
 */
@Deprecated
public class ManagedMakeProject implements ICOwner {

	/**
	 * Zero-argument constructor to fulfill the contract for
	 * implementation classes supplied via an extension point
	 */
	public ManagedMakeProject() {
		super();
	}

	@Override
	public void configure(ICDescriptor cproject) throws CoreException {
		cproject.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		cproject.remove(CCorePlugin.BUILDER_MODEL_ID);
		cproject.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);

		//		updateIndexers(cproject);
	}

	@Override
	public void update(ICDescriptor cproject, String extensionID) throws CoreException {
		/*		if (extensionID.equals(CCorePlugin.BINARY_PARSER_UNIQ_ID)) {
					updateBinaryParsers(cproject);
				}

				if (extensionID.equals(CCorePlugin.INDEXER_UNIQ_ID)) {
					updateIndexers(cproject);
				}
		*/
	}
}
