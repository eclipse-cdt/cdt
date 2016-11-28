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
import org.eclipse.cdt.linkerscript.ui.form.SectionTreeViewer
import org.eclipse.swt.widgets.Tree

class SectionTreeViewerTest extends ViewerTestBase {
	SectionTreeViewer secTree

	override Tree getTree() {
		secTree.treeViewer.expandAll()
		return secTree.treeViewer.control as Tree
	}

	override createControl(Composite parent) {
		secTree = new SectionTreeViewer(parent, getToolkit());
	}

	@Test
	def void opensAndCloses() {
		readAndDispatch
	}

	@Test
	def void simpler() {
		val contents = '''
			SECTIONS
			{
				.text :	{ *(.text)) }
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike('.text', '*(.text)')
	}

	@Test
	def void empty() {
		val contents = '''
			SECTIONS
			{
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike()
	}

	@Test
	def void noSectionsSection() {
		val contents = '''/* no SECTIONS section */;'''

		secTree.input = newModel(contents)
		assertLooksLike()
	}

	@Test
	def void withAddress() {
		val contents = '''
			SECTIONS
			{
				.text 0x1234567 : { *(.text)) }
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike('.text ( 0x1234567 )', '*(.text)')
	}

	@Test
	def void withAddressExpression() {
		val contents = '''
			SECTIONS
			{
				.text 1 + 2 + 3 : { *(.text)) }
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike('.text ( 1 + 2 + 3 )', '*(.text)')
	}

	@Test
	def void withRegion() {
		val contents = '''
			SECTIONS
			{
				.text : { *(.text)) } > RAM
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike('.text > RAM', '*(.text)')
	}

	@Test
	def void withAddressAndRegion() {
		val contents = '''
			SECTIONS
			{
				.text 0x1234567 : { *(.text)) } > RAM
			}
		'''

		secTree.input = newModel(contents)
		assertLooksLike('.text ( 0x1234567 ) > RAM', '*(.text)')
	}

	@Test
	def void addNewSection() {
		val contents = '''
			SECTIONS
			{
				.text : { *(.text)) }
			}
		'''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.text', '*(.text)', '.new_section')
	}

	@Test
	def void addNewSectionEmpty() {
		val contents = '''
			MEMORY
			{
			}
		'''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.new_section')
	}

	@Test
	def void addNewSectionEmptyContainer() {
		val contents = ''';'''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.new_section')
	}

	@Test
	def void addNewAssignment() {
		val contents = '''
			SECTIONS
			{
				.text : { *(.text)) }
			}
		'''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.text', '*(.text)', '.new_section')
	}

}
