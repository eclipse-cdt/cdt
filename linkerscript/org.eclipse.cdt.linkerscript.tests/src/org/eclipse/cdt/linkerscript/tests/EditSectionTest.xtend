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
import com.itemis.xtext.testing.XtextUtils
import java.io.ByteArrayOutputStream
import org.eclipse.xtext.resource.SaveOptions
import org.eclipse.emf.ecore.EObject

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class EditSectionTest {
	@Inject extension ParseHelper<LinkerScript> parseHelper
	@Inject extension ValidationTestHelper

	def serialize(EObject obj) {
		val bos = new ByteArrayOutputStream();
		val options = SaveOptions.newBuilder().options
		obj.eResource().save(bos, options.toOptionsMap());
		return bos.toString;
	}

	def assertEquals(EObject expected, EObject actual) {
		if (EcoreUtil.equals(expected, actual)) {
			return;
		}

		// if the objects are different, then the string
		// representation is probably different, assert
		// on that because it gives better error messages
		assertEquals(expected.serialize, actual.serialize)

		// hmm, should be unreachable
		fail("Objects compared unequal, but serialized were the same")

	}

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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
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

		assertEquals('''
			SECTIONS {
				.text : {}
				ASSERT(1, "New statement")
			}

		'''.sections, sections);
	}

}
