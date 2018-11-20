/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

/**
 * Enum that represents C++ visibilities, with methods to convert to and from ICPPASTVisiblityLabel.
 */
public enum VisibilityEnum {
	// The values are ordered by increasing visibility.
	v_private("private", ICPPASTVisibilityLabel.v_private), //$NON-NLS-1$
	v_protected("protected", ICPPASTVisibilityLabel.v_protected), //$NON-NLS-1$
	v_public("public", ICPPASTVisibilityLabel.v_public); //$NON-NLS-1$

	private final String stringRepresentation;
	private final int visibilityLabelValue;

	private VisibilityEnum(String stringRepresentation, int visibilityLabelValue) {
		this.stringRepresentation = stringRepresentation;
		this.visibilityLabelValue = visibilityLabelValue;
	}

	public static VisibilityEnum from(ICPPASTVisibilityLabel visibility) {
		switch (visibility.getVisibility()) {
		case ICPPASTVisibilityLabel.v_private:
			return VisibilityEnum.v_private;
		case ICPPASTVisibilityLabel.v_protected:
			return VisibilityEnum.v_protected;
		case ICPPASTVisibilityLabel.v_public:
			return VisibilityEnum.v_public;
		}
		return null;
	}

	public int getVisibilityLabelValue() {
		return visibilityLabelValue;
	}

	public static VisibilityEnum getEnumForStringRepresentation(String visibility) {
		if (VisibilityEnum.v_private.toString().equals(visibility)) {
			return VisibilityEnum.v_private;
		} else if (VisibilityEnum.v_protected.toString().equals(visibility)) {
			return VisibilityEnum.v_protected;
		} else if (VisibilityEnum.v_public.toString().equals(visibility)) {
			return VisibilityEnum.v_public;
		}
		return null;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
