/*******************************************************************************
 * Copyright (c) 2009, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

import org.eclipse.cdt.ui.tests.refactoring.extractfunction.ExtractFunctionRefactoringTest;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class RefactoringHistoryTest extends
		ExtractFunctionRefactoringTest {

	private TestSourceFile scriptFile;

	public RefactoringHistoryTest(String name,
			Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		scriptFile = fileMap.get(refactoringProperties.getProperty(
				"scriptFile", "refScript.xml"));

	}

	@Override
	protected void runTest() throws Throwable {
		String xmlSource = scriptFile.getSource();
		URI uri= URIUtil.toURI(project.getLocation());
		xmlSource = xmlSource.replaceAll("\\$\\$projectPath\\$\\$", uri.getPath());
		RefactoringHistory refHist = RefactoringHistoryService.getInstance()
				.readRefactoringHistory(
						new ByteArrayInputStream(xmlSource
								.getBytes()), 0);
		for (RefactoringDescriptorProxy proxy : refHist.getDescriptors()) {
			RefactoringStatus status = new RefactoringStatus();
			CRefactoring ref = (CRefactoring) proxy
					.requestDescriptor(new NullProgressMonitor())
					.createRefactoring(status);
			assertTrue(status.isOK());
			RefactoringStatus checkInitialConditions = ref.checkInitialConditions(NULL_PROGRESS_MONITOR);
			
			if(fatalError){
				assertConditionsFatalError(checkInitialConditions);
				return;
			}
			else{
				assertConditionsOk(checkInitialConditions);
				executeRefactoring(ref);
			}
		}
	}

}
