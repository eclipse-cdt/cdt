/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;

/**
 * Similar to IScannerInfoProvider but computes the IScannerInfo
 * based on a String path instead of IResource.
 *
 * @see IScannerInfoProvider
 */
public interface IStandaloneScannerInfoProvider {

	/**
	 * Returns an IScannerInfo for the given file path,
	 * or an empty IScannerInfo object if the file path is invalid.
	 */
	IScannerInfo getScannerInformation(String path);

	/**
	 * Returns an IScannerInfo when you don't necessary have access to a path.
	 *
	 * This is used by the "parse up front" feature. Since we are parsing
	 * files outside of the project a "default" IScannerInfo object
	 * is needed to get the minimal amount of available info in order
	 * to parse the file.
	 * @param linkageID
	 */
	IScannerInfo getDefaultScannerInformation(int linkageID);
}
