/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * MakeEditorPreferencePage
 * The page for setting the editor options.
 */
public class MakefileEditorPreferencePage extends AbstractMakefileEditorPreferencePage {
	/** The keys of the overlay store. */
	private String[][] fSyntaxColorListModel;

	private TableViewer fHighlightingColorListViewer;
	private final List<HighlightingColorListItem> fHighlightingColorList = new ArrayList<>(7);

	Button fAppearanceColorDefault;
	ColorSelector fSyntaxForegroundColorEditor;
	Button fBoldCheckBox;
	Button fItalicCheckBox;

	// folding
	protected Button fFoldingCheckbox;

	/**
	 * Item in the highlighting color list.
	 *
	 * @since 3.0
	 */
	private class HighlightingColorListItem {
		/** Display name */
		private String fDisplayName;
		/** Color preference key */
		private String fColorKey;
		/** Bold preference key */
		private String fBoldKey;
		/** Italic preference key */
		private String fItalicKey;
		/** Item color */
		private Color fItemColor;

		/**
		 * Initialize the item with the given values.
		 *
		 * @param displayName the display name
		 * @param colorKey the color preference key
		 * @param boldKey the bold preference key
		 * @param italicKey the italic preference key
		 * @param itemColor the item color
		 */
		public HighlightingColorListItem(String displayName, String colorKey, String boldKey, String italicKey,
				Color itemColor) {
			fDisplayName = displayName;
			fColorKey = colorKey;
			fBoldKey = boldKey;
			fItalicKey = italicKey;
			fItemColor = itemColor;
		}

		/**
		 * @return the bold preference key
		 */
		public String getBoldKey() {
			return fBoldKey;
		}

		/**
		 * @return the bold preference key
		 */
		public String getItalicKey() {
			return fItalicKey;
		}

		/**
		 * @return the color preference key
		 */
		public String getColorKey() {
			return fColorKey;
		}

		/**
		 * @return the display name
		 */
		public String getDisplayName() {
			return fDisplayName;
		}

		/**
		 * @return the item color
		 */
		public Color getItemColor() {
			return fItemColor;
		}
	}

	/**
	 * Color list label provider.
	 *
	 * @since 3.0
	 */
	private class ColorListLabelProvider extends LabelProvider implements IColorProvider {
		@Override
		public String getText(Object element) {
			return ((HighlightingColorListItem) element).getDisplayName();
		}

		@Override
		public Color getForeground(Object element) {
			return ((HighlightingColorListItem) element).getItemColor();
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
	}

	/**
	 * Color list content provider.
	 *
	 * @since 3.0
	 */
	private class ColorListContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Constructor.
	 */
	public MakefileEditorPreferencePage() {
		super();
	}

	@Override
	protected OverlayPreferenceStore createOverlayStore() {
		fSyntaxColorListModel = new String[][] {
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_comment"), //$NON-NLS-1$
						ColorManager.MAKE_COMMENT_COLOR, null },
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_macro_ref"), //$NON-NLS-1$
						ColorManager.MAKE_MACRO_REF_COLOR, null },
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_macro_def"), //$NON-NLS-1$
						ColorManager.MAKE_MACRO_DEF_COLOR, null },
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_function"), //$NON-NLS-1$
						ColorManager.MAKE_FUNCTION_COLOR, null },
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_keyword"), //$NON-NLS-1$
						ColorManager.MAKE_KEYWORD_COLOR, null },
				{ MakefilePreferencesMessages
						.getString("MakefileEditorPreferencePage.makefile_editor_matching_bracket"), //$NON-NLS-1$
						ColorManager.MAKE_MATCHING_BRACKETS_COLOR, null },
				{ MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.makefile_editor_default"), //$NON-NLS-1$
						ColorManager.MAKE_DEFAULT_COLOR, null }, };
		ArrayList<OverlayKey> overlayKeys = new ArrayList<>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				MakefileEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				MakefileEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				MakefileEditorPreferenceConstants.EDITOR_FOLDING_RULE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				MakefileEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS));

		for (int i = 0; i < fSyntaxColorListModel.length; i++) {
			String colorKey = fSyntaxColorListModel[i][1];
			addTextKeyToCover(overlayKeys, colorKey);
		}

		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}

	private void addTextKeyToCover(ArrayList<OverlayKey> overlayKeys, String mainKey) {
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, mainKey));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				mainKey + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				mainKey + MakefileEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX));
	}

	@Override
	protected Control createContents(Composite parent) {
		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				IMakeHelpContextIds.MAKE_EDITOR_PREFERENCE_PAGE);
		getOverlayStore().load();
		getOverlayStore().start();

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.syntax")); //$NON-NLS-1$
		item.setControl(createSyntaxPage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.folding")); //$NON-NLS-1$
		item.setControl(createFoldingTabContent(folder));

		initialize();

		applyDialogFont(folder);
		return folder;
	}

	private void initialize() {
		initializeFields();

		for (int i = 0, n = fSyntaxColorListModel.length; i < n; i++) {
			fHighlightingColorList.add(new HighlightingColorListItem(fSyntaxColorListModel[i][0],
					fSyntaxColorListModel[i][1],
					fSyntaxColorListModel[i][1] + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX,
					fSyntaxColorListModel[i][1] + MakefileEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX, null));
		}
		fHighlightingColorListViewer.setInput(fHighlightingColorList);
		fHighlightingColorListViewer
				.setSelection(new StructuredSelection(fHighlightingColorListViewer.getElementAt(0)));

		initializeFolding();
	}

	void initializeFolding() {
		boolean enabled = getOverlayStore().getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);
	}

	void initializeDefaultFolding() {
		boolean enabled = getOverlayStore().getDefaultBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		fFoldingCheckbox.setSelection(enabled);
	}

	@Override
	protected void handleDefaults() {
		handleSyntaxColorListSelection();
		initializeDefaultFolding();
	}

	private Control createSyntaxPage(Composite parent) {
		Composite colorComposite = new Composite(parent, SWT.NONE);
		colorComposite.setLayout(new GridLayout());

		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.Foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		fHighlightingColorListViewer = new TableViewer(editorComposite,
				SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fHighlightingColorListViewer.setLabelProvider(new ColorListLabelProvider());
		fHighlightingColorListViewer.setContentProvider(new ColorListContentProvider());
		fHighlightingColorListViewer.setComparator(new WorkbenchViewerComparator());
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fHighlightingColorListViewer.getControl().setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fSyntaxForegroundColorEditor = new ColorSelector(stylesComposite);
		Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.bold")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fBoldCheckBox.setLayoutData(gd);

		fItalicCheckBox = new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.italic")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fItalicCheckBox.setLayoutData(gd);

		fHighlightingColorListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleSyntaxColorListSelection();
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item = getHighlightingColorListItem();
				PreferenceConverter.setValue(getOverlayStore(), item.getColorKey(),
						fSyntaxForegroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item = getHighlightingColorListItem();
				getOverlayStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});

		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				HighlightingColorListItem item = getHighlightingColorListItem();
				getOverlayStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});

		return colorComposite;
	}

	private Composite createFoldingTabContent(TabFolder folder) {
		Composite composite = new Composite(folder, SWT.NULL);
		// assume parent page uses griddata
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);

		/* check box for new editors */
		fFoldingCheckbox = new Button(composite, SWT.CHECK);
		fFoldingCheckbox.setText(MakefilePreferencesMessages.getString("MakefileEditorPreferencePage.foldingenable")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		fFoldingCheckbox.setLayoutData(gd);
		fFoldingCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = fFoldingCheckbox.getSelection();
				getOverlayStore().setValue(MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED, enabled);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return composite;
	}

	void handleSyntaxColorListSelection() {
		HighlightingColorListItem item = getHighlightingColorListItem();
		RGB rgb = PreferenceConverter.getColor(getOverlayStore(), item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		// Do not show "Bold" and "Italic" for matching brackets which is annotated as a highlighted box
		boolean isTextType = !MakefilePreferencesMessages
				.getString("MakefileEditorPreferencePage.makefile_editor_matching_bracket") //$NON-NLS-1$
				.equals(item.getDisplayName());
		fBoldCheckBox.setVisible(isTextType);
		fItalicCheckBox.setVisible(isTextType);
		if (isTextType) {
			fBoldCheckBox.setSelection(getOverlayStore().getBoolean(item.getBoldKey()));
			fItalicCheckBox.setSelection(getOverlayStore().getBoolean(item.getItalicKey()));
		}
	}

	/**
	 * Returns the current highlighting color list item.
	 *
	 * @return the current highlighting color list item
	 * @since 3.0
	 */
	HighlightingColorListItem getHighlightingColorListItem() {
		IStructuredSelection selection = (IStructuredSelection) fHighlightingColorListViewer.getSelection();
		return (HighlightingColorListItem) selection.getFirstElement();
	}

	@Override
	public boolean performOk() {
		return super.performOk();
	}

	public static void initDefaults(IPreferenceStore prefs) {
		// Makefile Editor color preferences
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_COMMENT_COLOR, ColorManager.MAKE_COMMENT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_DEFAULT_COLOR, ColorManager.MAKE_DEFAULT_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_FUNCTION_COLOR, ColorManager.MAKE_FUNCTION_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_KEYWORD_COLOR, ColorManager.MAKE_KEYWORD_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_MACRO_DEF_COLOR, ColorManager.MAKE_MACRO_DEF_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_MACRO_REF_COLOR, ColorManager.MAKE_MACRO_REF_RGB);
		PreferenceConverter.setDefault(prefs, ColorManager.MAKE_MATCHING_BRACKETS_COLOR,
				ColorManager.MAKE_MATCHING_BRACKETS_RGB);

		prefs.setDefault(ColorManager.MAKE_KEYWORD_COLOR + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		prefs.setDefault(ColorManager.MAKE_FUNCTION_COLOR + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		prefs.setDefault(ColorManager.MAKE_MACRO_DEF_COLOR + MakefileEditorPreferenceConstants.EDITOR_BOLD_SUFFIX,
				true);
	}

}
