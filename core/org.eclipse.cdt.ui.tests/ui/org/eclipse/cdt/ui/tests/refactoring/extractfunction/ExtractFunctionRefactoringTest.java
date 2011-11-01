/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractfunction;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Emanuel Graf
 */
public class ExtractFunctionRefactoringTest extends RefactoringTest {
	protected String methodName;
	protected boolean replaceDuplicates;
	protected boolean returnValue;
	protected int returnParameterIndex;
	protected boolean fatalError;
	private VisibilityEnum visibility;
	private boolean virtual;
	private static int nr = 1;

	public ExtractFunctionRefactoringTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile refFile = project.getFile(fileName);
		ExtractFunctionInformation info = new ExtractFunctionInformation();
		CRefactoring refactoring = new ExtractFunctionRefactoring(refFile, selection, info, cproject);
		RefactoringStatus checkInitialConditions = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
		
		if (fatalError) {
			assertConditionsFatalError(checkInitialConditions);
			return;
		} else {
			assertConditionsOk(checkInitialConditions);
			setValues(info);
			executeRefactoring(refactoring);
		}
	}

	protected void executeRefactoring(Refactoring refactoring) throws Exception {
		RefactoringStatus finalConditions = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		assertConditionsOk(finalConditions);
		Change createChange = refactoring.createChange(NULL_PROGRESS_MONITOR);
		createChange.perform(NULL_PROGRESS_MONITOR);
		compareFiles(fileMap);
	}

	private void setValues(ExtractFunctionInformation info) {
		info.setMethodName(methodName);
		info.setReplaceDuplicates(replaceDuplicates);
		if (info.getInScopeDeclaredVariable() == null) {
			if (returnValue) {
				info.setReturnVariable(info.getAllAfterUsedNames().get(returnParameterIndex));
				info.getAllAfterUsedNames().get(returnParameterIndex).setUserSetIsReference(false);
			}
		} else {
			info.setReturnVariable(info.getInScopeDeclaredVariable());
		}
		info.setVisibility(visibility);
		info.setVirtual(virtual);
		
		for (NameInformation name : info.getAllAfterUsedNames()) {
			if (!name.isUserSetIsReturnValue()) {
				name.setUserSetIsReference(name.isReference());
			}
		}
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		methodName = refactoringProperties.getProperty("methodname", "exp"); //$NON-NLS-1$ //$NON-NLS-2$
		replaceDuplicates = Boolean.valueOf(refactoringProperties.getProperty("replaceduplicates", "false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
		returnValue = Boolean.valueOf(refactoringProperties.getProperty("returnvalue", "false")).booleanValue();  //$NON-NLS-1$//$NON-NLS-2$
		returnParameterIndex = new Integer(refactoringProperties.getProperty("returnparameterindex", "0")).intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		fatalError = Boolean.valueOf(refactoringProperties.getProperty("fatalerror", "false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
		visibility = VisibilityEnum.getEnumForStringRepresentation(refactoringProperties.getProperty("visibility", VisibilityEnum.v_private.toString())); //$NON-NLS-1$
		virtual = Boolean.valueOf(refactoringProperties.getProperty("virtual", "false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
