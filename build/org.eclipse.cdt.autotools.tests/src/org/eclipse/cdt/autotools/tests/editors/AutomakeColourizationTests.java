/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests.editors;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.cdt.autotools.tests.AutotoolsTestsPlugin;
import org.eclipse.cdt.autotools.tests.ProjectTools;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeDocumentProvider;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeEditor;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakefileCodeScanner;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakefileSourceConfiguration;
import org.eclipse.cdt.internal.autotools.ui.preferences.ColorManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;


public class AutomakeColourizationTests extends TestCase {
	
	ProjectTools tools;
	private IProject project;
	private IFile makefileAmFile;
	
	static String makefileAmContents =
		"# This is a comment" + "\n" +
		"if CONDITION" + "\n" +
		"MACRO = case1" + "\n" +
		"else" + "\n" +
		"MACRO = case2" + "\n" +
		"$(MACRO)" + "\n" +
		"${MACRO}" + "\n" +
		"";
	private IWorkbench workbench;
	private AutomakefileCodeScanner codeScanner;
	
    protected void setUp() throws Exception {
        super.setUp();
        tools = new ProjectTools();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
        
        project = ProjectTools.createProject("testProjectACT");
        
        if(project == null) {
        	fail("Unable to create test project");
        }
        
        project.open(new NullProgressMonitor());
        
        Display.getDefault().syncExec(new Runnable() {

        	public void run() {
        		try {
        			makefileAmFile = tools.createFile(project, "Makefile.am", makefileAmContents);
        			workbench = AutotoolsTestsPlugin.getDefault().getWorkbench();

        			IEditorPart openEditor = org.eclipse.ui.ide.IDE.openEditor(workbench
        					.getActiveWorkbenchWindow().getActivePage(), makefileAmFile,
        					true);

        			AutomakeEditor automakeEditor = (AutomakeEditor) openEditor;
        			AutomakeDocumentProvider docProvider = automakeEditor.getAutomakefileDocumentProvider();
        			IDocument automakeDocument = docProvider.getDocument(openEditor.getEditorInput());
        			AutomakefileSourceConfiguration automakeSourceViewerConfig = automakeEditor.getAutomakeSourceViewerConfiguration();

        			ITypedRegion region = automakeDocument.getPartition(0);
        			codeScanner = automakeSourceViewerConfig.getAutomakeCodeScanner();
        			codeScanner.setRange(automakeDocument, region.getOffset(), region.getLength());
        		} catch (Exception e) {
        			fail();
        		}
        	}

        });

    }
    
    IToken getNextToken() {
    	return codeScanner.nextToken();
    }
	
	public void testAutomakeEditorColourization() throws Exception {
		// # This is a comment
		IToken token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 20);
		assertEquals(codeScanner.getTokenOffset(), 0);

		Token token = (Token) token0;
		TextAttribute ta = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_COMMENT_RGB, ((Color) ta.getForeground()).getRGB());

		// if CONDITION
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 2);
		assertEquals(codeScanner.getTokenOffset(), 20);

		token = (Token) token0;
		TextAttribute attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_KEYWORD_RGB, ((Color) attribute.getForeground()).getRGB());

		// blank space between "if" and "CONDITION"
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 1);
		assertEquals(codeScanner.getTokenOffset(), 22);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertNull(attribute);

		// if CONDITION
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 9);
		assertEquals(codeScanner.getTokenOffset(), 23);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_DEFAULT_RGB, ((Color) attribute.getForeground()).getRGB());

		// line break
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 1);
		assertEquals(codeScanner.getTokenOffset(), 32);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertNull(attribute);

		// MACRO = case1
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 14);
		assertEquals(codeScanner.getTokenOffset(), 33);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_MACRO_DEF_RGB, ((Color) attribute.getForeground()).getRGB());

		// else
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 4);
		assertEquals(codeScanner.getTokenOffset(), 47);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_KEYWORD_RGB, ((Color) attribute.getForeground()).getRGB());

		// line break
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 1);
		assertEquals(codeScanner.getTokenOffset(), 51);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertNull(attribute);

		// MACRO = case2
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 14);
		assertEquals(codeScanner.getTokenOffset(), 52);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_MACRO_DEF_RGB, ((Color) attribute.getForeground()).getRGB());

		// $(MACRO)
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 8);
		assertEquals(codeScanner.getTokenOffset(), 66);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_MACRO_REF_RGB, ((Color) attribute.getForeground()).getRGB());

		// line break
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 1);
		assertEquals(codeScanner.getTokenOffset(), 74);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertNull(attribute);
		
		// ${MACRO}
		token0 = getNextToken();
		assertTrue(token0 instanceof Token);
		assertEquals(codeScanner.getTokenLength(), 8);
		assertEquals(codeScanner.getTokenOffset(), 75);

		token = (Token) token0;
		attribute = (TextAttribute) token.getData();
		assertEquals(ColorManager.MAKE_MACRO_REF_RGB, ((Color) attribute.getForeground()).getRGB());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, false, ProjectTools.getMonitor());
	}
}
