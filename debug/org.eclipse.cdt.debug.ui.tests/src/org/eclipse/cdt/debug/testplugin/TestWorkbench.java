/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.testplugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.IPath;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.internal.Workbench;

public class TestWorkbench extends Workbench {

	/**
	 * Run an event loop for the workbench.
	 */
	protected void runEventLoop() {
		// Dispatch all events.
		Display display = Display.getCurrent();
		while (true) {
			try {
				if (!display.readAndDispatch())
					break;
			} catch (Throwable e) {
				break;
			}
		}
		IPath location= CTestPlugin.getWorkspace().getRoot().getLocation();
		System.out.println("Workspace-location: " + location.toString());
				
		
		try {
			String[] args= getCommandLineArgs();
			if (args.length > 0) {
				Test test= getTest(args[0]);
				TestRunner.run(test);
			} else {
				System.out.println("TestWorkbench: Argument must be class name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		
		// Close the workbench.
		close();
	}
	
	public Test getTest(String className) throws Exception {
		Class testClass= getClass().getClassLoader().loadClass(className);

		Method suiteMethod= null;
		try {
			suiteMethod= testClass.getMethod(TestRunner.SUITE_METHODNAME, new Class[0]);
	 	} catch (Exception e) {
	 		// try to extract a test suite automatically	
			return new TestSuite(testClass);
		}
		try {
			return (Test) suiteMethod.invoke(null, new Class[0]); // static method
		} catch (InvocationTargetException e) {
			System.out.println("Failed to invoke suite():" + e.getTargetException().toString());
		} catch (IllegalAccessException e) {
			System.out.println("Failed to invoke suite():" + e.toString());
		}
		return null; 

	}
	
	
}