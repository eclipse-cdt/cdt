/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.make.scannerdiscovery;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCPerFileBOPConsoleParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class GCCPerFileBOPConsoleParserTests extends BaseBOPConsoleParserTests {
	private final static IMarkerGenerator MARKER_GENERATOR= new IMarkerGenerator() {
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		}
		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		}
	};

	public static TestSuite suite() {
		return suite(GCCPerFileBOPConsoleParserTests.class);
	}

	private ICProject fCProject;

	public GCCPerFileBOPConsoleParserTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("perfilescdtest", null);
		fOutputParser= new GCCPerFileBOPConsoleParser();
		final IProject project = fCProject.getProject();
		fOutputParser.startup(project, project.getLocation(), fCollector, MARKER_GENERATOR);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (fOutputParser != null) {
			fOutputParser.shutdown();
		}
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
	}
}
