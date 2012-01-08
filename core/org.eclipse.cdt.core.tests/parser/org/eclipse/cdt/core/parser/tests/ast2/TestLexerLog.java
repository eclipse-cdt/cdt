/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;

import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;

public class TestLexerLog implements ILexerLog {

	private ArrayList fComments= new ArrayList();
	private ArrayList fProblems= new ArrayList();
	private String fInput;
	
	public void setInput(String input) {
		fInput= input;
	}

	@Override
	public void handleComment(boolean isBlockComment, int offset, int endOffset) {
		fComments.add(fInput.substring(offset, endOffset));
	}

	@Override
	public void handleProblem(int problemID, char[] arg, int offset, int endOffset) {
		fProblems.add(createString(problemID, new String(arg)));
	}

	public String createString(int problemID, String image) {
		return String.valueOf(problemID) + ":" + image;
	}
	
	public void clear() {
		fComments.clear();
		fProblems.clear();
	}

	public int getProblemCount() {
		return fProblems.size();
	}

	public int getCommentCount() {
		return fComments.size();
	}

	public String removeFirstProblem() {
		if (fProblems.isEmpty()) {
			return "no problems have been reported";
		}
		return (String) fProblems.remove(0);
	}

	public String removeFirstComment() {
		if (fComments.isEmpty()) {
			return "no comments have been reported";
		}
		return (String) fComments.remove(0);
	}

}
