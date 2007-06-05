package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsImageIds;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for compile commands in the work with compile commands
 *  dialog.
 */
public class SystemCompileCommandLabelProvider extends LabelProvider {
	
	Image _compileCommandImage = null;
	
	/**
	 * Constructor
	 */
	public SystemCompileCommandLabelProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof SystemCompileCommand)
			return ((SystemCompileCommand) element).getLabel();
		else if (element != null)
			return element.toString();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image result = null;
		if (element instanceof SystemCompileCommand) {
			if (_compileCommandImage == null || _compileCommandImage.isDisposed()) {
				ImageDescriptor id = Activator.getDefault().getImageDescriptor(IUserActionsImageIds.COMPILE_1);
				_compileCommandImage = id.createImage();
			}
			result = _compileCommandImage;
		}
		return result;
	}
	
}
