package org.eclipse.cdt.core.model.tests;


/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.CTestPlugin;
import org.eclipse.cdt.testplugin.TestPluginLauncher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Class for testing the C Element Delta Builder. 
 */
public class ElementDeltaTests extends TestCase implements IElementChangedListener {
	private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
	private Vector addedElements;
	private Vector removedElements;
	private Vector changedElements;
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), WorkingCopyTests.class, args);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(ElementDeltaTests.class.getName());
		suite.addTest(new ElementDeltaTests("testElementDeltas"));
		return suite;
	}		
	
	public ElementDeltaTests(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();

		fCProject= CProjectHelper.createCCProject("TestProject1", "bin");
		//Path filePath = new Path(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+ fCProject.getPath().toString()+ "/WorkingCopyTest.h");
		headerFile = fCProject.getProject().getFile("WorkingCopyTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/WorkingCopyTestStart.h"))); 
				headerFile.create(fileIn,false, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		// register with the model manager to listen to delta changes
		CModelManager.getDefault().addElementChangedListener(this);
		addedElements = new Vector(10);
		removedElements = new Vector(10);
		changedElements = new Vector(20);
	}

	protected void tearDown()  {
		  CProjectHelper.delete(fCProject);
	}	
		
		
	public void testElementDeltas() throws Exception {
		//ITranslationUnit tu = new TranslationUnit(fCProject, headerFile);
		ICElement celement = CoreModel.getDefault().create(headerFile);
		ITranslationUnit tu = null;
		if (celement instanceof ITranslationUnit) {
			tu = (ITranslationUnit)celement;
		}
		assertNotNull (tu);
		IWorkingCopy wc = tu.getWorkingCopy();
		assertNotNull (wc);
		assertNotNull (wc.getBuffer());
		assertTrue (wc.exists());
		
		// add the class Hello
		IBuffer wcBuf = wc.getBuffer();
		wcBuf.setContents ("\n class Hello{ \n};");
		wc.reconcile();
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertAddedElement(ICElement.C_CLASS, "Hello");
		assertRemovedElement(ICElement.C_INCLUDE, "stdio.h");
		assertEmptyDelta();
		
		// add the field x		
		wcBuf.setContents ("\n class Hello{\n int x; \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertChangedElement(ICElement.C_CLASS, "Hello");
		assertAddedElement(ICElement.C_FIELD, "x");
		assertEmptyDelta();
		
		// add the method setValue
		wcBuf.setContents ("\n class Hello{\n int x; \n void setValue(int val); \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertChangedElement(ICElement.C_CLASS, "Hello");
		assertAddedElement(ICElement.C_METHOD_DECLARATION, "setValue");
		assertEmptyDelta();
		
		// rename x to y
		// this is not a change, this is add and remove
		wcBuf.setContents ("\n class Hello{\n int y; \n void setValue(int val); \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertChangedElement(ICElement.C_CLASS, "Hello");
		assertAddedElement(ICElement.C_FIELD, "y");
		assertRemovedElement(ICElement.C_FIELD, "x");
		assertEmptyDelta();

		// remove the method
		wcBuf.setContents ("\n class Hello{\n String y; \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertChangedElement(ICElement.C_CLASS, "Hello");
		assertChangedElement(ICElement.C_FIELD, "y");
		assertRemovedElement(ICElement.C_METHOD_DECLARATION, "setValue");
		assertEmptyDelta();
				
		// remove the field		
		wcBuf.setContents ("\n class Hello{ \n};");
		wc.reconcile();
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertChangedElement(ICElement.C_CLASS, "Hello");
		assertRemovedElement(ICElement.C_FIELD, "y");
		assertEmptyDelta();

		// remove the class
		wcBuf.setContents ("");
		wc.reconcile();
		wc.commit(true, monitor);
		assertChangedElement(ICElement.C_MODEL, "");
		assertChangedElement(ICElement.C_PROJECT, "TestProject1");
		assertChangedElement(ICElement.C_CCONTAINER, "TestProject1");
		assertChangedElement(ICElement.C_UNIT, "WorkingCopyTest.h");
		assertRemovedElement(ICElement.C_CLASS, "Hello");
		assertEmptyDelta();

		wc.destroy();
		assertFalse(wc.exists());		
	}
	
	public void assertAddedElement(int elementType, String elementName){
		if(!isElementInList(elementType, elementName, addedElements))
			fail("Element NOT found in Added list");
	}
	public void assertRemovedElement(int elementType, String elementName){
		if(!isElementInList(elementType, elementName, removedElements))
			fail("Element NOT found in Removed list");
	}
	public void assertChangedElement(int elementType, String elementName){
		if(!isElementInList(elementType, elementName, changedElements))
			fail("Element NOT found in Changed list");
	}
	public void assertEmptyDelta() {
		assertTrue(addedElements.isEmpty());
		assertTrue(removedElements.isEmpty());
		assertTrue(changedElements.isEmpty());
	}
	public boolean isElementInList(int elementType, String elementName, Vector elementList) {
		boolean found = false;
		Iterator i = elementList.iterator();
		while( i.hasNext()){
			ICElement element = (ICElement)i.next();

			if ((element.getElementName().equals(elementName)) && 
				(element.getElementType() == elementType)){
					// return true;
					// just to print the whole list
					found = true;
					// Remove the element
					elementList.remove(element);
					break;
				}
		}
		//return false;
		return found;						
	}
	
	public void elementChanged(ElementChangedEvent event){
		try {
			addedElements.clear();
			removedElements.clear();
			changedElements.clear();
			ICElementDelta delta = event.getDelta();
			processDelta(delta);
		} catch(CModelException e) {
		}		
	}

	protected void processDelta(ICElementDelta delta) throws CModelException {
		// check the delta elements
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();
		
		// handle open and closing of a solution or project
		if ((flags & ICElementDelta.F_CLOSED) != 0) {
		}
		if ((flags & ICElementDelta.F_OPENED) != 0) {
		}
		if (kind == ICElementDelta.REMOVED) {
			removedElements.add(element);			
		}
		if (kind == ICElementDelta.ADDED) {
			addedElements.add(element);			
		}
		if (kind == ICElementDelta.CHANGED) {
			changedElements.add(element);
				
			if (flags == ICElementDelta.F_MODIFIERS)	{	
			}
			if (flags == ICElementDelta.F_CONTENT)	{	
			}
			if (flags == ICElementDelta.F_CHILDREN)	{	
			}
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}
	
}
