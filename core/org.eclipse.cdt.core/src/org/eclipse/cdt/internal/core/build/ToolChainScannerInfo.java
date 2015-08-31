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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

public class ToolChainScannerInfo {
	private Map<String, String> definedSymbols;
	private List<String> includePaths;
	private List<String> macroFiles;
	private List<String> includeFiles;
	private List<String> localIncludePath;
	private Set<String> resourcePaths;

	private transient IScannerInfo scannerInfo;

	public ToolChainScannerInfo(Map<String, String> definedSymbols, List<String> includePaths,
			List<String> macroFiles, List<String> includeFiles, List<String> localIncludePath) {
		this.definedSymbols = definedSymbols;
		this.includePaths = includePaths;
		this.macroFiles = macroFiles;
		this.includeFiles = includeFiles;
		this.localIncludePath = localIncludePath;
	}

	public IScannerInfo getScannerInfo() {
		if (scannerInfo == null) {
			scannerInfo = new ExtendedScannerInfo(definedSymbols,
					includePaths != null ? includePaths.toArray(new String[includePaths.size()]) : null,
					macroFiles != null ? macroFiles.toArray(new String[includePaths.size()]) : null,
					includeFiles != null ? includeFiles.toArray(new String[includePaths.size()]) : null,
					localIncludePath != null ? localIncludePath.toArray(new String[includePaths.size()])
							: null);
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
