/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class MakeNavLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IMakeTarget) {
			return ((IMakeTarget) element).getName();
		} else if (element instanceof MakeTargetsContainer) {
			return MakeUIPlugin.getResourceString("BuildTargets.name"); //$NON-NLS-1$
		} else {
			return null;
		}
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IMakeTarget) {
			return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_TARGET);
		} else if (element instanceof MakeTargetsContainer) {
			return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_TARGET);
		} else {
			return null;
		}
	}

}
