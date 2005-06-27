/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatusEntry;
import org.eclipse.cdt.internal.ui.util.Strings;
import org.eclipse.cdt.internal.ui.CPluginImages;

public class RefactoringStatusEntryLabelProvider extends LabelProvider{
		public String getText(Object element){
			return Strings.removeNewLine(((RefactoringStatusEntry)element).getMessage());
		}
		public Image getImage(Object element){
			RefactoringStatusEntry entry= (RefactoringStatusEntry)element;
			if (entry.isFatalError())
				return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_FATAL);
			else if (entry.isError())
				return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
			else if (entry.isWarning())	
				return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_WARNING);
			else 
				return CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_INFO);
		}
}
