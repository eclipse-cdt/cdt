/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.scannerconfig;

import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;

/**
 * Implementation class for gathering the built-in compiler settings for 
 * Clang/LLVM targets. The assumption is that the tools will answer path 
 * information in POSIX format and that the Scanner will be able to search for 
 * files using this format.
 * 
 */
public class LlvmScannerInfoCollector
	extends PerProjectSICollector
	implements IManagedScannerInfoCollector {
}
