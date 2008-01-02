/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)   - [187711] IViewLinker to be API that system view part calls when link with editor
 ********************************************************************************/
package org.eclipse.rse.ui;

import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.ui.IEditorPart;

public interface IViewLinker {
	
	/**
	 * System View part calls link when using Link With Editor.  Provider of action supplies this implementation.
	 * @param editor the active editor 
	 * @param systemView the view to link
	 */
	public void link(IEditorPart editor, SystemView systemView);
}
