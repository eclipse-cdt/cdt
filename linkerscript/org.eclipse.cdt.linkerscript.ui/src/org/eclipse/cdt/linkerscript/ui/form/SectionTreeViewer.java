/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class SectionTreeViewer extends AbstractLinkerScriptViewer {
	private Composite control;
	private TreeViewer sectionTreeViewer;
	private Button addSectionButton;
	private Button addAssignmentButton;
	private Button removeButton;

	private LinkerScriptUIUtils util = new LinkerScriptUIUtils();

	public class SectionsContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			return getModel().readModel(parentElement, OutputSection.class, NOOBJECTS, outputSection -> {
				Resource resource = outputSection.eResource();
				return outputSection.getStatements().stream().map(resource::getURIFragment).toArray();
			});
		}

		@Override
		public boolean hasChildren(Object element) {
			return getModel().readModel(element, OutputSection.class, false, outputSection -> {
				return !outputSection.getStatements().isEmpty();
			});
		}

		@Override
		public Object getParent(Object element) {
			return getModel().readModel(element, EObject.class, null, obj -> {
				EObject container = obj.eContainer();
				if (container == null) {
					return null;
				}
				return container.eResource().getURIFragment(container);
			});
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getModel().readModel("/", LinkerScript.class, NOOBJECTS, ld -> {
				Resource resource = ld.eResource();
				return ld.getStatements().stream().filter(SectionsCommand.class::isInstance)
						.map(SectionsCommand.class::cast).flatMap(cc -> cc.getSectionCommands().stream())
						.map(resource::getURIFragment).toArray();
			});

		}

		@Override
		public void dispose() {
			// in newer Eclipse, this method has a default implementation
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// in newer Eclipse, this method has a default implementation
		}
	}

	private final class SectionsColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return getModel().readModel(element, EObject.class, null, t -> {
				if (t instanceof OutputSection) {
					OutputSection outputSection = (OutputSection) t;
					StringBuilder sb = new StringBuilder(outputSection.getName());
					if (outputSection.getAddress() != null) {
						sb.append(" ( ");
						sb.append(util.expressionToString(outputSection.getAddress()));
						sb.append(" )");
					}

					if (outputSection.getMemory() != null && !outputSection.getMemory().isEmpty()) {
						sb.append(" > ");
						sb.append(outputSection.getMemory());
					}
					return sb.toString();
				}
				return NodeModelUtils.getTokenText(NodeModelUtils.getNode(t));
			});
		}

		@Override
		public Image getImage(Object element) {
			if (!isWorkbenchRunning())
				return null;
			return getModel().readModel(element, EObject.class, null, t -> {
				if (t instanceof OutputSection) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				} else if (t instanceof StatementInputSection) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
				} else if (t instanceof StatementAssignment) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK);
				}
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			});
		}

	}

	public SectionTreeViewer(Composite parent, FormToolkit toolkit) {
		super(parent.getDisplay());

		control = toolkit.createComposite(parent, SWT.WRAP);
		control.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		Tree tree = toolkit.createTree(control, SWT.MULTI | SWT.BORDER);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(-1, 300).create());
		toolkit.paintBordersFor(control);

		Composite buttomComp = toolkit.createComposite(control, SWT.WRAP);
		buttomComp.setLayout(GridLayoutFactory.fillDefaults().create());
		buttomComp.setLayoutData(GridDataFactory.fillDefaults().create());

		addSectionButton = util.createButton(buttomComp, "Add Section", toolkit);
		addAssignmentButton = util.createButton(buttomComp, "Add Assignment", toolkit);
		removeButton = util.createButton(buttomComp, "Remove", toolkit);

		addSectionButton.addListener(SWT.Selection, e -> addSection());
		addAssignmentButton.addListener(SWT.Selection, e -> addAssignment());
		removeButton.addListener(SWT.Selection, e -> remove());

		sectionTreeViewer = new TreeViewer(tree);
		sectionTreeViewer.setContentProvider(new SectionsContentProvider());
		sectionTreeViewer.setLabelProvider(new SectionsColumnLabelProvider());

		sectionTreeViewer.getTree().setLinesVisible(true);
		sectionTreeViewer.getTree().setHeaderVisible(false);
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	protected Viewer getViewer() {
		return sectionTreeViewer;
	}

	/**
	 * Return the underlying tree viewer control
	 */
	public TreeViewer getTreeViewer() {
		return sectionTreeViewer;
	}

	/**
	 * Add a new output section to the end of the list
	 */
	public void addSection() {
		getModel().writeResource(resource -> {
			LinkerScript ld = LinkerScriptModelUtils.getOrCreateLinkerScript(resource);
			SectionsCommand secCmd = LinkerScriptModelUtils.getOrCreateLastSectionsCommand(ld);
			LinkerScriptModelUtils.createOutputSection(secCmd);
		});
		refresh();
	}

	/**
	 * Add a new assignment to the end of the list
	 */
	public void addAssignment() {
		getModel().writeResource(resource -> {
			LinkerScript ld = LinkerScriptModelUtils.getOrCreateLinkerScript(resource);
			SectionsCommand secCmd = LinkerScriptModelUtils.getOrCreateLastSectionsCommand(ld);
			LinkerScriptModelUtils.createAssignment(secCmd);
		});
		refresh();
	}

	public void remove() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			getSelectedObjects(ld).forEach(sel -> {
				EcoreUtil2.delete(sel, true);
			});
		});
		refresh();
	}

	/**
	 * Must only be called from within a model.writeModel or model.readModel
	 */
	private List<EObject> getSelectedObjects(LinkerScript ld) {
		Resource resource = ld.eResource();
		List<?> selection = sectionTreeViewer.getStructuredSelection().toList();
		List<EObject> collect = selection.stream().map(sel -> {
			if (sel instanceof String) {
				return resource.getEObject((String) sel);
			}
			return null;
		}).filter(selObj -> selObj != null).collect(Collectors.toList());
		return collect;
	}

}
