package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for our user actions and named types tree views
 */
public class SystemUDBaseTreeViewLabelProvider extends LabelProvider {
	private SystemUDBaseManager docManager;

	/**
	 * Constructor
	 */
	public SystemUDBaseTreeViewLabelProvider(SystemUDBaseManager docManager) {
		super();
		this.docManager = docManager;
	}

	/**
	 * Override of parent so we can supply an image, if we desire.
	 */
	public Image getImage(Object element) {
		if (element instanceof SystemUDTreeViewNewItem) {
			if (!((SystemUDTreeViewNewItem) element).isExecutable()) {
				//System.out.println("Calling docManager.getNewImage...");
				return docManager.getNewImage();
			} else {
				//System.out.println("Calling actionss.getDomainNewImage...");
				if (!docManager.isUserActionsManager())
					return docManager.getActionSubSystem().getDomainNewTypeImage(((SystemUDTreeViewNewItem) element).getDomain());
				else
					return docManager.getActionSubSystem().getDomainNewImage(((SystemUDTreeViewNewItem) element).getDomain());
			}
		} else if (element instanceof SystemXMLElementWrapper) {
			if (((SystemXMLElementWrapper) element).isDomain())
				return docManager.getActionSubSystem().getDomainImage(((SystemXMLElementWrapper) element).getDomain());
			else
				return ((SystemXMLElementWrapper) element).getImage();
		}
		return null;
	}
}
