/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.dialogs.EncodingFieldEditor;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.text.spelling.SpellCheckEngine;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.wizards.indexwizards.StringVariableSelectionDialog;

/**
 * Options configuration block for spell check related settings.
 */
public class SpellingConfigurationBlock extends OptionsConfigurationBlock {
	/**
	 * Tells whether content assist proposal block should be shown.
	 * Currently the spelling engine cannot return word proposals but
	 * only correction proposals and hence this is disabled.
	 */
	private static final boolean SUPPORT_CONTENT_ASSIST_PROPOSALS= false;
	
	/** Preference keys for the preferences in this block */
	private static final Key PREF_SPELLING_IGNORE_DIGITS= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_DIGITS);
	private static final Key PREF_SPELLING_IGNORE_MIXED= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_MIXED);
	private static final Key PREF_SPELLING_IGNORE_SENTENCE= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_SENTENCE);
	private static final Key PREF_SPELLING_IGNORE_UPPER= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_UPPER);
	private static final Key PREF_SPELLING_IGNORE_STRING_LITERALS= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_STRING_LITERALS);
	private static final Key PREF_SPELLING_IGNORE_SINGLE_LETTERS= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_SINGLE_LETTERS);
	private static final Key PREF_SPELLING_IGNORE_NON_LETTERS= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_NON_LETTERS);
	private static final Key PREF_SPELLING_IGNORE_URLS= getCDTUIKey(PreferenceConstants.SPELLING_IGNORE_URLS);
	private static final Key PREF_SPELLING_LOCALE= getCDTUIKey(PreferenceConstants.SPELLING_LOCALE);
	private static final Key PREF_SPELLING_PROPOSAL_THRESHOLD= getCDTUIKey(PreferenceConstants.SPELLING_PROPOSAL_THRESHOLD);
	private static final Key PREF_SPELLING_PROBLEMS_THRESHOLD= getCDTUIKey(PreferenceConstants.SPELLING_PROBLEMS_THRESHOLD);
	private static final Key PREF_SPELLING_USER_DICTIONARY= getCDTUIKey(PreferenceConstants.SPELLING_USER_DICTIONARY);
	private static final Key PREF_SPELLING_USER_DICTIONARY_ENCODING= getCDTUIKey(PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING);
	private static final Key PREF_SPELLING_ENABLE_CONTENTASSIST= getCDTUIKey(PreferenceConstants.SPELLING_ENABLE_CONTENTASSIST);

	/**
	 * The value for no platform dictionary.
	 */
	private static final String PREF_VALUE_NO_LOCALE= ""; //$NON-NLS-1$

	/**
	 * Creates a selection dependency between a master and a slave control.
	 * 
	 * @param master     The master button that controls the state of the slave
	 * @param slave      The slave control that is enabled only if the master is
	 *                   selected
	 */
	protected static void createSelectionDependency(final Button master, final Control slave) {
		master.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
				// Do nothing
			}

			public void widgetSelected(SelectionEvent event) {
				slave.setEnabled(master.getSelection());
			}
		});
		slave.setEnabled(master.getSelection());
	}

	/**
	 * Returns the locale codes for the locale list.
	 * 
	 * @param locales
	 *                   The list of locales
	 * @return Array of locale codes for the list
	 */
	protected static String[] getDictionaryCodes(final Set<Locale> locales) {
		int index= 0;

		final String[] codes= new String[locales.size() + 1];
		for (Locale locale : locales) {
			codes[index++]= locale.toString();
		}
		codes[index++]= PREF_VALUE_NO_LOCALE;
		return codes;
	}

	/**
	 * Returns the display labels for the locale list.
	 * 
	 * @param locales    The list of locales
	 * @return Array of display labels for the list
	 */
	protected static String[] getDictionaryLabels(final Set<Locale> locales) {
		int index= 0;

		final String[] labels= new String[locales.size() + 1];
		for (Locale locale : locales) {
			labels[index++]= locale.getDisplayName();
		}
		labels[index++]= PreferencesMessages.SpellingPreferencePage_dictionary_none;
		return labels;
	}

	/**
	 * Validates that the file with the specified absolute path exists and can
	 * be opened.
	 * 
	 * @param path       The path of the file to validate
	 * @return a status without error if the path is valid
	 */
	protected static IStatus validateAbsoluteFilePath(String path) {
		final StatusInfo status= new StatusInfo();
		IStringVariableManager variableManager= VariablesPlugin.getDefault().getStringVariableManager();
		try {
			path= variableManager.performStringSubstitution(path);
			if (path.length() > 0) {
				
				final File file= new File(path);
				if (!file.exists() && (!file.isAbsolute() || !file.getParentFile().canWrite()))
					status.setError(PreferencesMessages.SpellingPreferencePage_dictionary_error);
				else if (file.exists() && (!file.isFile() || !file.isAbsolute() || !file.canRead() || !file.canWrite()))
					status.setError(PreferencesMessages.SpellingPreferencePage_dictionary_error);
			}
		} catch (CoreException e) {
			status.setError(e.getLocalizedMessage());
		}
		return status;
	}

	/**
	 * Validates that the specified locale is available.
	 * 
	 * @param localeString the locale to validate
	 * @return The status of the validation
	 */
	private static IStatus validateLocale(final String localeString) {
		if (PREF_VALUE_NO_LOCALE.equals(localeString))
			return new StatusInfo(); 

		Locale locale= SpellCheckEngine.convertToLocale(localeString);
		
		if (SpellCheckEngine.findClosestLocale(locale) != null)
			return new StatusInfo();
		
		return new StatusInfo(IStatus.ERROR, PreferencesMessages.SpellingPreferencePage_locale_error);
	}
	
	/**
	 * Validates that the specified number is positive.
	 * 
	 * @param number the number to validate
	 * @return The status of the validation
	 */
	protected static IStatus validatePositiveNumber(final String number) {
		final StatusInfo status= new StatusInfo();
		if (number.length() == 0) {
			status.setError(PreferencesMessages.SpellingPreferencePage_empty_threshold); 
		} else {
			try {
				final int value= Integer.parseInt(number);
				if (value < 0) {
					status.setError(NLS.bind(PreferencesMessages.SpellingPreferencePage_invalid_threshold, number)); 
				}
			} catch (NumberFormatException exception) {
				status.setError(NLS.bind(PreferencesMessages.SpellingPreferencePage_invalid_threshold, number)); 
			}
		}
		return status;
	}

	/** The dictionary path field */
	private Text fDictionaryPath= null;

	/** The status for the workspace dictionary file */
	private IStatus fFileStatus= new StatusInfo();

	/** The status for the proposal threshold */
	private IStatus fThresholdStatus= new StatusInfo();
	
	/** The status for the encoding field editor */
	private IStatus fEncodingFieldEditorStatus= new StatusInfo();
	
	/** The encoding field editor. */
	private EncodingFieldEditor fEncodingEditor;	
	/** The encoding field editor's parent. */
	private Composite fEncodingEditorParent;	

	/**
	 * All controls
	 */
	private Control[] fAllControls;
	
	/**
	 * All previously enabled controls
	 */
	private Control[] fEnabledControls;
	
	/**
	 * Creates a new spelling configuration block.
	 * 
	 * @param context the status change listener
	 * @param project the Java project
	 * @param container the preference container
	 */
	public SpellingConfigurationBlock(final IStatusChangeListener context, final IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);

		IStatus status= validateAbsoluteFilePath(getValue(PREF_SPELLING_USER_DICTIONARY));
		if (status.getSeverity() != IStatus.OK)
			setValue(PREF_SPELLING_USER_DICTIONARY, ""); //$NON-NLS-1$

		status= validateLocale(getValue(PREF_SPELLING_LOCALE));
		if (status.getSeverity() != IStatus.OK)
			setValue(PREF_SPELLING_LOCALE, SpellCheckEngine.getDefaultLocale().toString());
	}

	@Override
	protected Combo addComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
		ControlData data= new ControlData(key, values);
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indent;
				
		Label labelControl= new Label(parent, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(gd);
		
		Combo comboBox= new Combo(parent, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		comboBox.setLayoutData(gd);
		comboBox.addSelectionListener(getSelectionListener());
		
		fLabels.put(comboBox, labelControl);
		
		String currValue= getValue(key);
		
		Locale locale= SpellCheckEngine.convertToLocale(currValue);
		locale= SpellCheckEngine.findClosestLocale(locale);
		if (locale != null)
			currValue= locale.toString();
		
		comboBox.select(data.getSelection(currValue));
		
		fComboBoxes.add(comboBox);
		return comboBox;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		List<Control> allControls= new ArrayList<Control>();
		final PixelConverter converter= new PixelConverter(parent);

		final String[] trueFalse= new String[] { IPreferenceStore.TRUE, IPreferenceStore.FALSE };

		Group user= new Group(composite, SWT.NONE);
		user.setText(PreferencesMessages.SpellingPreferencePage_group_user); 
		user.setLayout(new GridLayout());		
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		allControls.add(user);

		String label= PreferencesMessages.SpellingPreferencePage_ignore_digits_label; 
		Control slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_DIGITS, trueFalse, 0);
		allControls.add(slave);

		label= PreferencesMessages.SpellingPreferencePage_ignore_mixed_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_MIXED, trueFalse, 0);
		allControls.add(slave);

		label= PreferencesMessages.SpellingPreferencePage_ignore_sentence_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_SENTENCE, trueFalse, 0);
		allControls.add(slave);

		label= PreferencesMessages.SpellingPreferencePage_ignore_upper_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_UPPER, trueFalse, 0);
		allControls.add(slave);

		label= PreferencesMessages.SpellingPreferencePage_ignore_url_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_URLS, trueFalse, 0);
		allControls.add(slave);
		
		label= PreferencesMessages.SpellingPreferencePage_ignore_non_letters_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_NON_LETTERS, trueFalse, 0);
		allControls.add(slave);
		
		label= PreferencesMessages.SpellingPreferencePage_ignore_single_letters_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_SINGLE_LETTERS, trueFalse, 0);
		allControls.add(slave);
		
		label= PreferencesMessages.SpellingPreferencePage_ignore_string_literals_label; 
		slave= addCheckBox(user, label, PREF_SPELLING_IGNORE_STRING_LITERALS, trueFalse, 0);
		allControls.add(slave);
		
		final Set<Locale> locales= SpellCheckEngine.getLocalesWithInstalledDictionaries();
		boolean hasPlaformDictionaries= locales.size() > 0;
		
		final Group engine= new Group(composite, SWT.NONE);
		if (hasPlaformDictionaries)
			engine.setText(PreferencesMessages.SpellingPreferencePage_group_dictionaries);
		else
			engine.setText(PreferencesMessages.SpellingPreferencePage_group_dictionary);
		engine.setLayout(new GridLayout(4, false));
		engine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		allControls.add(engine);

		if (hasPlaformDictionaries) {
			label= PreferencesMessages.SpellingPreferencePage_dictionary_label; 
			Combo combo= addComboBox(engine, label, PREF_SPELLING_LOCALE, getDictionaryCodes(locales), getDictionaryLabels(locales), 0);
			combo.setEnabled(locales.size() > 0);
			allControls.add(combo);
			allControls.add(fLabels.get(combo));
			
			new Label(engine, SWT.NONE); // placeholder
		}
		
		label= PreferencesMessages.SpellingPreferencePage_workspace_dictionary_label; 
		fDictionaryPath= addTextField(engine, label, PREF_SPELLING_USER_DICTIONARY, 0, 0);
		GridData gd= (GridData) fDictionaryPath.getLayoutData();
		gd.grabExcessHorizontalSpace= true;
		gd.widthHint= converter.convertWidthInCharsToPixels(40);
		allControls.add(fDictionaryPath);
		allControls.add(fLabels.get(fDictionaryPath));

		Composite buttons=new Composite(engine, SWT.NONE);
		buttons.setLayout(new GridLayout(2,true));
		buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		Button button= new Button(buttons, SWT.PUSH);
		button.setText(PreferencesMessages.SpellingPreferencePage_browse_label); 
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				handleBrowseButtonSelected();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		allControls.add(button);
		
		button=new Button(buttons, SWT.PUSH);
		button.setText(PreferencesMessages.SpellingPreferencePage_variables);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleVariablesButtonSelected();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		allControls.add(button);
		
		// Description for user dictionary
		new Label(engine, SWT.NONE); // filler
		Label description= new Label(engine, SWT.NONE);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		description.setLayoutData(gd);
		description.setText(PreferencesMessages.SpellingPreferencePage_user_dictionary_description);
		allControls.add(description);
		
		createEncodingFieldEditor(engine, allControls);

		Group advanced= new Group(composite, SWT.NONE);
		advanced.setText(PreferencesMessages.SpellingPreferencePage_group_advanced); 
		advanced.setLayout(new GridLayout(3, false));
		advanced.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		allControls.add(advanced);

		label= PreferencesMessages.SpellingPreferencePage_problems_threshold; 
		int digits= 4;
		Text  text= addTextField(advanced, label, PREF_SPELLING_PROBLEMS_THRESHOLD, 0, converter.convertWidthInCharsToPixels(digits+1));
		text.setTextLimit(digits);
		allControls.add(text);
		allControls.add(fLabels.get(text));

		label= PreferencesMessages.SpellingPreferencePage_proposals_threshold; 
		digits= 3;
		text= addTextField(advanced, label, PREF_SPELLING_PROPOSAL_THRESHOLD, 0, converter.convertWidthInCharsToPixels(digits+1));
		text.setTextLimit(digits);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		allControls.add(text);
		allControls.add(fLabels.get(text));

		if (SUPPORT_CONTENT_ASSIST_PROPOSALS) {
			label= PreferencesMessages.SpellingPreferencePage_enable_contentassist_label; 
			button= addCheckBox(advanced, label, PREF_SPELLING_ENABLE_CONTENTASSIST, trueFalse, 0);
			allControls.add(button);
		}

		fAllControls= allControls.toArray(new Control[allControls.size()]);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.SPELLING_CONFIGURATION_BLOCK);
		return composite;
	}

	/**
	 * Creates the encoding field editor.
	 * 
	 * @param composite the parent composite
	 * @param allControls list with all controls
	 */
	private void createEncodingFieldEditor(Composite composite, List<Control> allControls) {
		Label filler= new Label(composite, SWT.NONE);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 4;
		filler.setLayoutData(gd);
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(PreferencesMessages.SpellingPreferencePage_encoding_label);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		allControls.add(label);
		
		fEncodingEditorParent= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(2, false);
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		fEncodingEditorParent.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		fEncodingEditorParent.setLayoutData(gd);
		
		fEncodingEditor= new EncodingFieldEditor(PREF_SPELLING_USER_DICTIONARY_ENCODING.getName(), "", null, fEncodingEditorParent); //$NON-NLS-1$
		
		PreferenceStore store= new PreferenceStore();
		String defaultEncoding= ResourcesPlugin.getEncoding();
		store.setDefault(PREF_SPELLING_USER_DICTIONARY_ENCODING.getName(), defaultEncoding);
		String encoding= getValue(PREF_SPELLING_USER_DICTIONARY_ENCODING);
		if (encoding != null && encoding.length() > 0)
			store.setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING.getName(), encoding);

		fEncodingEditor.setPreferenceStore(store);

		// Redirect status messages from the field editor to the status change listener  
		DialogPage fakePage= new DialogPage() {
			public void createControl(Composite c) {
			}
			@Override
			public void setErrorMessage(String newMessage) {
				StatusInfo status= new StatusInfo();
				if (newMessage != null)
					status.setError(newMessage);
				fEncodingFieldEditorStatus= status;
				fContext.statusChanged(StatusUtil.getMostSevere(new IStatus[] { fThresholdStatus, fFileStatus, fEncodingFieldEditorStatus }));
			}
		};
		fEncodingEditor.setPage(fakePage);
		
		fEncodingEditor.load();
		
		if (encoding == null || encoding.equals(defaultEncoding) || encoding.length() == 0)
			fEncodingEditor.loadDefault();
	}

	private static Key[] getAllKeys() {
		if (SUPPORT_CONTENT_ASSIST_PROPOSALS)
			return new Key[] { PREF_SPELLING_USER_DICTIONARY, PREF_SPELLING_USER_DICTIONARY_ENCODING, PREF_SPELLING_IGNORE_DIGITS, PREF_SPELLING_IGNORE_MIXED, PREF_SPELLING_IGNORE_SENTENCE, PREF_SPELLING_IGNORE_UPPER, PREF_SPELLING_IGNORE_URLS, PREF_SPELLING_IGNORE_NON_LETTERS, PREF_SPELLING_IGNORE_SINGLE_LETTERS, PREF_SPELLING_LOCALE, PREF_SPELLING_PROPOSAL_THRESHOLD, PREF_SPELLING_PROBLEMS_THRESHOLD, PREF_SPELLING_ENABLE_CONTENTASSIST, PREF_SPELLING_IGNORE_STRING_LITERALS };
		return new Key[] { PREF_SPELLING_USER_DICTIONARY, PREF_SPELLING_USER_DICTIONARY_ENCODING, PREF_SPELLING_IGNORE_DIGITS, PREF_SPELLING_IGNORE_MIXED, PREF_SPELLING_IGNORE_SENTENCE, PREF_SPELLING_IGNORE_UPPER, PREF_SPELLING_IGNORE_URLS, PREF_SPELLING_IGNORE_NON_LETTERS, PREF_SPELLING_IGNORE_SINGLE_LETTERS, PREF_SPELLING_LOCALE, PREF_SPELLING_PROPOSAL_THRESHOLD, PREF_SPELLING_PROBLEMS_THRESHOLD, PREF_SPELLING_IGNORE_STRING_LITERALS };
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#getFullBuildDialogStrings(boolean)
	 */
	protected final String[] getFullBuildDialogStrings(final boolean workspace) {
		return null;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#performOk()
	 */
	@Override
	public boolean performOk() {
		fEncodingEditor.store();
		if (fEncodingEditor.presentsDefaultValue())
			setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING, ""); //$NON-NLS-1$
		else
			setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING, fEncodingEditor.getPreferenceStore().getString(PREF_SPELLING_USER_DICTIONARY_ENCODING.getName()));
		return super.performOk();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#performApply()
	 */
	@Override
	public boolean performApply() {
		fEncodingEditor.store();
		if (fEncodingEditor.presentsDefaultValue())
			setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING, ""); //$NON-NLS-1$
		else
			setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING, fEncodingEditor.getPreferenceStore().getString(PREF_SPELLING_USER_DICTIONARY_ENCODING.getName()));
		return super.performApply();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#performDefaults()
	 */
	@Override
	public void performDefaults() {
		super.performDefaults();
		
		setValue(PREF_SPELLING_USER_DICTIONARY_ENCODING, ""); //$NON-NLS-1$
		
		fEncodingEditor.getPreferenceStore().setValue(fEncodingEditor.getPreferenceName(), ResourcesPlugin.getEncoding());
		fEncodingEditor.load();
		
		fEncodingEditor.loadDefault();
	}

	protected void handleVariablesButtonSelected() {
		StringVariableSelectionDialog dialog= new StringVariableSelectionDialog(fDictionaryPath.getShell());
		if (dialog.open() == Window.OK)
			fDictionaryPath.setText(fDictionaryPath.getText() + dialog.getVariableExpression());
	}
	
	/**
	 * Handles selections of the browse button.
	 */
	protected void handleBrowseButtonSelected() {
		final FileDialog dialog= new FileDialog(fDictionaryPath.getShell(), SWT.OPEN);
		dialog.setText(PreferencesMessages.SpellingPreferencePage_filedialog_title); 
		dialog.setFilterPath(fDictionaryPath.getText());

		final String path= dialog.open();
		if (path != null)
			fDictionaryPath.setText(path);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#validateSettings(java.lang.String,java.lang.String)
	 */
	@Override
	protected void validateSettings(final Key key, final String oldValue, final String newValue) {
		if (key == null || PREF_SPELLING_PROPOSAL_THRESHOLD.equals(key))
			fThresholdStatus= validatePositiveNumber(getValue(PREF_SPELLING_PROPOSAL_THRESHOLD));
		else
			fThresholdStatus= new StatusInfo();
		
		if (key == null || PREF_SPELLING_PROBLEMS_THRESHOLD.equals(key)) {
			IStatus status= validatePositiveNumber(getValue(PREF_SPELLING_PROBLEMS_THRESHOLD)); 
			fThresholdStatus= StatusUtil.getMostSevere(new IStatus[] {fThresholdStatus, status});
		}

		if (key == null || PREF_SPELLING_USER_DICTIONARY.equals(key))
			fFileStatus= validateAbsoluteFilePath(getValue(PREF_SPELLING_USER_DICTIONARY));

		fContext.statusChanged(StatusUtil.getMostSevere(new IStatus[] { fThresholdStatus, fFileStatus, fEncodingFieldEditorStatus }));
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#updateCheckBox(org.eclipse.swt.widgets.Button)
	 */
	@Override
	protected void updateCheckBox(Button curr) {
		super.updateCheckBox(curr);
		Event event= new Event();
		event.type= SWT.Selection;
		event.display= curr.getDisplay();
		event.widget= curr;
		curr.notifyListeners(SWT.Selection, event);
	}
	
	/**
	 * Sets the enabled state.
	 * 
	 * @param enabled the new state
	 */
	protected void setEnabled(boolean enabled) {
		fEncodingEditor.setEnabled(enabled, fEncodingEditorParent);
		
		if (enabled && fEnabledControls != null) {
			for (int i= fEnabledControls.length - 1; i >= 0; i--)
				fEnabledControls[i].setEnabled(true);
			fEnabledControls= null;
		}
		if (!enabled && fEnabledControls == null) {
			List<Control> enabledControls= new ArrayList<Control>();
			for (int i= fAllControls.length - 1; i >= 0; i--) {
				Control control= fAllControls[i];
				if (control.isEnabled()) {
					enabledControls.add(control);
					control.setEnabled(false);
				}
			}
			fEnabledControls= enabledControls.toArray(new Control[enabledControls.size()]);
		}
	}
}
