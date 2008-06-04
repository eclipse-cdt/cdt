/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)   - [187711] IViewLinker to be API that system view part calls when link with editor
 *******************************************************************************/
package org.eclipse.rse.ui.view;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @since 3.0
 */
public interface IViewLinker {

	/**
	 * System View part calls link when using Link With Editor.  Provider of action supplies this implementation.
	 * @param editor the active editor
	 * @param systemTree the view to link
	 */
	public void linkEditorToView(IEditorPart editor, ISystemTree systemTree);


	/**
	 * System View part calls link when using Link With Editor.  Provider of action supplies this implementation.
	 * @param editor the active editor
	 * @param page the active workbench page
	 */
	public void linkViewToEditor(Object remoteObject, IWorkbenchPage page);
}
