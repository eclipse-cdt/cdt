/*
 * Created on Jan 16, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
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
