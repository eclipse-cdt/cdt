/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
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

/**
 * The preference block for configuring styles of different categories of include statements.
 */
public class IncludeCategoriesBlock extends OptionsConfigurationBlock {
	private final List<IncludeGroupStyle> styles;
	private final Map<IncludeKind, Category> categories = new HashMap<>();
	private TreeListDialogField<Category> categoryTree;
	private PixelConverter pixelConverter;
	private StackLayout editorAreaStack;
	private Category selectedCategory;

	public IncludeCategoriesBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container, List<IncludeGroupStyle> styles) {
		super(context, project, new Key[0], container);
		this.styles = styles;
		createCategories();
	}

	private void createCategories() {
		createCategory(IncludeKind.RELATED);
		createCategory(IncludeKind.PARTNER);
		createCategory(IncludeKind.IN_SAME_FOLDER);
		createCategory(IncludeKind.IN_SUBFOLDER);
		createCategory(IncludeKind.SYSTEM);
		createCategory(IncludeKind.SYSTEM_WITH_EXTENSION);
		createCategory(IncludeKind.SYSTEM_WITHOUT_EXTENSION);
		createCategory(IncludeKind.OTHER);
		createCategory(IncludeKind.IN_SAME_PROJECT);
		createCategory(IncludeKind.IN_OTHER_PROJECT);
		createCategory(IncludeKind.EXTERNAL);
	}

	private Category createCategory(IncludeKind includeKind) {
		Category parentCategory = categories.get(includeKind.parent);
		Category category = new Category(includeKind, parentCategory);
		categories.put(category.getIncludeKind(), category);
		return category;
	}

	public void postSetSelection(Object element) {
		categoryTree.postSetSelection(new StructuredSelection(element));
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter = new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		IncludeStyleAdapter adapter = new IncludeStyleAdapter();
		categoryTree = new TreeListDialogField<>(adapter, null, new IncludeStyleLabelProvider());
		categoryTree.setDialogFieldListener(adapter);
		categoryTree.setLabelText(PreferencesMessages.IncludeCategoriesBlock_header_categories);
		categoryTree.setViewerComparator(adapter);

		for (Category category : categories.values()) {
			if (category.parent == null)
				categoryTree.addElement(category);
		}

		Label label = categoryTree.getLabelControl(composite);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

		Control tree = categoryTree.getTreeControl(composite);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = pixelConverter.convertWidthInCharsToPixels(50);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(2);
		tree.setLayoutData(gd);

		createCategoryEditors(composite);

		categoryTree.setTreeExpansionLevel(2);
		categoryTree.selectFirstElement();

		updateControls();
		return composite;
	}

	private void createCategoryEditors(Composite parent) {
		Composite editorArea = new Composite(parent, SWT.NONE);
		editorArea.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		editorArea.setFont(parent.getFont());
		editorAreaStack = new StackLayout();
		editorArea.setLayout(editorAreaStack);
		Map<IncludeKind, IncludeGroupStyle> stylesByKind = new HashMap<>();
		for (IncludeGroupStyle style : styles) {
			if (style.getIncludeKind() != IncludeKind.MATCHING_PATTERN)
				stylesByKind.put(style.getIncludeKind(), style);
		}

		for (Category category : categories.values()) {
			IncludeGroupStyleBlock block = new IncludeGroupStyleBlock(fContext, fProject, fContainer,
					category.getDescription());
			IncludeGroupStyle style = stylesByKind.get(category.getIncludeKind());
			block.setStyle(style);
			Control composite = block.createContents(editorArea);
			category.setEditor(block, composite);
		}
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		// Refresh
		categoryTree.refresh();
		updateConfigurationBlock(categoryTree.getSelectedElements());
		for (Category category : categories.values()) {
			category.getEditor().updateControls();
		}
	}

	private void updateConfigurationBlock(List<Object> selection) {
		if (selection.size() == 0)
			return;
		selectedCategory = (Category) selection.get(0);
		editorAreaStack.topControl = selectedCategory.getEditorArea();
		editorAreaStack.topControl.getParent().layout();
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		fContext.statusChanged(new StatusInfo());
	}

	/**
	 * Represents a category of settings.
	 */
	private final static class Category {
		public final Category parent;
		public final int index; // Index in the siblings list
		private final List<Category> children;
		private final IncludeKind includeKind;
		private Control editorArea;
		private IncludeGroupStyleBlock editor;

		Category(IncludeKind includeKind, Category parent) {
			this.includeKind = includeKind;
			this.parent = parent;
			children = new ArrayList<>();
			index = parent != null ? parent.addChild(this) : 0;
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
			return includeKind.name;
		}

		IncludeKind getIncludeKind() {
			return includeKind;
		}

		IncludeGroupStyleBlock getEditor() {
			return editor;
		}

		Control getEditorArea() {
			return editorArea;
		}

		void setEditor(IncludeGroupStyleBlock editor, Control editorArea) {
			this.editor = editor;
			this.editorArea = editorArea;
		}

		String getName() {
			return includeKind.name;
		}

		String getDescription() {
			return includeKind.description;
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
			return ((Category) element).getName();
		}
	}
}
