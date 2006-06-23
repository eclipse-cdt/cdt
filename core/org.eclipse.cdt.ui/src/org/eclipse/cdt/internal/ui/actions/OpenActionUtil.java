/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class OpenActionUtil {
	
	private OpenActionUtil() {
		// no instance.
	}
		
	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element) throws CModelException, PartInitException {
		open(element, true);
	}
	
	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element, boolean activate) throws CModelException, PartInitException {
		IEditorPart part= EditorUtility.openInEditor(element, activate);
		if (element instanceof ICElement)
			EditorUtility.revealInEditor(part, (ICElement)element);
	}
	
	/**
	 * Filters out source references from the given code resolve results.
	 * A utility method that can be called by subclassers. 
	 */
	public static List filterResolveResults(ICElement[] codeResolveResults) {
		int nResults= codeResolveResults.length;
		List refs= new ArrayList(nResults);
		for (int i= 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference)
				refs.add(codeResolveResults[i]);
		}
		return refs;
	}
						
	/**
	 * Shows a dialog for resolving an ambigous C element.
	 * Utility method that can be called by subclassers.
	 */
	public static ICElement selectCElement(ICElement[] elements, Shell shell, String title, String message) {
		
		int nResults= elements.length;
		
		if (nResults == 0)
			return null;
		
		if (nResults == 1)
			return elements[0];
		
		int flags= CElementLabelProvider.SHOW_DEFAULT
						| CElementLabelProvider.SHOW_QUALIFIED;
//						| CElementLabelProvider.SHOW_ROOT;
						
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new CElementLabelProvider(flags));
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements);
		
		if (dialog.open() == Window.OK) {
			Object[] selection= dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults= selection.length;
				for (int i= 0; i < nResults; i++) {
					Object current= selection[i];
					if (current instanceof ICElement)
						return (ICElement) current;
				}
			}
		}		
		return null;
	}	
}
