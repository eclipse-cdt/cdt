/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.problem;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IReferenceContext;
import org.eclipse.cdt.core.parser.ITranslationOptions;
import org.eclipse.cdt.core.parser.ITranslationResult;
import org.eclipse.cdt.internal.core.parser.IErrorHandlingPolicy;
import org.eclipse.cdt.internal.core.parser.IProblemFactory;


/*
 * Translation problem handler, responsible to determine whether
 * a problem is actually a warning or an error (or a task); also will
 * decide whether the translation task can be processed further or not.
 *
 * Behavior : will request its current policy if need to stop on
 * first error, and if should proceed (persist) with problems.
 */

public class ProblemHandler {

	public final static String[] NoArgument = new String[0];
	
	final public IErrorHandlingPolicy policy;
	public final IProblemFactory problemFactory;
	private final ITranslationOptions options;
	
	
	/**
	 * Problem handler can be supplied with a policy to specify
	 * its behavior in error handling. Also see static methods for
	 * built-in policies.
	 *
	 */
	public ProblemHandler(IErrorHandlingPolicy policy, ITranslationOptions options, IProblemFactory problemFactory) {
		this.policy = policy;
		this.problemFactory = problemFactory;
		this.options = options;
	}
	
	
	/**
	 * Given the current configuration, answers which category the problem
	 * falls into:
	 *		Error | Warning | Ignore | Task
	 */
	public int computeSeverity(int problemId) {
		if (problemId == IProblem.Task) return IProblemSeverities.Task;
		
		// by default all problems are errors
		return IProblemSeverities.Error;
	}
	
	
	public IProblem createProblem(
		char[] fileName, 
		int problemId, 
		String[] problemArguments, 
		String[] messageArguments,
		int severity, 
		int problemStartPosition, 
		int problemEndPosition, 
		int lineNumber,
		IReferenceContext referenceContext,
		ITranslationResult unitResult) {
	
		return problemFactory.createProblem(
			fileName, 
			problemId, 
			problemArguments, 
			messageArguments,
			severity, 
			problemStartPosition, 
			problemEndPosition, 
			lineNumber); 
	}
	
	
	public void handle(
		int problemId, 
		String[] problemArguments, 
		String[] messageArguments,
		int severity, 
		int problemStartPosition, 
		int problemEndPosition,
		int line, 
		IReferenceContext referenceContext, 
		ITranslationResult unitResult) {
	
		if (severity == IProblemSeverities.Ignore)
			return;
	
		IProblem problem = 
			this.createProblem(
				unitResult.getFileName(), 
				problemId, 
				problemArguments, 
				messageArguments,
				severity, 
				problemStartPosition, 
				problemEndPosition, 
				line >= 0
					? line
					: 1,
				referenceContext,
				unitResult); 
		if (problem == null) return; // problem couldn't be created, ignore
		
		this.record(problem, unitResult, referenceContext);
	}
    
    
	/**
	 * Standard problem handling API, the actual severity (warning/error/ignore) is deducted
	 * from the problem ID and the current translation options.
	 */
	public void handle(
		int problemId, 
		String[] problemArguments, 
		String[] messageArguments,
		int problemStartPosition, 
		int problemEndPosition, 
		int line,
		IReferenceContext referenceContext, 
		ITranslationResult unitResult) {
	
		this.handle(
			problemId,
			problemArguments,
			messageArguments,
			this.computeSeverity(problemId), // severity inferred using the ID
			problemStartPosition,
			problemEndPosition,
			line,
			referenceContext,
			unitResult);
	}
	
	
	public void record(IProblem problem, ITranslationResult unitResult, IReferenceContext referenceContext) {
		unitResult.record(problem, referenceContext);
	}

    public ITranslationOptions getOptions() {
        return options;
    }	
}
