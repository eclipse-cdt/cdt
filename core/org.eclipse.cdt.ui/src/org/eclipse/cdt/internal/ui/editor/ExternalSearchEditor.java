/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
 IBM Rational Software - Initial Contribution
**********************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

public class ExternalSearchEditor extends CEditor {
	
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
