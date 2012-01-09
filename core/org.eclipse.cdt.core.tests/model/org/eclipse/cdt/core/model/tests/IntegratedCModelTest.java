/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 3, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author bnicolle
 *
 */
public abstract class IntegratedCModelTest extends TestCase {

	private ICProject fCProject;
	private IFile sourceFile;
	private NullProgressMonitor monitor;
	private boolean structuralParse = false;

	/**
	 * 
	 */
	public IntegratedCModelTest() {
		super();
	}

	/**
	 * @param name
	 */
	public IntegratedCModelTest(String name) {
		super(name);
	}

	/**
	 * @return the subdirectory (from the plugin root) containing the required
	 *         test sourcefile (plus a trailing slash)
	 */
	abstract public String getSourcefileSubdir();

	/**
	 * @return the name of the test source-file
	 */
	abstract public String getSourcefileResource();

	@Override
	public void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		fCProject= CProjectHelper.createCCProject("TestProject1", "bin", IPDOMManager.ID_FAST_INDEXER);
		sourceFile = fCProject.getProject().getFile( getSourcefileResource() );
		if (!sourceFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path(getSourcefileSubdir() + getSourcefileResource()))); 
				sourceFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		CCorePlugin.getIndexManager().joinIndexer(2000, new NullProgressMonitor());
	}

	@Override
	protected void tearDown() {
		CProjectHelper.delete(fCProject);
	}	

	protected ITranslationUnit getTU() throws CModelException {
		ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(sourceFile);
		CCorePlugin.getDefault().setStructuralParseMode(isStructuralParse());
		// parse the translation unit to get the elements tree		
		// Force the parsing now to do this in the right ParseMode.
		tu.close();
		tu.open(new NullProgressMonitor());
		CCorePlugin.getDefault().setStructuralParseMode(false);
		return tu;
	}
	/**
	 * @return Returns the structuralParse.
	 */
	public boolean isStructuralParse() {
		return structuralParse;
	}
	/**
	 * @param structuralParse The structuralParse to set.
	 */
	public void setStructuralParse(boolean structuralParse) {
		this.structuralParse = structuralParse;
	}

}
