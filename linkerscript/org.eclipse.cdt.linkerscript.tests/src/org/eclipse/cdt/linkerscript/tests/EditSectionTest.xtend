/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.eclipse.cdt.linkerscript.tests.TestUtils.*
import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class EditSectionTest {
	@Inject extension ParseHelper<LinkerScript> parseHelper
	@Inject extension ValidationTestHelper


	def SectionsCommand getSections(CharSequence script) {
		val model = script.parse
		assertNotNull(model)
		model.assertNoErrors
		val sectionCommand = model.statements.get(0)
		return sectionCommand as SectionsCommand
	}

	@Test
	def void createOutputSection() {
		val sections = '''
			SECTIONS {
				.text : {}
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {}
				.new_section : {}
			}

		'''.sections, sections)
	}

	@Test
	def void createOutputSectionEmpty() {
		val sections = '''
			SECTIONS {
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.new_section : {}
			}

		'''.sections, sections)
	}

	@Test
	def void createOutputSectionInheritRegion() {
		val sections = '''
			SECTIONS {
				.text : {} > RAM
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {} > RAM
				.new_section : {} > RAM
			}

		'''.sections, sections)
	}

	@Test
	def void createOutputSectionInheritAtRegion() {
		val sections = '''
			SECTIONS {
				.text : {} AT> RAM
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {} AT> RAM
				.new_section : {} AT> RAM
			}

		'''.sections, sections)
	}

	@Test
	def void createOutputSectionInheritRegionSkipOtherStatments1() {
		val sections = '''
			SECTIONS {
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
				.new_section : {} > RAM
			}

		'''.sections, sections)
	}

	@Test
	def void createOutputSectionInheritRegionSkipOtherStatments2() {
		val sections = '''
			SECTIONS {
				.other : {} > ROM
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertEObjectEquals('''
			SECTIONS {
				.other : {} > ROM
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
				.new_section : {} > RAM
			}

		'''.sections, sections)
	}

	@Test
	def void createAssignment() {
		val sections = '''
			SECTIONS {
				.text : {}
			}
		'''.sections

		LinkerScriptModelUtils.createAssignment(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {}
				symbol = 0;
			}

		'''.sections, sections);
	}

	@Test
	def void createStatement() {
		val sections = '''
			SECTIONS {
				.text : {}
			}
		'''.sections

		LinkerScriptModelUtils.createStatement(sections)

		assertEObjectEquals('''
			SECTIONS {
				.text : {}
				ASSERT(1, "New statement")
			}

		'''.sections, sections);
	}

}
