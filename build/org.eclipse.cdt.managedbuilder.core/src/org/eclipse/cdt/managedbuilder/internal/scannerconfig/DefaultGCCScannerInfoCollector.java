/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * GCC-based targets. The assumption is that the tools will answer path 
 * information in POSIX format and that the Scanner will be able to search for 
 * files using this format.
 * 
 * @since 2.0
 */
public class DefaultGCCScannerInfoCollector extends PerProjectSICollector implements IScannerInfoCollector3, IManagedScannerInfoCollector {
}
