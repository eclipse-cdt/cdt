/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Convenience implementation of IChecker interface. Has a default
 * implementation for common methods.
 */
public abstract class AbstractChecker implements IChecker {
	/**
	 * @since 2.0
	 */
	private ICheckerInvocationContext context;
	private IProblemReporter problemReporter;

	/**
	 * Default constructor
	 */
	public AbstractChecker() {
	}

	/**
	 * @return true if checker is enabled in context of resource, if returns
	 *         false checker's "processResource" method won't be called
	 */
	@Override
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
	public void reportProblem(String id, IFile file, int lineNumber, Object... args) {
		getProblemReporter().reportProblem(id, createProblemLocation(file, lineNumber), args);
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
		IProblem problem = CheckersRegistry.getInstance().getResourceProfile(file).findProblem(id);
		if (problem == null)
			throw new IllegalArgumentException("Id is not registered"); //$NON-NLS-1$
		return problem;
	}

	/**
	 * @param id - main problem id
	 * @param file - checked resource
	 * @return - list of problems matching with this id, including duplicates
	 * @since 2.0
	 */
	public List<IProblem> getProblemsByMainId(String id, IResource file) {
		ArrayList<IProblem> list = new ArrayList<IProblem>();
		IProblemProfile resourceProfile = CheckersRegistry.getInstance().getResourceProfile(file);
		IProblem[] problems = resourceProfile.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			if (p.getId().equals(id)) {
				list.add(p);
			} else if (p.getId().startsWith(id + CheckersRegistry.CLONE_SUFFIX)) {
				list.add(p);
			}
		}
		return list;
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
		getProblemReporter().reportProblem(id, createProblemLocation(file, lineNumber), new Object[] {});
	}

	/**
	 * @return problem reporter for given checker
	 * @since 2.0
	 */
	@Override
	public IProblemReporter getProblemReporter() {
		return problemReporter;
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
		return getRuntime().getProblemLocationFactory().createProblemLocation(file, line);
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
	protected IProblemLocation createProblemLocation(IFile file, int startChar, int endChar) {
		return getRuntime().getProblemLocationFactory().createProblemLocation(file, startChar, endChar);
	}

	/**
	 * Defines if checker should be run as user type in editor. Override this
	 * method is checker is too heavy for that (runs too long)
	 */
	@Override
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
	public void reportProblem(String problemId, IProblemLocation loc, Object... args) {
		getProblemReporter().reportProblem(problemId, loc, args);
	}

	/**
	 * Returns the invocation context.
	 *
	 * @return checker invocation context
	 *
	 * @since 2.0
	 */
	protected ICheckerInvocationContext getContext() {
		return context;
	}

	/**
	 * @since 2.0
	 */
	protected void setContext(ICheckerInvocationContext context) {
		this.context = context;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void before(IResource resource) {
		IProblemReporter problemReporter = CodanRuntime.getInstance().getProblemReporter();
		this.problemReporter = problemReporter;
		if (problemReporter instanceof IProblemReporterSessionPersistent) {
			// create session problem reporter
			this.problemReporter = ((IProblemReporterSessionPersistent) problemReporter).createReporter(resource, this);
			((IProblemReporterSessionPersistent) this.problemReporter).start();
		} else if (problemReporter instanceof IProblemReporterPersistent) {
			// delete markers if checker can possibly run on this
			// resource  this way if checker is not enabled markers would be
			// deleted too
			((IProblemReporterPersistent) problemReporter).deleteProblems(resource, this);
		}
	}

	/**
	 * @since 2.0
	 */
	@Override
	public void after(IResource resource) {
		if (problemReporter instanceof IProblemReporterSessionPersistent) {
			// Delete general markers
			((IProblemReporterSessionPersistent) problemReporter).done();
		}
		problemReporter = null;
	}

	/**
	 * @param resource the resource to process.
	 * @return true if framework should traverse children of the resource and
	 *      run this checkers on them again.
	 * @throws OperationCanceledException if the checker was interrupted.
	 * @since 2.0
	 */
	public abstract boolean processResource(IResource resource) throws OperationCanceledException;

	/**
	 * @see IChecker#processResource(IResource, ICheckerInvocationContext)
	 * @since 2.0
	 */
	@Override
	public synchronized boolean processResource(IResource resource, ICheckerInvocationContext context)
			throws OperationCanceledException {
		this.setContext(context);
		try {
			return processResource(resource);
		} finally {
			this.setContext(null);
		}
	}
}
