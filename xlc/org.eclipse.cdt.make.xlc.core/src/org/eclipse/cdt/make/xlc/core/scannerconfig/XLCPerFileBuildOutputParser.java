/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.xlc.core.activator.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author crecoskie
 *
 */
public class XLCPerFileBuildOutputParser extends AbstractXLCBuildOutputParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.xlc.core.scannerconfig.AbstractXLCBuildOutputParser#processCommand(java.lang.String[])
	 */
	@Override
	protected boolean processCommand(String[] tokens) {
		try {
			
		// GCC C/C++ compiler invocation
		int compilerInvocationIndex = findCompilerInvocation(tokens);
		if (compilerInvocationIndex < 0) {
			return false;
		}

		// find a file name
		int extensionsIndex = -1;
		boolean found = false;
		String filePath = null;
		for (int i = compilerInvocationIndex + 1; i < tokens.length; i++) {
			String token = tokens[i];
			int k = token.lastIndexOf('.');
			if (k != -1 && (token.length() - k < 5)) {
				String fileExtension = token.substring(k);
				extensionsIndex = getFileExtensionsList().indexOf(fileExtension);
				if (extensionsIndex != -1) {
					filePath = token;
					found = true;
					break;
				}
			}
		}
		if (!found) {
			TraceUtil.outputTrace("Error identifying file name :1", tokens, TraceUtil.EOL); //$NON-NLS-1$
			return false;
		}
		// sanity check
		if (filePath.indexOf(getFileExtensions()[extensionsIndex]) == -1) {
			TraceUtil.outputTrace("Error identifying file name :2", tokens, TraceUtil.EOL); //$NON-NLS-1$
			return false;
		}
		if (getUtility() != null) {
			IPath pFilePath = fUtility.getAbsolutePath(filePath);
			String shortFileName = pFilePath.removeFileExtension().lastSegment();

			// generalize occurrences of the file name
			for (int i = compilerInvocationIndex + 1; i < tokens.length; i++) {
				String token = tokens[i];
				if (token.equals("-include")) { //$NON-NLS-1$
					++i;
				} else if (token.equals("-imacros")) { //$NON-NLS-1$
					++i;
				} else if (token.equals(filePath)) {
					tokens[i] = "LONG_NAME"; //$NON-NLS-1$
				} else if (token.startsWith(shortFileName)) {
					tokens[i] = token.replaceFirst(shortFileName, "SHORT_NAME"); //$NON-NLS-1$
				}
			}

			IFile file = null;
			IPath baseDirectory = fUtility.getBaseDirectory();
			if (baseDirectory.isPrefixOf(pFilePath)) {
				IPath relPath = pFilePath.removeFirstSegments(baseDirectory.segmentCount());
				// Note: We add the scanner-config even if the resource doesn't
				// actually
				// exist below this project (which may happen when reading
				// existing
				// build logs, because resources can be created as part of the
				// build
				// and may not exist at the time of analyzing the config but
				// re-built
				// later on.
				// if (getProject().exists(relPath)) {
				file = getProject().getFile(relPath);
			} else {
				file = getUtility().findFileInWorkspace(pFilePath);
			}
			if (file != null) {
				CCommandDSC cmd = getUtility().getNewCCommandDSC(tokens, compilerInvocationIndex, extensionsIndex > 0);
				List<CCommandDSC> cmdList = new ArrayList<CCommandDSC>();
				cmdList.add(cmd);
				Map<ScannerInfoTypes, List<CCommandDSC>> sc = new HashMap<ScannerInfoTypes, List<CCommandDSC>>(1);
				sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);
				getCollector().contributeToScannerConfig(file, sc);
				if (fCollector != null && fCollector instanceof IScannerInfoCollector2) {
					IScannerInfoCollector2 collector = (IScannerInfoCollector2) fCollector;
					try {
						collector.updateScannerConfiguration(null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						Activator.log(e);
					}
				}
			} else
				TraceUtil.outputError("Build command for file outside project: " + pFilePath.toString(), tokens); //$NON-NLS-1$
		}
 		return true;
		
		}
		catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

}
