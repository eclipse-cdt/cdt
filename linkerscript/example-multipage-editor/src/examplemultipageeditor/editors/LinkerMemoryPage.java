/**
 *
 */
package examplemultipageeditor.editors;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

/**
 * Linkers script editor sections and memory page
 *
 */
public class LinkerMemoryPage extends FormPage {

	private class MemoryEditingSupport extends EditingSupport {
		protected TextCellEditor editor;
		protected BiConsumer<String, String> setOp;
		protected Function<Memory, String> getOp;

		private MemoryEditingSupport(ColumnViewer viewer, Function<Memory, String> getOp,
				BiConsumer<String, String> setOp) {
			super(viewer);
			this.getOp = getOp;
			this.setOp = setOp;
			this.editor = new TextCellEditor(memoryTableViewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return readModel(element, Memory.class, "", getOp);
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof String && value instanceof String) {
				String uri = (String) element;
				String newValue = (String) value;
				setOp.accept(uri, newValue);
			}
			memoryTableViewer.update(element, null);
		}
	}

	private final class MemoryColumnLabelProvider extends ColumnLabelProvider {
		private Function<Memory, String> op;

		public MemoryColumnLabelProvider(Function<Memory, String> op) {
			this.op = op;
		}

		@Override
		public String getText(Object element) {
			return readModel(element, Memory.class, "", op);
		}
	}

	public static final String ID = "memory.section"; //$NON-NLS-1$

	private TableViewer memoryTableViewer;

	public LinkerMemoryPage(FormEditor editor) {
		super(editor, ID, "Memory");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText("Linker Script Settings");
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		createMemoryContents(managedForm, toolkit);

	}


	private Button createButton(Composite parent, String label, FormToolkit toolkit) {
		Button button;
		if (toolkit != null)
			button = toolkit.createButton(parent, label, SWT.PUSH);
		else {
			button = new Button(parent, SWT.PUSH);
			button.setText(label);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);

		// Set the default button size
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);

		return button;
	}

	private void createMemoryContents(IManagedForm managedForm, FormToolkit toolkit) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(GridLayoutFactory.fillDefaults().create());
		Section section = toolkit.createSection(body, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Memory Regions");
		section.setDescription("Specify memory regions by defining the location and size.");
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		section.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite composite = toolkit.createComposite(section, SWT.WRAP);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		Table table = toolkit.createTable(composite, SWT.NONE);
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		toolkit.paintBordersFor(composite);

		Composite buttomComp = toolkit.createComposite(composite, SWT.WRAP);
		buttomComp.setLayout(GridLayoutFactory.fillDefaults().create());
		buttomComp.setLayoutData(GridDataFactory.fillDefaults().create());
		createButton(buttomComp, "Add", toolkit);
		createButton(buttomComp, "Remove", toolkit);
		createButton(buttomComp, "Up", toolkit);
		createButton(buttomComp, "Down", toolkit);

		section.setClient(composite);

		memoryTableViewer = new TableViewer(table);
		memoryTableViewer.setContentProvider(new MemoryContentProvider());

		TableColumn nameColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		nameColumn.setWidth(150);
		nameColumn.setText("Region Name");
		TableViewerColumn nameViewerColumn = new TableViewerColumn(memoryTableViewer, nameColumn);
		nameViewerColumn.setLabelProvider(new MemoryColumnLabelProvider(mem -> mem.getName()));
		nameViewerColumn.setEditingSupport(new MemoryEditingSupport(memoryTableViewer, mem -> mem.getName(),
				(uri, val) -> writeModel(uri, Memory.class, mem -> mem.setName(val))));

		TableColumn originColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		originColumn.setWidth(150);
		originColumn.setText("Start Address (Origin)");
		TableViewerColumn originViewerColumn = new TableViewerColumn(memoryTableViewer, originColumn);
		originViewerColumn.setLabelProvider(new MemoryColumnLabelProvider(mem -> expressionToString(mem.getOrigin())));
		originViewerColumn.setEditingSupport(
				new MemoryEditingSupport(memoryTableViewer, mem -> expressionToString(mem.getOrigin()), (uri, val) -> {
					String uriOrigin = readModel(uri, Memory.class,
							mem -> mem.eResource().getURIFragment(mem.getOrigin()));
					writeText(uriOrigin, val);
				}));

		TableColumn lengthColumn = new TableColumn(memoryTableViewer.getTable(), SWT.NONE);
		lengthColumn.setWidth(150);
		lengthColumn.setText("Length");
		TableViewerColumn lengthviewerColumn = new TableViewerColumn(memoryTableViewer, lengthColumn);
		lengthviewerColumn.setLabelProvider(new MemoryColumnLabelProvider(mem -> expressionToString(mem.getLength())));
		lengthviewerColumn.setEditingSupport(
				new MemoryEditingSupport(memoryTableViewer, mem -> expressionToString(mem.getLength()), (uri, val) -> {
					String uriLength = readModel(uri, Memory.class,
							mem -> mem.eResource().getURIFragment(mem.getLength()));
					writeText(uriLength, val);
				}));

		IXtextDocument xtextDocument = getXtextDocument();
		if (xtextDocument != null) {
			memoryTableViewer.setInput(xtextDocument);
			xtextDocument.addModelListener(resource -> refreshAsync());
		}

		memoryTableViewer.getTable().setLinesVisible(true);
		memoryTableViewer.getTable().setHeaderVisible(true);

	}

	private void writeText(Object uri, String text) {
		IXtextDocument doc = getXtextDocument();
		if (doc == null) {
			return;
		}

		if (!(uri instanceof String)) {
			return;
		}

		ITextRegion region = readModel(uri, EObject.class, obj -> {
			ICompositeNode node = NodeModelUtils.getNode(obj);
			if (node == null) {
				return null;
			}
			return node.getTextRegion();
		});
		try {
			doc.replace(region.getOffset(), region.getLength(), text);
		} catch (BadLocationException e) {
			// TODO log not reachable, this is the location we were just given.
			e.printStackTrace();
		}
	}

	private String expressionToString(LExpression origin) {
		if (origin != null) {
			ICompositeNode node = NodeModelUtils.getNode(origin);
			return NodeModelUtils.getTokenText(node);
		}
		return "TODO ERROR? Missing Info?";
	}

	@Override
	public void setFocus() {
//		memoryTableViewer.getControl().setFocus();
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
			EObject element = resource.getEObject((String) uri);
			if (clazz.isInstance(element)) {
				@SuppressWarnings("unchecked")
				P elementTyped = (P) element;
				return op.apply(elementTyped);
			}
			return defaultValue;
		});
	}

	protected <P extends EObject> void writeModel(Object uri, Class<P> clazz, Consumer<P> op) {
		IXtextDocument doc = getXtextDocument();
		if (doc == null) {
			return;
		}

		if (!(uri instanceof String)) {
			return;
		}

		doc.modify(resource -> {
			EObject element = (EObject) resource.getEObject((String) uri);
			if (clazz.isInstance(element)) {
				@SuppressWarnings("unchecked")
				P elementTyped = (P) element;
				op.accept(elementTyped);
			}
			return null;
		});
	}

	protected <P extends EObject> void writeText(Object uri, Class<P> clazz, String text) {
		writeText(uri, clazz, text, UnaryOperator.identity());
	}

	protected <P extends EObject> void writeText(Object uri, Class<P> clazz, String text,
			Function<P, ? extends EObject> op) {
		IXtextDocument doc = getXtextDocument();
		if (doc == null) {
			return;
		}

		if (!(uri instanceof String)) {
			return;
		}

		ITextRegion region = readModel(uri, clazz, obj -> {
			EObject objResolved = op.apply(obj);
			ICompositeNode node = NodeModelUtils.getNode(objResolved);
			if (node == null) {
				return null;
			}
			return node.getTextRegion();
		});
		try {
			doc.replace(region.getOffset(), region.getLength(), text);
		} catch (BadLocationException e) {
			// TODO log not reachable, this is the location we were just given.
			e.printStackTrace();
		}
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

	private int count = 0;
//
//	private void swapOrderOfMemories() {
//		IXtextDocument xtextDocument = getXtextDocument();
//		if (xtextDocument != null) {
//			xtextDocument.modify((resource) -> {
//				if (resource.getContents().isEmpty()) {
//					return null;
//				}
//				LinkerScript root = (LinkerScript) resource.getContents().get(0);
//				EList<LinkerScriptStatement> statements = root.getStatements();
//				for (LinkerScriptStatement statement : statements) {
//
//				}
//				MemoryCommand memories = statements.getMemories();
//				EList<Memory> list = memories.getMemories();
//
//				// Memory mem0 = list.get(0);
//				// Memory mem1 = list.get(1);
//				list.add(list.remove(0));
//
//				return null;
//			});
//		}
//		refresh();
//	}
//
//	private void createNewMemory() {
//		IXtextDocument xtextDocument = getXtextDocument();
//		if (xtextDocument != null) {
//			xtextDocument.modify((resource) -> {
//				if (resource.getContents().isEmpty()) {
//					return null;
//				}
//				LinkerScript root = (LinkerScript) resource.getContents().get(0);
//				MemoryCommand memories = root.getMemories();
//				EList<Memory> list = memories.getMemories();
//
//				// TODO: How to do this with injection?
//				Memory newMemory = LinkerScriptFactory.eINSTANCE.createMemory();
//				newMemory.setName("NewRAM" + ++count);
//				LNumberLiteral origin = LinkerScriptFactory.eINSTANCE.createLNumberLiteral();
//				origin.setValue(20000L + count);
//				newMemory.setOrigin(origin);
//				LNumberLiteral length = LinkerScriptFactory.eINSTANCE.createLNumberLiteral();
//				length.setValue(10000L + count);
//				newMemory.setLength(length);
//
//				list.add(newMemory);
//
//				return null;
//			});
//		}
//	}
//
//	private void changeTextually() {
//		IXtextDocument xtextDocument = getXtextDocument();
//		if (xtextDocument != null) {
//			ITextRegion region = xtextDocument.readOnly(resource -> {
//				if (resource.getContents().isEmpty()) {
//					return null;
//				}
//				LinkerScript root = (LinkerScript) resource.getContents().get(0);
//				MemoryCommand memories = root.getMemories();
//				EList<Memory> list = memories.getMemories();
//				Memory memory = list.get(0);
//				LExpression length = memory.getLength();
//				ICompositeNode memNode = NodeModelUtils.getNode(memory);
//				System.out.println(memNode.getText());
//				ICompositeNode node = NodeModelUtils.getNode(length);
//				return node.getTextRegion();
//				// LineAndColumn lineAndColumn =
//				// NodeModelUtils.getLineAndColumn(node, 0);
//				// return null;
//			});
//			// textual changes happen /outside/ modify, that also means that a
//			// readOnly can be done to get location to apply edit to
//			try {
//				xtextDocument.replace(region.getOffset(), region.getLength(), "1234");
//			} catch (BadLocationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		refresh();
//	}
//
//	/**
//	 * Sorts the words in page 0, and shows them in page 2.
//	 */
//	private void sortWords() {
//		IXtextDocument xtextDocument = getXtextDocument();
//		if (xtextDocument != null) {
//			xtextDocument.modify((resource) -> {
//				if (resource.getContents().isEmpty()) {
//					return null;
//				}
//				LinkerScript root = (LinkerScript) resource.getContents().get(0);
//				MemoryCommand memories = root.getMemories();
//				EList<Memory> list = memories.getMemories();
//				StringBuilder result = new StringBuilder();
//				for (Memory memory : list) {
//					result.append("Memory: ");
//					result.append(memory.getName());
//					result.append(System.lineSeparator());
//				}
//				return result.toString();
//			});
//		}
//
//	}

	public void refreshAsync() {
		getSite().getShell().getDisplay().asyncExec(() -> refresh());

	}

	public void refresh() {
//		memoryTableViewer.refresh();

	}

}
