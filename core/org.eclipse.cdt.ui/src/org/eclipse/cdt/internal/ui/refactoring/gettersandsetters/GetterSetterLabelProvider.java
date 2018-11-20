/*******************************************************************************
 * Copyright (c) 2009, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

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
