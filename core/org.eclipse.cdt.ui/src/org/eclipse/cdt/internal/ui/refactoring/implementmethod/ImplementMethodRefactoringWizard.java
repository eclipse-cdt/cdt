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
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Mirko Stocker
 *
 */
public class ImplementMethodRefactoringWizard extends RefactoringWizard {

       private final ImplementMethodRefactoring refactoring;
       private Map<MethodToImplementConfig, ParameterNamesInputPage>pagesMap = new HashMap<MethodToImplementConfig, ParameterNamesInputPage>();

	public ImplementMethodRefactoringWizard(ImplementMethodRefactoring refactoring) {
    	   super(refactoring, WIZARD_BASED_USER_INTERFACE);
    	   this.refactoring = refactoring;
       }

	@Override
	protected void addUserInputPages() {
		addPage(new ImplementMethodInputPage(refactoring.getRefactoringData(), this));
		ImplementMethodData data = refactoring.getRefactoringData();
		for (MethodToImplementConfig config : data.getMethodDeclarations()) {
			if(config.getParaHandler().needsAdditionalArgumentNames()) {
				ParameterNamesInputPage page = new ParameterNamesInputPage(config, this);
				pagesMap.put(config, page);
				addPage(page);
			}
		}
	}
	
	public ParameterNamesInputPage getPageForConfig(MethodToImplementConfig config) {
		return pagesMap.get(config);
	}
}
