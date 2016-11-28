/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.tests

import com.google.inject.Inject
import java.io.ByteArrayOutputStream
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.resource.SaveOptions
import org.eclipse.xtext.serializer.ISerializer
import org.junit.Test
import org.junit.runner.RunWith

import static org.eclipse.cdt.linkerscript.tests.TestUtils.*
import static org.junit.Assert.*
import org.junit.Ignore
import org.eclipse.cdt.linkerscript.serializer.LinkerScriptSerializer

@RunWith(XtextRunner)
@InjectWith(LinkerScriptInjectorProvider)
class SerializerTest {

	@Inject extension ParseHelper<LinkerScript> parseHelper
	@Inject extension ISerializer serializer

	@Test
	def void serializerMergesMemoryLines() {
		val model = '''
			MEMORY
			{
				ORAM1 : ORIGIN = 0x0, LENGTH = 1000
				ORAM2 : ORIGIN = 0x0, LENGTH = 2000
				ORAM3 : ORIGIN = 0x0, LENGTH = 3000
			}
		'''.parse

		val memCmd = (model.statements.get(0) as MemoryCommand)
		memCmd.memories.move(1, 2)

		val bos = new ByteArrayOutputStream();
		// need format for test to pass
		val options = SaveOptions.newBuilder().format.options
		model.eResource().save(bos, options.toOptionsMap());

		val afterModel = bos.toString.parse
		assertEObjectEquals(model, afterModel)
	}

	@Test
	def void editOutputSection() {
		val model = '''
			SECTIONS
			{
				name = 0;
			}
		'''.parse

		val secCmd = (model.statements.get(0) as SectionsCommand)
		val stmt = secCmd.sectionCommands.get(0) as StatementAssignment
		stmt.assignment.name = "name2"

		val bos = new ByteArrayOutputStream();
		// need format for test to pass
		val options = SaveOptions.newBuilder().format.options
		model.eResource().save(bos, options.toOptionsMap());

		val serialized = bos.toString
		assertEquals('''
			SECTIONS
			{
				name2 = 0;
			}
		'''.toString, serialized)
		val afterModel = serialized.parse
		assertEObjectEquals(model, afterModel)
	}

	/**
	 * @see LinkerScriptSerializer
	 */
	@Ignore("Some changes mess up formatting when serializing")
	@Test
	def void testSerializeReplacementWithFormat() {
		val input = '''
			SECTIONS
			{
				sec1 : {} >MEM
				/*comment*/
				sec2 : {} >MEM
			}
		'''
		val model = input.parse

		val secCmd = model.statements.get(0) as SectionsCommand
		val section = secCmd.sectionCommands.get(1) as OutputSection
		section.name = "newsec2"

		val options = SaveOptions.newBuilder().format.options
		val replacement = serializer.serializeReplacement(section, options)
		println(replacement)

		val inputSb = new StringBuilder(input)
		replacement.applyTo(inputSb)
		val serialized = inputSb.toString
		assertEquals('''
			SECTIONS
			{
				sec1 : {} >MEM
				/*comment*/
				newsec2 : {} >MEM
			}
		'''.toString, serialized)
	}

	/**
	 * @see LinkerScriptSerializer
	 */
	@Ignore("Some changes mess up formatting when serializing")
	@Test
	def void testSerializeReplacement() {
		val input = '''
			SECTIONS
			{
				sec1 : {} >MEM
				/*comment*/
				sec2 : {} >MEM
			}
		'''
		val model = input.parse

		val secCmd = model.statements.get(0) as SectionsCommand
		val section = secCmd.sectionCommands.get(1) as OutputSection
		section.name = "newsec2"

		val options = SaveOptions.newBuilder().options
		val replacement = serializer.serializeReplacement(section, options)
		println(replacement)

		val inputSb = new StringBuilder(input)
		replacement.applyTo(inputSb)
		val serialized = inputSb.toString
		assertEquals('''
			SECTIONS
			{
				sec1 : {} >MEM
				/*comment*/
				newsec2 : {} >MEM
			}
		'''.toString, serialized)
	}

}
