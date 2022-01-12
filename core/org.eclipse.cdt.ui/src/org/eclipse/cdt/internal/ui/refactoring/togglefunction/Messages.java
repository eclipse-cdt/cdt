/*******************************************************************************
 * Copyright (c) 2011, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Emanuel Graf IFS - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String DeclaratorFinder_NestedFunction;
	public static String DeclaratorFinder_NoDeclarator;
	public static String DeclaratorFinder_MultipleDeclarators;
	public static String RefactoringJob_UndoName;
	public static String ToggleFileCreator_CanNotCreateNewFile;
	public static String ToggleFileCreator_CreateNewFilePrompt;
	public static String ToggleFileCreator_NewImplFile;
	public static String ToggleFileCreator_NoTuForSibling;
	public static String ToggleFromClassToInHeaderStrategy_DefAndDecInsideClass;
	public static String EditGroupName;
	public static String ToggleFromImplementationToHeaderOrClassStrategy_CanNotCreateNewFile;
	public static String ToggleFromImplementationToHeaderOrClassStrategy_CanNotToggle;
	public static String ToggleFromInHeaderToClassStrategy_CanNotToggleTemplateFreeFunction;
	public static String ToggleFromInHeaderToImplementationStrategy_CanNotCreateImplFile;
	public static String ToggleRefactoring_AnalyseSelection;
	public static String ToggleRefactoring_CalculateModifications;
	public static String ToggleRefactoring_CanNotSaveFiles;
	public static String ToggleRefactoring_InvalidSelection;
	public static String ToggleRefactoring_NoIndex;
	public static String ToggleRefactoring_SyntaxError;
	public static String ToggleRefactoring_WaitingForIndexer;
	public static String ToggleRefactoringContext_MultipleDeclarations;
	public static String ToggleRefactoringContext_MultipleDefinitions;
	public static String ToggleRefactoringContext_NoDefinitionFound;
	public static String ToggleRefactoringContext_NoTuFound;
	public static String ToggleStrategyFactory_NoDefinitionFound;
	public static String ToggleStrategyFactory_UnsupportedSituation;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
