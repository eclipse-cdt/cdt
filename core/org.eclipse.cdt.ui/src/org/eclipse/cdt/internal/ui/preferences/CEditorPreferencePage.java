/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Marco Stornelli <marco.stornelli@gmail.com> - Bug 333134
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import static org.eclipse.cdt.ui.PreferenceConstants.FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION;
import static org.eclipse.cdt.ui.PreferenceConstants.FORMATTING_SCOPE_DOCUMENT;
import static org.eclipse.cdt.ui.PreferenceConstants.FORMATTING_SCOPE_FOR_EMPTY_SELECTION;
import static org.eclipse.cdt.ui.PreferenceConstants.FORMATTING_SCOPE_STATEMENT;

import java.util.ArrayList;

import org.eclipse.cdt.doxygen.core.DoxygenPreferences;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.DocCommentOwnerArea;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

/**
 * The preference page for setting the editor options.
 */
public class CEditorPreferencePage extends AbstractPreferencePage {
	protected final String[][] fAppearanceColorListModel = new String[][] {
			{ PreferencesMessages.CEditorPreferencePage_behaviorPage_matchingBracketColor,
					CEditor.MATCHING_BRACKETS_COLOR, null },
			{ PreferencesMessages.CEditorPreferencePage_behaviorPage_inactiveCodeColor, CEditor.INACTIVE_CODE_COLOR,
					null },
			{ PreferencesMessages.CEditorPreferencePage_ContentAssistPage_completionProposalBackgroundColor,
					ContentAssistPreference.PROPOSALS_BACKGROUND, null },
			{ PreferencesMessages.CEditorPreferencePage_ContentAssistPage_completionProposalForegroundColor,
					ContentAssistPreference.PROPOSALS_FOREGROUND, null },
			{ PreferencesMessages.CEditorPreferencePage_ContentAssistPage_parameterBackgroundColor,
					ContentAssistPreference.PARAMETERS_BACKGROUND, null },
			{ PreferencesMessages.CEditorPreferencePage_ContentAssistPage_parameterForegroundColor,
					ContentAssistPreference.PARAMETERS_FOREGROUND, null },
			{ PreferencesMessages.CEditorPreferencePage_sourceHoverBackgroundColor,
					PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR,
					PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT }, };

	private List fAppearanceColorList;
	private ColorSelector fAppearanceColorEditor;
	private Button fAppearanceColorDefault;
	private DocCommentOwnerArea docCommentOwnerArea;
	// TODO(sprigogin): Remove once compatibility with Platform 4.4 is no longer required.
	private final boolean formattingScopeForEmptySelectionSupported;
	private final DoxygenPreferences doxygenPreferences;

	public CEditorPreferencePage() {
		super();
		Bundle jfaceText = Platform.getBundle("org.eclipse.jface.text"); //$NON-NLS-1$
		formattingScopeForEmptySelectionSupported = jfaceText.getVersion().compareTo(new Version(3, 10, 0)) >= 0;
		doxygenPreferences = EclipseContextFactory
				.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext())
				.get(DoxygenPreferences.class);
	}

	@Override
	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList<OverlayKey> overlayKeys = new ArrayList<>();

		overlayKeys.add(
				new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.SUB_WORD_NAVIGATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS));
		overlayKeys.add(
				new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.MATCHING_BRACKETS_COLOR));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.MATCHING_BRACKETS));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CEditor.INACTIVE_CODE_COLOR));
		overlayKeys.add(
				new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CEditor.INACTIVE_CODE_ENABLE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.PROPOSALS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.PARAMETERS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.PARAMETERS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				FORMATTING_SCOPE_FOR_EMPTY_SELECTION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION));

		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(CEditor.SUB_WORD_NAVIGATION, true);

		store.setDefault(CEditor.MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, CEditor.MATCHING_BRACKETS_COLOR, new RGB(170, 170, 170));

		store.setDefault(CEditor.INACTIVE_CODE_ENABLE, true);
		PreferenceConverter.setDefault(store, CEditor.INACTIVE_CODE_COLOR, new RGB(224, 224, 224));
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_EDITOR_PREF_PAGE);
	}

	/**
	 * Sets enabled flag for a control and all its sub-tree.
	 */
	protected static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] children = composite.getChildren();
			for (Control element : children)
				setEnabled(element, enable);
		}
	}

	private Control createBehaviorBlock(Composite parent) {
		Composite behaviorComposite = ControlFactory.createGroup(parent,
				PreferencesMessages.CEditorPreferencePage_GeneralAppearanceGroupTitle, 1);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		behaviorComposite.setLayout(layout);

		int indent = convertHorizontalDLUsToPixels(8);

		String label = PreferencesMessages.CEditorPreferencePage_behaviorPage_subWordNavigation;
		addCheckBox(behaviorComposite, label, CEditor.SUB_WORD_NAVIGATION, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviourPage_EnableEditorProblemAnnotation;
		addCheckBox(behaviorComposite, label, PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_matchingBrackets;
		addCheckBox(behaviorComposite, label, CEditor.MATCHING_BRACKETS, 0);

		label = PreferencesMessages.CEditorPreferencePage_behaviorPage_inactiveCode;
		addCheckBox(behaviorComposite, label, CEditor.INACTIVE_CODE_ENABLE, 0);

		if (formattingScopeForEmptySelectionSupported) {
			Label l = new Label(behaviorComposite, SWT.LEFT);
			l.setText(PreferencesMessages.CEditorPreferencePage_behaviorPage_formattingWithEmptySelection);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 2;
			l.setLayoutData(gd);

			label = PreferencesMessages.CEditorPreferencePage_behaviorPage_formatFile;
			addRadioButton(behaviorComposite, label, FORMATTING_SCOPE_FOR_EMPTY_SELECTION, FORMATTING_SCOPE_DOCUMENT,
					indent);

			label = PreferencesMessages.CEditorPreferencePage_behaviorPage_formatStatement;
			addRadioButton(behaviorComposite, label, FORMATTING_SCOPE_FOR_EMPTY_SELECTION, FORMATTING_SCOPE_STATEMENT,
					indent);

			label = PreferencesMessages.CEditorPreferencePage_behaviorPage_confirmFormattingScope;
			addCheckBox(behaviorComposite, label, FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION, indent);
		}

		Label l = new Label(behaviorComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);

		l = new Label(behaviorComposite, SWT.LEFT);
		l.setText(PreferencesMessages.CEditorPreferencePage_behaviorPage_appearanceColorOptions);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);

		Composite editorComposite = new Composite(behaviorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		editorComposite.setLayoutData(gd);

		fAppearanceColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint = convertHeightInCharsToPixels(8);
		fAppearanceColorList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l = new Label(stylesComposite, SWT.LEFT);
		l.setText(PreferencesMessages.CEditorPreferencePage_behaviorPage_Color);
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor = new ColorSelector(stylesComposite);
		Button foregroundColorButton = fAppearanceColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		SelectionListener colorDefaultSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean systemDefault = fAppearanceColorDefault.getSelection();
				fAppearanceColorEditor.getButton().setEnabled(!systemDefault);

				int i = fAppearanceColorList.getSelectionIndex();
				String key = fAppearanceColorListModel[i][2];
				if (key != null)
					fOverlayStore.setValue(key, systemDefault);
			}
		};

		fAppearanceColorDefault = new Button(stylesComposite, SWT.CHECK);
		fAppearanceColorDefault.setText(PreferencesMessages.CEditorPreferencePage_colorPage_systemDefault);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fAppearanceColorDefault.setLayoutData(gd);
		fAppearanceColorDefault.setVisible(false);
		fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);

		fAppearanceColorList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = fAppearanceColorList.getSelectionIndex();
				String key = fAppearanceColorListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
			}
		});
		return behaviorComposite;
	}

	private void handleAppearanceColorListSelection() {
		int i = fAppearanceColorList.getSelectionIndex();
		String key = fAppearanceColorListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceColorEditor.setColorValue(rgb);
		updateAppearanceColorWidgets(fAppearanceColorListModel[i][2]);
	}

	private void updateAppearanceColorWidgets(String systemDefaultKey) {
		if (systemDefaultKey == null) {
			fAppearanceColorDefault.setSelection(false);
			fAppearanceColorDefault.setVisible(false);
			fAppearanceColorEditor.getButton().setEnabled(true);
		} else {
			boolean systemDefault = fOverlayStore.getBoolean(systemDefaultKey);
			fAppearanceColorDefault.setSelection(systemDefault);
			fAppearanceColorDefault.setVisible(true);
			fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
		}
	}

	private Control createHeader(Composite parent) {
		String text = PreferencesMessages.CEditorPreferencePage_link;
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener(SWT.Selection, event -> {
			String u = event.text;
			PreferencesUtil.createPreferenceDialogOn(getShell(), u, null, null);
		});
		// TODO replace by link-specific tooltips when
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
		link.setToolTipText(PreferencesMessages.CEditorPreferencePage_link_tooltip);

		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint = 150; // only expand further if anyone else requires it
		link.setLayoutData(gridData);
		return link;
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		Composite contents = ControlFactory.createComposite(parent, 1);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));

		createHeader(contents);

		ControlFactory.createEmptySpace(contents, 2);
		createBehaviorBlock(contents);

		ControlFactory.createEmptySpace(contents, 2);

		String dsc = PreferencesMessages.CEditorPreferencePage_SelectDocToolDescription;
		String msg = PreferencesMessages.CEditorPreferencePage_WorkspaceDefaultLabel;
		docCommentOwnerArea = new DocCommentOwnerArea(contents, doxygenPreferences.metadata(), dsc, msg);
		initialize();
		return contents;
	}

	private void initialize() {
		initializeFields();
		initializeDefaultColors();

		for (String[] element : fAppearanceColorListModel) {
			fAppearanceColorList.add(element[0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(() -> {
			fAppearanceColorList.select(0);
			handleAppearanceColorListSelection();
		});
		IDocCommentOwner workspaceOwner = DocCommentOwnerManager.getInstance().getWorkspaceCommentOwner();
		docCommentOwnerArea.initialize(workspaceOwner, doxygenPreferences.workspaceStorage());
	}

	@Override
	public boolean performOk() {
		DocCommentOwnerManager.getInstance().setWorkspaceCommentOwner(docCommentOwnerArea.getSelectedDocCommentOwner());
		docCommentOwnerArea.apply(doxygenPreferences.workspaceStorage());
		return super.performOk();
	}

	/**
	 * Initializes the default colors.
	 */
	private void initializeDefaultColors() {
		if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT)) {
			Display display = fAppearanceColorList.getDisplay();
			RGB rgb = SourceViewerInformationControl.getVisibleBackgroundColor(display);
			if (rgb == null)
				rgb = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();
			PreferenceConverter.setValue(getPreferenceStore(), PreferenceConstants.EDITOR_SOURCE_HOVER_BACKGROUND_COLOR,
					rgb);
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaultColors();
		handleAppearanceColorListSelection();
	}
}
