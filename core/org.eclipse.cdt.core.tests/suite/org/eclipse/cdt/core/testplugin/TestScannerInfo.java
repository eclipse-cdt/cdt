/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;

public class TestScannerInfo implements IScannerInfo {
	private Map emptyMap = new HashMap(0);

	public Map getDefinedSymbols() {
		return emptyMap;
	}

	public String[] getIncludePaths() {
		return new String[0];
	}
	
}
