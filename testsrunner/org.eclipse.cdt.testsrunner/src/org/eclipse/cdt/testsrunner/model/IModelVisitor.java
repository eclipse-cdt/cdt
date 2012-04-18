/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

/**
 * Interface to the visitor through the test model.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IModelVisitor {

	public void visit(ITestSuite testSuite);
	
	public void leave(ITestSuite testSuite);
	
	public void visit(ITestCase testCase);
	
	public void leave(ITestCase testCase);
	
	public void visit(ITestMessage testMessage);
	
	public void leave(ITestMessage testMessage);
	
}
