package org.eclipse.cdt.linkerscript.ui.form;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionCommand;
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand;
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class SectionTreeViewer extends AbstractLinkerScriptViewer {
	private Composite control;
	private TreeViewer sectionTreeViewer;
	private Button addSectionButton;
	private Button addAssignmentButton;
	private Button addStatementButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;

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
						sb.append(expressionToString(outputSection.getAddress()));
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

	}

	public SectionTreeViewer(Composite parent, FormToolkit toolkit) {
		super(parent.getDisplay());

		control = toolkit.createComposite(parent, SWT.WRAP);
		control.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		Tree tree = toolkit.createTree(control, SWT.MULTI);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(-1, 300).create());
		toolkit.paintBordersFor(control);

		Composite buttomComp = toolkit.createComposite(control, SWT.WRAP);
		buttomComp.setLayout(GridLayoutFactory.fillDefaults().create());
		buttomComp.setLayoutData(GridDataFactory.fillDefaults().create());

		addSectionButton = createButton(buttomComp, "Add Section", toolkit);
		addAssignmentButton = createButton(buttomComp, "Add Assignment", toolkit);
		addStatementButton = createButton(buttomComp, "Add Statement", toolkit);
		removeButton = createButton(buttomComp, "Remove", toolkit);
		upButton = createButton(buttomComp, "Up", toolkit);
		downButton = createButton(buttomComp, "Down", toolkit);

		addSectionButton.addListener(SWT.Selection, e -> addSection());
		addAssignmentButton.addListener(SWT.Selection, e -> addAssignment());
		addStatementButton.addListener(SWT.Selection, e -> addStatement());
		removeButton.addListener(SWT.Selection, e -> remove());
		upButton.addListener(SWT.Selection, e -> up());
		downButton.addListener(SWT.Selection, e -> down());

		sectionTreeViewer = new TreeViewer(tree);
		sectionTreeViewer.setContentProvider(new SectionsContentProvider());
		sectionTreeViewer.setLabelProvider(new SectionsColumnLabelProvider());

		sectionTreeViewer.getTree().setLinesVisible(true);
		sectionTreeViewer.getTree().setHeaderVisible(true);
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
		getModel().writeModel("/", LinkerScript.class, ld -> {
			SectionsCommand secCmd = LinkerScriptModelUtils.getOrCreateLastSectionsCommand(ld);
			LinkerScriptModelUtils.createOutputSection(secCmd);
		});
		refresh();
	}

	/**
	 * Add a new output section to the end of the list
	 */
	public void addAssignment() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			SectionsCommand secCmd = LinkerScriptModelUtils.getOrCreateLastSectionsCommand(ld);
			LinkerScriptModelUtils.createAssignment(secCmd);
		});
		refresh();
	}

	/**
	 * Add a new output section to the end of the list
	 */
	public void addStatement() {
		getModel().writeModel("/", LinkerScript.class, ld -> {
			SectionsCommand secCmd = LinkerScriptModelUtils.getOrCreateLastSectionsCommand(ld);
			LinkerScriptModelUtils.createStatement(secCmd);
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

	public void up() {

	}

	public void down() {

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
