/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.builder.CodanBuilder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RunCodeAnalysis implements IObjectActionDelegate {
	private ISelection sel;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}

	public void run(IAction action) {
		for (Iterator iterator = ((IStructuredSelection) sel).iterator(); iterator
				.hasNext();) {
			Object o = iterator.next();
			if (o instanceof IResource) {
				IResource res = (IResource) o;
				try {
					res.accept(new CodanBuilder().new CodanResourceVisitor());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.sel = selection;
	}
}
