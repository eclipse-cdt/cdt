/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

public class ExternalSearchEditor extends CEditor {
	
	public static final String EDITOR_ID = "org.eclipse.cdt.ui.editor.ExternalSearchEditor";
	
	public ExternalSearchEditor(){
		super();
		setDocumentProvider(CUIPlugin.getDefault().getExternalSearchDocumentProvider());
	}
	
	public void editorContextMenuAboutToShow(IMenuManager menu) {
	  super.editorContextMenuAboutToShow(menu);
	  IContributionItem[] contrItem = menu.getItems();
	  for (int i=0; i<contrItem.length; i++){
	     if (contrItem[i] instanceof ActionContributionItem)
	     	((ActionContributionItem) contrItem[i]).getAction().setEnabled(false);
	  }
	}
	
}
