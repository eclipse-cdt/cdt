/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactoringArgument;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRefactory;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameProcessor;
import org.eclipse.cdt.internal.ui.refactoring.rename.CRenameRefactoring;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.internal.core.refactoring.resource.MoveResourcesProcessor;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;

import junit.framework.Test;

/**
 * Tests for
 * {@link org.eclipse.cdt.internal.ui.refactoring.rename.HeaderFileRenameParticipant} and
 * {@link org.eclipse.cdt.internal.ui.refactoring.rename.HeaderFileMoveParticipant}.
 */
public class RenameMoveHeaderRefactoringTest extends RefactoringTestBase {

	public RenameMoveHeaderRefactoringTest() {
		super();
	}

	public RenameMoveHeaderRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(RenameMoveHeaderRefactoringTest.class);
	}

	@Override
	protected void resetPreferences() {
		super.resetPreferences();
		getPreferenceStore().setToDefault(PreferenceConstants.FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT);
		getPreferenceStore().setToDefault(PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER);
	}

	@Override
	protected CRefactoring createRefactoring() {
		throw new UnsupportedOperationException();
	}

	protected CRenameRefactoring createRenameRefactoring(String newName) {
		IFile file = getSelectedFile();
		TextSelection selection = getSelection();
		CRefactoringArgument arg = new CRefactoringArgument(file, selection.getOffset(), selection.getLength());
		CRenameProcessor processor = new CRenameProcessor(CRefactory.getInstance(), arg);
		processor.setReplacementText(newName);
		processor.setSelectedOptions(0xFFFF & ~CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH);
		return new CRenameRefactoring(processor);
	}

	protected void executeRenameRefactoring(String newName, boolean expectedSuccess) throws Exception {
		CRenameRefactoring refactoring = createRenameRefactoring(newName);
		refactoring.getProcessor().lockIndex();
		try {
			executeRefactoring(refactoring, expectedSuccess);
		} finally {
			refactoring.getProcessor().unlockIndex();
		}
	}

	// test1.h
	//#ifndef TEST1_H_
	//#define TEST1_H_
	//
	//class A {};
	//
	//#endif // TEST1_H_
	//====================
	// test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class A {};
	//
	//#endif // TEST_H_

	// test.cpp
	//#include <string>
	//#include "test1.h"  /* Comment1 */ // Comment2
	//====================
	// test.cpp
	//#include "test.h"  /* Comment1 */ // Comment2
	//
	//#include <string>
	public void testFileRename() throws Exception {
		IResource resource = getProject().getFile("test1.h");
		RenameResourceProcessor processor = new RenameResourceProcessor(resource);
		processor.setNewResourceName("test.h");
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		executeRefactoring(refactoring, true);
		compareFiles();
	}

	// header1.h
	//#ifndef HEADER1_H_
	//#define HEADER1_H_
	//
	//class A {};
	//
	//#endif // HEADER1_H_
	//====================
	// dir/header1.h
	//#ifndef DIR_HEADER1_H_
	//#define DIR_HEADER1_H_
	//
	//class A {};
	//
	//#endif // DIR_HEADER1_H_

	// source1.cpp
	//#include "header1.h"
	//====================
	// source1.cpp
	//#include "dir/header1.h"
	public void testFileMove() throws Exception {
		IResource resource = getProject().getFile("header1.h");
		MoveResourcesProcessor processor = new MoveResourcesProcessor(new IResource[] { resource });
		IFolder destination = getProject().getFolder("dir");
		destination.create(true, true, npm());
		processor.setDestination(destination);
		MoveRefactoring refactoring = new MoveRefactoring(processor);
		executeRefactoring(refactoring, true);
		compareFiles();
	}

	// dir1/header1.h
	//#ifndef DIR1_HEADER1_H_
	//#define DIR1_HEADER1_H_
	//
	//#include "dir1/header2.h"
	//
	//#endif // DIR1_HEADER1_H_
	//====================
	// dir3/header1.h
	//#ifndef DIR3_HEADER1_H_
	//#define DIR3_HEADER1_H_
	//
	//#include "dir3/header2.h"
	//
	//#endif // DIR3_HEADER1_H_

	// dir1/header2.h
	//#if !defined(DIR1_HEADER2_H_)
	//#define DIR1_HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* DIR1_HEADER2_H_ */
	//====================
	// dir3/header2.h
	//#if !defined(DIR3_HEADER2_H_)
	//#define DIR3_HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* DIR3_HEADER2_H_ */

	// dir1/source1.cpp
	//#include <string>
	//
	//#include "dir1/header1.h"
	//====================
	// dir3/source1.cpp
	//#include <string>
	//
	//#include "dir3/header1.h"

	// source2.cpp
	//#include "dir1/header1.h"
	//#include "dir2/header3.h"
	//
	//#ifdef SOMETHING
	//  #include "dir1/header2.h"
	//#endif
	//====================
	//#include "dir2/header3.h"
	//#include "dir3/header1.h"
	//
	//#ifdef SOMETHING
	//  #include "dir3/header2.h"
	//#endif
	public void testFolderRename() throws Exception {
		IFolder resource = getProject().getFolder("dir1");
		RenameResourceProcessor processor = new RenameResourceProcessor(resource);
		processor.setNewResourceName("dir3");
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		executeRefactoring(refactoring, true);
		compareFiles();
	}

	// src1/header1.h
	//#ifndef HEADER1_H_
	//#define HEADER1_H_
	//
	//#include "src1/header2.h"
	//
	//#endif // HEADER1_H_
	//====================
	// src2/header1.h
	//#ifndef HEADER1_H_
	//#define HEADER1_H_
	//
	//#include "src2/header2.h"
	//
	//#endif // HEADER1_H_

	// src1/header2.h
	//#if !defined(HEADER2_H_)
	//#define HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* HEADER2_H_ */
	//====================
	// src2/header2.h
	//#if !defined(HEADER2_H_)
	//#define HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* HEADER2_H_ */

	// src1/source1.cpp
	//#include <string>
	//
	//#include "src1/header1.h"
	//====================
	// src2/source1.cpp
	//#include <string>
	//
	//#include "src2/header1.h"
	public void testSourceRootRename() throws Exception {
		CProjectHelper.addSourceRoot(getCProject(), "src1");
		IFolder resource = getProject().getFolder("src1");
		RenameResourceProcessor processor = new RenameResourceProcessor(resource);
		processor.setNewResourceName("src2");
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		executeRefactoring(refactoring, true);
		ISourceRoot[] sourceRoots = getCProject().getAllSourceRoots();
		assertEquals(2, sourceRoots.length);
		assertEquals(getProject().getName(), sourceRoots[0].getElementName());
		assertEquals("src2", sourceRoots[1].getElementName());
		compareFiles();
	}

	// dir1/header1.h
	//#ifndef DIR1_HEADER1_H_
	//#define DIR1_HEADER1_H_
	//
	//#include "dir1/header2.h"
	//
	//#endif // DIR1_HEADER1_H_
	//====================
	// dir3/dir1/header1.h
	//#ifndef DIR3_DIR1_HEADER1_H_
	//#define DIR3_DIR1_HEADER1_H_
	//
	//#include "dir3/dir1/header2.h"
	//
	//#endif // DIR3_DIR1_HEADER1_H_

	// dir1/header2.h
	//#if !defined(DIR1_HEADER2_H_)
	//#define DIR1_HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* DIR1_HEADER2_H_ */
	//====================
	// dir3/dir1/header2.h
	//#if !defined(DIR3_DIR1_HEADER2_H_)
	//#define DIR3_DIR1_HEADER2_H_
	//
	//class A {};
	//
	//#endif  /* DIR3_DIR1_HEADER2_H_ */

	// dir1/source1.cpp
	//#include <string>
	//
	//#include "dir1/header1.h"
	//====================
	// dir3/dir1/source1.cpp
	//#include <string>
	//
	//#include "dir3/dir1/header1.h"

	// source2.cpp
	//#include "dir1/header1.h"
	//#include "dir2/header3.h"
	//
	//int x = 0;
	//#include "dir1/header2.h"
	//====================
	//#include "dir2/header3.h"
	//#include "dir3/dir1/header1.h"
	//
	//int x = 0;
	//#include "dir3/dir1/header2.h"
	public void testFolderMove() throws Exception {
		IFolder resource = getProject().getFolder("dir1");
		MoveResourcesProcessor processor = new MoveResourcesProcessor(new IResource[] { resource });
		IFolder destination = getProject().getFolder("dir3");
		destination.create(true, true, npm());
		processor.setDestination(destination);
		MoveRefactoring refactoring = new MoveRefactoring(processor);
		executeRefactoring(refactoring, true);
		compareFiles();
	}

	// my-class.h
	//#ifndef MY_CLASS_H_
	//#define MY_CLASS_H_
	//
	//class MyClass {};
	//
	//#endif // MY_CLASS_H_
	//====================
	// my-new-class.h
	//#ifndef MY_NEW_CLASS_H_
	//#define MY_NEW_CLASS_H_
	//
	//class MyNewClass {};
	//
	//#endif // MY_NEW_CLASS_H_

	// my-class.cpp
	//#include "my-class.h"
	//
	//#include <cstdio>
	//====================
	// my-new-class.cpp
	//#include "my-new-class.h"
	//
	//#include <cstdio>

	// my-class_test.cpp
	//#include "my-class.h"
	//====================
	// my-new-class_test.cpp
	//#include "my-new-class.h"

	// some-other-file.cpp
	//#include "my-class.h"
	///*$*/MyClass/*$$*/ a;
	//====================
	// some-other-file.cpp
	//#include "my-new-class.h"
	//MyNewClass a;
	public void testClassRename() throws Exception {
		executeRenameRefactoring("MyNewClass", true);
		compareFiles();
	}
}
