/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;

/**
 * Returns a IScannerInfo for the given file by a path.
 * 
 * Similar to IScannerInfoProvider but computes the IScannerInfo
 * based on a String path instead of IResource.
 * 
 * @see IScannerInfoProvider
 */
public interface IStandaloneScannerInfoProvider {

	IScannerInfo getScannerInformation(String path);
}
