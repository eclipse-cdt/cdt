/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.Optional;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPragma;

public class Pragma extends SourceManipulation implements IPragma {

	private boolean isPragmaOperator = false;
	private Optional<PragmaMarkInfo> pragmaMarkInfo = null;

	public Pragma(ICElement parent, String name) {
		super(parent, name, ICElement.C_PRAGMA);
	}

	private PragmaMarkInfo calculateMarkInfo(String name) {
		String nameTrimmed = name.trim();

		StringTokenizer tokenizer = new StringTokenizer(nameTrimmed);
		if (tokenizer.hasMoreTokens()) {

			boolean dividerBeforeMark = false;
			boolean dividerAfterMark = false;
			String markName = ""; //$NON-NLS-1$

			String pragmaName = tokenizer.nextToken();
			String restOfLine;
			if (tokenizer.hasMoreTokens()) {
				restOfLine = tokenizer.nextToken("").trim(); //$NON-NLS-1$
			} else {
				restOfLine = ""; //$NON-NLS-1$
			}
			switch (pragmaName) {
			case "mark": { //$NON-NLS-1$
				if (restOfLine.startsWith("-")) { //$NON-NLS-1$
					dividerBeforeMark = true;
					restOfLine = restOfLine.substring(1);
				}
				if (restOfLine.endsWith("-")) { //$NON-NLS-1$
					dividerAfterMark = true;
					restOfLine = restOfLine.substring(0, restOfLine.length() - 1);
				}
				markName = restOfLine.trim();
			}
				break;
			case "region": //$NON-NLS-1$
			case "endregion": { //$NON-NLS-1$
				if (restOfLine.isEmpty()) {
					dividerBeforeMark = true;
					dividerAfterMark = false;
				} else {
					dividerBeforeMark = true;
					dividerAfterMark = true;
					markName = restOfLine;
				}
			}
				break;
			default:
				return null;
			}

			boolean finalDividerBeforeMark = dividerBeforeMark;
			boolean finalDividerAfterMark = dividerAfterMark;
			String finalMarkName = markName;
			return new PragmaMarkInfo() {

				@Override
				public boolean isDividerBeforeMark() {
					return finalDividerBeforeMark;
				}

				@Override
				public boolean isDividerAfterMark() {
					return finalDividerAfterMark;
				}

				@Override
				public String getMarkName() {
					return finalMarkName;
				}
			};
		} else {
			return null;
		}
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new SourceManipulationInfo(this);
	}

	public void setPragmaOperator(boolean isPragmaOperator) {
		this.isPragmaOperator = isPragmaOperator;
	}

	@Override
	public boolean isPragmaOperator() {
		return isPragmaOperator;
	}

	@Override
	public Optional<PragmaMarkInfo> getPragmaMarkInfo() {
		if (pragmaMarkInfo == null) {
			pragmaMarkInfo = Optional.ofNullable(calculateMarkInfo(getElementName()));
		}
		return pragmaMarkInfo;
	}
}
