/*******************************************************************************
 * Copyright (c) 2013 Andreas Muelder and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Muelder  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.model.AbstractProblemFilter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Allows suppressing specific codan problems via a single line comment.
 *
 * To suppress all codan problems for a given code block use:
 * //codan:off and //codan:on
 *
 * To suppress a specific codan problem use:
 * //codan:off #ProblemId and //codan:on #ProblemId
 *
 */
public class CommentBasedProblemFilter extends AbstractProblemFilter {

	protected static final String ON = "on"; //$NON-NLS-1$
	protected static final Pattern CODAN_COMMENT = Pattern.compile("codan:(off|on)[\\s]*([#]([A-Za-z]*))?"); //$NON-NLS-1$
	public static final String ID = "org.eclipse.cdt.codan.internal.core.model.CommentBasedProblemFilter"; //$NON-NLS-1$

	@Override
	protected List<CodanIgnoreComment> createIgnoreComments(IFile file) {
		List<CodanIgnoreComment> result = new ArrayList<AbstractProblemFilter.CodanIgnoreComment>();
		int lineNr = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					lineNr++;
					Matcher matcher = CODAN_COMMENT.matcher(line);
					while (matcher.find()) {
						String problemId = CodanIgnoreComment.PROBLEM_ID_ALL;
						if (matcher.group(3) != null) {
							problemId = matcher.group(3);
						}
						String group = matcher.group(1);
						result.add(new CodanIgnoreComment(problemId, lineNr, ON.equals(group) ? true : false));
					}
				}
			} finally {
				reader.close();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
