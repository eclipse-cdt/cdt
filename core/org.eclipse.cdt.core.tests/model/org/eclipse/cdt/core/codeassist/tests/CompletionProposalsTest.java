/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.codeassist.tests;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.ui.text.CCompletionProcessor;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompletionProposalsTest  extends TestCase{
	private final static long MAGIC_NUMBER = 1000;
	private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
		
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionProposalsTest.class.getName());
		suite.addTest(new CompletionProposalsTest("testCompletionProposals"));
		return suite;
	}		
		
	public CompletionProposalsTest(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
	
		fCProject= CProjectHelper.createCProject("TestProject1", "bin");
		headerFile = fCProject.getProject().getFile("CompletionProposalsTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "resources/cfiles/CompletionProposalsTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}

		CCorePlugin.getDefault().setUseNewParser(true);
		// use the new indexer
		
		IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.setEnabled(fCProject.getProject(),true);
		
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}	
			
	public void testCompletionProposals(){
		try{
			TranslationUnit tu = new TranslationUnit(fCProject, headerFile);
			Document document = new Document(tu.getBuffer().getContents());
			int pos = 399;
			int length = 0;
			CCompletionProcessor completionProcessor = new CCompletionProcessor(null);
			ICompletionProposal[] results = completionProcessor.evalProposals(document, pos, length, tu);
			try {
				Thread.sleep(MAGIC_NUMBER);
			} catch (InterruptedException e1) {
				fail( "Bogdan's hack did not suffice");
			}
			assertEquals(results.length, 9);
			for (int i = 0; i<results.length; i++){
				ICompletionProposal proposal = results[i];
				String displayString = proposal.getDisplayString();
				switch(i){
					case 0:
						assertEquals(displayString, "anotherField");
					break;	
					case 1:
						assertEquals(displayString, "aVariable");
					break;	
					case 2:
						assertEquals(displayString, "anotherMethod(int, char) void");
					break;	
					case 3:
						assertEquals(displayString, "aFunction(char, int) void");
					break;	
					case 4:
						assertEquals(displayString, "aClass");
					break;	
					case 5:
						assertEquals(displayString, "anotherClass");
					break;	
					case 6:
						assertEquals(displayString, "aStruct");
					break;	
					case 7:
						assertEquals(displayString, "aMacro");
					break;	
					case 8:
						assertEquals(displayString, "anEnumeration");
					break;	
				}
			}			 
		} catch(CModelException e){
			
		}
	}
}
