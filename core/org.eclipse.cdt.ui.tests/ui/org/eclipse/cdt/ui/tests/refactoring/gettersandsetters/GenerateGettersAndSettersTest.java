/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 	   Emanuel Graf & Leo Buettiker - initial API and implementation 
 * 	   Thomas Corbat - implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.gettersandsetters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GenerateGettersAndSettersRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterSetterContext;

/**
 * @author Thomas Corbat
 */
public class GenerateGettersAndSettersTest extends RefactoringTest {
	protected boolean fatalError;
	private int warnings;
	private List<String> selectedGetters;
	private List<String> selectedSetters;
	private GenerateGettersAndSettersRefactoring refactoring;
	private boolean definitionSeparate;
	private int infos;

	/**
	 * @param name
	 * @param files
	 */
	public GenerateGettersAndSettersTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile file = project.getFile(fileName);
		ICElement element = CoreModel.getDefault().create(file);
		refactoring = new GenerateGettersAndSettersRefactoring(element, selection, cproject, astCache);
		RefactoringStatus initialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);

		if (fatalError) {
			assertConditionsFatalError(initialConditions);
			return;
		} else {
			assertConditionsOk(initialConditions);
			executeRefactoring();
		}
	}

	private void executeRefactoring() throws CoreException, Exception {
		selectFields();
		refactoring.getContext().setDefinitionSeparate(definitionSeparate);
		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
		if (warnings > 0) {
			assertConditionsWarning(finalConditions, warnings);
		} else if (infos > 0) {
			assertConditionsInfo(finalConditions, infos);
		} else {
			assertConditionsOk(finalConditions);
		}

		createChange.perform(NULL_PROGRESS_MONITOR);
		compareFiles(fileMap);
	}

	private void selectFields() {
		GetterSetterContext context = refactoring.getContext();
	
		for (IASTSimpleDeclaration currentDecl : context.existingFields) {
			String name = currentDecl.getDeclarators()[0].getName().getRawSignature();
			if (selectedGetters.contains(name)) {
				selectedGetters.remove(name);
				context.selectedFunctions.add(context.createGetterInserter(currentDecl));
			}

			if (selectedSetters.contains(name)) {
				selectedSetters.remove(name);
				context.selectedFunctions.add(context.createSetterInserter(currentDecl));
			}
		}
	}
	
	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		fatalError = Boolean.valueOf(refactoringProperties.getProperty("fatalerror", "false")).booleanValue();  //$NON-NLS-1$//$NON-NLS-2$
		warnings = new Integer(refactoringProperties.getProperty("warnings", "0")).intValue();  //$NON-NLS-1$//$NON-NLS-2$
		infos = new Integer(refactoringProperties.getProperty("infos", "0"));
		String getters = refactoringProperties.getProperty("getters", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String setters = refactoringProperties.getProperty("setters", ""); //$NON-NLS-1$ //$NON-NLS-2$
		definitionSeparate = Boolean.valueOf(refactoringProperties.getProperty("definitionSeparate", "false"));
		
		selectedGetters = new ArrayList<String>();	
		for (String getterName : getters.split(",")) { //$NON-NLS-1$
			selectedGetters.add(getterName);
		}
		selectedSetters = new ArrayList<String>();
		for (String setterName : setters.split(",")) { //$NON-NLS-1$
			selectedSetters.add(setterName);
		}
	}
}
