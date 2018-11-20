/*******************************************************************************
 * Copyright (c) 2009,2015 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.examples.checkers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * This is sample of non AST checker integration. This one is not
 * external checker because it does not actually call other process, but it
 * can easily made into one.
 *
 * This checker is parametrized by the search strings
 */
public class GrepChecker extends AbstractCheckerWithProblemPreferences {
	public final static String ID = "org.eclipse.cdt.codan.examples.checkers.GrepCheckerProblemError";
	private static final String PARAM_STRING_LIST = "searchlist";

	@Override
	public synchronized boolean processResource(IResource resource) {
		if (!shouldProduceProblems(resource))
			return false;
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			processFile(file);
			return false;
		}
		return true;
	}

	void processFile(IFile file) {
		Collection<IProblem> refProblems = getRuntime().getCheckersRegistry().getRefProblems(this);
		for (Iterator<IProblem> iterator = refProblems.iterator(); iterator.hasNext();) {
			IProblem checkerProblem = iterator.next();
			IProblem problem = getProblemById(checkerProblem.getId(), file);
			if (shouldProduceProblem(problem, file.getFullPath())) {
				// do something
				Object[] values = (Object[]) getPreference(problem, PARAM_STRING_LIST);
				if (values.length == 0)
					continue; // nothing to do
				externalRun(file, values, problem);
			}
		}
	}

	/**
	 * @param file
	 * @param checkerProblem
	 * @param values
	 * @param problem
	 * @return
	 */
	private void externalRun(IFile file, Object[] values, IProblem problem) {
		try {
			InputStream is = file.getContents();
			BufferedReader bis = new BufferedReader(new InputStreamReader(is));
			String line;
			int iline = 0;
			while ((line = bis.readLine()) != null) {
				iline++;
				for (int i = 0; i < values.length; i++) {
					String str = (String) values[i];
					if (line.contains(str)) {
						reportProblem(problem.getId(), file, iline, str);
					}
				}
			}
			bis.close();
		} catch (IOException e) {
			// ignore
		} catch (CoreException e) {
			// ignore
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_STRING_LIST, "Search strings", "Search string");
	}
}
