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

import  org.eclipse.cdt.core.parser.IProblem;
import  org.eclipse.cdt.core.parser.IProblemReporter;
import  org.eclipse.cdt.core.parser.IReferenceContext;
import  org.eclipse.cdt.core.parser.ITranslationOptions;
import  org.eclipse.cdt.core.parser.ITranslationResult;
import  org.eclipse.cdt.internal.core.parser.IErrorHandlingPolicy;
import  org.eclipse.cdt.internal.core.parser.IProblemFactory;


public class ProblemReporter extends ProblemHandler implements IProblemReporter {
	
	public IReferenceContext referenceContext = null;
	
	public ProblemReporter(IErrorHandlingPolicy policy, ITranslationOptions options, IProblemFactory problemFactory) {
		super(policy, options, problemFactory);
	}
	
	public void task(String tag, String message, String priority, int start, int end, int line, ITranslationResult result){
		this.handle(
			IProblem.Task,
			new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/},
			new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/}, 
			start,
			end,
			line,
			result);
	}
	
	//	use this private API when the translation unit result can be found through the
	//	reference context. Otherwise, use the other API taking a problem and a translation result
	//	as arguments	
	 private void handle(
		 int problemId, 
		 String[] problemArguments,
		 String[] messageArguments,
		 int problemStartPosition, 
		 int problemEndPosition,
		 int line){
	
		 this.handle(
				 problemId,
				 problemArguments,
				 messageArguments,
				 problemStartPosition,
				 problemEndPosition,
				 line,
				 referenceContext, 
				 referenceContext == null ? null : referenceContext.translationResult()); 
		 referenceContext = null;
	 }
	 
	//	use this private API when the translation unit result can be found through the
	//	reference context. Otherwise, use the other API taking a problem and a translation result
	//	as arguments
	 private void handle(
		 int problemId, 
		 String[] problemArguments,
		 String[] messageArguments,
		 int severity,
		 int problemStartPosition, 
		 int problemEndPosition,
		 int line){
	
		 this.handle(
				 problemId,
				 problemArguments,
				 messageArguments,
				 severity,
				 problemStartPosition,
				 problemEndPosition,
				 referenceContext, 
				 referenceContext == null ? null : referenceContext.translationResult()); 
		 referenceContext = null;
	 }
	 
	//	use this private API when the translation unit result cannot be found through the
	//	reference context. 
	 private void handle(
		 int problemId, 
		 String[] problemArguments,
		 String[] messageArguments,
		 int problemStartPosition, 
		 int problemEndPosition,
		 int line,
		 ITranslationResult unitResult){
	
		 this.handle(
				 problemId,
				 problemArguments,
				 messageArguments,
				 problemStartPosition,
				 problemEndPosition,
				 line,
				 referenceContext, 
				 unitResult); 
		 referenceContext = null;
	 }
}
