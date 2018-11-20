/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.IPersistableProblem;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.ui.editor.CMarkerAnnotation;
import org.eclipse.cdt.internal.ui.editor.ICAnnotation;
import org.eclipse.cdt.ui.text.IProblemLocation;

public class ProblemLocation implements IProblemLocation {
	private final int fId;
	private final String[] fArguments;
	private final int fOffset;
	private final int fLength;
	private final boolean fIsError;
	private final String fMarkerType;

	public ProblemLocation(int offset, int length, ICAnnotation annotation) {
		fId = annotation.getId();
		fArguments = annotation.getArguments();
		fOffset = offset;
		fLength = length;
		fIsError = CMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(annotation.getType());

		String markerType = annotation.getMarkerType();
		fMarkerType = markerType != null ? markerType : ICModelMarker.C_MODEL_PROBLEM_MARKER;
	}

	public ProblemLocation(int offset, int length, int id, String[] arguments, boolean isError, String markerType) {
		fId = id;
		fArguments = arguments;
		fOffset = offset;
		fLength = length;
		fIsError = isError;
		fMarkerType = markerType;
	}

	public ProblemLocation(IProblem problem) {
		fId = problem.getID();
		fArguments = problem.getArguments();
		fOffset = problem.getSourceStart();
		fLength = problem.getSourceEnd() - fOffset + 1;
		fIsError = problem.isError();
		fMarkerType = problem instanceof IPersistableProblem ? ((IPersistableProblem) problem).getMarkerType()
				: ICModelMarker.C_MODEL_PROBLEM_MARKER;
	}

	@Override
	public int getProblemId() {
		return fId;
	}

	@Override
	public String[] getProblemArguments() {
		return fArguments;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public boolean isError() {
		return fIsError;
	}

	@Override
	public String getMarkerType() {
		return fMarkerType;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Id: ").append(getErrorCode(fId)).append('\n'); //$NON-NLS-1$
		buf.append('[').append(fOffset).append(", ").append(fLength).append(']').append('\n'); //$NON-NLS-1$
		String[] arg = fArguments;
		if (arg != null) {
			for (int i = 0; i < arg.length; i++) {
				buf.append(arg[i]);
				buf.append('\n');
			}
		}
		return buf.toString();
	}

	private String getErrorCode(int code) {
		StringBuilder buf = new StringBuilder();
		if ((code & IProblem.SCANNER_RELATED) != 0) {
			buf.append("ScannerRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.PREPROCESSOR_RELATED) != 0) {
			buf.append("PreprocessorRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.INTERNAL_RELATED) != 0) {
			buf.append("Internal + "); //$NON-NLS-1$
		}
		if ((code & IProblem.SYNTAX_RELATED) != 0) {
			buf.append("Syntax + "); //$NON-NLS-1$
		}
		buf.append(code & IProblem.IGNORE_CATEGORIES_MASK);

		return buf.toString();
	}
}
