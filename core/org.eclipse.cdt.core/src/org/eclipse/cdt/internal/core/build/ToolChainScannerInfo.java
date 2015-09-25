/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

public class ToolChainScannerInfo {
	private Map<String, String> definedSymbols;
	private String[] includePaths;
	private String[] macroFiles;
	private String[] includeFiles;
	private String[] localIncludePath;
	private Set<String> resourcePaths;

	private transient IScannerInfo scannerInfo;

	public ToolChainScannerInfo(ExtendedScannerInfo scannerInfo) {
		this.scannerInfo = scannerInfo;
		this.definedSymbols = scannerInfo.getDefinedSymbols();
		this.includePaths = scannerInfo.getIncludePaths();
		this.macroFiles = scannerInfo.getMacroFiles();
		this.includeFiles = scannerInfo.getIncludeFiles();
		this.localIncludePath = scannerInfo.getLocalIncludePath();
	}

	public IScannerInfo getScannerInfo() {
		if (scannerInfo == null) {
			scannerInfo = new ExtendedScannerInfo(definedSymbols, includePaths, macroFiles, includeFiles,
					localIncludePath);
		}
		return scannerInfo;
	}

	public Collection<String> getResourcePaths() {
		return resourcePaths != null ? resourcePaths : Collections.<String> emptySet();
	}

	public void addResource(IResource resource) {
		if (resourcePaths == null) {
			resourcePaths = new HashSet<>();
		}
		resourcePaths.add(resource.getFullPath().toString());
	}

}
