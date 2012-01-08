/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Tests for CModel identifier API.
 * 
 * @see ICElement#getHandleIdentifier()
 * @see CoreModel#create(String)
 *
 * @since 5.0
 */
public class CModelIdentifierTests extends BaseTestCase {

	public static Test suite() {
		return BaseTestCase.suite(CModelIdentifierTests.class);
	}

	private ICProject fCProject;
	private IFile fHeaderFile;

	@Override
	protected void setUp() throws Exception {
		// reusing project setup from CModelElementsTests
		NullProgressMonitor monitor= new NullProgressMonitor();
		fCProject= CProjectHelper.createCCProject("CModelIdentifierTests", "bin", IPDOMManager.ID_FAST_INDEXER);
		fHeaderFile = fCProject.getProject().getFile("CModelIdentifierTests.h");
		if (!fHeaderFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/CModelElementsTestStart.h"))); 
				fHeaderFile.create(fileIn,false, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor());
	}

	@Override
	protected void tearDown() {
		CProjectHelper.delete(fCProject);
	}	

	public void testIdentifierConsistency() throws Exception {
		ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(fHeaderFile);

		final String cModelIdentifier= tu.getCModel().getHandleIdentifier();
		assertNotNull(cModelIdentifier);
		assertEquals(tu.getCModel(), CoreModel.create(cModelIdentifier));
		
		final String cProjectIdentifier= tu.getCProject().getHandleIdentifier();
		assertNotNull(cProjectIdentifier);
		assertEquals(tu.getCProject(), CoreModel.create(cProjectIdentifier));

		final String tUnitIdentifier= tu.getHandleIdentifier();
		assertNotNull(tUnitIdentifier);
		assertEquals(tu, CoreModel.create(tUnitIdentifier));

		final List elements= new ArrayList();
		final List identifiers= new ArrayList();
		ICElementVisitor visitor= new ICElementVisitor() {
			@Override
			public boolean visit(ICElement element) throws CoreException {
				elements.add(element);
				identifiers.add(element.getHandleIdentifier());
				return true;
			}};
		tu.accept(visitor);
		
		assertEquals(elements.size(), identifiers.size());
		int size= elements.size();
		for (int i = 0; i < size; i++) {
			ICElement expected= (ICElement) elements.get(i);
			String identifier= (String) identifiers.get(i);
			assertNotNull("Could not create identifier for element: "+ expected, identifier);
			ICElement actual= CoreModel.create(identifier);
			assertNotNull("Cannot create element '" + expected + "' from identifier: " + identifier, actual);
			assertEquals(expected.getElementName(), actual.getElementName());
			assertEquals(expected.getElementType(), actual.getElementType());
			assertEquals(expected, actual);
		}
	}
}
