package org.eclipse.cdt.linkerscript.ui.form.tests

import com.google.inject.Inject
import com.google.inject.Injector
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.function.Consumer
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.ui.form.MemoryTableViewer
import org.eclipse.cdt.linkerscript.ui.form.MemoryTableViewer.COLUMN
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.Text
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.util.ResourceHelper
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

class MemoryTableViewerTest extends ViewerTestBase {

	LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE

	@Inject ResourceHelper resourceHelper

	@Inject extension ParseHelper<LinkerScript> parseHelper
//
//	@Inject
//	XtextDocument document
	MemoryTableViewer memTable

	override Table getTable() {
		return memTable.tableViewer.control as Table
	}

	override createControl(Composite parent) {
		memTable = new MemoryTableViewer(parent, getToolkit());
	}

	@Test
	def void opensAndCloses() {
		readAndDispatch
	}

//
//	def getDocument(CharSequence code) {
//		return getDocument(code.toString)
//	}
//
//	def getDocument(String code) {
//		val resource = resourceHelper.resource(code)
//		return getDocument(resource as XtextResource, code)
//	}
//
//	def getDocument(XtextResource xtextResource, String code) {
//		document.setInput(xtextResource)
//		document.set(code)
//		(xtextResource.resourceSet as XtextResourceSet).markSynced
//		document
//	}
//
//	@Test
//	def void simple() {
//		val document = '''
//			MEMORY
//			{
//				RAM : ORIGIN = 0x0, LENGTH = 0x2000
//				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
//			}
//		'''.document
//		assertNotNull(document)
//		table.input = new ILinkerScriptModel {
//
//			override <T, P extends EObject> read(Object uri, Class<P> clazz, T defaultValue, Function<P, T> op) {
//				if (document == null) {
//					return defaultValue;
//				}
//
//				if (!(uri instanceof String)) {
//					return defaultValue;
//				}
//
//				return document.readOnly([ resource |
//					val element = resource.getEObject(uri as String);
//					if (clazz.isInstance(element)) {
//						return op.apply(element as P);
//					}
//					return defaultValue;
//				]);
//			}
//
//			override <P extends EObject> write(Object uri, Class<P> clazz, Consumer<P> op) {
//				throw new UnsupportedOperationException("TODO: auto-generated method stub")
//			}
//
//			override write(Object uri, String text) {
//				throw new UnsupportedOperationException("TODO: auto-generated method stub")
//			}
//
//		}
////		readAndDispatchForever
//	}
	@Test
	def void simpler() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		memTable.input = newModel(contents)
		assertLooksLike(ROW('RAM', '0x0', '0x2000'), ROW('ROM', '0x80000', '0x10000'))
	}

	@Test
	def void noMemorySection() {
		val contents = '''/* no MEMORY section */;'''

		memTable.input = newModel(contents)
		assertLooksLike()
	}

	@Test
	def void emptyMemorySection() {
		val contents = '''
			MEMORY
			{
			}
		'''

		memTable.input = newModel(contents)
		assertLooksLike()
	}

	/**
	 * Test that getting/setting selection on viewer works as expected
	 */
	@Test
	def void testGetSetSelection() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model
		assertLooksLike(ROW('RAM', '0x0', '0x2000'), ROW('ROM', '0x80000', '0x10000'))

		val uriRam = model.model.eResource.getURIFragment(
			(model.model.statements.get(0) as MemoryCommand).memories.get(0))
		val uriRom = model.model.eResource.getURIFragment(
			(model.model.statements.get(0) as MemoryCommand).memories.get(1))

		// empty selection
		memTable.selection = new StructuredSelection()
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(nullValue))

		// single selection
		memTable.selection = new StructuredSelection(uriRam)
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(uriRam))
		memTable.selection = new StructuredSelection(uriRom)
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(uriRom))

		// multi selection
		memTable.selection = new StructuredSelection(newArrayList(uriRam, uriRom))
		assertThat((memTable.selection as IStructuredSelection).toList, contains(uriRam, uriRom))

		// empty selection
		memTable.selection = new StructuredSelection()
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(nullValue))
	}

	/**
	 * Test that getting/setting selection on underlying table (close to
	 * simulating what a click on the table does)
	 */
	@Test
	def void testGetSetSelectionWithControl() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model
		assertLooksLike(ROW('RAM', '0x0', '0x2000'), ROW('ROM', '0x80000', '0x10000'))

		val uriRam = model.model.eResource.getURIFragment(
			(model.model.statements.get(0) as MemoryCommand).memories.get(0))
		val uriRom = model.model.eResource.getURIFragment(
			(model.model.statements.get(0) as MemoryCommand).memories.get(1))

		// empty selection
		memTable.tableViewer.table.deselectAll()
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(nullValue))

		// single selection
		memTable.tableViewer.table.selection = 0
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(uriRam))
		memTable.tableViewer.table.selection = 1
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(uriRom))

		// multi selection
		memTable.tableViewer.table.setSelection(0, 1)
		assertThat((memTable.selection as IStructuredSelection).toList, contains(uriRam, uriRom))

		// empty selection
		memTable.tableViewer.table.deselectAll()
		assertThat((memTable.selection as IStructuredSelection).firstElement, is(nullValue))
	}

	def void setColumnText(COLUMN column, String value) {
		edit(column, [text|text.text = value])
	}

	/**
	 * Edit the current selection on given column
	 */
	def void edit(COLUMN column, Consumer<Text> op) {
		val Text text = editElement(column)
		op.accept(text)
		finishEditElement(text)
	}

	/**
	 * Finish an edit event on the given text (which was returned by editElement()
	 */
	def void finishEditElement(Text text) {
		text.notifyListeners(SWT.DefaultSelection, new Event());
	}

	/** Trigger edit on currently selected row for the given column */
	def Text editElement(COLUMN column) {
		val selection = memTable.selection
		assertThat("Row must already be selected!", selection, instanceOf(StructuredSelection))
		val element = (selection as StructuredSelection).getFirstElement()
		assertThat("Selection is of unexpected type", element, instanceOf(String))
		memTable.tableViewer.editElement(element, column.COLUMN_INDEX)
		val editingSupport = memTable.getColumnEditingSupport(column)
		val text = editingSupport.textCellEditor.getControl()
		return text as Text;
	}

	def void setSelection(int rowIndex) {
		memTable.tableViewer.table.selection = rowIndex
	}

	@Test
	def void changeName() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(0)
		setColumnText(COLUMN.NAME, "NewNameForRam")
		setSelection(1)
		setColumnText(COLUMN.NAME, "NewNameForRom")

		assertLooksLike(ROW('NewNameForRam', '0x0', '0x2000'), ROW('NewNameForRom', '0x80000', '0x10000'))
		assertThat(model.contents.toString, is('''
			MEMORY
			{
				NewNameForRam : ORIGIN = 0x0, LENGTH = 0x2000
				NewNameForRom : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''))
	}

	@Test
	def void changeOrigin() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(0)
		setColumnText(COLUMN.ORIGIN, "1+2*3")
		setSelection(1)
		setColumnText(COLUMN.ORIGIN, "9*8+7")

		assertLooksLike(ROW('RAM', '1+2*3', '0x2000'), ROW('ROM', '9*8+7', '0x10000'))
		assertThat(model.contents.toString, is('''
			MEMORY
			{
				RAM : ORIGIN = 1+2*3, LENGTH = 0x2000
				ROM : ORIGIN = 9*8+7, LENGTH = 0x10000
			}
		'''))
	}

	@Test
	def void changeLength() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(0)
		setColumnText(COLUMN.LENGTH, "1+2*3")
		setSelection(1)
		setColumnText(COLUMN.LENGTH, "9*8+7")

		assertLooksLike(ROW('RAM', '0x0', '1+2*3'), ROW('ROM', '0x80000', '9*8+7'))
		assertThat(model.contents.toString, is('''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 1+2*3
				ROM : ORIGIN = 0x80000, LENGTH = 9*8+7
			}
		'''))
	}

	@Test
	def void addNewMemory() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.add()

		assertLooksLike(
			ROW('RAM', '0x0', '0x2000'),
			ROW('ROM', '0x80000', '0x10000'),
			ROW('Memory', '0x0', '0x0')
		)
	}

	@Test
	def void addNewMemoryEmpty() {
		val contents = '''
			MEMORY
			{
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.add()

		assertLooksLike(
			ROW('Memory', '0x0', '0x0')
		)
	}

	@Test
	def void addNewMemoryEmptyContainer() {
		val contents = ''';
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.add()

		assertLooksLike(
			ROW('Memory', '0x0', '0x0')
		)
	}

	@Test
	def void removeMemory() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(0)
		memTable.remove()

		assertLooksLike(
			ROW('ROM', '0x80000', '0x10000')
		)
	}

	@Test
	def void removeMemoryNoSelection() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.remove()

		assertLooksLike(
			ROW('RAM', '0x0', '0x2000'),
			ROW('ROM', '0x80000', '0x10000')
		)
	}

	@Test
	def void removeMemoryMultiSelection() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.tableViewer.table.select(0)
		memTable.tableViewer.table.select(1)
		memTable.remove()

		assertLooksLike()
	}

	@Test
	def void moveMemoryUp() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(1)
		memTable.up()

		assertLooksLike(
			ROW('ROM', '0x80000', '0x10000'),
			ROW('RAM', '0x0', '0x2000')
		)
	}

	@Test
	def void moveMemoryUpNoSelection() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.up()

		assertLooksLike(
			ROW('RAM', '0x0', '0x2000'),
			ROW('ROM', '0x80000', '0x10000')
		)
	}

	@Test
	def void moveMemoryUpMulti() {
		val contents = '''
			MEMORY
			{
				RAM1 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM2 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM3 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM4 : ORIGIN = 0x0, LENGTH = 0x2000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.tableViewer.table.select(1)
		memTable.tableViewer.table.select(2)
		memTable.up()

		assertLooksLike(
			ROW('RAM2', '0x0', '0x2000'),
			ROW('RAM3', '0x0', '0x2000'),
			ROW('RAM1', '0x0', '0x2000'),
			ROW('RAM4', '0x0', '0x2000')
		)
	}

	@Test
	def void moveMemoryUpFirstSelected() {
		val contents = '''
			MEMORY
			{
				RAM1 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM2 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM3 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM4 : ORIGIN = 0x0, LENGTH = 0x2000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.tableViewer.table.select(0)
		memTable.up()

		assertLooksLike(
			ROW('RAM1', '0x0', '0x2000'),
			ROW('RAM2', '0x0', '0x2000'),
			ROW('RAM3', '0x0', '0x2000'),
			ROW('RAM4', '0x0', '0x2000')
		)
	}

	@Test
	def void moveMemoryDown() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		setSelection(0)
		memTable.down()

		assertLooksLike(
			ROW('ROM', '0x80000', '0x10000'),
			ROW('RAM', '0x0', '0x2000')
		)
	}

	@Test
	def void moveMemoryDownNoSelection() {
		val contents = '''
			MEMORY
			{
				RAM : ORIGIN = 0x0, LENGTH = 0x2000
				ROM : ORIGIN = 0x80000, LENGTH = 0x10000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.down()

		assertLooksLike(
			ROW('RAM', '0x0', '0x2000'),
			ROW('ROM', '0x80000', '0x10000')
		)
	}

	@Test
	def void moveMemoryDownMulti() {
		val contents = '''
			MEMORY
			{
				RAM1 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM2 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM3 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM4 : ORIGIN = 0x0, LENGTH = 0x2000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.tableViewer.table.select(1)
		memTable.tableViewer.table.select(2)
		memTable.down()

		assertLooksLike(
			ROW('RAM1', '0x0', '0x2000'),
			ROW('RAM4', '0x0', '0x2000'),
			ROW('RAM2', '0x0', '0x2000'),
			ROW('RAM3', '0x0', '0x2000')
		)
	}

	@Test
	def void moveMemoryDownMultiLastSelected() {
		val contents = '''
			MEMORY
			{
				RAM1 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM2 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM3 : ORIGIN = 0x0, LENGTH = 0x2000
				RAM4 : ORIGIN = 0x0, LENGTH = 0x2000
			}
		'''

		val model = newModel(contents)
		memTable.input = model

		memTable.tableViewer.table.select(3)
		memTable.down()

		assertLooksLike(
			ROW('RAM1', '0x0', '0x2000'),
			ROW('RAM2', '0x0', '0x2000'),
			ROW('RAM3', '0x0', '0x2000'),
			ROW('RAM4', '0x0', '0x2000')
		)
	}
}
