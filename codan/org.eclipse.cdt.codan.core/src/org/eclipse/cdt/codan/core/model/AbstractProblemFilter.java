/*******************************************************************************
 * Copyright (c) 2013 Andreas Muelder and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Muelder (itemis)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Abstract implementation of {@link IProblemFilter} interface. Maintains a List
 * of {@link CodanIgnoreComment}s and calculates whether a specific warning
 * should be ignored or not.
 *
 * @since 3.3
 *
 */
public abstract class AbstractProblemFilter implements IProblemFilter {
	private List<CodanIgnoreComment> comments;

	protected abstract List<CodanIgnoreComment> createIgnoreComments(IFile file);

	@Override
	public void before(IResource resource) {
		if (resource instanceof IFile) {
			comments = createIgnoreComments((IFile) resource);
			Collections.sort(comments, new Comparator<CodanIgnoreComment>() {
				@Override
				public int compare(CodanIgnoreComment o1, CodanIgnoreComment o2) {
					return o1.getLineNr() - o2.getLineNr();
				}
			});
		}
	}

	public boolean shouldIgnore(String problemId, IProblemLocation location) {
		boolean result = false;
		for (CodanIgnoreComment comment : comments) {
			if (comment.getProblemId().equals(CodanIgnoreComment.PROBLEM_ID_ALL) || problemId.endsWith(comment.getProblemId())) {
				if (location.getLineNumber() < comment.getLineNr()) {
					return result;
				}
				result = !comment.isEnable();
			}
		}
		return result;
	}

	@Override
	public void after(IResource resource) {
		comments = null;
	}

	public static class CodanIgnoreComment {
		public static final String PROBLEM_ID_ALL = "IgnoreAll"; //$NON-NLS-1$
		private String problemId;
		private int lineNr;
		private boolean enable;

		public CodanIgnoreComment(String problemId, int lineNr, boolean enable) {
			this.problemId = problemId;
			this.lineNr = lineNr;
			this.enable = enable;
		}

		public String getProblemId() {
			return problemId;
		}

		public int getLineNr() {
			return lineNr;
		}

		public boolean isEnable() {
			return enable;
		}
	}
}
