/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CHLabelProvider implements IStyledLabelProvider {

	public static String ICON_PATH = "$nl$/icons/obj16/container_obj.gif";

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof DslNode) {
			DslNode node = (DslNode) element;
			ICElement decl = node.getRepresentedDeclaration();

			if (decl != null) {
				StyledString label = new StyledString();
				label.append(decl.getElementName());
				if (node.getDslNodeName() != null) {
					return StyledCellLabelProvider.styleDecoratedString(node.getDslNodeName(),
							StyledString.DECORATIONS_STYLER, label);
				}
				return label;
			}
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof DslNode) {
			Image img = AbstractUIPlugin.imageDescriptorFromPlugin(CUIPlugin.PLUGIN_ID, ICON_PATH).createImage(); //$NON-NLS-1$
			return img;
		}
		return null;
	}

}
