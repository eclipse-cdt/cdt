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
import java.util.Iterator;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.IBuffer;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.testplugin.CProjectHelper;
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
		TestSuite suite= new TestSuite();
		suite.addTest(new ElementDeltaTests("testElementDeltas"));
		return suite;
	}		
	
	public ElementDeltaTests(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();

		fCProject= CProjectHelper.createCProject("TestProject1", "bin");
		//Path filePath = new Path(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+ fCProject.getPath().toString()+ "/WorkingCopyTest.h");
		headerFile = fCProject.getProject().getFile("WorkingCopyTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "model/org/eclipse/cdt/core/model/tests/resources/cfiles/WorkingCopyTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		// register with the model manager to listen to delta changes
		CModelManager.getDefault().addElementChangedListener(this);
		addedElements = new Vector(10);
		removedElements = new Vector(10);
		changedElements = new Vector(100);
		CCorePlugin.getDefault().setUseNewParser(true);
	}

	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}	
		
		
	public void testElementDeltas() throws Exception {
		ITranslationUnit tu = new TranslationUnit(fCProject, headerFile);		
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
		assertAddedElement(ICElement.C_CLASS, "Hello");
		
		// add the field x		
		wcBuf.setContents ("\n class Hello{\n int x; \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertAddedElement(ICElement.C_FIELD, "x");
		
		// add the method setValue
		wcBuf.setContents ("\n class Hello{\n int x; \n void setValue(int val); \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertAddedElement(ICElement.C_METHOD, "setValue");
		
		// rename x to y
		// this is not a change, this is add and remove
		wcBuf.setContents ("\n class Hello{\n int y; \n void setValue(int val); \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertAddedElement(ICElement.C_FIELD, "y");
		assertRemovedElement(ICElement.C_FIELD, "x");

		// remove the method
		wcBuf.setContents ("\n class Hello{\n String y; \n};");
		wc.reconcile();		
		wc.commit(true, monitor);
		assertRemovedElement(ICElement.C_METHOD, "setValue");
				
		// remove the field		
		wcBuf.setContents ("\n class Hello{ \n};");
		wc.reconcile();
		wc.commit(true, monitor);
		assertRemovedElement(ICElement.C_FIELD, "y");

		// remove the class
		wcBuf.setContents ("");
		wc.reconcile();
		wc.commit(true, monitor);
		assertRemovedElement(ICElement.C_CLASS, "Hello");

		wc.destroy();
		assertFalse(wc.exists());		
	}
	
	public void assertAddedElement(int elementType, String elementName){
		System.out.println("Printing Added List: ");
		if(!isElementInList(elementType, elementName, addedElements))
			fail("Element NOT found in Added list");
	}
	public void assertRemovedElement(int elementType, String elementName){
		System.out.println("Printing Removed List: ");
		if(!isElementInList(elementType, elementName, removedElements))
			fail("Element NOT found in Removed list");
	}
	public void assertChangedElement(int elementType, String elementName){
		System.out.println("Printing Changed List: ");
		if(!isElementInList(elementType, elementName, changedElements))
			fail("Element NOT found in Changed list");
	}
	public boolean isElementInList(int elementType, String elementName, Vector elementList) {
		boolean found = false;
		Iterator i = elementList.iterator();
		while( i.hasNext()){
			ICElement element = (ICElement)i.next();
			
			System.out.print("ElementName " + element.getElementName());
			System.out.println("  ElementType " + element.getElementType());

			if ((element.getElementName().equals(elementName)) && 
				(element.getElementType() == elementType)){
					// return true;
					// just to print the whole list
					found = true;
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
			
			processDelta(event.getDelta());
		} catch(CModelException e) {
		}		
	}

	protected void processDelta(ICElementDelta delta) throws CModelException {
		// check the delta elements
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();
		
		System.out.print("Processing " + element);
		// handle open and closing of a solution or project
		if ((flags & ICElementDelta.F_CLOSED) != 0) {
			System.out.println("  Element Closed");
		}
		if ((flags & ICElementDelta.F_OPENED) != 0) {
			System.out.println("  Element Opened");
		}

		if (kind == ICElementDelta.REMOVED) {
			System.out.println("  Element Removed");
			removedElements.add(element);			
		}

		if (kind == ICElementDelta.ADDED) {
			System.out.println("  Element Added");
			addedElements.add(element);			
		}

		if (kind == ICElementDelta.CHANGED) {
			System.out.println("  Element Changed");
			changedElements.add(element);
				
			if (flags == ICElementDelta.F_MODIFIERS)	{	
				System.out.println("  Modifiers changed");	
			}
			if (flags == ICElementDelta.F_CONTENT)	{	
				System.out.println("  Contents changed");	
			}
			if (flags == ICElementDelta.F_CHILDREN)	{	
				System.out.println("  Children changed");	
			}
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}
		
}
