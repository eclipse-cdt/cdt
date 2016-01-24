/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.msw.build;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class WinDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IPath[] paths;
	private final Map<String, String> symbols = new HashMap<String, String>();
	
	public WinDiscoveredPathInfo() {
		// Include paths
		paths = WinEnvironmentVariableSupplier.getIncludePath();
				
		symbols.put("_M_IX86", "600");
		symbols.put("_WIN32", "1");
		symbols.put("_MSC_VER", "1400");
		
		// Microsoft specific modifiers that can be ignored
		symbols.put("__cdecl", "");
		symbols.put("__fastcall", "");
		symbols.put("__restrict", "");
		symbols.put("__sptr", "");
		symbols.put("__stdcall", "");
		symbols.put("__unaligned", "");
		symbols.put("__uptr", "");
		symbols.put("__w64", "");
		
		// Redefine some things so that the CDT parser can handle them, until there is a VC specific parser
		symbols.put("__forceinline", "__inline");
		symbols.put("__int8", "char");
		symbols.put("__int16", "short");
		symbols.put("__int32", "int");
		symbols.put("__int64", "long long");
	}
	
	public IPath[] getIncludePaths() {
		return paths;
	}

	public IProject getProject() {
		return null;
	}

	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	public Map<String, String> getSymbols() {
		return symbols;
	}

}
