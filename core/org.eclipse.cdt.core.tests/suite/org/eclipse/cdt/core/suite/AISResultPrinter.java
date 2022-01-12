/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.suite;

import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.textui.ResultPrinter;

/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AISResultPrinter extends ResultPrinter {

	/**
	 * @param writer
	 */
	public AISResultPrinter(PrintStream writer) {
		super(writer);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
	 */
	@Override
	public void addFailure(Test test, AssertionFailedError t) {
		super.addFailure(test, t);
		getWriter().print("---> ");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
	 */
	@Override
	public void addError(Test test, Throwable t) {
		super.addError(test, t);
		getWriter().print("---> ");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestListener#startTest(junit.framework.Test)
	 */
	@Override
	public void startTest(Test test) {
		getWriter().print(".");
	}

}
