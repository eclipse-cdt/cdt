/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * @author Emanuel Graf IFS
 */
public class GetterSetterLabelProvider extends LabelProvider {
	@Override
	public Image getImage(Object element) {
		if (element instanceof AccessorDescriptor) {
			return CElementImageProvider.getMethodImageDescriptor(ASTAccessVisibility.PUBLIC).createImage();
		}
		return null;
	}
}
