/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractAutotoolsHandler extends AbstractHandler {
	
	protected Object execute(ExecutionEvent event, InvokeAction a) throws ExecutionException {
		ISelection k = HandlerUtil.getCurrentSelection(event);
		if (!k.isEmpty() && k instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection)k).getFirstElement();
			IContainer container = getContainer(obj);
			if (container != null) {
				a.setSelectedContainer(container);
				a.run(null);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected IContainer getContainer(Object obj) {
		IContainer fContainer = null;

		if (obj instanceof Collection) {
			Collection<Object> c = (Collection<Object>)obj;
			Object[] objArray = c.toArray();
			if (objArray.length > 0)
				obj = objArray[0];
		}
		if (obj instanceof ICElement) {
			if ( obj instanceof ICContainer || obj instanceof ICProject) {
				fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
			} else {
				obj = ((ICElement)obj).getResource();
				if ( obj != null) {
					fContainer = ((IResource)obj).getParent();
				}
			}
		} else if (obj instanceof IResource) {
			if (obj instanceof IContainer) {
				fContainer = (IContainer) obj;
			} else {
				fContainer = ((IResource)obj).getParent();
			}
		} else {
			fContainer = null;
		}
		return fContainer;
	}

}
