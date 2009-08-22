/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public abstract class AbstractChecker implements IChecker {
	protected String name;

	public AbstractChecker() {
	}

	/**
	 * @return true if checker is enabled in context of resource, if returns
	 *         false checker's "processResource" method won't be called
	 */
	public boolean enabledInContext(IResource res) {
		return true;
	}

	/**
	 * Reports a simple problem for given file and line
	 * 
	 * @param id
	 *            - problem id
	 * @param file
	 *            - file
	 * @param lineNumber
	 *            - line
	 * @param arg
	 *            - problem argument, if problem does not define error message
	 *            it will be error message (not recommended because of
	 *            internationalization)
	 */
	public void reportProblem(String id, IFile file, int lineNumber, String arg) {
		getProblemReporter().reportProblem(id,
				new ProblemLocation(file, lineNumber), arg);
	}

	/**
	 * Reports a simple problem for given file and line, error message comes
	 * from problem definition
	 * 
	 * @param id
	 *            - problem id
	 * @param file
	 *            - file
	 * @param lineNumber
	 *            - line
	 */
	public void reportProblem(String id, IFile file, int lineNumber) {
		getProblemReporter().reportProblem(id,
				new ProblemLocation(file, lineNumber), new Object[] {});
	}

	/**
	 * @return problem reporter for given checker
	 */
	protected IProblemReporter getProblemReporter() {
		return CodanRuntime.getInstance().getProblemReporter();
	}

	public boolean runInEditor() {
		return false;
	}
}
