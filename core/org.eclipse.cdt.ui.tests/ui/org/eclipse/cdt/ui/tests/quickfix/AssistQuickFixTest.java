/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - testInvertEquals1-23
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.quickfix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.PartInitException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.correction.CCorrectionProcessor;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionContext;
import org.eclipse.cdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.cdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal;
import org.eclipse.cdt.internal.ui.text.correction.proposals.RenameRefactoringProposal;
import org.eclipse.cdt.internal.ui.text.correction.proposals.TUCorrectionProposal;

public class AssistQuickFixTest extends BaseUITestCase {
	private static final Class<AssistQuickFixTest> THIS = AssistQuickFixTest.class;
	private static final String PROJECT = "AssistQuickFixTest";
	
	private ICProject fCProject;
	private CEditor fEditor;
	private IDocument fDocument;
	private FindReplaceDocumentAdapter fFindReplaceDocumentAdapter;
	private final IProblem[] fProblems;
	private IAnnotationModel fAnnotationModel;
	private IRegion fMatch;
	private StyledText fTextWidget;

	private ProjectTestSetup fProjectSetup;

	protected static class ProjectTestSetup extends TestSetup {
		private ICProject fCProject;
		
		public ProjectTestSetup(Test test) {
			super(test);
		}
		@Override
		protected void setUp() throws Exception {
			super.setUp();
			fCProject= EditorTestHelper.createCProject(PROJECT, "resources/quickFix", false, true);
		}
		@Override
		protected void tearDown() throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			
			super.tearDown();
		}
	}
	
	public AssistQuickFixTest(String name) {
		super(name);
		fProblems = new IProblem[0];
	}
	
	public static Test setUpTest(Test someTest) {
		return new ProjectTestSetup(someTest);
	}
	
	public static Test suite() {
		return setUpTest(new TestSuite(THIS));
	}
	
	@Override
	protected void setUp() throws Exception {
		if (!ResourcesPlugin.getWorkspace().getRoot().exists(new Path(PROJECT))) {
			fProjectSetup= new ProjectTestSetup(this);
			fProjectSetup.setUp();
		}
		fEditor= openCEditor(new Path("/" + PROJECT + "/src/RenameInFile.cpp"));
		assertNotNull(fEditor);
		fTextWidget= fEditor.getViewer().getTextWidget();
		assertNotNull(fTextWidget);
		boolean ok= EditorTestHelper.joinReconciler((SourceViewer) fEditor.getViewer(), 10, 500, 20);
		assertTrue("Reconciler did not finish in time", ok);
		fDocument= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		assertNotNull(fDocument);
		fFindReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		fAnnotationModel= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
	
		fMatch= null;
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		EditorTestHelper.closeAllEditors();
		if (fProjectSetup != null) {
			fProjectSetup.tearDown();
		}
	}
	
	private CEditor openCEditor(IPath path) {
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		assertTrue(file != null && file.exists());
		try {
			return (CEditor) EditorTestHelper.openInEditor(file, true);
		} catch (PartInitException e) {
			fail();
			return null;
		}
	}	
	
	private static final List<ICCompletionProposal> collectCorrections(CorrectionContext context, Class<?>[] filteredTypes) throws CoreException {
		List<ICCompletionProposal> proposals= new ArrayList<ICCompletionProposal>();
		IStatus status= CCorrectionProcessor.collectCorrections(context, new IProblemLocation[0], proposals);
		assertStatusOk(status);

		if (!proposals.isEmpty()) {
			assertTrue("should be marked as 'has assist'", CCorrectionProcessor.hasAssists(context));
		}

		if (filteredTypes != null && filteredTypes.length > 0) {
			for (Iterator<ICCompletionProposal> iter= proposals.iterator(); iter.hasNext(); ) {
				if (isFiltered(iter.next(), filteredTypes)) {
					iter.remove();
				}
			}
		}
		return proposals;
	}

	private static boolean isFiltered(Object curr, Class<?>[] filteredTypes) {
		for (int k = 0; k < filteredTypes.length; k++) {
			if (filteredTypes[k].isInstance(curr)) {
				return true;
			}
		}
		return false;
	}

	private CorrectionContext getCorrectionContext(int offset, int length) {
		ITranslationUnit tu= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		return new CorrectionContext(tu, offset, length);
	}

	private static final ArrayList<ICCompletionProposal> collectAssists(CorrectionContext context, Class<?>[] filteredTypes) throws CoreException {
		ArrayList<ICCompletionProposal> proposals= new ArrayList<ICCompletionProposal>();
		IStatus status= CCorrectionProcessor.collectAssists(context, new IProblemLocation[0], proposals);
		assertStatusOk(status);

		if (!proposals.isEmpty()) {
			assertTrue("should be marked as 'has assist'", CCorrectionProcessor.hasAssists(context));
		}

		if (filteredTypes != null && filteredTypes.length > 0) {
			for (Iterator<ICCompletionProposal> iter= proposals.iterator(); iter.hasNext(); ) {
				if (isFiltered(iter.next(), filteredTypes)) {
					iter.remove();
				}
			}
		}
		return proposals;
	}

	private static void assertStatusOk(IStatus status) throws CoreException {
		if (!status.isOK() && status.getException() == null) {
			// Find a status with an exception
			for (IStatus child : status.getChildren()) {
				if (child.getException() != null) {
					throw new CoreException(child);
				}
			}
		}
	}

	private static void assertCorrectContext(IInvocationContext context, ProblemLocation problem) {
		if (problem.getProblemId() != 0) {
			if (!CCorrectionProcessor.hasCorrections(context.getTranslationUnit(), problem.getProblemId(), problem.getMarkerType())) {
				assertTrue("Problem type not marked with light bulb: " + problem, false);
			}
		}
	}

	private static void assertNumberOfProposals(List<ICCompletionProposal> proposals, int expectedProposals) {
		if (proposals.size() != expectedProposals) {
			StringBuilder buf= new StringBuilder();
			buf.append("Wrong number of proposals, is: ").append(proposals.size()). append(", expected: ").append(expectedProposals).append('\n');
			for (ICCompletionProposal proposal : proposals) {
				buf.append(" - ").append(proposal.getDisplayString()).append('\n');
				if (proposal instanceof TUCorrectionProposal) {
					try {
						buf.append(((TUCorrectionProposal) proposal).getPreviewContent());
					} catch (CoreException e) {
						// ignore
					}
				}
			}
			assertTrue(buf.toString(), false);
		}
	}

	private static void assertCorrectLabels(List<ICCompletionProposal> proposals) {
		for (ICCompletionProposal proposal : proposals) {
			String name= proposal.getDisplayString();
			if (name == null || name.length() == 0 || name.charAt(0) == '!' || name.indexOf("{0}") != -1 || name.indexOf("{1}") != -1) {
				assertTrue("wrong proposal label: " + name, false);
			}
			if (proposal.getImage() == null) {
				assertTrue("wrong proposal image", false);
			}
		}
	}

	private static void assertCorrectContext(CorrectionContext context, ProblemLocation problem) {
		if (problem.getProblemId() != 0) {
			if (!CCorrectionProcessor.hasCorrections(context.getTranslationUnit(), problem.getProblemId(), problem.getMarkerType())) {
				assertTrue("Problem type not marked with light bulb: " + problem, false);
			}
		}
	}

	public void testLocalRenameClass() throws Exception {
    	final String name = "Base2"; 
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, name, true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);
        CorrectionContext context= getCorrectionContext(fMatch.getOffset(), 0);
        List<ICCompletionProposal> proposals= collectAssists(context, null);
        
        assertNumberOfProposals(proposals, 2);
        assertCorrectLabels(proposals);
        assertTrue(proposals.get(0) instanceof LinkedNamesAssistProposal);
        assertTrue(proposals.get(1) instanceof RenameRefactoringProposal);
    }

	public void testLocalRenameMacro() throws Exception {
    	final String name = "INT"; 
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, name, true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);
        CorrectionContext context= getCorrectionContext(fMatch.getOffset(), 0);
        List<ICCompletionProposal> proposals= collectAssists(context, null);
        
        assertNumberOfProposals(proposals, 2);
        assertCorrectLabels(proposals);
        assertTrue(proposals.get(0) instanceof LinkedNamesAssistProposal);
        assertTrue(proposals.get(1) instanceof RenameRefactoringProposal);
    }

	public void testLocalRenameLanguageKeyword() throws Exception {
    	final String name = "int"; 
		try {
			fMatch= fFindReplaceDocumentAdapter.find(0, name, true, true, true, false);
		} catch (BadLocationException e) {
			fail();
		}
		assertNotNull(fMatch);
        CorrectionContext context= getCorrectionContext(fMatch.getOffset(), 0);
        List<ICCompletionProposal> proposals= collectAssists(context, null);
        
        assertNumberOfProposals(proposals, 0);
    }
}
