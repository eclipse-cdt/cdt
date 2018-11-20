/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
