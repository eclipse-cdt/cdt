/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
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
import org.eclipse.cdt.codan.internal.core.CheckerInvocationContext;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Convenience implementation of IChecker interface. Has a default
 * implementation for common methods.
 * 
 */
public abstract class AbstractChecker implements IChecker {
	protected String name;
	/**
	 * @since 2.0
	 */
	protected ICheckerInvocationContext context;

	/**
	 * Default constructor
	 */
	public AbstractChecker() {
	}

	/**
	 * @return true if checker is enabled in context of resource, if returns
	 *         false checker's "processResource" method won't be called
	 */
	public boolean enabledInContext(IResource res) {
		return res instanceof IFile;
	}

	/**
	 * Reports a simple problem for given file and line
	 * 
	 * @param id
	 *        - problem id
	 * @param file
	 *        - file
	 * @param lineNumber
	 *        - line
	 * @param args
	 *        - problem arguments, if problem does not define error message
	 *        it will be error message (not recommended because of
	 *        internationalization)
	 */
	public void reportProblem(String id, IFile file, int lineNumber,
			Object... args) {
		getProblemReporter().reportProblem(id,
				createProblemLocation(file, lineNumber), args);
	}

	/**
	 * Finds an instance of problem by given id, in user profile registered for
	 * specific file
	 * 
	 * @param id
	 *        - problem id
	 * @param file
	 *        - file in scope
	 * @return problem instance
	 */
	public IProblem getProblemById(String id, IResource file) {
		IProblem problem = CheckersRegistry.getInstance()
				.getResourceProfile(file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered"); //$NON-NLS-1$
		return problem;
	}

	/**
	 * Reports a simple problem for given file and line, error message comes
	 * from problem definition
	 * 
	 * @param id
	 *        - problem id
	 * @param file
	 *        - file
	 * @param lineNumber
	 *        - line
	 */
	public void reportProblem(String id, IFile file, int lineNumber) {
		getProblemReporter().reportProblem(id,
				createProblemLocation(file, lineNumber), new Object[] {});
	}

	/**
	 * @return problem reporter for given checker
	 * @since 2.0
	 */
	public IProblemReporter getProblemReporter() {
		try {
			return getContext().getProblemReporter();
		} catch (Exception e) {
			return CodanRuntime.getInstance().getProblemReporter();
		}
	}

	/**
	 * Convenience method to return codan runtime
	 * 
	 * @return
	 */
	protected CodanRuntime getRuntime() {
		return CodanRuntime.getInstance();
	}

	/**
	 * Convenience method to create and return instance of IProblemLocation
	 * 
	 * @param file
	 *        - file where problem is found
	 * @param line
	 *        - line number 1-relative
	 * @return instance of IProblemLocation
	 */
	protected IProblemLocation createProblemLocation(IFile file, int line) {
		return getRuntime().getProblemLocationFactory().createProblemLocation(
				file, line);
	}

	/**
	 * Convenience method to create and return instance of IProblemLocation
	 * 
	 * @param file
	 *        - file where problem is found
	 * @param startChar
	 *        - start char of the problem in the file, is zero-relative
	 * @param endChar
	 *        - end char of the problem in the file, is zero-relative and
	 *        exclusive.
	 * @return instance of IProblemLocation
	 */
	protected IProblemLocation createProblemLocation(IFile file, int startChar,
			int endChar) {
		return getRuntime().getProblemLocationFactory().createProblemLocation(
				file, startChar, endChar);
	}

	/**
	 * Defines if checker should be run as user type in C editor. Override this
	 * method is checker is too heavy for that (runs too long)
	 */
	public boolean runInEditor() {
		return this instanceof IRunnableInEditorChecker;
	}

	/**
	 * report a problem
	 * 
	 * @param problemId - id of a problem
	 * @param loc - problem location
	 * @param args - extra problem arguments
	 */
	public void reportProblem(String problemId, IProblemLocation loc,
			Object... args) {
		getProblemReporter().reportProblem(problemId, loc, args);
	}

	/**
	 * Get invocation context.
	 * 
	 * @return checker invocation context
	 * 
	 * @since 2.0
	 */
	public ICheckerInvocationContext getContext() {
		return context;
	}

	/**
	 * Set the invocation context. Usually called by codan builder.
	 * Object that calls this should also synchronize of checker object
	 * to prevent multi-thread access to a running context
	 * 
	 * @since 2.0
	 */
	public void setContext(ICheckerInvocationContext context) {
		this.context = context;
	}

	/**
	 * @since 2.0
	 */
	public boolean before(IResource resource) {
		IChecker checker = this;
		IProblemReporter problemReporter = CodanRuntime.getInstance()
				.getProblemReporter();
		IProblemReporter sessionReporter = problemReporter;
		if (problemReporter instanceof IProblemReporterSessionPersistent) {
			// create session problem reporter
			sessionReporter = ((IProblemReporterSessionPersistent) problemReporter)
					.createReporter(resource, checker);
			((IProblemReporterSessionPersistent) sessionReporter).start();
		} else if (problemReporter instanceof IProblemReporterPersistent) {
			// delete markers if checker can possibly run on this
			// resource  this way if checker is not enabled markers would be
			// deleted too
			((IProblemReporterPersistent) problemReporter).deleteProblems(
					resource, checker);
		}
		((AbstractChecker) checker).setContext(new CheckerInvocationContext(
				resource, sessionReporter));
		return true;
	}

	/**
	 * @since 2.0
	 */
	public boolean after(IResource resource) {
		if (getContext().getProblemReporter() instanceof IProblemReporterSessionPersistent) {
			// delete general markers
			((IProblemReporterSessionPersistent) getContext()
					.getProblemReporter()).done();
		}
		return true;
	}
}
