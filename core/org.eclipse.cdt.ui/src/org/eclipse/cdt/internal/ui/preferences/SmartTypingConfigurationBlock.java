/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.util.Messages;

/**
 * Configures C Editor typing preferences.
 */
class SmartTypingConfigurationBlock extends AbstractConfigurationBlock {

	public SmartTypingConfigurationBlock(OverlayPreferenceStore store) {
		super(store);
		store.addKeys(createOverlayStoreKeys());
	}
	
	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		return new OverlayPreferenceStore.OverlayKey[] {
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_PASTE),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_AUTO_INDENT),
			
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_CLOSE_STRINGS),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_CLOSE_BRACKETS),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_CLOSE_ANGULAR_BRACKETS),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_CLOSE_BRACES),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_WRAP_STRINGS),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_ESCAPE_STRINGS),
			
//			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_SEMICOLON),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_TAB),
//			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_OPENING_BRACE),
		};
	}	

	/**
	 * Creates page for mark occurrences preferences.
	 * 
	 * @param parent the parent composite
	 * @return the control for the preference page
	 */
	@Override
	public Control createControl(Composite parent) {
		ScrolledPageContent scrolled= new ScrolledPageContent(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		
		Composite control= new Composite(scrolled, SWT.NONE);
		GridLayout layout= new GridLayout();
		control.setLayout(layout);

		Composite composite;
		
		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_autoclose_title); 
		addAutoclosingSection(composite);
		
//		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_automove_title); 
//		addAutopositionSection(composite);
		
		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_tabs_title); 
		addTabSection(composite);

		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_pasting_title); 
		addPasteSection(composite);
		
		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_strings_title); 
		addStringsSection(composite);

		composite= createSubsection(control, null, PreferencesMessages.SmartTypingConfigurationBlock_autoindent_title); 
		addAutoIndentSection(composite);

		scrolled.setContent(control);
		final Point size= control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolled.setMinSize(size.x, size.y);
		return scrolled;
	}

	private void addAutoIndentSection(Composite composite) {
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);

		String label;
		label= PreferencesMessages.SmartTypingConfigurationBlock_autoindent_newlines;
		addCheckBox(composite, label, PreferenceConstants.EDITOR_AUTO_INDENT, 0);
	}

	private void addStringsSection(Composite composite) {
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);

		String label;
		Button master, slave;
		label= PreferencesMessages.CEditorPreferencePage_wrapStrings; 
		master= addCheckBox(composite, label, PreferenceConstants.EDITOR_WRAP_STRINGS, 0);
		
		label= PreferencesMessages.CEditorPreferencePage_escapeStrings; 
		slave= addCheckBox(composite, label, PreferenceConstants.EDITOR_ESCAPE_STRINGS, 0);
		createDependency(master, slave);
	}

	private void addPasteSection(Composite composite) {
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);

		String label;
		label= PreferencesMessages.CEditorPreferencePage_smartPaste; 
		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_PASTE, 0);
	}

	private void addTabSection(Composite composite) {
		GridLayout layout= new GridLayout();
		composite.setLayout(layout);

		String label;
		label= PreferencesMessages.CEditorPreferencePage_typing_smartTab; 
		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_TAB, 0);
		
		createMessage(composite);
	}

//	private void addAutopositionSection(Composite composite) {
//		GridLayout layout= new GridLayout();
//		composite.setLayout(layout);
//
//		String label;
//		
//		label= PreferencesMessages.CEditorPreferencePage_typing_smartSemicolon; 
//		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_SEMICOLON, 0);
//		
//		label= PreferencesMessages.CEditorPreferencePage_typing_smartOpeningBrace; 
//		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_OPENING_BRACE, 0);
//	}

	private void addAutoclosingSection(Composite composite) {
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);

		String label;

		label= PreferencesMessages.CEditorPreferencePage_closeStrings;
		addCheckBox(composite, label, PreferenceConstants.EDITOR_CLOSE_STRINGS, 0);

		label= PreferencesMessages.CEditorPreferencePage_closeBrackets; 
		addCheckBox(composite, label, PreferenceConstants.EDITOR_CLOSE_BRACKETS, 0);

		label= PreferencesMessages.CEditorPreferencePage_closeAngularBrackets; 
		addCheckBox(composite, label, PreferenceConstants.EDITOR_CLOSE_ANGULAR_BRACKETS, 0);

		label= PreferencesMessages.CEditorPreferencePage_closeBraces; 
		addCheckBox(composite, label, PreferenceConstants.EDITOR_CLOSE_BRACES, 0);
	}
	
	private void createMessage(final Composite composite) {
		// TODO create a link with an argument, so the formatter preference page can open the 
		// current profile automatically.
		String linkTooltip= PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_tooltip; 
		String text;
		String indentMode= CUIPlugin.getDefault().getCombinedPreferenceStore().getString(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		if (CCorePlugin.TAB.equals(indentMode)) {
			text= Messages.format(PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_tab_text,
					Integer.toString(getTabDisplaySize()));
		} else {
			text= Messages.format(PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_others_text,
					Integer.toString(getTabDisplaySize()), Integer.toString(getIndentSize()), getIndentMode());
		}
		
		final Link link= new Link(composite, SWT.NONE);
		link.setText(text);
		link.setToolTipText(linkTooltip);
		GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.widthHint= 300; // don't get wider initially
		link.setLayoutData(gd);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(link.getShell(),
						"org.eclipse.cdt.ui.preferences.CodeFormatterPreferencePage", null, null); //$NON-NLS-1$
			}
		});
		
		final IPreferenceStore combinedStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
		final IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
			private boolean fHasRun= false;
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (fHasRun)
					return;
				if (composite.isDisposed())
					return;
				String property= event.getProperty();
				if (DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR.equals(property)
						|| DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE.equals(property)) {
					fHasRun= true;
					link.dispose();
					createMessage(composite);
					Dialog.applyDialogFont(composite);
					composite.redraw();
					composite.layout();
				}
			}
		};
		combinedStore.addPropertyChangeListener(propertyChangeListener);
		link.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
				combinedStore.removePropertyChangeListener(propertyChangeListener);
			}
		});
	}

	private String getIndentMode() {
		String indentMode= CUIPlugin.getDefault().getCombinedPreferenceStore().getString(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		
		if (CCorePlugin.SPACE.equals(indentMode))
			return PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_spaces; 
		
		if (CCorePlugin.TAB.equals(indentMode))
			return PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_tabs;
		
		if (DefaultCodeFormatterConstants.MIXED.equals(indentMode))
			return PreferencesMessages.SmartTypingConfigurationBlock_tabs_message_tabsAndSpaces;

		Assert.isTrue(false, "Illegal indent mode - must not happen"); //$NON-NLS-1$
		return null;
	}

	private int getIndentSize() {
		return CodeFormatterUtil.getIndentWidth(null);
	}
	
	private int getTabDisplaySize() {
		return CodeFormatterUtil.getTabWidth(null);
	}
}
