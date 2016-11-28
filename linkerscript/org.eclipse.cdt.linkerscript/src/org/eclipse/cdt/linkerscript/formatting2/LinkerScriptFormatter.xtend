/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.formatting2;

import java.util.List
import org.eclipse.cdt.linkerscript.linkerScript.Assignment
import org.eclipse.cdt.linkerscript.linkerScript.FileList
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionFile
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionWild
import org.eclipse.cdt.linkerscript.linkerScript.LExpression
import org.eclipse.cdt.linkerscript.linkerScript.LParenthesizedExpression
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.Memory
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionAlignExpression
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionAlignWithInput
import org.eclipse.cdt.linkerscript.linkerScript.PhdrsCommand
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.cdt.linkerscript.linkerScript.Statement
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment
import org.eclipse.cdt.linkerscript.linkerScript.StatementExtern
import org.eclipse.cdt.linkerscript.linkerScript.StatementGroup
import org.eclipse.cdt.linkerscript.linkerScript.StatementInclude
import org.eclipse.cdt.linkerscript.linkerScript.StatementNoCrossRefs
import org.eclipse.cdt.linkerscript.linkerScript.StatementNoCrossRefsTo
import org.eclipse.cdt.linkerscript.linkerScript.StatementNop
import org.eclipse.cdt.linkerscript.linkerScript.Wildcard
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion

import static org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage.Literals.*
import org.eclipse.cdt.linkerscript.linkerScript.StatementInput

class LinkerScriptFormatter extends AbstractFormatter2 {

	def dispatch void format(LinkerScript linkerScript, extension IFormattableDocument document) {
		linkerScript.prepend[setNewLines(0); noSpace]
		linkerScript.statements.forEach[format]
	}

	/**
	 * Apply common paired brace formatting
	 * @return the closing brace (which has different formatting depending on context)
	 */
	def ISemanticRegion formatBraces(EObject obj, extension IFormattableDocument document) {
		val open = obj.regionFor.keyword("{")
		val close = obj.regionFor.keyword("}")
		interior(open, close)[indent]
		open.surround[newLine]
		return close
	}

	def dispatch void format(MemoryCommand memoryCommand, extension IFormattableDocument document) {
		memoryCommand.formatBraces(document).surround[newLine]
		memoryCommand.memories.forEach[format]
	}

	def dispatch void format(SectionsCommand sectionsCommand, extension IFormattableDocument document) {
		sectionsCommand.formatBraces(document).surround[newLine]
		sectionsCommand.sectionCommands.forEach[format]
	}

	def dispatch void format(OutputSection outputSection, extension IFormattableDocument document) {
		val closeBrace = outputSection.formatBraces(document)
		closeBrace.prepend[newLine]
		if (outputSection.regionFor.keyword('>') != null) {
			closeBrace.append[oneSpace]
		}

		outputSection.regionFor.feature(OUTPUT_SECTION__NAME).surround[oneSpace]
		outputSection.address.surround[oneSpace]
		outputSection.type.surround[noSpace]
		outputSection.regionFor.keyword(':').surround[oneSpace]
		// two ATs!
		outputSection.regionFor.keywords('AT').forEach[append[noSpace].prepend[oneSpace]]
		outputSection.at.surround[noSpace]
		outputSection.align.surround[oneSpace]
		outputSection.regionFor.keyword('SUBALIGN').append[noSpace].prepend[oneSpace]
		outputSection.subAlign.surround[noSpace]
		outputSection.constraint.surround[oneSpace]
		outputSection.regionFor.feature(OUTPUT_SECTION__MEMORY).prepend[noSpace].append[oneSpace]
		outputSection.regionFor.feature(OUTPUT_SECTION__AT_MEMORY).prepend[noSpace].append[oneSpace]
		outputSection.regionFor.features(OUTPUT_SECTION__PHDRS).forEach[prepend[noSpace].append[oneSpace]]
		outputSection.fill.prepend[noSpace]
		outputSection.regionFor.keyword(',').prepend[noSpace]
		outputSection.append[newLine]

		outputSection.eContents.forEach[format]
	}

	def dispatch void format(OutputSectionAlignExpression align, extension IFormattableDocument document) {
		align.surround[oneSpace]
		align.regionFor.keyword('ALIGN').append[noSpace]
		align.regionFor.keyword('(').append[noSpace]
		align.regionFor.keyword(')').prepend[noSpace]
		align.exp.format
	}

	def dispatch void format(OutputSectionAlignWithInput align, extension IFormattableDocument document) {
		align.surround[oneSpace]
	}

	def dispatch void format(PhdrsCommand phdrs, extension IFormattableDocument document) {
		phdrs.formatBraces(document).surround[newLine]
	}

	def dispatch void format(Memory memory, extension IFormattableDocument document) {
		memory.prepend[newLine]
		memory.regionFor.keyword(':').surround[oneSpace]
		memory.regionFor.keywords('=').forEach[surround[oneSpace]]
		memory.regionFor.keyword(',').prepend[noSpace].append[oneSpace]
		memory.origin.format
		memory.length.format
	}

	def dispatch void format(LParenthesizedExpression exp, extension IFormattableDocument document) {
		exp.regionFor.keyword('(').append[noSpace]
		exp.regionFor.keyword(')').prepend[noSpace]
		exp.exp.format
	}

	def dispatch void format(LExpression exp, extension IFormattableDocument document) {
		// these ( ) cover function call looking things, note parenthesised
		// expressions are specially handled above
		exp.regionFor.keyword('(').surround[noSpace]
		exp.regionFor.keyword(')').prepend[noSpace]
		val operators = exp.regionFor.features(LBINARY_OPERATION__FEATURE, ASSIGNMENT__FEATURE,
			LUNARY_OPERATION__FEATURE)
		operators.forEach[surround[oneSpace]]
		exp.eContents.forEach[format]
	}

	def dispatch void format(StatementNop nop, extension IFormattableDocument document) {
		nop.prepend[noSpace]
	}

	def dispatch void format(StatementAssignment assign, extension IFormattableDocument document) {
		assign.regionFor.keywords(',', ';').forEach[prepend[noSpace]]
		assign.append[newLine]
		assign.eContents.forEach[format]
	}

	def dispatch void format(Assignment assign, extension IFormattableDocument document) {
		assign.regionFor.feature(ASSIGNMENT__NAME).prepend[noSpace].append[oneSpace]
		assign.regionFor.feature(ASSIGNMENT__FEATURE).surround[oneSpace]
		assign.regionFor.keywords('(').forEach[surround[noSpace]]
		assign.regionFor.keywords(')').forEach[prepend[noSpace]]
		assign.exp.format
	}

	def void formatStatementCommon(EObject statement, extension IFormattableDocument document) {
		statement.regionFor.keywords('(').forEach[surround[noSpace]]
		statement.regionFor.keywords(')').forEach[prepend[noSpace]]
		statement.regionFor.keywords(',').forEach[prepend[noSpace].append[oneSpace]]

		statement.append[newLine]
		statement.eContents.forEach[format]
	}

	def void formatSpacesInParameters(List<ISemanticRegion> names, extension IFormattableDocument document) {
		// apply to all but first
		for (var i = 1; i < names.length; i++) {
			names.get(i).prepend[oneSpace]
		}
	}

	def dispatch void format(Statement statement, extension IFormattableDocument document) {
		statement.formatStatementCommon(document)
	}

	def dispatch void format(StatementGroup group, extension IFormattableDocument document) {
		group.formatStatementCommon(document)
		// apply to all but first
		for (var i = 1; i < group.files.length; i++) {
			group.files.get(i).prepend[oneSpace]
		}
	}

	def dispatch void format(StatementInput input, extension IFormattableDocument document) {
		input.formatStatementCommon(document)
		input.list.format
	}

	def dispatch void format(FileList fileList, extension IFormattableDocument document) {
		fileList.regionFor.keywords('(').forEach[surround[noSpace]]
		fileList.regionFor.keywords(')').forEach[prepend[noSpace]]
		fileList.regionFor.keywords(',').forEach[prepend[noSpace].append[oneSpace]]
		// apply to all but first
		for (var i = 1; i < fileList.files.length; i++) {
			fileList.files.get(i).prepend[oneSpace]
		}
		fileList.list.format
	}

	def dispatch void format(StatementNoCrossRefs statement, extension IFormattableDocument document) {
		statement.formatStatementCommon(document)
		formatSpacesInParameters(statement.regionFor.features(STATEMENT_NO_CROSS_REFS__SECTIONS), document)
	}

	def dispatch void format(StatementNoCrossRefsTo statement, extension IFormattableDocument document) {
		statement.formatStatementCommon(document)
		formatSpacesInParameters(statement.regionFor.features(STATEMENT_NO_CROSS_REFS_TO__SECTIONS), document)
	}

	def dispatch void format(StatementExtern statement, extension IFormattableDocument document) {
		statement.formatStatementCommon(document)
		formatSpacesInParameters(statement.regionFor.features(STATEMENT_EXTERN__SECTIONS), document)
	}

	def dispatch void format(StatementInclude statement, extension IFormattableDocument document) {
		statement.formatStatementCommon(document)
		statement.regionFor.keyword('INCLUDE').append[oneSpace]
	}

	def dispatch void format(InputSectionFile inputSection, extension IFormattableDocument document) {
		inputSection.regionFor.keywords('(').forEach[surround[noSpace]]
		inputSection.regionFor.keywords(')').forEach[prepend[noSpace]]
		inputSection.regionFor.keywords('&').forEach[surround[oneSpace]]
		if (inputSection.regionFor.keyword('INPUT_SECTION_FLAGS') != null) {
			inputSection.regionFor.feature(INPUT_SECTION_FILE__FILE).prepend[oneSpace]
		}

		inputSection.append[newLine]
	}

	def dispatch void format(InputSectionWild inputSection, extension IFormattableDocument document) {
		inputSection.regionFor.keywords('(').forEach[surround[noSpace]]
		inputSection.regionFor.keywords(')').forEach[prepend[noSpace]]
		inputSection.regionFor.keywords('&').forEach[surround[oneSpace]]
		inputSection.regionFor.keywords(',').forEach[prepend[noSpace].append[oneSpace]]

		if (inputSection.regionFor.keyword('INPUT_SECTION_FLAGS') != null) {
			inputSection.wildFile.prepend[oneSpace]
		}

		inputSection.append[newLine]
		inputSection.wildFile.format
		val sections = inputSection.sections
		for (var i = 0; i < sections.size; i++) {
			if (i != 0) {
				sections.get(i).prepend[oneSpace]
			}
			sections.get(i).format
		}
	}

	def dispatch void format(Wildcard wild, extension IFormattableDocument document) {
		wild.regionFor.keywords('(').forEach[surround[noSpace]]
		wild.regionFor.keywords(')').forEach[prepend[noSpace]]
		if (wild.excludes != null && wild.excludes.size != 0) {
			wild.regionFor.feature(WILDCARD__NAME).prepend[oneSpace]
		}
		formatSpacesInParameters(wild.regionFor.features(WILDCARD__EXCLUDES), document)
	}
}
