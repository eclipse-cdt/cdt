/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.IChangeExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A default implementation of <code>IChangeExceptionHandler</code> which
 * always aborts an change if an exception is caught.
 */
public class AbortChangeExceptionHandler implements IChangeExceptionHandler {
	
	public void handle(ChangeContext context, IChange change, Exception e) {
		CUIPlugin.getDefault().log(e);
		throw new ChangeAbortException(e);
	}
}
