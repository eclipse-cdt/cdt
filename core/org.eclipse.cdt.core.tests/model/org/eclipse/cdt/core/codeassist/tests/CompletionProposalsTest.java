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
import org.eclipse.core.internal.resources.ResourceException;
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
	private IFile bodyFile;
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
		bodyFile = fCProject.getProject().getFile("CompletionProposalsTestStart.cpp");
		headerFile = fCProject.getProject().getFile("CompletionProposalsTestStart.h");
		if ((!headerFile.exists()) || (!bodyFile.exists())) {
			try{
				FileInputStream bodyFileIn = new FileInputStream(pluginRoot+ "resources/cfiles/CompletionProposalsTestStart.cpp"); 
				bodyFile.create(bodyFileIn,false, monitor);        
				FileInputStream headerFileIn = new FileInputStream(pluginRoot+ "resources/cfiles/CompletionProposalsTestStart.h"); 
				headerFile.create(headerFileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}

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
	
	protected void tearDown() {
		try{
		  CProjectHelper.delete(fCProject);
		} 
		catch (ResourceException e) {} 
		catch (CoreException e) {} 
	}	
			
	public void testCompletionProposals(){
		try{
			TranslationUnit headerTu = new TranslationUnit(fCProject, headerFile);
			TranslationUnit tu = new TranslationUnit(fCProject, bodyFile);
			String buffer = tu.getBuffer().getContents();
			Document document = new Document(buffer);
			int pos = buffer.indexOf(" a ") + 2;
			int length = 0;
			CCompletionProcessor completionProcessor = new CCompletionProcessor(null);
			ICompletionProposal[] results = completionProcessor.evalProposals(document, pos, length, tu);
			try {
				Thread.sleep(MAGIC_NUMBER);
			} catch (InterruptedException e1) {
				fail( "Bogdan's hack did not suffice");
			}
			assertEquals(results.length, 7);
			for (int i = 0; i<results.length; i++){
				ICompletionProposal proposal = results[i];
				String displayString = proposal.getDisplayString();
				switch(i){
					case 0:
						assertEquals(displayString, "aVariable");
					break;	
					case 1:
						assertEquals(displayString, "aFunction() bool");
					break;	
					case 2:
						assertEquals(displayString, "aClass");
					break;	
					case 3:
						assertEquals(displayString, "anotherClass");
					break;	
					case 4:
						assertEquals(displayString, "AStruct");
					break;	
					case 5:
						assertEquals(displayString, "AMacro");
					break;	
					case 6:
						assertEquals(displayString, "anEnumeration");
					break;	
				}
			}			 
		} catch(CModelException e){
			
		}
	}
}
