/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Enum that represents C++ visibilities, with methods to convert to
 * and from ICPPASTVisiblityLabel.
 *
 */
public enum VisibilityEnum {
	
	v_public(Messages.VisibilityEnum_public),  
	v_protected(Messages.VisibilityEnum_protected),  
	v_private(Messages.VisibilityEnum_private); 

	private final String stringRepresentation;
	
	VisibilityEnum(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}
	
	public static VisibilityEnum from(ICPPASTVisibilityLabel visibility) {
		switch(visibility.getVisibility()){
		case ICPPASTVisibilityLabel.v_private:
			return VisibilityEnum.v_private;
		case ICPPASTVisibilityLabel.v_protected:
			return VisibilityEnum.v_protected;
		case ICPPASTVisibilityLabel.v_public:
			return VisibilityEnum.v_public;
		}
		return null;
	}
	
	public int getASTBaseSpecifierVisibility() {
		switch (this) {
		case v_private:
			return ICPPASTBaseSpecifier.v_private;
		case v_protected:
			return ICPPASTBaseSpecifier.v_protected;
		case v_public:
			return ICPPASTBaseSpecifier.v_public;
		}
		return 0;
	}
	
	public int getICPPASTVisiblityLabelVisibility() {
		switch (this) {
		case v_private:
			return ICPPASTVisibilityLabel.v_private;
		case v_protected:
			return ICPPASTVisibilityLabel.v_protected;
		case v_public:
			return ICPPASTVisibilityLabel.v_public;
		}
		return 0;
	}
	
	public static VisibilityEnum getEnumForStringRepresentation(String visibility){
		if( VisibilityEnum.v_private.toString().equals( visibility ) ) {
			return VisibilityEnum.v_private;
		}else if( VisibilityEnum.v_protected.toString().equals( visibility ) ) {
			return VisibilityEnum.v_protected;
		}else if ( VisibilityEnum.v_public.toString().equals( visibility ) ) {
			return VisibilityEnum.v_public;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return stringRepresentation;
	}
}
