/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Dump the contents of the PDOM index to stdout (for when you need
 * a lo-fidelity debugging tool)
 */
public class PDOMPrettyPrinter implements IPDOMVisitor {
	StringBuffer indent = new StringBuffer();
	final String step = "   "; //$NON-NLS-1$
	
	public void leave(IPDOMNode node) throws CoreException {
		if(indent.length()>=step.length())
			indent.setLength(indent.length()-step.length());
	}

	public boolean visit(IPDOMNode node) throws CoreException {
		indent.append(step);
		System.out.println(indent+""+node);
		return true;
	}
}
