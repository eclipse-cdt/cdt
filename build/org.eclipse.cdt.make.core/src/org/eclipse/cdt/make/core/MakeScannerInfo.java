/*
 * Created on Aug 14, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.make.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


/**
 * @author David
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MakeScannerInfo implements IScannerInfo {
	private IProject project;
	private ArrayList symbolList;
	private ArrayList pathList;

	MakeScannerInfo(IProject project) {
		this.project = project;
	}

	IProject getProject() {
		return project;
	}

	public void update() throws CoreException {
		MakeScannerProvider.updateScannerInfo(this);
	}

	public synchronized void setPreprocessorSymbols(String[] symbols) {
		// Clear out any existing symbols and add the new stuff
		getSymbolList().clear();
		getSymbolList().addAll(Arrays.asList(symbols));
	}

	public synchronized void setIncludePaths(String[] paths) {
		// Clear the existing list and add the paths
		getPathList().clear();
		getPathList().addAll(Arrays.asList(paths));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized String[] getIncludePaths() {
		return (String[])getPathList().toArray(new String[getPathList().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized Map getDefinedSymbols() {
		// Return the defined symbols for the default configuration
		HashMap symbols = new HashMap();
		String[] symbolList = getPreprocessorSymbols();
		for (int i = 0; i < symbolList.length; ++i) {
			String symbol = symbolList[i];
			if (symbol.length() == 0) {
				continue;
			}
			String key = new String();
			String value = new String();
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

	protected List getPathList() {
		if (pathList == null) {
			pathList = new ArrayList();
		}
		return pathList;
	}

	public synchronized String[] getPreprocessorSymbols() {
		return (String[])getSymbolList().toArray(new String[getSymbolList().size()]);
	}

	protected List getSymbolList() {
		if (symbolList == null) {
			symbolList = new ArrayList();
		}
		return symbolList;
	}
}