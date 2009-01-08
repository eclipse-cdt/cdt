/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - extended implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport;

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.PlatformUI;

/**
 * An check box action that allows the word wrap property to be set, determining if the detail pane
 * should wrap text.
 */
public class DetailPaneWordWrapAction extends Action {

	ITextViewer fTextViewer;
	
	public DetailPaneWordWrapAction(ITextViewer textViewer) {
		super(MessagesForDetailPane.PaneWordWrapAction_WrapText,IAction.AS_CHECK_BOX);
        
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDsfDebugUIConstants.DETAIL_PANE_WORD_WRAP_ACTION);
		
		fTextViewer = textViewer;
		setEnabled(true);
		
		boolean prefSetting = DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP);
		fTextViewer.getTextWidget().setWordWrap(prefSetting);
		setChecked(prefSetting);
		

	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		fTextViewer.getTextWidget().setWordWrap(isChecked());
		DsfUIPlugin.getDefault().getPreferenceStore().setValue(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP,isChecked());
		DsfUIPlugin.getDefault().savePluginPreferences();
	}
	
}
