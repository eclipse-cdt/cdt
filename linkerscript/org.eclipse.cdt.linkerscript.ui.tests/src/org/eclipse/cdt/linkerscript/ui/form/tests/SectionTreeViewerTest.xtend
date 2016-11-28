/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form.tests

import org.eclipse.cdt.linkerscript.ui.form.SectionTreeViewer
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Tree
import org.junit.Test

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

	def void setSelection(int rowIndex) {
		val item = secTree.treeViewer.tree.getItem(rowIndex)
		secTree.treeViewer.tree.selection = item
	}

	@Test
	def void simpler() {
		val contents = '''
			SECTIONS
			{
				.text :	{ *(.text) }
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
				.text 0x1234567 : { *(.text) }
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
				.text 1 + 2 + 3 : { *(.text) }
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
				.text : { *(.text) } > RAM
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
				.text 0x1234567 : { *(.text) } > RAM
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
				.text : { *(.text) }
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
		val contents = ''''''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.new_section')
	}

	@Test
	def void addNewAssignment() {
		val contents = '''
			SECTIONS
			{
				.text : { *(.text) }
			}
		'''

		secTree.input = newModel(contents)

		secTree.addSection

		assertLooksLike('.text', '*(.text)', '.new_section')
	}

	@Test
	def void remove() {
		val contents = '''
			SECTIONS
			{
				.text1 : { }
				.text2 : { }
			}
		'''

		secTree.input = newModel(contents)

		assertLooksLike('.text1', '.text2')

		setSelection(0)
		secTree.remove()

		assertLooksLike('.text2')

	}

	@Test
	def void removeMult() {
		val contents = '''
			SECTIONS
			{
				.text1 : { }
				.text2 : { }
				.text3 : { }
			}
		'''

		secTree.input = newModel(contents)

		assertLooksLike('.text1', '.text2', '.text3')

		val tree = secTree.treeViewer.tree;
		tree.selection = #[tree.getItem(0), tree.getItem(2)]

		secTree.remove()
		assertLooksLike('.text2')

	}

}
