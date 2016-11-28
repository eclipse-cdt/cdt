/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand;
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.EcoreUtil2;

public class MemoryTableViewer extends AbstractLinkerScriptViewer {
	public enum COLUMN {
		NAME(0), ORIGIN(1), LENGTH(2);
		public final int COLUMN_INDEX;

		COLUMN(int column) {
			this.COLUMN_INDEX = column;
		}
	}

	private Composite control;
	TableViewer memoryTableViewer;

	private LinkerScriptEditingSupport lengthEditingSupport;
	private LinkerScriptEditingSupport originEditingSupport;
	private LinkerScriptEditingSupport nameEditingSupport;

	private Button addButton;
	private Button removeButton;
	// private Button upButton;
	// private Button downButton;

	private LinkerScriptUIUtils util = new LinkerScriptUIUtils();

	public class MemoryContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return getModel().readModel("/", LinkerScript.class, NOOBJECTS, ld -> {
				Resource resource = ld.eResource();
				return LinkerScriptModelUtils.getAllMemories(ld).stream().map(resource::getURIFragment).toArray();

			});
		}
	}

	public MemoryTableViewer(Composite parent, FormToolkit toolkit) {
		super(parent.getDisplay());

		control = toolkit.createComposite(parent, SWT.WRAP);
		control.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		Table table = toolkit.createTable(control, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).hint(-1, 300).create());
		toolkit.paintBordersFor(control);

		Composite buttomComp = toolkit.createComposite(control, SWT.WRAP);
		buttomComp.setLayout(GridLayoutFactory.fillDefaults().create());
		buttomComp.setLayoutData(GridDataFactory.fillDefaults().create());
		addButton = util.createButton(buttomComp, "Add", toolkit);
		removeButton = util.createButton(buttomComp, "Remove", toolkit);

		addButton.addListener(SWT.Selection, e -> add());
		removeButton.addListener(SWT.Selection, e -> remove());

		// TODO up and down are disabled because UX of them is
		// bad without custom URIs
		// upButton = util.createButton(buttomComp, "Up", toolkit);
		// downButton = util.createButton(buttomComp, "Down", toolkit);
		// upButton.addListener(SWT.Selection, e -> up());
		// downButton.addListener(SWT.Selection, e -> down());

		memoryTableViewer = new TableViewer(table);
		memoryTableViewer.setContentProvider(new MemoryContentProvider());

		TableColumn nameColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		nameColumn.setWidth(150);
		nameColumn.setText("Region Name");
		TableViewerColumn nameViewerColumn = new TableViewerColumn(memoryTableViewer, nameColumn);
		nameViewerColumn
				.setLabelProvider(new MemoryLabelProvider(this::getModel, Memory::getName, isWorkbenchRunning()));
		nameEditingSupport = new LinkerScriptEditingSupport(memoryTableViewer, value -> {
			if (!(value instanceof String) || ((String) value).contains("\"")) {
				return "Invalid Name";
			}
			return null;

		}, mem -> mem.getName(), (uri, val) -> getModel().writeModel(uri, Memory.class, mem -> mem.setName(val)));
		nameViewerColumn.setEditingSupport(nameEditingSupport);

		TableColumn originColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		originColumn.setWidth(150);
		originColumn.setText("Start Address");
		originColumn.setToolTipText("Origin of the memory region");
		TableViewerColumn originViewerColumn = new TableViewerColumn(memoryTableViewer, originColumn);
		originViewerColumn.setLabelProvider(
				new MemoryLabelProvider(this::getModel, mem -> util.expressionToString(mem.getOrigin()), false));
		originEditingSupport = new LinkerScriptEditingSupport(memoryTableViewer, null,
				mem -> util.expressionToString(mem.getOrigin()), (uri, val) -> {
					String uriOrigin = getModel().readModel(uri, Memory.class, null,
							mem -> mem.eResource().getURIFragment(mem.getOrigin()));
					getModel().writeText(uriOrigin, val);
				});
		originViewerColumn.setEditingSupport(originEditingSupport);

		TableColumn lengthColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		lengthColumn.setWidth(150);
		lengthColumn.setText("Length");
		lengthColumn.setToolTipText("Size of the memory region");
		TableViewerColumn lengthviewerColumn = new TableViewerColumn(memoryTableViewer, lengthColumn);
		lengthviewerColumn.setLabelProvider(
				new MemoryLabelProvider(this::getModel, mem -> util.expressionToString(mem.getLength()), false));
		lengthEditingSupport = new LinkerScriptEditingSupport(memoryTableViewer, null,
				mem -> util.expressionToString(mem.getLength()), (uri, val) -> {
					String uriLength = getModel().readModel(uri, Memory.class, null,
							mem -> mem.eResource().getURIFragment(mem.getLength()));
					getModel().writeText(uriLength, val);
				});
		lengthviewerColumn.setEditingSupport(lengthEditingSupport);

		memoryTableViewer.getTable().setLinesVisible(true);
		memoryTableViewer.getTable().setHeaderVisible(true);
	}

	/**
	 * Must only be called from within a model.writeModel or model.readModel
	 */
	private List<Memory> getSelectedMemories(LinkerScript ld) {
		Resource resource = ld.eResource();
		List<?> selection = memoryTableViewer.getStructuredSelection().toList();
		List<Memory> collect = selection.stream().map(sel -> {
			if (sel instanceof String) {
				EObject obj = resource.getEObject((String) sel);
				if (obj instanceof Memory) {
					return (Memory) obj;
				}
			}
			return null;
		}).filter(mem -> mem != null).collect(Collectors.toList());
		return collect;
	}

	private static class MemoriesWithIndex {
		private EList<Memory> memories;
		private int index;

		private MemoriesWithIndex(EList<Memory> memories, int index) {
			this.memories = memories;
			this.index = index;
		}
	}

	public void down() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			List<MemoriesWithIndex> selected = getSelectedMemoryIndexes(ld);
			Collections.sort(selected, Comparator.comparingInt((MemoriesWithIndex o) -> o.index).reversed());
			selected.forEach(entry -> {
				if (entry.index != entry.memories.size() - 1) {
					entry.memories.move(entry.index + 1, entry.index);
				}
			});
		});
		refresh();
	}

	public void up() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			List<MemoriesWithIndex> selected = getSelectedMemoryIndexes(ld);
			Collections.sort(selected, Comparator.comparingInt((MemoriesWithIndex o) -> o.index));
			selected.forEach(entry -> {
				if (entry.index != 0) {
					entry.memories.move(entry.index - 1, entry.index);
				}
			});
		});
		refresh();
	}

	/**
	 * Get the selected memories' index and the containing list that the index
	 * refers to
	 */
	private List<MemoriesWithIndex> getSelectedMemoryIndexes(LinkerScript ld) {
		List<Memory> selection = getSelectedMemories(ld);
		Stream<MemoriesWithIndex> map = selection.stream().map((Memory mem) -> {
			if (mem != null) {
				EObject container = mem.eContainer();
				if (container instanceof MemoryCommand) {
					MemoryCommand memoryCommand = (MemoryCommand) container;
					EList<Memory> list = memoryCommand.getMemories();
					int index = list.indexOf(mem);
					return new MemoriesWithIndex(list, index);
				}
			}
			return null;
		});
		Stream<MemoriesWithIndex> filter = map.filter(Objects::nonNull);
		return filter.collect(Collectors.toList());
	}

	public void remove() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			getSelectedMemories(ld).forEach(mem -> {
				EcoreUtil2.delete(mem, true);
			});
		});
		refresh();
	}

	/**
	 * Add a new memory to the end of the list
	 */
	public void add() {
		getModel().writeResource(resource -> {
			LinkerScript ld = LinkerScriptModelUtils.getOrCreateLinkerScript(resource);
			MemoryCommand memCmd = LinkerScriptModelUtils.getOrCreateLastMemoryCommand(ld);
			LinkerScriptModelUtils.createMemory(memCmd);
		});
		// getModel().writeModel("/", LinkerScript.class, ld -> {
		// MemoryCommand memCmd =
		// LinkerScriptModelUtils.getOrCreateLastMemoryCommand(ld);
		// LinkerScriptModelUtils.createMemory(memCmd);
		// });
		refresh();
	}

	private void updateEnabledButtons() {
		Table table = memoryTableViewer.getTable();
		int[] indices = table.getSelectionIndices();
		// int itemCount = table.getItemCount();
		// boolean firstSelected = false;
		// boolean lastSelected = false;
		// for (int i : indices) {
		// if (i == 0) {
		// firstSelected = true;
		// }
		// if (i == itemCount - 1) {
		// lastSelected = true;
		// }
		// }

		// addButton always enabled
		removeButton.setEnabled(indices.length > 0);
		// upButton.setEnabled(!firstSelected);
		// downButton.setEnabled(!lastSelected);
	}

	/**
	 * Return the underlying table viewer control
	 */
	public TableViewer getTableViewer() {
		return memoryTableViewer;
	}

	/** public for testing only */
	public ITextEditingSupport getColumnEditingSupport(COLUMN column) {
		switch (column) {
		case NAME:
			return nameEditingSupport;
		case ORIGIN:
			return originEditingSupport;
		case LENGTH:
			return lengthEditingSupport;
		}
		throw new AssertionError("Invalid column: " + column);
	}

	@Override
	public void refresh() {
		super.refresh();
		updateEnabledButtons();
	}

	@Override
	protected Viewer getViewer() {
		return memoryTableViewer;
	}

	@Override
	public Control getControl() {
		return control;
	}
}
