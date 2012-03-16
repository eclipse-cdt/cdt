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
import org.eclipse.cdt.autotools.tests.AutotoolsTestsPlugin;
import org.eclipse.cdt.autotools.tests.ProjectTools;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.AutomakeEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;


public class AutomakeEditorTests extends TestCase {
	
	ProjectTools tools;
	private IProject project;
	
    protected void setUp() throws Exception {
        super.setUp();
        tools = new ProjectTools();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
    }
	  
	public void testAutomakeEditorAssociation() throws Exception {
		project = ProjectTools.createProject("testProjectAET");
		
		if(project == null) {
            fail("Unable to create test project");
        }
		
		project.open(new NullProgressMonitor());
		
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				try {
					IFile makefileAmFile = tools.createFile(project, "Makefile.am", "");
					assertTrue(makefileAmFile.exists());

					IWorkbench workbench = AutotoolsTestsPlugin.getDefault().getWorkbench();

					IEditorPart openEditor = org.eclipse.ui.ide.IDE.openEditor(workbench
							.getActiveWorkbenchWindow().getActivePage(), makefileAmFile,
							true);
					assertTrue(openEditor instanceof AutomakeEditor);
				} catch (Exception e) {
					fail();
				}
			}

		});

		project.delete(true, false, ProjectTools.getMonitor());
	}
}
