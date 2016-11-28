package examplemultipageeditor.editors;

import java.util.function.Function;

import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

public class LinkerSectionPage extends FormPage {
	private static final Object[] NOOBJECTS = new Object[0];

	private final class SectionsContentProvider implements ITreeContentProvider {

		@Override
		public boolean hasChildren(Object element) {
			return true;
		}

		@Override
		public Object getParent(Object element) {
			return readModel(element, EObject.class, obj -> {
				EObject container = obj.eContainer();
				if (container == null) {
					return null;
				}
				return container.eResource().getURIFragment(container);
			});
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IXtextDocument) {
				IXtextDocument xtextDocument = (IXtextDocument) inputElement;
				return xtextDocument.readOnly((resource) -> {
					if (resource.getContents().isEmpty()) {
						return NOOBJECTS;
					}
					LinkerScript root = (LinkerScript) resource.getContents().get(0);
					SectionsCommand sectionCommand = root.getSections();
					if (sectionCommand == null) {
						return NOOBJECTS;
					}
					EList<OutputSection> sectionList = sectionCommand.getSections();
					if (sectionList == null) {
						return NOOBJECTS;
					}
					return sectionList.stream().map(section -> resource.getURIFragment(section)).toArray();
				});
			}
			return NOOBJECTS;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return readModel(parentElement, OutputSection.class, NOOBJECTS, section -> {
				Resource resource = section.eResource();
				return section.getStatements().stream().map(statement -> resource.getURIFragment(statement)).toArray();
			});

		}
	}

	private final class SectionsColumnLabelProvider extends ColumnLabelProvider {
		private Function<EObject, String> op;

		public SectionsColumnLabelProvider(Function<EObject, String> op) {
			this.op = op;
		}

		@Override
		public String getText(Object element) {
			return readModel(element, EObject.class, null, op);
		}
	}

	public static final String ID = "sections.section"; //$NON-NLS-1$
	private TreeViewer sectionTreeViewer;

	public LinkerSectionPage(FormEditor editor) {
		super(editor, ID, "Sections");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText("Linker Script Sections Settings");
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		createSectionContents(managedForm, toolkit);
	}

	private void createSectionContents(IManagedForm managedForm, FormToolkit toolkit) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		Section section = toolkit.createSection(body, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Defined Sections");
		section.setDescription("");

		Composite composite = toolkit.createComposite(section, SWT.WRAP);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		Tree tree = toolkit.createTree(composite, SWT.NONE);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(300, 600).create());
		toolkit.paintBordersFor(composite);

		section.setClient(composite);

		sectionTreeViewer = new TreeViewer(tree);
		sectionTreeViewer.setContentProvider(new SectionsContentProvider());
		sectionTreeViewer.setLabelProvider(
				new SectionsColumnLabelProvider(sec -> NodeModelUtils.getTokenText(NodeModelUtils.getNode(sec))));
		IXtextDocument xtextDocument = getXtextDocument();
		if (xtextDocument != null) {
			sectionTreeViewer.setInput(xtextDocument);
			xtextDocument.addModelListener(resource -> refreshAsync());
		}

		sectionTreeViewer.getTree().setLinesVisible(true);
		sectionTreeViewer.getTree().setHeaderVisible(true);
	}

	/**
	 * As {@link #readModel(Object, Class, Object, IUnitOfWork)} with
	 * defaultValue of <code>null</code>
	 */
	protected <T, P extends EObject> T readModel(Object uri, Class<P> clazz, Function<P, T> readaccess) {
		return readModel(uri, clazz, null, readaccess);
	}

	/**
	 * Read the element in the resource specified by uri, performing the unit of
	 * work to obtain details.
	 * <p>
	 * Key prupose of this document is it reads "safely" from the document using
	 * the {@link IXtextDocument#readOnly(IUnitOfWork)} safe read.
	 * <p>
	 * <b>Do not return references to model from this method.</b>
	 *
	 * @param <T>
	 *            return type
	 * @param <P>
	 *            element type uri refers to, determined by clazz
	 * @param uri
	 *            in the model
	 * @param clazz
	 *            expected object type in the model
	 * @param defaultValue
	 *            see return annotation for details
	 * @param op
	 *            operation to extract relevant info from the model element.
	 * @return defaultValue if uri does not refer to object of type clazz in the
	 *         document
	 */
	protected <T, P extends EObject> T readModel(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op) {
		IXtextDocument doc = getXtextDocument();
		if (doc == null) {
			return defaultValue;
		}

		if (!(uri instanceof String)) {
			return defaultValue;
		}

		return doc.readOnly(resource -> {
			EObject element = (EObject) resource.getEObject((String) uri);
			if (clazz.isInstance(element)) {
				@SuppressWarnings("unchecked")
				P elementTyped = (P) element;
				return op.apply(elementTyped);
			}
			return defaultValue;
		});
	}

	protected IXtextDocument getXtextDocument() {
		IEditorInput editorInput = getEditorInput();
		MultiPageEditor editor = (MultiPageEditor) getEditor();
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IDocument document = documentProvider.getDocument(editorInput);
		if (document instanceof IXtextDocument) {
			IXtextDocument document2 = (IXtextDocument) document;
			return document2;
		}

		return null;
	}

	public void refreshAsync() {
		getSite().getShell().getDisplay().asyncExec(() -> refresh());

	}

	public void refresh() {
		sectionTreeViewer.refresh();

	}

}
