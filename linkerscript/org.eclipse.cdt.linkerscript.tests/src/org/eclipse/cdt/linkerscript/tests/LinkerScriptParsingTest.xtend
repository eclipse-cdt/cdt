/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import java.util.HashMap
import java.util.Map
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentHidden
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentProvide
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentProvideHidden
import org.eclipse.cdt.linkerscript.linkerScript.InputSection
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionFile
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionWild
import org.eclipse.cdt.linkerscript.linkerScript.LExpression
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionAlignExpression
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionAlignWithInput
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionConstraintOnlyIfRO
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionTypeNoLoad
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.cdt.linkerscript.linkerScript.Statement
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssert
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment
import org.eclipse.cdt.linkerscript.linkerScript.StatementConstructors
import org.eclipse.cdt.linkerscript.linkerScript.StatementConstructorsSorted
import org.eclipse.cdt.linkerscript.linkerScript.StatementCreateObjectSymbols
import org.eclipse.cdt.linkerscript.linkerScript.StatementData
import org.eclipse.cdt.linkerscript.linkerScript.StatementFill
import org.eclipse.cdt.linkerscript.linkerScript.StatementInclude
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection
import org.eclipse.cdt.linkerscript.linkerScript.StatementNop
import org.eclipse.cdt.linkerscript.linkerScript.Wildcard
import org.eclipse.cdt.linkerscript.linkerScript.WildcardSort
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import org.eclipse.cdt.linkerscript.validation.LExpressionReducer

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class LinkerScriptParsingTest {

	@Inject extension ParseHelper<LinkerScript> parseHelper
	@Inject extension ValidationTestHelper

	@Inject
	private LExpressionReducer reducer;

//	def Matcher<InputSectionSpec> nameIs(String s) {
//		return new FeatureMatcher<InputSectionSpec, String>(equalTo(s), "string", "string") {
//			override protected String featureValueOf(InputSectionSpec actual) {
//				return actual.name
//			}
//		};
//	}
//
//	def Matcher<InputSectionSpec> excludesAre(Collection<String> excludes) {
//		return new FeatureMatcher<InputSectionSpec, Collection<String>>(equalTo(excludes), "strings", "strings") {
//			override protected Collection<String> featureValueOf(InputSectionSpec actual) {
//				return actual.excludes
//			}
//		};
//	}
	def Long reduce(LExpression exp) {
		return reduce(exp, emptyMap)
	}

	def Long reduce(LExpression exp, Map<String, Long> memorySize) {
		return reduce(exp, memorySize, emptyMap)
	}

	def Long reduce(LExpression exp, Map<String, Long> memorySize, Map<String, Long> variableValues) {
		reducer.memoryLengthMap.putAll(memorySize)
		reducer.variableValuesMap.putAll(variableValues)
		// remove Optional under test, makes it easier to write all the asserts
		val x = reducer.reduceToLong(exp)
		return x.orElse(null)
	}

	def OutputSection outputSection(CharSequence script) {
		val model = script.parse
		assertNotNull(model)
		model.assertNoErrors
		val sectionCommand = model.statements.get(0) as SectionsCommand
		val outputSection = sectionCommand.sectionCommands.get(0)
		assertNotNull(outputSection)
		return outputSection as OutputSection
	}

	@Test
	def void outputSectionAllFields() {
		val outputSection = '''
			SECTIONS {
				.output+name 2*(3+4) (NOLOAD) : AT(2*(3+4)) ALIGN(2*(3+4)) SUBALIGN(2*(3+4))  ONLY_IF_RO {
				}>.dots.everywhere. AT>.dots.everywhere2. :.dots.everywhere3. :.dots.everywhere4. =2*(3+4),
			}

		'''.outputSection
		assertThat(outputSection.name, is(".output+name"))
		assertThat(outputSection.address.reduce, is(14L))
		assertThat(outputSection.type, instanceOf(OutputSectionTypeNoLoad))
		assertThat(outputSection.at.reduce, is(14L))
		assertThat(outputSection.align, instanceOf(OutputSectionAlignExpression))
		assertThat((outputSection.align as OutputSectionAlignExpression).exp.reduce, is(14L))
		assertThat(outputSection.subAlign.reduce, is(14L))
		assertThat(outputSection.constraint, instanceOf(OutputSectionConstraintOnlyIfRO))
		assertThat(outputSection.memory, is(".dots.everywhere."))
		assertThat(outputSection.atMemory, is(".dots.everywhere2."))
		assertThat(outputSection.phdrs, contains(".dots.everywhere3.", ".dots.everywhere4."))
		assertThat(outputSection.fill.reduce, is(14L))
	}

	@Test
	def void outputSectionName() {
		assertThat('''
			SECTIONS {
				.output+name : {
				},
			}

		'''.outputSection.name, is(".output+name"))
	}

	@Test
	def void outputSectionAddress() {
		assertThat('''
			SECTIONS {
				.output+name 2*(3+4) : {
				},
			}

		'''.outputSection.address.reduce, is(14L))
	}

	@Test
	def void outputSectionType() {
		assertThat('''
			SECTIONS {
				.output+name (NOLOAD) : {
				},
			}

		'''.outputSection.type, instanceOf(OutputSectionTypeNoLoad))
	}

	@Test
	def void outputSectionAtAddress() {
		assertThat('''
			SECTIONS {
				.output+name : AT(2*(3+4)) {
				},
			}

		'''.outputSection.at.reduce, is(14L))
	}

	@Test
	def void outputSectionAlign() {
		val sectionAlign = '''
			SECTIONS {
				.output+name : ALIGN(2*(3+4)) {
				},
			}

		'''.outputSection.align
		assertThat(sectionAlign, instanceOf(OutputSectionAlignExpression))
		assertThat((sectionAlign as OutputSectionAlignExpression).exp.reduce, is(14L))
	}

	@Test
	def void outputSectionAlignWithInput() {
		assertThat('''
			SECTIONS {
				.output+name : ALIGN_WITH_INPUT {
				},
			}

		'''.outputSection.align, instanceOf(OutputSectionAlignWithInput))
	}

	@Test
	def void outputSectionSubAlign() {
		assertThat('''
			SECTIONS {
				.output+name : SUBALIGN(2*(3+4)) {
				},
			}

		'''.outputSection.subAlign.reduce, is(14L))
	}

	@Test
	def void outputSectionConstraint() {
		assertThat('''
			SECTIONS {
				.output+name : ONLY_IF_RO {
				},
			}

		'''.outputSection.constraint, instanceOf(OutputSectionConstraintOnlyIfRO))
	}

	@Test
	def void outputSectionMemory() {
		assertThat('''
			SECTIONS {
				.output+name : {
				} >.dots.everywhere. ,
			}

		'''.outputSection.memory, is(".dots.everywhere."))
	}

	@Test
	def void outputSectionAtMemory() {
		assertThat('''
			SECTIONS {
				.output+name : {
				} AT>.dots.everywhere. ,
			}

		'''.outputSection.atMemory, is(".dots.everywhere."))
	}

	@Test
	def void outputSectionPHdrs() {
		assertThat('''
			SECTIONS {
				.output+name : {
				} :.dots.everywhere. :.dots.everywhere2.,
			}

		'''.outputSection.phdrs, contains(".dots.everywhere.", ".dots.everywhere2."))
	}

	@Test
	def void outputSectionFill() {
		assertThat('''
			SECTIONS {
				.output+name : {
				} =2*(3+4) ,
			}

		'''.outputSection.fill.reduce, is(14L))
	}

	def Statement statement(CharSequence statement) {
		val statements = ("SECTIONS { .name : { " + statement + " }}").outputSection.statements
		assertNotNull(statements)
		assertThat(statements.size, is(1))
		assertNotNull(statements.get(0))
		return statements.get(0)
	}

	@Test
	def void statementAssignmentAssign() {
		val assignment = ('''value = 2*(3+4);'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('value'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(14L))
	}

	@Test
	def void statementAssignmentMultiAssign() {
		for (assign : #['+=', '-=', '*=', '/=', '<<=', '>>=', '&=', '|=']) {
			val assignment = (('''value ''' + assign + ''' 2*(3+4);''').statement as StatementAssignment).assignment
			assertThat(assignment.name, is('value'))
			assertThat(assignment.feature, is(assign))
			assertThat(assignment.exp.reduce, is(14L))
		}
	}

	@Test
	def void statementAssignmentToDot() {
		val assignment = ('''. = 2*(3+4);'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('.'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(14L))
	}

	@Test
	def void statementAssignmentFromDot() {
		val assignment = ('''etext = .;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce(emptyMap, #{'.' -> 14L}), is(14L))
	}

	@Test
	def void statementAssignmentFromSymbol() {
		val assignment = ('''etext = othertext;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce(emptyMap, #{'othertext' -> 14L}), is(14L))
	}

	@Test
	def void statementAssignmentHidden() {
		val assignment = ('''HIDDEN(value = 2*(3+4));'''.statement as StatementAssignment).assignment
		assertThat(assignment, instanceOf(AssignmentHidden))
		assertThat(assignment.name, is('value'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(14L))
	}

	@Test
	def void statementAssignmentProvide() {
		val assignment = ('''PROVIDE(value = 2*(3+4));'''.statement as StatementAssignment).assignment
		assertThat(assignment, instanceOf(AssignmentProvide))
		assertThat(assignment.name, is('value'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(14L))
	}

	@Test
	def void statementAssignmentProvideHidden() {
		val assignment = ('''PROVIDE_HIDDEN(value = 2*(3+4));'''.statement as StatementAssignment).assignment
		assertThat(assignment, instanceOf(AssignmentProvideHidden))
		assertThat(assignment.name, is('value'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(14L))
	}

	@Test
	def void statementAssignmentNop() {
		assertThat(";".statement, instanceOf(StatementNop))
	}

	@Test
	def void statementCreateObjectSymbols() {
		assertThat("CREATE_OBJECT_SYMBOLS".statement, instanceOf(StatementCreateObjectSymbols))
	}

	@Test
	def void statementConstructors() {
		assertThat("CONSTRUCTORS".statement, instanceOf(StatementConstructors))
	}

	@Test
	def void statementConstructorsSorted() {
		assertThat("SORT_BY_NAME(CONSTRUCTORS)".statement, instanceOf(StatementConstructorsSorted))
	}

	@Test
	def void statementData() {
		for (size : #['BYTE', 'SHORT', 'LONG', 'QUAD', 'SQUAD']) {
			val data = ((size + "(2*(3+4))").statement as StatementData)
			assertThat(data.size, is(size))
			assertThat(data.data.reduce, is(14L))
		}
	}

	@Test
	def void statementFill() {
		val data = ("FILL(2*(3+4))".statement as StatementFill)
		assertThat(data.fill.reduce, is(14L))
	}

	@Test
	def void statementAssert() {
		val assertStatement = ('''ASSERT(2*(3+4), "Message")'''.statement as StatementAssert)
		assertThat(assertStatement.exp.reduce, is(14L))
		assertThat(assertStatement.message, is("Message"))
	}

	@Test
	def void statementAssertUnquoted() {
		val assertStatement = ('''ASSERT(2*(3+4), Message:Doesn_t-Have~To[Quoted])'''.statement as StatementAssert)
		assertThat(assertStatement.exp.reduce, is(14L))
		assertThat(assertStatement.message, is("Message:Doesn_t-Have~To[Quoted]"))
	}

	@Test
	def void statementInclude() {
		val assertStatement = ('''INCLUDE fileName!here'''.statement as StatementInclude)
		assertThat(assertStatement.filename, is("fileName!here"))
	}

	@Test
	def void statementAssignmentExpressionAdd() {
		val assignment = ('''etext = 2+1;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(3L))
	}

	@Test
	def void statementAssignmentExpressionSub() {
		val assignment = ('''etext = 2-1;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(1L))
	}

	@Test
	def void statementAssignmentExpressionMult() {
		val assignment = ('''etext = 2*3;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(6L))
	}

	@Test
	def void statementAssignmentExpressionDiv() {
		// Space is required after divide (same for GNU ld)
		val assignment = ('''etext = 4/ 2;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(2L))
	}

	@Test
	def void statementAssignmentExpressionTernary() {
		var assignment = ('''etext = 4 ? 23 : 19;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(23L))

		assignment = ('''etext = 0 ? 23 : 19;'''.statement as StatementAssignment).assignment
		assertThat(assignment.name, is('etext'))
		assertThat(assignment.feature, is('='))
		assertThat(assignment.exp.reduce, is(19L))
	}

	def InputSection inputSection(CharSequence inputSectionText) {
		val statement = inputSectionText.statement
		assertNotNull(statement)
		assertThat(statement, instanceOf(StatementInputSection))
		val inputSection = (statement as StatementInputSection).spec
		assertNotNull(inputSection)
		return inputSection
	}

	def InputSectionWild inputSectionWild(CharSequence inputSectionText) {
		val inputSectionWild = inputSectionText.inputSection
		assertThat(inputSectionWild, instanceOf(InputSectionWild))
		return inputSectionWild as InputSectionWild
	}

	def InputSectionFile inputSectionFile(CharSequence inputSectionText) {
		val inputSectionFile = inputSectionText.inputSection
		assertThat(inputSectionFile, instanceOf(InputSectionFile))
		return inputSectionFile as InputSectionFile
	}

	@Test
	def void inputSectionFile() {
		assertThat('filename'.inputSectionFile.file, is("filename"))
		assertThat('*'.inputSectionFile.file, is("*"))
		assertThat('"*.o"'.inputSectionFile.file, is("*.o"))
	}

	@Test
	def void inputSectionFileFlag() {
		val inputSection = 'INPUT_SECTION_FLAGS(FLAG1) filename'.inputSectionFile

		assertThat(inputSection.file, is("filename"))
		assertThat(inputSection.flags, contains("FLAG1"))
	}

	@Test
	def void inputSectionFileFlags() {
		val inputSection = 'INPUT_SECTION_FLAGS(FLAG1 & FLAG2) filename'.inputSectionFile

		assertThat(inputSection.file, is("filename"))
		assertThat(inputSection.flags, contains("FLAG1", "FLAG2"))
	}

	def Matcher<Wildcard> nameIs(String s) {
		return new FeatureMatcher<Wildcard, String>(equalTo(s), "string", "string") {
			override protected String featureValueOf(Wildcard actual) {
				return actual.name
			}
		};
	}

	def Matcher<Wildcard> primarySortIs(WildcardSort s) {
		return new FeatureMatcher<Wildcard, WildcardSort>(equalTo(s), "sort", "sort") {
			override protected WildcardSort featureValueOf(Wildcard actual) {
				return actual.primarySort
			}
		};
	}

	def Matcher<Wildcard> secondarySortIs(WildcardSort s) {
		return new FeatureMatcher<Wildcard, WildcardSort>(equalTo(s), "sort", "sort") {
			override protected WildcardSort featureValueOf(Wildcard actual) {
				return actual.secondarySort
			}
		};
	}

	@Test
	def void inputSectionWildFile() {
		assertThat('*(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_NONE), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('filename(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("filename"), primarySortIs(WildcardSort.SORT_NONE), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('"*.o"(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*.o"), primarySortIs(WildcardSort.SORT_NONE), secondarySortIs(WildcardSort.SORT_NONE)))

		// single sort options
		assertThat('SORT(*)(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('SORT_BY_NAME(*)(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_NAME), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('SORT_BY_ALIGNMENT(*)(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_ALIGNMENT), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('SORT_NONE(*)(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_NONE), secondarySortIs(WildcardSort.SORT_NONE)))
		assertThat('SORT_BY_INIT_PRIORITY(*)(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_INIT_PRIORITY),
				secondarySortIs(WildcardSort.SORT_NONE)))

		// legal combinations of sort options
		assertThat('SORT_BY_NAME(SORT_BY_NAME(*))(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_NAME), secondarySortIs(WildcardSort.SORT_BY_NAME)))
		assertThat('SORT_BY_ALIGNMENT(SORT_BY_ALIGNMENT(*))(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_ALIGNMENT),
				secondarySortIs(WildcardSort.SORT_BY_ALIGNMENT)))
		assertThat('SORT_BY_ALIGNMENT(SORT_BY_NAME(*))(.text)'.inputSectionWild.wildFile,
			allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_ALIGNMENT),
				secondarySortIs(WildcardSort.SORT_BY_NAME)))
			assertThat('SORT_BY_NAME(SORT_BY_ALIGNMENT(*))(.text)'.inputSectionWild.wildFile,
				allOf(nameIs("*"), primarySortIs(WildcardSort.SORT_BY_NAME),
					secondarySortIs(WildcardSort.SORT_BY_ALIGNMENT)))

			// excludes
			assertThat('EXCLUDE_FILE(excluded)*(.text)'.inputSectionWild.wildFile.excludes, contains("excluded"))
			assertThat('EXCLUDE_FILE(excluded)*(.text)'.inputSectionWild.wildFile, allOf(nameIs("*")))
			assertThat('EXCLUDE_FILE(excluded1 excluded2)*(.text)'.inputSectionWild.wildFile.excludes,
				contains("excluded1", "excluded2"))
			assertThat('EXCLUDE_FILE(excluded1 excluded2)*(.text)'.inputSectionWild.wildFile, allOf(nameIs("*")))

			// sorted excludes
			assertThat('SORT(EXCLUDE_FILE(excluded)*)(.text)'.inputSectionWild.wildFile.excludes, contains("excluded"))
			assertThat('SORT(EXCLUDE_FILE(excluded)*)(.text)'.inputSectionWild.wildFile,
				allOf(nameIs("*"), primarySortIs(WildcardSort.SORT), secondarySortIs(WildcardSort.SORT_NONE)))
		}

		@Test
		def void inputSectionWildSections() {
			assertThat('*(.text)'.inputSectionWild.sections, contains(nameIs(".text")))
			assertThat('*(.text .text.*)'.inputSectionWild.sections, contains(nameIs(".text"), nameIs(".text.*")))
			assertThat('*(.text, .text.*)'.inputSectionWild.sections, contains(nameIs(".text"), nameIs(".text.*")))
		}

		@Test
		def void inputSectionWildKeep() {
			val inputKeep = 'KEEP(*(.text))'.inputSectionWild
			assertThat(inputKeep.sections, contains(nameIs(".text")))
			assertThat(inputKeep.isKeep, is(true))
			val inputNoKeep = '*(.text)'.inputSectionWild
			assertThat(inputNoKeep.sections, contains(nameIs(".text")))
			assertThat(inputNoKeep.isKeep, is(false))
		}

		@Test
		def void inputSectionWildFlag() {
			val inputSection = 'INPUT_SECTION_FLAGS(FLAG1) *(.text)'.inputSectionWild

			assertThat(inputSection.wildFile, nameIs("*"))
			assertThat(inputSection.flags, contains("FLAG1"))
		}

		@Test
		def void inputSectionWildFlags() {
			val inputSection = 'INPUT_SECTION_FLAGS(FLAG1 & FLAG2) *(.text)'.inputSectionWild

			assertThat(inputSection.wildFile, nameIs("*"))
			assertThat(inputSection.flags, contains("FLAG1", "FLAG2"))
		}

		@Test
		def void starAndExclamationConflicts() {
			val model = '''
				MEMORY {
					FIRST (R ! W ! I) : ORIGIN = 0, LENGTH = 1
					SECOND (R ! W ! I) : ORIGIN = 0, LENGTH = 1*1
					THIRD (R ! W ! I) : ORIGIN = 0, LENGTH = 1*1
				}
			'''.parse
			assertNotNull(model)
			model.assertNoErrors
		}

		@Test
		def void memoryParsing() {
			val model = '''
				MEMORY {
					"FIRST MEMORY" : ORIGIN = A1ABx, LENGTH = (1+2)*3
					OTHER : ORIGIN = A1ABx, LENGTH = ALIGN(LENGTH("FIRST MEMORY"), 1000)
					.dots.everywhere. : ORIGIN = A1ABx, LENGTH = LENGTH(OTHER)
					TRICKY : ORIGIN = A1ABx, LENGTH = A1ABx +3 /* space required before + */
					MIXED_EXPRESSION_AND_NAME : ORIGIN = 0x0, LENGTH = ALIGN((5000+2)*3, LENGTH(RAM3))
					SAMELINE1 : ORIGIN = 0x0, LENGTH = 2 + 3 SAMELINE2 : ORIGIN = 0x0, LENGTH = 3 + 4
					MEMATTRIBS (RW!I) : ORIGIN = 5, LENGTH = 6
				}
			'''.parse
			assertNotNull(model)
			model.assertNoErrors
			val memoryCommand = model.statements.get(0) as MemoryCommand
			val memories = memoryCommand.memories
			assertThat(memories.size, is(8))

			// We are not setting up real memory sizes, so
			// we preload with known values here
			val Map<String, Long> memorySizes = new HashMap();
			memorySizes.put("FIRST MEMORY", 1234L)
			memorySizes.put("OTHER", 5678L)
			memorySizes.put("RAM3", 5000L)

			val memory0 = memories.get(0)
			assertThat(memory0.name, is("FIRST MEMORY"))
			assertThat(memory0.origin.reduce(memorySizes), is(0xA1AB#L))
			assertThat(memory0.length.reduce(memorySizes), is(9L))

			val memory1 = memories.get(1)
			assertThat(memory1.name, is("OTHER"))
			assertThat(memory1.origin.reduce(memorySizes), is(0xA1AB#L))
			assertThat(memory1.length.reduce(memorySizes), is(2000L))

			val memory2 = memories.get(2)
			assertThat(memory2.name, is(".dots.everywhere."))
			assertThat(memory2.origin.reduce(memorySizes), is(0xA1AB#L))
			assertThat(memory2.length.reduce(memorySizes), is(5678L))

			val memory3 = memories.get(3)
			assertThat(memory3.name, is("TRICKY"))
			assertThat(memory3.origin.reduce(memorySizes), is(0xA1AB#L))
			assertThat(memory3.length.reduce(memorySizes), is(0xA1AB#L + 3))

			val memory4 = memories.get(4)
			assertThat(memory4.name, is("MIXED_EXPRESSION_AND_NAME"))
			assertThat(memory4.origin.reduce(memorySizes), is(0L))
			assertThat(memory4.length.reduce(memorySizes), is(20000L))

			val memory5 = memories.get(5)
			assertThat(memory5.name, is("SAMELINE1"))
			assertThat(memory5.origin.reduce(memorySizes), is(0L))
			assertThat(memory5.length.reduce(memorySizes), is(5L))

			val memory6 = memories.get(6)
			assertThat(memory6.name, is("SAMELINE2"))
			assertThat(memory6.origin.reduce(memorySizes), is(0L))
			assertThat(memory6.length.reduce(memorySizes), is(7L))

			val memory7 = memories.get(7)
			assertThat(memory7.name, is("MEMATTRIBS"))
			assertThat(memory7.origin.reduce(memorySizes), is(5L))
			assertThat(memory7.length.reduce(memorySizes), is(6L))
			assertThat(memory7.attr, is("(RW!I)"))
		}
	}
