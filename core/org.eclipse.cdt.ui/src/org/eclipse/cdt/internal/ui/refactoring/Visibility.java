/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;

/**
 * Represents the visibility of an IASTName.
 * 
 */
public class Visibility {

	/**
	 * The visibility public.
	 */
	public static final Visibility PUBLIC = new Visibility(){
		@Override
		public String stringValue(){
			return "public"; //$NON-NLS-1$
		}
	};
	
	/**
	 * The visibility protected.
	 */
	public static final Visibility PROTECTED = new Visibility(){
		@Override
		public String stringValue(){
			return "protected"; //$NON-NLS-1$
		}
	};
	
	/**
	 * The visibility private.
	 */
	public static final Visibility PRIVATE = new Visibility(){
		@Override
		public String stringValue(){
			return "private"; //$NON-NLS-1$
		}
	};
	
	/**
	 * The visibility unknown, cause of parsing error.
	 */
	public static final Visibility UNKNOWN = new Visibility(){ };
	
	private Visibility(){}
	
	public static Visibility getVisibility(IASTName name){
		try {
			ICPPMember member = ((ICPPMember)name.resolveBinding());			
			
			switch (member.getVisibility()){
				case ICPPASTVisibilityLabel.v_public:
					return PUBLIC;
				case ICPPASTVisibilityLabel.v_protected:
					return PROTECTED;
				case ICPPASTVisibilityLabel.v_private:
					return PRIVATE;
				default:
					return UNKNOWN;
			}
			
		} catch (RuntimeException e){
			return UNKNOWN;
		}
	}
	
	public String stringValue(){
		return ""; //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
