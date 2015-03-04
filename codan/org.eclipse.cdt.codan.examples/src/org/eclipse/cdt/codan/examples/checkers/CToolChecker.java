/*******************************************************************************
 * Copyright (c) Mar 4, 2015 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.cxx.internal.externaltool.ExternalToolInvoker;
import org.eclipse.cdt.codan.core.cxx.model.AbstractCElementChecker;
import org.eclipse.cdt.codan.core.cxx.model.CodanMarkerGenerator;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.examples.Activator;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author elaskavaia
 *
 */
public class CToolChecker extends AbstractCElementChecker {
	public final static String ID = "org.eclipse.cdt.codan.examples.checkers.CToolChecker.error";
	public static final String MAKE_PLUGIN_ID = "org.eclipse.cdt.make.core"; //$NON-NLS-1$
	final ExternalToolInvoker externalToolInvoker = new ExternalToolInvoker();

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		getTopLevelPreference(problem); // initialize
		getLaunchModePreference(problem).enableInLaunchModes(
				CheckerLaunchMode.RUN_ON_FILE_SAVE, 
				CheckerLaunchMode.RUN_ON_DEMAND);
	}

	@Override
	public void processUnit(ITranslationUnit unit) {
		IScannerInfo scannerInfo = unit.getScannerInfo(true);
		List<String> res = getCompilerOptionsList(scannerInfo);
		res.add("-c");
		res.add("-o/dev/null");
		res.add("-O2");
		res.add("-Wall");
		res.add("-Werror");
		res.add(unit.getFile().getLocation().toPortableString());
		String args[] = res.toArray(new String[res.size()]);
		try {
			externalToolInvoker.launchOnBuildConsole(unit.getResource().getProject(), new IConsoleParser[] { getConsoleParser(unit) },
					"check", getToolPath(), args, new String[] {}, getWorkingDirectory(), new NullProgressMonitor());
		} catch (CoreException | InvocationFailure e) {
			Activator.log(e);
		}
	}

	protected List<String> getCompilerOptionsList(IScannerInfo scannerInfo) {
		final Map<String, String> symbols = scannerInfo.getDefinedSymbols();
		List<String> res = new ArrayList<>();
		for (String macro : symbols.keySet()) {
			if (macro.startsWith("_"))
				continue; // likely embedded macro
			String value = symbols.get(macro);
			if (value.isEmpty())
				res.add("-D" + macro);
			else
				res.add("-D" + macro + "=" + value);
		}
		for (String inc : scannerInfo.getIncludePaths()) {
			res.add("-I" + inc);
		}
		return res;
	}

	protected Path getToolPath() {
		return new Path("gcc");
	}

	protected IConsoleParser getConsoleParser(ITranslationUnit unit) {
		IProject project = unit.getResource().getProject();
		return new ErrorParserManager(project, getWorkingDirectory(), getMarkerGenerator(), getParserIDs());
	}

	protected IMarkerGenerator getMarkerGenerator() {
		return new CodanMarkerGenerator(ID, getProblemReporter());
	}

	protected String[] getParserIDs() {
		return null; // null means all default error parsers
	}

	protected IPath getWorkingDirectory() {
		return null; // null means project root path
	}
}
