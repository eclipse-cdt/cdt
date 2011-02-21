/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.Profile;

public class FormatterModifyDialog extends ModifyDialog {
    
    public FormatterModifyDialog(Shell parentShell, Profile profile, ProfileManager profileManager, ProfileStore profileStore, boolean newProfile, String dialogPreferencesKey, String lastSavePathKey) {
		super(parentShell, profile, profileManager, profileStore, newProfile, dialogPreferencesKey, lastSavePathKey);
	}
	
	@Override
	protected void addPages(Map<String, String> values) {
	    addTabPage(FormatterMessages.ModifyDialog_tabpage_indentation_title, new IndentationTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_braces_title, new BracesTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_whitespace_title, new WhiteSpaceTabPage(this, values)); 
//		addTabPage(FormatterMessages.ModifyDialog_tabpage_blank_lines_title, new BlankLinesTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_new_lines_title, new NewLinesTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_control_statements_title, new ControlStatementsTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_line_wrapping_title, new LineWrappingTabPage(this, values)); 
		addTabPage(FormatterMessages.ModifyDialog_tabpage_comments_title, new CommentsTabPage(this, values));
    }
}
