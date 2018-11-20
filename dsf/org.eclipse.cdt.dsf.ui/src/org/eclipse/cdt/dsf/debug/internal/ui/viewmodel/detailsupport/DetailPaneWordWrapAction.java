/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - extended implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport;

import org.eclipse.cdt.dsf.debug.internal.ui.IDsfDebugHelpContextIds;
import org.eclipse.cdt.dsf.debug.internal.ui.IInternalDsfDebugUIConstants;
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
		super(MessagesForDetailPane.PaneWordWrapAction_WrapText, IAction.AS_CHECK_BOX);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDsfDebugHelpContextIds.DETAIL_PANE_WORD_WRAP_ACTION);

		fTextViewer = textViewer;
		setEnabled(true);

		boolean prefSetting = DsfUIPlugin.getDefault().getPreferenceStore()
				.getBoolean(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP);
		fTextViewer.getTextWidget().setWordWrap(prefSetting);
		setChecked(prefSetting);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		fTextViewer.getTextWidget().setWordWrap(isChecked());
		DsfUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP,
				isChecked());
		DsfUIPlugin.getDefault().savePluginPreferences();
	}

}
