/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.typehierarchy;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;

import org.eclipse.cdt.internal.ui.editor.CEditor;


public class TypeHierarchyAcrossProjectsTest extends TypeHierarchyBaseTest {
	
	private ICProject fCProject2;

	public TypeHierarchyAcrossProjectsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(TypeHierarchyAcrossProjectsTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		fCProject2= CProjectHelper.createCCProject("__thTest_2__", "bin", IPDOMManager.ID_FAST_INDEXER);
		IProjectDescription desc= fCProject2.getProject().getDescription();
		desc.setReferencedProjects(new IProject[]{fCProject.getProject()});
		fCProject2.getProject().setDescription(desc, new NullProgressMonitor());
		
		CCorePlugin.getIndexManager().reindex(fCProject2);
		fIndex= CCorePlugin.getIndexManager().getIndex(new ICProject[] {fCProject, fCProject2});
		TestScannerProvider.sIncludes= new String[]{fCProject.getProject().getLocation().toOSString(), fCProject2.getProject().getLocation().toOSString()};
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject2 != null) {
			CProjectHelper.delete(fCProject2);
		}
		super.tearDown();
	}

	// class Simple1 {
	// public:
	//    int field1;
	//    int method1();
	// };
	// class Simple2 : public Simple1 {
	// public:
	//    int field2;
	//    int method2();
	// };
	
	// #include "simpleHeader.h"
	// class Simple3 : public Simple2 {
	// public:
	//    int field3;
	//    int method3();
	// };
	// class Simple4 : public Simple1 {
	// public:
	//    int field4;
	//    int method4();
	// };
	public void testSimpleInheritanceAcross() throws Exception {
		StringBuffer[] content= getContentsForTest(2);
		String header= content[0].toString();
		String source = content[1].toString();
		IFile headerFile= createFile(fCProject.getProject(), "simpleHeader.h", header);
		IFile sourceFile= createFile(fCProject2.getProject(), "simple.cpp", source);
		waitForIndexer(fIndex, sourceFile, TypeHierarchyBaseTest.INDEXER_WAIT_TIME);
		
		CEditor editor= openEditor(sourceFile);
		Tree tree;
		TreeItem item1, item2, item3, item4;
		
		editor.selectAndReveal(source.indexOf("Simple1"), 1);
		openTypeHierarchy(editor);
		getHierarchyViewer().expandAll();
		tree= getHierarchyViewer().getTree();
		
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		item4= checkTreeNode(item1, 1, "Simple4");
		assertEquals(2, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());
		assertEquals(0, item4.getItemCount());
		checkMethodTable(new String[] {"field1 : int", "method1() : int"});

		
		editor.selectAndReveal(source.indexOf("Simple2"), 1);
		openTypeHierarchy(editor);
		tree= getHierarchyViewer().getTree();
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());
		checkMethodTable(new String[] {"field2 : int", "method2() : int"});

		
		editor.selectAndReveal(source.indexOf("Simple3"), 1);
		openTypeHierarchy(editor);
		tree= getHierarchyViewer().getTree();
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item2= checkTreeNode(item1, 0, "Simple2");
		assertEquals(1, item1.getItemCount());
		
		item3= checkTreeNode(item2, 0, "Simple3");
		assertEquals(1, item2.getItemCount());
		
		assertEquals(0, item3.getItemCount());
		checkMethodTable(new String[] {"field3 : int", "method3() : int"});

		
		editor.selectAndReveal(source.indexOf("Simple4"), 1);
		openTypeHierarchy(editor);
		tree= getHierarchyViewer().getTree();
		item1= checkTreeNode(tree, 0, "Simple1");
		assertEquals(1, tree.getItemCount());
		
		item4= checkTreeNode(item1, 0, "Simple4");
		assertEquals(1, item1.getItemCount());
		
		assertEquals(0, item4.getItemCount());
		checkMethodTable(new String[] {"field4 : int", "method4() : int"});
	}
}
