/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class LibraryTree extends FilteredTree {

	private static final String PLATFORMS = "Platform Libraries";
	private static final String UNCATEGORIZED = "Others";

	private boolean includePlatforms;
	private Set<ArduinoLibrary> checkedLibs = new HashSet<>();
	private ArduinoManager manager = Activator.getService(ArduinoManager.class);

	private static class LibPatternFilter extends PatternFilter {
		@Override
		protected boolean isLeafMatch(Viewer viewer, Object element) {
			if (element instanceof String) {
				return wordMatches((String) element);
			} else if (element instanceof ArduinoLibrary) {
				ArduinoLibrary lib = (ArduinoLibrary) element;
				return wordMatches(lib.getName()) || wordMatches(lib.getSentence()) || wordMatches(lib.getParagraph());
			} else {
				return false;
			}
		}
	}

	public class ContentProvider implements ITreeContentProvider {
		public Map<String, List<ArduinoLibrary>> categories = new HashMap<>();
		public List<ArduinoLibrary> uncategorized;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				return;
			}

			@SuppressWarnings("unchecked")
			Collection<ArduinoLibrary> libraries = (Collection<ArduinoLibrary>) newInput;
			for (ArduinoLibrary library : libraries) {
				if (library.getPlatform() == null) {
					String category = library.getCategory();
					if (category != null) {
						List<ArduinoLibrary> libs = categories.get(category);
						if (libs == null) {
							libs = new ArrayList<>();
							categories.put(category, libs);
						}
						libs.add(library);
					} else {
						if (uncategorized == null) {
							uncategorized = new ArrayList<>();
						}
						uncategorized.add(library);
					}
				}
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			List<String> elements = new ArrayList<>(categories.keySet());
			Collections.sort(elements, (o1, o2) -> o1.compareToIgnoreCase(o2));
			if (uncategorized != null) {
				elements.add(UNCATEGORIZED);
			}
			if (includePlatforms) {
				elements.add(PLATFORMS);
			}
			return elements.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof String) {
				if (parentElement == UNCATEGORIZED) {
					return uncategorized.toArray();
				} else if (parentElement == PLATFORMS) {
					List<ArduinoPlatform> platforms = new ArrayList<>();
					try {
						for (ArduinoPlatform platform : manager.getInstalledPlatforms()) {
							if (!platform.getLibraries().isEmpty()) {
								platforms.add(platform);
							}
						}
					} catch (CoreException e) {
						Activator.log(e);
					}
					Collections.sort(platforms, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
					return platforms.toArray();
				} else {
					String category = (String) parentElement;
					List<ArduinoLibrary> libs = categories.get(category);
					Collections.sort(libs, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
					return libs.toArray();
				}
			} else if (parentElement instanceof ArduinoPlatform) {
				try {
					List<ArduinoLibrary> libs = new ArrayList<>(((ArduinoPlatform) parentElement).getLibraries());
					Collections.sort(libs, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
					return libs.toArray();
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof ArduinoLibrary) {
				ArduinoLibrary library = (ArduinoLibrary) element;
				ArduinoPlatform platform = library.getPlatform();
				if (platform != null) {
					return platform;
				}

				String category = library.getCategory();
				return category != null ? category : UNCATEGORIZED;
			} else if (element instanceof ArduinoPlatform) {
				return ((ArduinoPlatform) element).getName();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element instanceof String || element instanceof ArduinoPlatform;
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
			} else if (element instanceof ArduinoPlatform) {
				return columnIndex == 0 ? ((ArduinoPlatform) element).getName() : null;
			} else if (element instanceof ArduinoLibrary) {
				ArduinoLibrary library = (ArduinoLibrary) element;
				switch (columnIndex) {
				case 0:
					return library.getName();
				case 1:
					return library.getVersion();
				case 2:
					return library.getSentence();
				default:
					return null;
				}
			} else {
				return null;
			}
		}

	}

	public LibraryTree(Composite parent) {
		super(parent, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, new LibPatternFilter(), true);

		TreeViewer viewer = getViewer();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText("Library");
		column1.setWidth(200);
		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
		column2.setText("Version");
		column2.setWidth(100);
		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
		column3.setText("Description");
		column3.setWidth(300);
	}

	public void setIncludePlatforms(boolean includePlatforms) {
		this.includePlatforms = includePlatforms;
	}

	public void setChecked(Collection<ArduinoLibrary> checkedLibs) {
		this.checkedLibs = new HashSet<>(checkedLibs);
	}

	public Collection<ArduinoLibrary> getChecked() {
		return checkedLibs;
	}

	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		CheckboxTreeViewer viewer = new CheckboxTreeViewer(parent, style);
		viewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				if (element instanceof String) {
					String category = (String) element;
					if (category == PLATFORMS) {
						for (ArduinoLibrary lib : checkedLibs) {
							if (lib.getPlatform() != null) {
								return true;
							}
						}
					} else if (category == UNCATEGORIZED) {
						for (ArduinoLibrary lib : checkedLibs) {
							if (lib.getPlatform() == null && lib.getCategory() == null) {
								return true;
							}
						}
					} else {
						for (ArduinoLibrary lib : checkedLibs) {
							if (element.equals(lib.getCategory())) {
								return true;
							}
						}
					}
				} else if (element instanceof ArduinoPlatform) {
					for (ArduinoLibrary lib : checkedLibs) {
						if (element == lib.getPlatform()) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				if (element instanceof ArduinoLibrary) {
					return checkedLibs.contains(element);
				} else {
					return isGrayed(element);
				}
			}
		});

		viewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (element instanceof ArduinoLibrary) {
					if (event.getChecked()) {
						checkedLibs.add((ArduinoLibrary) element);
					} else {
						checkedLibs.remove(element);
					}
				} else if (element instanceof String) {
					if (!event.getChecked()) {
						for (ArduinoLibrary lib : new ArrayList<>(checkedLibs)) {
							if (element.equals(lib.getCategory())) {
								checkedLibs.remove(lib);
							}
						}
					}
				}
			}
		});
		return viewer;
	}
}
