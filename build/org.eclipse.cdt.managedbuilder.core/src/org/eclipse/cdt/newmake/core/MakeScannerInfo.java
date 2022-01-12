/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.newmake.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @deprecated as of CDT 4.0
 * @author DInglis
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class MakeScannerInfo implements IScannerInfo {

	private IProject project;
	private ArrayList<String> symbolList;
	private ArrayList<String> pathList;
	boolean hasChanged = false;

	public MakeScannerInfo(IProject project) {
		this.project = project;
	}

	IProject getProject() {
		return project;
	}

	public void update() throws CoreException {
		if (hasChanged) {
			MakeScannerProvider.updateScannerInfo(this);
			hasChanged = false;
		}
	}

	public synchronized void setPreprocessorSymbols(String[] symbols) {
		if (!Arrays.equals(symbols, getSymbolList().toArray())) {
			hasChanged = true;
			// Clear out any existing symbols and add the new stuff
			getSymbolList().clear();
			getSymbolList().addAll(Arrays.asList(symbols));
		}
	}

	public synchronized void setIncludePaths(String[] paths) {
		if (!Arrays.equals(paths, getPathList().toArray())) {
			hasChanged = true;
			// Clear the existing list and add the paths
			getPathList().clear();
			getPathList().addAll(Arrays.asList(paths));
		}
	}

	@Override
	public synchronized String[] getIncludePaths() {
		return getPathList().toArray(new String[getPathList().size()]);
	}

	@Override
	public synchronized Map<String, String> getDefinedSymbols() {
		// Return the defined symbols for the default configuration
		HashMap<String, String> symbols = new HashMap<>();
		String[] symbolList = getPreprocessorSymbols();
		for (int i = 0; i < symbolList.length; ++i) {
			String symbol = symbolList[i];
			if (symbol.length() == 0) {
				continue;
			}
			String key = ""; //$NON-NLS-1$
			String value = ""; //$NON-NLS-1$
			int index = symbol.indexOf("="); //$NON-NLS-1$
			if (index != -1) {
				key = symbol.substring(0, index).trim();
				value = symbol.substring(index + 1).trim();
			} else {
				key = symbol.trim();
			}
			symbols.put(key, value);
		}
		return symbols;
	}

	protected List<String> getPathList() {
		if (pathList == null) {
			pathList = new ArrayList<>();
		}
		return pathList;
	}

	public synchronized String[] getPreprocessorSymbols() {
		return getSymbolList().toArray(new String[getSymbolList().size()]);
	}

	protected List<String> getSymbolList() {
		if (symbolList == null) {
			symbolList = new ArrayList<>();
		}
		return symbolList;
	}
}
