/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checker for lack of copyright information
 */
public class CopyrightChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.CopyrightProblem"; //$NON-NLS-1$
	public static final String PARAM_REGEX = "regex"; //$NON-NLS-1$
	private static final String DEF_REGEX = ".*Copyright.*"; //$NON-NLS-1$
	private Pattern fPattern;
	private StringBuilder builder = new StringBuilder();
	private int lastOffset;
	private boolean multiLine = false;

	/**
	 * Internal result
	 * Not found: we didn't find a proper comment
	 * Found: we found a comment at line 1 but without match, we can stop the search
	 * Found match: comment at line 1 with matching regex
	 */
	private enum Result {
		NOT_FOUND, FOUND, FOUND_MATCH;
	}

	@Override
	public boolean runInEditor() {
		return false;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		multiLine = false;
		lastOffset = 0;
		builder.setLength(0);
		String regex = getRegex();
		if (regex == null || regex.isEmpty())
			regex = DEF_REGEX;
		try {
			fPattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
		} catch (PatternSyntaxException e) {
			CodanCheckersActivator.log(e);
			return;
		}
		IASTComment[] comments = ast.getComments();
		if (comments == null) {
			setProblem();
			return;
		}
		Result found = Result.NOT_FOUND;
		for (IASTComment comment : comments) {
			found = processComment(comment);
			if (found == Result.FOUND || found == Result.FOUND_MATCH)
				break;
		}
		if (found == Result.NOT_FOUND && multiLine) {
			String c = builder.toString();
			if (!fPattern.matcher(c).matches()) {
				setProblem();
			}
		} else if (found != Result.FOUND_MATCH)
			setProblem();
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_REGEX, CheckersMessages.Copyright_regex, DEF_REGEX);
	}

	public String getRegex() {
		final IProblem pt = getProblemById(ERR_ID, getFile());
		return (String) getPreference(pt, PARAM_REGEX);
	}

	protected Result processComment(IASTComment comment) {
		if (comment.isPartOfTranslationUnitFile()) {
			IASTNodeLocation nodeLocation = comment.getFileLocation();
			if (nodeLocation == null) {
				return Result.NOT_FOUND;
			}
			String c = comment.getRawSignature();
			int currentOffset = nodeLocation.getNodeOffset();
			if (!comment.isBlockComment() && c.startsWith("//") //$NON-NLS-1$
					&& (currentOffset == lastOffset + 1 || (!multiLine && currentOffset == 0))) {
				builder.append(c);
				builder.append("\n"); //$NON-NLS-1$
				lastOffset = nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
				multiLine = true;
				return Result.NOT_FOUND;
			}
			if (multiLine) {
				c = builder.toString();
			} else if (nodeLocation.getNodeOffset() != 0) {
				return Result.NOT_FOUND;
			}
			if (fPattern.matcher(c).matches())
				return Result.FOUND_MATCH;
			return Result.FOUND;
		}
		return Result.NOT_FOUND;
	}

	private void setProblem() {
		IProblemLocationFactory locFactory = getRuntime().getProblemLocationFactory();
		IProblemLocation p = locFactory.createProblemLocation(getFile(), 1);
		reportProblem(ERR_ID, p);
	}
}