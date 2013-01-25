/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;

/**
 * The preference block for configuring styles of different categories of include statements.
 */
public class IncludeCategoriesBlock extends OptionsConfigurationBlock {
	private static final Key KEY_STYLE_GROUP_RELATED = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_GROUP_RELATED);
	private static final Key KEY_STYLE_PARTNER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_PARTNER);
	private static final Key KEY_STYLE_GROUP_PARTNER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_GROUP_PARTNER);
	private static final Key KEY_STYLE_SAME_FOLDER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_SAME_FOLDER);
	private static final Key KEY_STYLE_GROUP_SAME_FOLDER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_GROUP_SAME_FOLDER);
	private static final Key KEY_STYLE_SUBFOLDER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_SUBFOLDER);
	private static final Key KEY_STYLE_GROUP_SUBFOLDER = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_GROUP_SUBFOLDER);
	private static final Key KEY_STYLE_GROUP_SYSTEM = getCDTUIKey(IncludePreferences.INCLUDE_STYLE_GROUP_SYSTEM);

	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_STYLE_GROUP_RELATED,
				KEY_STYLE_PARTNER,
				KEY_STYLE_GROUP_PARTNER,
				KEY_STYLE_SAME_FOLDER,
				KEY_STYLE_GROUP_SAME_FOLDER,
				KEY_STYLE_SUBFOLDER,
				KEY_STYLE_GROUP_SUBFOLDER,
				KEY_STYLE_GROUP_SYSTEM,
			};
	}

	private final Category[] rootCategories; 
	private TreeListDialogField<Category> categoryTree;
	private PixelConverter pixelConverter;
	private StackLayout editorAreaStack;
	private Category selectedCategory;

	public IncludeCategoriesBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		rootCategories = createCategories();
	}

	private static Category[] createCategories() {
		Category related = new Category(PreferencesMessages.IncludeCategoriesBlock_related_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_related_headers_node_description)
				.setGroupingKey(KEY_STYLE_GROUP_RELATED);
		new Category(PreferencesMessages.IncludeCategoriesBlock_partner_header_node,
				PreferencesMessages.IncludeCategoriesBlock_partner_header_node_description, related)
				.setIncludeKind(IncludeKind.PARTNER)
				.setGroupingKey(KEY_STYLE_GROUP_PARTNER)
				.setStyleKey(KEY_STYLE_PARTNER);
		new Category(PreferencesMessages.IncludeCategoriesBlock_same_folder_header_node,
				PreferencesMessages.IncludeCategoriesBlock_same_folder_header_node_description, related)
				.setIncludeKind(IncludeKind.IN_SAME_FOLDER)
				.setGroupingKey(KEY_STYLE_GROUP_SAME_FOLDER)
				.setStyleKey(KEY_STYLE_SAME_FOLDER);
		new Category(PreferencesMessages.IncludeCategoriesBlock_subfolder_header_node,
				PreferencesMessages.IncludeCategoriesBlock_subfolder_header_node_description, related)
				.setIncludeKind(IncludeKind.IN_SUBFOLDERS)
				.setGroupingKey(KEY_STYLE_GROUP_SUBFOLDER)
				.setStyleKey(KEY_STYLE_SUBFOLDER);
		Category system = new Category(PreferencesMessages.IncludeCategoriesBlock_system_headers_node,
				PreferencesMessages.IncludeCategoriesBlock_system_headers_node_description)
				.setGroupingKey(KEY_STYLE_GROUP_SYSTEM);
		return new Category[] { related, system };
	}

	public void postSetSelection(Object element) {
		categoryTree.postSetSelection(new StructuredSelection(element));
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

		IncludeStyleAdapter adapter = new IncludeStyleAdapter();
		categoryTree = new TreeListDialogField<Category>(adapter, null, new IncludeStyleLabelProvider());
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
		IncludeGroupStyle style = null;
		Key styleKey = category.getStyleKey();
		if (styleKey != null) {
			IncludeKind includeKind = category.getIncludeKind();
			String str = getValue(styleKey);
			if (str != null)
				style = IncludeGroupStyle.fromString(str, includeKind);
			if (style == null)
				style = new IncludeGroupStyle(includeKind);
		}
		IncludeGroupStyleBlock block = new IncludeGroupStyleBlock(fContext, fProject, fContainer,
				category.getDescription(), category.getGroupingKey(), style);
		Control composite = block.createContents(parent);

		category.setEditorArea(composite);

		for (Category child : category.getChildren()) {
			createCategoryEditor(parent, child);
		}
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		// XXX Implement
	}

	private void updateConfigurationBlock(List<Object> selection) {
		if (selection.size() == 0)
			return;
		selectedCategory = (Category) selection.get(0);
		editorAreaStack.topControl = selectedCategory.getEditorArea();
		editorAreaStack.topControl.getParent().layout();
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
		private IncludeKind includeKind;
		private Key styleKey;
		private Key groupingKey;

		private Control editorArea;

		Category(String name, String description, Category parent) {
			this.name = name;
			this.description = description;
			this.parent = parent;
			children = new ArrayList<Category>();
			index = parent != null ? parent.addChild(this) : 0;
		}

		/**
		 * @param name Category name
		 */
		Category(String name, String description) {
		    this(name, description, null);
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

		IncludeKind getIncludeKind() {
			return includeKind;
		}

		Category setIncludeKind(IncludeKind includeKind) {
			this.includeKind = includeKind;
			return this;
		}

		Key getStyleKey() {
			return styleKey;
		}

		Category setStyleKey(Key key) {
			this.styleKey = key;
			return this;
		}

		Key getGroupingKey() {
			return groupingKey;
		}

		Category setGroupingKey(Key key) {
			this.groupingKey = key;
			return this;
		}

		Control getEditorArea() {
			return editorArea;
		}

		Category setEditorArea(Control editorArea) {
			this.editorArea = editorArea;
			return this;
		}

		public String getDescription() {
			return description;
		}
	}

	private class IncludeStyleAdapter extends ViewerComparator
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

	private static class IncludeStyleLabelProvider extends LabelProvider {
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
