/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Jun 24, 2003
 */
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;

/**
 * @author aniefer
 */
public class CElementLabels extends CElementBaseLabels {

	public static String getTextLabel(Object obj, int flags) {
		if (obj instanceof ICElement) {
			return getElementLabel((ICElement) obj, flags);
		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter= (IWorkbenchAdapter) ((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return wbadapter.getLabel(obj);
			}
		}
		return ""; //$NON-NLS-1$
	}
}
