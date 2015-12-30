/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.LibraryIndex;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.PropertyPage;

public class LibrariesPropertyPage extends PropertyPage {

	private static ArduinoManager manager = Activator.getService(ArduinoManager.class);

	private class ContentProvider implements ITreeContentProvider {
		private LibraryIndex index;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			index = (LibraryIndex) newInput;
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof LibraryIndex) {
				return true;
			} else if (element instanceof String) { // category
				return true;
			} else if (element instanceof ArduinoLibrary) {
				return false;
			} else {
				return false;
			}
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof ArduinoLibrary) {
				ArduinoLibrary lib = (ArduinoLibrary) element;
				String category = lib.getCategory();
				return category != null ? category : LibraryIndex.UNCATEGORIZED;
			} else if (element instanceof String || element instanceof ArduinoPlatform) {
				return index;
			} else {
				return null;
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			Set<String> categories = new HashSet<>();
			categories.addAll(((LibraryIndex) inputElement).getCategories());

			try {
				for (ArduinoLibrary lib : getPlatform().getLibraries()) {
					String category = lib.getCategory();
					categories.add(category != null ? category : LibraryIndex.UNCATEGORIZED);
				}
			} catch (CoreException e) {
				Activator.log(e);
			}

			return categories.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof String) {
				String category = (String) parentElement;
				List<ArduinoLibrary> libs = new ArrayList<>();
				libs.addAll(index.getLibraries(category));
				try {
					for (ArduinoLibrary lib : getPlatform().getLibraries()) {
						String cat = lib.getCategory();
						if (cat != null) {
							if (cat.equals(category)) {
								libs.add(lib);
							}
						} else if (category.equals(LibraryIndex.UNCATEGORIZED)) { // cat
																					// ==
																					// null
							libs.add(lib);
						}
					}
				} catch (CoreException e) {
					Activator.log(e);
				}
				return libs.toArray();
			} else {
				return new Object[0];
			}
		}
	}

	private static class LabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				return columnIndex == 0 ? (String) element : null;
			} else if (element instanceof ArduinoLibrary) {
				switch (columnIndex) {
				case 0:
					return ((ArduinoLibrary) element).getName();
				case 1:
					return ((ArduinoLibrary) element).getSentence();
				default:
					return null;
				}
			} else {
				return null;
			}
		}

	}

	private FilteredTree filteredTree;

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout());

		Text desc = new Text(comp, SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.LEFT, SWT.FILL, true, false);
		layoutData.widthHint = 500;
		desc.setLayoutData(layoutData);
		desc.setBackground(parent.getBackground());
		desc.setText(Messages.LibrariesPropertyPage_desc);

		filteredTree = new FilteredTree(comp, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL,
				new PatternFilter() {
					@Override
					protected boolean isLeafMatch(Viewer viewer, Object element) {
						if (element instanceof String) {
							return wordMatches((String) element);
						} else if (element instanceof ArduinoLibrary) {
							ArduinoLibrary lib = (ArduinoLibrary) element;
							return wordMatches(lib.getName()) || wordMatches(lib.getSentence())
									|| wordMatches(lib.getParagraph());
						} else {
							return false;
						}
					}
				}, true) {

			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new ContainerCheckedTreeViewer(parent, style);
			}
		};
		filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ContainerCheckedTreeViewer viewer = (ContainerCheckedTreeViewer) filteredTree.getViewer();

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText(Messages.LibrariesPropertyPage_0);
		column1.setWidth(200);
		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setText(Messages.LibrariesPropertyPage_1);
		column2.setWidth(200);

		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());

		try {
			viewer.setInput(manager.getLibraryIndex());
			// Set the check states for currently selected libraries
			IProject project = getElement().getAdapter(IProject.class);
			Collection<ArduinoLibrary> libraries = manager.getLibraries(project);
			for (ArduinoLibrary lib : libraries) {
				viewer.setChecked(lib, true);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return comp;

	}

	private IProject getProject() {
		return getElement().getAdapter(IProject.class);
	}

	private ArduinoPlatform getPlatform() throws CoreException {
		return getProject().getActiveBuildConfig().getAdapter(ArduinoBuildConfiguration.class).getBoard().getPlatform();
	}

	@Override
	public boolean performOk() {
		List<ArduinoLibrary> libs = new ArrayList<>();
		for (TreeItem categoryItem : filteredTree.getViewer().getTree().getItems()) {
			for (TreeItem libItem : categoryItem.getItems()) {
				ArduinoLibrary lib = (ArduinoLibrary) libItem.getData();
				if (libItem.getChecked()) {
					libs.add(lib);
				}
			}
		}
		try {
			manager.setLibraries(getProject(), libs);
		} catch (CoreException e) {
			Activator.log(e);
		}
		return true;
	}

}
