package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.cdt.linkerscript.util.LinkerScriptModelUtils

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class EditMemoryTest {
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

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.text : {}
				.new_section : {}
			}

		'''.sections))
	}

	@Test
	def void createOutputSectionEmpty() {
		val sections = '''
			SECTIONS {
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.new_section : {}
			}

		'''.sections))
	}

	@Test
	def void createOutputSectionInheritRegion() {
		val sections = '''
			SECTIONS {
				.text : {} > RAM
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.text : {} > RAM
				.new_section : {} > RAM
			}

		'''.sections))
	}

	@Test
	def void createOutputSectionInheritAtRegion() {
		val sections = '''
			SECTIONS {
				.text : {} AT> RAM
			}
		'''.sections

		LinkerScriptModelUtils.createOutputSection(sections)

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.text : {} AT> RAM
				.new_section : {} AT> RAM
			}

		'''.sections))
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

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
				.new_section : {} > RAM
			}

		'''.sections))
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

		assertTrue(EcoreUtil.equals(sections, '''
			SECTIONS {
				.other : {} > ROM
				.text : {} > RAM
				sym = 2;
				ASSERT(1, "message");
				.new_section : {} > RAM
			}

		'''.sections))
	}


}
