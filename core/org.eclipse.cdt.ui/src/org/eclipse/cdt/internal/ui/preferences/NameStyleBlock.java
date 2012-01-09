/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.util.NameComposer;
import org.eclipse.cdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;

/**
 * The preference block for configuring styles of names.
 */
public class NameStyleBlock extends OptionsConfigurationBlock {
	private static final String EXAMPLE_CONSTANT_NAME = "MY_CONSTANT"; //$NON-NLS-1$
	private static final String EXAMPLE_VARIABLE_NAME = "myVariable"; //$NON-NLS-1$
	private static final String EXAMPLE_FIELD_NAME = "myField"; //$NON-NLS-1$
	private static final String EXAMPLE_CLASS_NAME = "MyClass"; //$NON-NLS-1$

	private final String[] CAPITALIZATION_VALUES = {
			String.valueOf(PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL),
			String.valueOf(PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE),
			String.valueOf(PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE),
			String.valueOf(PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE),
			String.valueOf(PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE),
		};

	private final String[] CAPITALIZATION_LABELS = {
			String.valueOf(PreferencesMessages.NameStyleBlock_capitalization_original),
			String.valueOf(PreferencesMessages.NameStyleBlock_capitalization_upper_case),
			String.valueOf(PreferencesMessages.NameStyleBlock_capitalization_lower_case),
			String.valueOf(PreferencesMessages.NameStyleBlock_capitalization_camel_case),
			String.valueOf(PreferencesMessages.NameStyleBlock_capitalization_lower_camel_case),
		};

	private static final Key KEY_CONSTANT_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_CONSTANT_CAPITALIZATION);
	private static final Key KEY_CONSTANT_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_CONSTANT_WORD_DELIMITER);
	private static final Key KEY_CONSTANT_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CONSTANT_PREFIX);
	private static final Key KEY_CONSTANT_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CONSTANT_SUFFIX);
	private static final Key KEY_VARIABLE_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_VARIABLE_CAPITALIZATION);
	private static final Key KEY_VARIABLE_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_VARIABLE_WORD_DELIMITER);
	private static final Key KEY_VARIABLE_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_VARIABLE_PREFIX);
	private static final Key KEY_VARIABLE_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_VARIABLE_SUFFIX);
	private static final Key KEY_FIELD_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_FIELD_CAPITALIZATION);
	private static final Key KEY_FIELD_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_FIELD_WORD_DELIMITER);
	private static final Key KEY_FIELD_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_FIELD_PREFIX);
	private static final Key KEY_FIELD_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_FIELD_SUFFIX);
	private static final Key KEY_GETTER_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_GETTER_CAPITALIZATION);
	private static final Key KEY_GETTER_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_GETTER_WORD_DELIMITER);
	private static final Key KEY_GETTER_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_GETTER_PREFIX);
	private static final Key KEY_GETTER_PREFIX_FOR_BOOLEAN = getCDTUIKey(PreferenceConstants.NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN);
	private static final Key KEY_GETTER_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_GETTER_SUFFIX);
	private static final Key KEY_SETTER_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_SETTER_CAPITALIZATION);
	private static final Key KEY_SETTER_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_SETTER_WORD_DELIMITER);
	private static final Key KEY_SETTER_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_SETTER_PREFIX);
	private static final Key KEY_SETTER_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_SETTER_SUFFIX);
	private static final Key KEY_CPP_SOURCE_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_SOURCE_CAPITALIZATION);
	private static final Key KEY_CPP_SOURCE_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_SOURCE_WORD_DELIMITER);
	private static final Key KEY_CPP_SOURCE_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_SOURCE_PREFIX);
	private static final Key KEY_CPP_SOURCE_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_SOURCE_SUFFIX);
	private static final Key KEY_CPP_HEADER_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_HEADER_CAPITALIZATION);
	private static final Key KEY_CPP_HEADER_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_HEADER_WORD_DELIMITER);
	private static final Key KEY_CPP_HEADER_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_HEADER_PREFIX);
	private static final Key KEY_CPP_HEADER_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_HEADER_SUFFIX);
	private static final Key KEY_CPP_TEST_CAPITALIZATION = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_TEST_CAPITALIZATION);
	private static final Key KEY_CPP_TEST_WORD_DELIMITER = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_TEST_WORD_DELIMITER);
	private static final Key KEY_CPP_TEST_PREFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_TEST_PREFIX);
	private static final Key KEY_CPP_TEST_SUFFIX = getCDTUIKey(PreferenceConstants.NAME_STYLE_CPP_TEST_SUFFIX);

	private static final IdentifierValidator IDENTIFIER_VALIDATOR = new IdentifierValidator();
	private static final FilenameValidator FILENAME_VALIDATOR = new FilenameValidator();

	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_CONSTANT_CAPITALIZATION,
				KEY_CONSTANT_WORD_DELIMITER,
				KEY_CONSTANT_PREFIX,
				KEY_CONSTANT_SUFFIX,
				KEY_VARIABLE_CAPITALIZATION,
				KEY_VARIABLE_WORD_DELIMITER,
				KEY_VARIABLE_PREFIX,
				KEY_VARIABLE_SUFFIX,
				KEY_FIELD_CAPITALIZATION,
				KEY_FIELD_WORD_DELIMITER,
				KEY_FIELD_PREFIX,
				KEY_FIELD_SUFFIX,
				KEY_GETTER_CAPITALIZATION,
				KEY_GETTER_WORD_DELIMITER,
				KEY_GETTER_PREFIX,
				KEY_GETTER_PREFIX_FOR_BOOLEAN,
				KEY_GETTER_SUFFIX,
				KEY_SETTER_CAPITALIZATION,
				KEY_SETTER_WORD_DELIMITER,
				KEY_SETTER_PREFIX,
				KEY_SETTER_SUFFIX,
				KEY_CPP_SOURCE_CAPITALIZATION,
				KEY_CPP_SOURCE_WORD_DELIMITER,
				KEY_CPP_SOURCE_PREFIX,
				KEY_CPP_SOURCE_SUFFIX,
				KEY_CPP_HEADER_CAPITALIZATION,
				KEY_CPP_HEADER_WORD_DELIMITER,
				KEY_CPP_HEADER_PREFIX,
				KEY_CPP_HEADER_SUFFIX,
				KEY_CPP_TEST_CAPITALIZATION,
				KEY_CPP_TEST_WORD_DELIMITER,
				KEY_CPP_TEST_PREFIX,
				KEY_CPP_TEST_SUFFIX,
			};
	}

	private final Category[] rootCategories; 
	private TreeListDialogField<Category> categoryTree;
	private PixelConverter pixelConverter;
	private StackLayout editorAreaStack;
	private Category selectedCategory;

	public NameStyleBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		rootCategories = createCategories();
	}

	private static Category[] createCategories() {
		Category codeCategory = new Category(PreferencesMessages.NameStyleBlock_code_node); 
		new Category(PreferencesMessages.NameStyleBlock_constant_node,
				PreferencesMessages.NameStyleBlock_constant_node_description, EXAMPLE_CONSTANT_NAME,
				codeCategory)
				.setCapitalizationKey(KEY_CONSTANT_CAPITALIZATION)
				.setWordDelimiterKey(KEY_CONSTANT_WORD_DELIMITER)
				.setPrefixKey(KEY_CONSTANT_PREFIX)
				.setSuffixKey(KEY_CONSTANT_SUFFIX)
				.setNameValidator(IDENTIFIER_VALIDATOR);
		new Category(PreferencesMessages.NameStyleBlock_variable_node,
				PreferencesMessages.NameStyleBlock_variable_node_description, EXAMPLE_VARIABLE_NAME,
				codeCategory)
				.setCapitalizationKey(KEY_VARIABLE_CAPITALIZATION)
				.setWordDelimiterKey(KEY_VARIABLE_WORD_DELIMITER)
				.setPrefixKey(KEY_VARIABLE_PREFIX)
				.setSuffixKey(KEY_VARIABLE_SUFFIX)
				.setNameValidator(IDENTIFIER_VALIDATOR);
		Category fieldCategory = new Category(PreferencesMessages.NameStyleBlock_field_node,
				PreferencesMessages.NameStyleBlock_field_node_description, EXAMPLE_FIELD_NAME,
				codeCategory)
				.setCapitalizationKey(KEY_FIELD_CAPITALIZATION)
				.setWordDelimiterKey(KEY_FIELD_WORD_DELIMITER)
				.setPrefixKey(KEY_FIELD_PREFIX)
				.setSuffixKey(KEY_FIELD_SUFFIX)
				.setNameValidator(IDENTIFIER_VALIDATOR);
		new Category(PreferencesMessages.NameStyleBlock_getter_node,
				PreferencesMessages.NameStyleBlock_getter_node_description, EXAMPLE_FIELD_NAME,
				codeCategory)
				.setCapitalizationKey(KEY_GETTER_CAPITALIZATION)
				.setWordDelimiterKey(KEY_GETTER_WORD_DELIMITER)
				.setPrefixKey(KEY_GETTER_PREFIX)
				.setAlternativePrefixKey(KEY_GETTER_PREFIX_FOR_BOOLEAN)
				.setSuffixKey(KEY_GETTER_SUFFIX)
				.setSeedNameGenerator(fieldCategory)
				.setNameValidator(IDENTIFIER_VALIDATOR)
				.setTrimFieldName(true);
		new Category(PreferencesMessages.NameStyleBlock_setter_node,
				PreferencesMessages.NameStyleBlock_setter_node_description, EXAMPLE_FIELD_NAME,
				codeCategory)
				.setCapitalizationKey(KEY_SETTER_CAPITALIZATION)
				.setWordDelimiterKey(KEY_SETTER_WORD_DELIMITER)
				.setPrefixKey(KEY_SETTER_PREFIX)
				.setSuffixKey(KEY_SETTER_SUFFIX)
				.setSeedNameGenerator(fieldCategory)
				.setNameValidator(IDENTIFIER_VALIDATOR)
				.setTrimFieldName(true);
		Category fileCategory = new Category(PreferencesMessages.NameStyleBlock_files_node);
		new Category(PreferencesMessages.NameStyleBlock_cpp_header_node,
				PreferencesMessages.NameStyleBlock_cpp_header_node_description, EXAMPLE_CLASS_NAME,
				fileCategory)
				.setCapitalizationKey(KEY_CPP_HEADER_CAPITALIZATION)
				.setWordDelimiterKey(KEY_CPP_HEADER_WORD_DELIMITER)
				.setPrefixKey(KEY_CPP_HEADER_PREFIX)
				.setSuffixKey(KEY_CPP_HEADER_SUFFIX)
				.setNameValidator(FILENAME_VALIDATOR);
		new Category(PreferencesMessages.NameStyleBlock_cpp_source_node,
				PreferencesMessages.NameStyleBlock_cpp_source_node_description, EXAMPLE_CLASS_NAME,
				fileCategory)
				.setCapitalizationKey(KEY_CPP_SOURCE_CAPITALIZATION)
				.setWordDelimiterKey(KEY_CPP_SOURCE_WORD_DELIMITER)
				.setPrefixKey(KEY_CPP_SOURCE_PREFIX)
				.setSuffixKey(KEY_CPP_SOURCE_SUFFIX)
				.setNameValidator(FILENAME_VALIDATOR);
		new Category(PreferencesMessages.NameStyleBlock_cpp_test_node,
				PreferencesMessages.NameStyleBlock_cpp_test_node_description, EXAMPLE_CLASS_NAME,
				fileCategory)
				.setCapitalizationKey(KEY_CPP_TEST_CAPITALIZATION)
				.setWordDelimiterKey(KEY_CPP_TEST_WORD_DELIMITER)
				.setPrefixKey(KEY_CPP_TEST_PREFIX)
				.setSuffixKey(KEY_CPP_TEST_SUFFIX)
				.setNameValidator(FILENAME_VALIDATOR);
		return new Category[] { codeCategory, fileCategory };
	}

	public void postSetSelection(Object element) {
		categoryTree.postSetSelection(new StructuredSelection(element));
	}

	@Override
	public boolean hasProjectSpecificOptions(IProject project) {
		if (super.hasProjectSpecificOptions(project))
			return true;

		if (project != null) {
			return ProjectTemplateStore.hasProjectSpecificTempates(project);
		}
		return false;
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter =  new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite =  new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		NameStyleAdapter adapter = new NameStyleAdapter();
		categoryTree = new TreeListDialogField<Category>(adapter, null, new NameStyleLabelProvider());
		categoryTree.setDialogFieldListener(adapter);
		categoryTree.setLabelText(PreferencesMessages.NameStyleBlock_categories_label);
		categoryTree.setViewerComparator(adapter);

		createCategories();

		for (Category category : rootCategories) {
			categoryTree.addElement(category);
		}

		Label label = categoryTree.getLabelControl(composite);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		Control tree = categoryTree.getTreeControl(composite);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = pixelConverter.convertWidthInCharsToPixels(50);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(12);
		tree.setLayoutData(gd);

		createCategoryEditorArea(composite);

		categoryTree.setTreeExpansionLevel(2);
		categoryTree.selectFirstElement();

		updateControls();
		return composite;
	}

	private void createCategoryEditorArea(Composite parent) {
		Composite editorArea =  new Composite(parent, SWT.NONE);
		editorArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		editorArea.setFont(parent.getFont());
		editorAreaStack = new StackLayout();
		editorArea.setLayout(editorAreaStack);
		for (Category category : rootCategories) {
			createCategoryEditor(editorArea, category);
		}
	}

	private void createCategoryEditor(Composite parent, Category category) {
		Composite composite =  new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = pixelConverter.convertHeightInCharsToPixels(1);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		if (category.isConcrete()) {
			Group group = ControlFactory.createGroup(composite,	category.description, 1);

			Composite envelope = new Composite(group, SWT.NONE);
			layout = new GridLayout(4, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			envelope.setLayout(layout);

			Control control = addComboBox(envelope, PreferencesMessages.NameStyleBlock_capitalization_label,
					category.getCapitalizationKey(), CAPITALIZATION_VALUES,
					CAPITALIZATION_LABELS, 0);
			LayoutUtil.setHorizontalSpan(getLabel(control), 1);
			LayoutUtil.setHorizontalSpan(control, 3);
			control = addTextField(envelope, PreferencesMessages.NameStyleBlock_word_delimiter_label,
					category.getWordDelimiterKey(), 0, pixelConverter.convertWidthInCharsToPixels(10));
			LayoutUtil.setHorizontalSpan(control, 3);
			LayoutUtil.setHorizontalAlignment(control, SWT.BEGINNING);
			control = addTextField(envelope, PreferencesMessages.NameStyleBlock_prefix_label,
					category.getPrefixKey(), 0, pixelConverter.convertWidthInCharsToPixels(10));
			boolean getter = PreferencesMessages.NameStyleBlock_getter_node.equals(category.name);
			LayoutUtil.setHorizontalSpan(control, getter ? 1 : 3);
			LayoutUtil.setHorizontalAlignment(control, SWT.BEGINNING);
			if (getter) {
				control = addTextField(envelope, PreferencesMessages.NameStyleBlock_prefix_for_boolean_label,
						category.getAlternativePrefixKey(), pixelConverter.convertWidthInCharsToPixels(2),
						pixelConverter.convertWidthInCharsToPixels(10));
				LayoutUtil.setHorizontalSpan(control, 1);
				LayoutUtil.setHorizontalAlignment(control, SWT.BEGINNING);
			}
			control = addTextField(envelope, PreferencesMessages.NameStyleBlock_suffix_label,
					category.getSuffixKey(), 0, pixelConverter.convertWidthInCharsToPixels(10));
			LayoutUtil.setHorizontalSpan(control, 3);
			LayoutUtil.setHorizontalAlignment(control, SWT.BEGINNING);

			ControlFactory.insertSpace(envelope, 4, pixelConverter.convertHeightInCharsToPixels(1));
			ControlFactory.createLabel(envelope, PreferencesMessages.NameStyleBlock_preview_label);
			Text previewText = ControlFactory.createTextField(envelope, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
			LayoutUtil.setWidthHint(previewText, pixelConverter.convertWidthInCharsToPixels(35));
			LayoutUtil.setHorizontalSpan(previewText, 3);
			category.setPreviewControl(previewText);
		} else {
			ControlFactory.createLabel(composite, PreferencesMessages.NameStyleBlock_select_concrete_category);
		}
		category.setEditorArea(composite);

		for (Category child : category.getChildren()) {
			createCategoryEditor(parent, child);
		}
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		updatePreview();
	}

	private void updateConfigurationBlock(List<Object> selection) {
		if (selection.size() == 0)
			return;
		selectedCategory = (Category) selection.get(0);
		editorAreaStack.topControl = selectedCategory.getEditorArea();
		editorAreaStack.topControl.getParent().layout();
		updatePreview();
	}

	private void updatePreview() {
		Text text = selectedCategory.getPreviewControl();
		if (text != null) {
			text.setText(selectedCategory.composeExampleName(this));
		}
	}

	@Override
	public void performDefaults() {
		super.performDefaults();

		// Refresh
		categoryTree.refresh();
		updateConfigurationBlock(categoryTree.getSelectedElements());
	}

	@Override
	public boolean performOk() {
		return super.performOk();
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		StatusInfo status = new StatusInfo();
		if (selectedCategory != null && changedKey != null) {
			NameValidator validator = selectedCategory.getNameValidator();
			if (changedKey.equals(selectedCategory.getPrefixKey()) ||
					changedKey.equals(selectedCategory.getAlternativePrefixKey())) {
				if (!validator.isValidStart(newValue)) {
					status.setError(PreferencesMessages.NameStyleBlock_invalid_prefix);
				}
			} else if (changedKey.equals(selectedCategory.getWordDelimiterKey())) {
				if (!validator.isValidPart(newValue)) {
					status.setError(PreferencesMessages.NameStyleBlock_invalid_word_delimiter);
				}
			} else if (changedKey.equals(selectedCategory.getSuffixKey())) {
				if (!validator.isValidPart(newValue)) {
					status.setError(PreferencesMessages.NameStyleBlock_invalid_suffix);
				}
			}
		}
		updatePreview();
		fContext.statusChanged(status);
	}

    /**
     * Represents a category of settings.
     */
	private final static class Category {
		public final String name;
		public final String description;
		public final Category parent;
		public final int index;  // Index in the siblings list
		private final List<Category> children;
		private Key capitalizationKey;
		private Key wordDelimiterKey;
		private Key prefixKey;
		private Key alternativePrefixKey;
		private Key suffixKey;
		private final String seedName;
		private Category seedNameGenerator;
		private NameValidator nameValidator;

		private Text previewText;
		private Composite editorArea;
		private boolean trimFieldName = false;

		Category(String name, String description, String seedName, Category parent) {
			this.name = name;
			this.description = description;
			this.seedName = seedName;
			this.parent = parent;
			children = new ArrayList<Category>();
			index = parent != null ? parent.addChild(this) : 0;
		}

		/**
		 * @param name Category name
		 */
		Category(String name) {
		    this(name, null, null, null);
		}

		private int addChild(Category category) {
			children.add(category);
			return children.size() - 1;
		}

		Category[] getChildren() {
			return children.toArray(new Category[children.size()]);
		}

		boolean hasChildren() {
			return !children.isEmpty();
		}

		@Override
		public String toString() {
			return name;
		}

		Key getCapitalizationKey() {
			return capitalizationKey;
		}

		Category setCapitalizationKey(Key capitalizationKey) {
			this.capitalizationKey = capitalizationKey;
			return this;
		}

		Key getWordDelimiterKey() {
			return wordDelimiterKey;
		}

		Category setWordDelimiterKey(Key wordDelimiterKey) {
			this.wordDelimiterKey = wordDelimiterKey;
			return this;
		}

		Key getPrefixKey() {
			return prefixKey;
		}

		Category setPrefixKey(Key prefixKey) {
			this.prefixKey = prefixKey;
			return this;
		}

		Key getAlternativePrefixKey() {
			return alternativePrefixKey;
		}

		Category setAlternativePrefixKey(Key alternativePrefixKey) {
			this.alternativePrefixKey = alternativePrefixKey;
			return this;
		}

		Key getSuffixKey() {
			return suffixKey;
		}

		Category setSuffixKey(Key suffixKey) {
			this.suffixKey = suffixKey;
			return this;
		}

		boolean isConcrete() {
			return capitalizationKey != null;
		}

		Composite getEditorArea() {
			return editorArea;
		}

		Category setEditorArea(Composite editorArea) {
			this.editorArea = editorArea;
			return this;
		}

		Text getPreviewControl() {
			return previewText;
		}

		Category setPreviewControl(Text previewText) {
			this.previewText = previewText;
			return this;
		}

		NameValidator getNameValidator() {
			return nameValidator;
		}

		Category setNameValidator(NameValidator nameValidator) {
			this.nameValidator = nameValidator;
			return this;
		}

		Category setSeedNameGenerator(Category seedNameGenerator) {
			this.seedNameGenerator = seedNameGenerator;
			return this;
		}

		String composeExampleName(NameStyleBlock settings) {
			int capitalization = Integer.parseInt(settings.getValue(capitalizationKey));
			String wordDelimiter = settings.getValue(wordDelimiterKey);
			String prefix = settings.getValue(prefixKey);
			String suffix = settings.getValue(suffixKey);
			NameComposer composer = new NameComposer(capitalization, wordDelimiter, prefix, suffix);
			String name = seedNameGenerator != null ?
					seedNameGenerator.composeExampleName(settings) : seedName;
			if (trimFieldName) {
				name = NameComposer.trimFieldName(name);
			}
			return composer.compose(name);
		}

		void setTrimFieldName(boolean trimSeedName) {
			this.trimFieldName = trimSeedName;
		}
	}

	private abstract static class NameValidator {
		boolean isValidStart(String prefix) {
			for (int i = 0; i < prefix.length(); i++) {
				if (i == 0 ? !isValidStart(prefix.charAt(i)) : !isValidPart(prefix.charAt(i)))
					return false;
			}
			return true;
		}

		boolean isValidPart(String part) {
			for (int i = 0; i < part.length(); i++) {
				if (!isValidPart(part.charAt(i)))
					return false;
			}
			return true;
		}

		abstract boolean isValidStart(char ch);
		abstract boolean isValidPart(char ch);
	}

	private static class IdentifierValidator extends NameValidator {
		@Override
		boolean isValidStart(char ch) {
			return Character.isJavaIdentifierStart(ch);
		}

		@Override
		boolean isValidPart(char ch) {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	private static class FilenameValidator extends NameValidator {
		@Override
		boolean isValidStart(char ch) {
			return isValidPart(ch);
		}

		@Override
		boolean isValidPart(char ch) {
			return "\\/:*?<>|\" ".indexOf(ch) == -1; //$NON-NLS-1$
		}
	}

	private class NameStyleAdapter extends ViewerComparator
			implements ITreeListAdapter<Category>, IDialogFieldListener {

		@Override
		public void selectionChanged(TreeListDialogField<Category> field) {
			updateConfigurationBlock(field.getSelectedElements());
		}

		@Override
		public void customButtonPressed(TreeListDialogField<Category> field, int index) {
		}

		@Override
		public void doubleClicked(TreeListDialogField<Category> field) {
		}

		@Override
		public Category[] getChildren(TreeListDialogField<Category> field, Object element) {
			return ((Category) element).getChildren();
		}

		@Override
		public Category getParent(TreeListDialogField<Category> field, Object element) {
			return ((Category) element).parent;
		}

		@Override
		public boolean hasChildren(TreeListDialogField<Category> field, Object element) {
			return ((Category) element).hasChildren();
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
		}

		@Override
		public void keyPressed(TreeListDialogField<Category> field, KeyEvent event) {
		}

		@Override
		public int category(Object element) {
			return ((Category) element).index;
		}
	}

	private static class NameStyleLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			return ((Category) element).name;
		}
	}
}
