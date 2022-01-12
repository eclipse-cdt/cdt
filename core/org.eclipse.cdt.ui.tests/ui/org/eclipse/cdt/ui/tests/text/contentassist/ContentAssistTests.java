/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author aniefer
 */
public class ContentAssistTests extends BaseUITestCase {
	private final NullProgressMonitor monitor = new NullProgressMonitor();
	static IProject project;
	static ICProject cproject;
	static boolean disabledHelpContributions;

	@Override
	public void setUp() throws InterruptedException {
		//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();

		if (project == null) {
			try {
				cproject = CProjectHelper.createCCProject("ContentAssistTestProject", "bin", //$NON-NLS-1$//$NON-NLS-2$
						IPDOMManager.ID_FAST_INDEXER);
				project = cproject.getProject();
				waitForIndexer(cproject);
			} catch (CoreException e) {
				/*boo*/
			}
			if (project == null)
				fail("Unable to create project"); //$NON-NLS-1$
		}
	}

	public ContentAssistTests() {
		super();
	}

	/**
	 * @param name
	 */
	public ContentAssistTests(String name) {
		super(name);
	}

	private void disableContributions() {
		//disable the help books so we don't get proposals we weren't expecting
		CHelpBookDescriptor helpBooks[];
		helpBooks = CHelpProviderManager.getDefault().getCHelpBookDescriptors(new ICHelpInvocationContext() {
			@Override
			public IProject getProject() {
				return project;
			}

			@Override
			public ITranslationUnit getTranslationUnit() {
				return null;
			}
		});
		for (CHelpBookDescriptor helpBook : helpBooks) {
			if (helpBook != null)
				helpBook.enable(false);
		}
	}

	public static Test suite() {
		TestSuite suite = suite(ContentAssistTests.class, "_");
		suite.addTest(new ContentAssistTests("cleanupProject")); //$NON-NLS-1$
		return suite;
	}

	public void cleanupProject() throws Exception {
		closeAllEditors();
		try {
			project.delete(true, false, monitor);
			project = null;
		} catch (Throwable e) {
			/*boo*/
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (project == null || !project.exists())
			return;

		closeAllEditors();

		// wait for indexer before deleting project to avoid errors in the log
		waitForIndexer(cproject);

		IResource[] members = project.members();
		for (IResource member : members) {
			if (member.getName().equals(".project") || member.getName().equals(".cproject")) //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			if (member.getName().equals(".settings"))
				continue;
			try {
				member.delete(false, monitor);
			} catch (Throwable e) {
				/*boo*/
			}
		}

	}

	protected IFile importFile(String fileName, String contents) throws Exception {
		// Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		// Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, monitor);
		} else {
			file.create(stream, false, monitor);
		}

		return file;
	}

	protected ICompletionProposal[] getResults(IFile file, int offset) throws Exception {
		if (!disabledHelpContributions)
			disableContributions();

		ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault().create(file);
		String buffer = tu.getBuffer().getContents();
		IWorkingCopy wc = null;
		try {
			wc = tu.getWorkingCopy();
		} catch (CModelException e) {
			fail("Failed to get working copy"); //$NON-NLS-1$
		}

		// call the ContentAssistProcessor
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		FileEditorInput editorInput = new FileEditorInput(file);
		IEditorPart editorPart = page.openEditor(editorInput, "org.eclipse.cdt.ui.editor.CEditor");
		CEditor editor = (CEditor) editorPart;
		IAction completionAction = editor.getAction("ContentAssistProposal");

		String contentType = editor.getViewer().getDocument().getContentType(offset);
		ContentAssistant assistant = new ContentAssistant();
		CContentAssistProcessor processor = new CContentAssistProcessor(editor, assistant, contentType);
		return processor.computeCompletionProposals(editor.getViewer(), offset);
	}

	public void testBug69334a() throws Exception {
		importFile("test.h", "class Test{ public : Test( int ); }; \n"); //$NON-NLS-1$//$NON-NLS-2$
		StringBuilder buf = new StringBuilder();
		buf.append("#include \"test.h\"                \n"); //$NON-NLS-1$
		buf.append("Test::Test( int i ) { return; }    \n"); //$NON-NLS-1$
		buf.append("int main() {                       \n"); //$NON-NLS-1$
		buf.append("   int veryLongName = 1;           \n"); //$NON-NLS-1$
		buf.append("   Test * ptest = new Test( very   \n"); //$NON-NLS-1$

		String code = buf.toString();
		IFile cu = importFile("test.cpp", code); //$NON-NLS-1$

		ICompletionProposal[] results = getResults(cu, code.indexOf("very ") + 4); //$NON-NLS-1$

		assertEquals(1, results.length);
		assertEquals("veryLongName : int", results[0].getDisplayString()); //$NON-NLS-1$
	}

	public void testBug69334b() throws Exception {
		importFile("test.h", "class Test{ public : Test( int ); }; \n"); //$NON-NLS-1$//$NON-NLS-2$
		StringBuilder buf = new StringBuilder();
		buf.append("#include \"test.h\"                \n"); //$NON-NLS-1$
		buf.append("Test::Test( int i ) { return; }    \n"); //$NON-NLS-1$
		buf.append("int main() {                       \n"); //$NON-NLS-1$
		buf.append("   int veryLongName = 1;           \n"); //$NON-NLS-1$
		buf.append("   Test test( very   \n"); //$NON-NLS-1$

		String code = buf.toString();
		IFile cu = importFile("test.cpp", code); //$NON-NLS-1$

		ICompletionProposal[] results = getResults(cu, code.indexOf("very ") + 4); //$NON-NLS-1$

		assertEquals(1, results.length);
		assertEquals("veryLongName : int", results[0].getDisplayString()); //$NON-NLS-1$
	}

	public void testBug72824() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("class Strategy {                             \n"); //$NON-NLS-1$
		buf.append("public :                                     \n"); //$NON-NLS-1$
		buf.append("   enum _Ability { IDIOT, NORMAL, CHEAT } ;  \n"); //$NON-NLS-1$
		buf.append("   Strategy( _Ability a ) { }                \n"); //$NON-NLS-1$
		buf.append("   _Ability getAbility();                    \n"); //$NON-NLS-1$
		buf.append("};                                           \n"); //$NON-NLS-1$
		buf.append("int main(){                                  \n"); //$NON-NLS-1$

		String code = buf.toString();
		String c2 = code + "   Strategy *p[3] = { new Strategy( Str \n"; //$NON-NLS-1$

		IFile cu = importFile("strategy.cpp", c2); //$NON-NLS-1$

		ICompletionProposal[] results = getResults(cu, c2.indexOf("Str ") + 3); //$NON-NLS-1$
		assertEquals(1, results.length);
		assertEquals("Strategy", results[0].getDisplayString()); //$NON-NLS-1$

		c2 = code + "   Strategy *p[3] = { new Strategy( Strategy:: \n"; //$NON-NLS-1$

		cu = importFile("strategy.cpp", c2); //$NON-NLS-1$

		results = getResults(cu, c2.indexOf("::") + 2); //$NON-NLS-1$
		assertEquals(4, results.length);
		assertEquals("CHEAT", results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals("IDIOT", results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals("NORMAL", results[2].getDisplayString()); //$NON-NLS-1$
		// "_Ability" is here due to fix for bug 199598
		// Difficult to differentiate between declaration and expression context
		assertEquals("_Ability", results[3].getDisplayString()); //$NON-NLS-1$

		// in a method definition context, constructors and methods should be proposed

		c2 = code + "return 0;}\nStrategy::\n"; //$NON-NLS-1$

		cu = importFile("strategy.cpp", c2); //$NON-NLS-1$

		results = getResults(cu, c2.indexOf("::") + 2); //$NON-NLS-1$
		assertEquals(3, results.length);
		assertEquals("getAbility(void) : enum Strategy::_Ability", results[1].getDisplayString()); //$NON-NLS-1$
		assertEquals("Strategy(enum Strategy::_Ability a)", results[0].getDisplayString()); //$NON-NLS-1$
		assertEquals("_Ability", results[2].getDisplayString()); //$NON-NLS-1$
	}

	public void testBug72559() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("void foo(){               \n"); //$NON-NLS-1$
		buf.append("   int var;               \n"); //$NON-NLS-1$
		buf.append("   {                      \n"); //$NON-NLS-1$
		buf.append("      float var;          \n"); //$NON-NLS-1$
		buf.append("      v                   \n"); //$NON-NLS-1$
		buf.append("   }                      \n"); //$NON-NLS-1$
		buf.append("}                         \n"); //$NON-NLS-1$

		String code = buf.toString();
		IFile cu = importFile("t.cpp", code); //$NON-NLS-1$
		ICompletionProposal[] results = getResults(cu, code.indexOf("v ") + 1); //$NON-NLS-1$

		assertEquals(results.length, 3);
		assertEquals(results[0].getDisplayString(), "var : float"); //$NON-NLS-1$
		assertEquals(results[1].getDisplayString(), "virtual"); //$NON-NLS-1$
		assertEquals(results[2].getDisplayString(), "volatile"); //$NON-NLS-1$
	}
}
