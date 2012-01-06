/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Enum that represents C++ visibilities, with methods to convert to
 * and from ICPPASTVisiblityLabel.
 */
public enum VisibilityEnum {
	// The values are ordered by increasing visibility.
	v_private(Messages.VisibilityEnum_private, ICPPASTVisibilityLabel.v_private, ICPPASTBaseSpecifier.v_private), 
	v_protected(Messages.VisibilityEnum_protected, ICPPASTVisibilityLabel.v_protected, ICPPASTBaseSpecifier.v_protected),  
	v_public(Messages.VisibilityEnum_public, ICPPASTVisibilityLabel.v_public, ICPPASTBaseSpecifier.v_public);

	private final String stringRepresentation;
	private final int visibilityLabelValue;
	private final int baseSpecifierValue;
	
	private VisibilityEnum(String stringRepresentation, int visibilityLabelValue,
			int baseSpecifierValue) {
		this.stringRepresentation = stringRepresentation;
		this.visibilityLabelValue = visibilityLabelValue;
		this.baseSpecifierValue = baseSpecifierValue;
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
	
	public int getBaseSpecifierValue() {
		return baseSpecifierValue;
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
