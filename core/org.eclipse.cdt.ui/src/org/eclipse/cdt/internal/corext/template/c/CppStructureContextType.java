/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on 19/12/2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.internal.corext.template.ITemplateEditor;
import org.eclipse.cdt.internal.corext.template.TemplateContext;

public class CppStructureContextType extends CompilationUnitContextType {

	public CppStructureContextType() {
		super(ITemplateEditor.TemplateContextKind.CPP_STRUCTURE_CONTEXT_TYPE);
		// global
		addVariable(new GlobalVariables.Cursor());
		addVariable(new GlobalVariables.Dollar());
		addVariable(new GlobalVariables.Date());
		addVariable(new GlobalVariables.Time());
		addVariable(new GlobalVariables.User());
		
		// compilation unit
		addVariable(new File());
		/* addVariable(new Method());
		 addVariable(new ReturnType());
		 addVariable(new Arguments());
		 addVariable(new Type());
		 addVariable(new Package()); */
		addVariable(new Project());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.corext.template.ContextType#createContext()
	 */
	public TemplateContext createContext() {
		return new CContext(this, fString, fPosition, fCompilationUnit);
	}
	
}
