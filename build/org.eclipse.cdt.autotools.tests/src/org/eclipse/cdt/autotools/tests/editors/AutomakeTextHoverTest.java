/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.cdt.autotools.tests.AutotoolsTestsPlugin;
import org.eclipse.cdt.autotools.tests.ProjectTools;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeDocumentProvider;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeEditor;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeTextHover;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakefileSourceConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;


public class AutomakeTextHoverTest extends TestCase {

	ProjectTools tools;
	private IProject project;
	private IFile makefileAmFile;
	private AutomakeTextHover textHover;
	private AutomakeEditor automakeEditor;
	private IDocument automakeDocument;

	static String makefileAmContents =
			// There are 6 characters from line beginning to
			// the space after the echo
			// ie. '\techo ' == 6 characters
			"MACRO = case1" + "\n" +
			// 14
			"target1:" + "\n" +
			// 23 (before the tab)
			// 32 is after the M in MACRO
			"\t" + "echo $(MACRO)" + "\n" +
			// 38 (before the tab)
			"\t" + "echo $@" + "\n" +
			"\n" +
			// 48
			"target2: target1" + "\n" +
			// 65 (before the tab)
			"\t" + "echo ${MACRO}" + "\n" +
			// 80 (before the tab)
			"\t" + "echo $@" + "\n" +
			// 89 (before the tab)
			"\t" + "echo $<" + "\n" +
			"\n" +
			// 99
			"target3: target1 target2" + "\n" +
			// 124 (before the tab)
			"\t" + "echo $?" + "\n" +
			"";
	private IWorkbench workbench;

	protected void setUp() throws Exception {
		super.setUp();
		tools = new ProjectTools();
		if (!ProjectTools.setup())
			fail("could not perform basic project workspace setup");

		project = ProjectTools.createProject("testProjectATHT");

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

					automakeEditor = (AutomakeEditor) openEditor;
					AutomakeDocumentProvider docProvider = automakeEditor.getAutomakefileDocumentProvider();
					automakeDocument = docProvider.getDocument(openEditor.getEditorInput());
					AutomakefileSourceConfiguration automakeSourceViewerConfig = automakeEditor.getAutomakeSourceViewerConfiguration();
					textHover = (AutomakeTextHover) automakeSourceViewerConfig.getTextHover(null, "");
				} catch (Exception e) {
					fail();
				}
			}

		});
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		project.delete(true, false, ProjectTools.getMonitor());
	}

	public void testGetHoverInfoTargetName1() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 45);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				// hover between the $ and the @ in target1
				assertEquals(44, hoverRegion.getOffset());
				assertEquals(2, hoverRegion.getLength());
				try {
					assertEquals("$@", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("target1", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

	public void testGetHoverInfoTargetName2() {
		// hover between the $ and the @ in target2
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 87);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				assertEquals(86, hoverRegion.getOffset());
				assertEquals(2, hoverRegion.getLength());
				try {
					assertEquals("$@", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("target2", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

	public void testGetHoverInfoForTargetDependency() {
		// hover between the $ and the < in target2
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 96);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				assertEquals(95, hoverRegion.getOffset());
				assertEquals(2, hoverRegion.getLength());
				try {
					assertEquals("$<", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("target1", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

	public void testGetHoverInfoForTargetDependencies() {
		// hover between the $ and the ? in target3
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 131);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				assertEquals(130, hoverRegion.getOffset());
				assertEquals(2, hoverRegion.getLength());
				try {
					assertEquals("$?", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("target1 target2", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

	public void testGetHoverForMacro1() {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 32);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				// hover between the M and the A in the first $(MACRO) reference
				assertEquals(31, hoverRegion.getOffset());
				assertEquals(5, hoverRegion.getLength());
				try {
					assertEquals("MACRO", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("case1", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

	public void testGetHoverForMacro2() {
		// hover between the M and the A in the ${MACRO} reference in target2
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				IRegion hoverRegion = textHover.getHoverRegion(automakeEditor.getAutomakeSourceViewer(), 74);
				if (hoverRegion == null)
					fail("Null hoverRegion");
				assertEquals(73, hoverRegion.getOffset());
				assertEquals(5, hoverRegion.getLength());
				try {
					assertEquals("MACRO", automakeDocument.get(hoverRegion.getOffset(),
							hoverRegion.getLength()));
				} catch (BadLocationException e) {
					fail("BadLocationException");
					e.printStackTrace();
				}
				assertEquals("case1", textHover.getHoverInfo(
						automakeEditor.getAutomakeSourceViewer(), hoverRegion));
			}
		});
	}

}
