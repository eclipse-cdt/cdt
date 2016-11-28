/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.linkerscript.linkerScript.Assignment;
import org.eclipse.cdt.linkerscript.linkerScript.LExpression;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.cdt.linkerscript.linkerScript.MemoryCommand;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.SectionsCommand;
import org.eclipse.cdt.linkerscript.linkerScript.Statement;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssert;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.XtextResource;

public class LinkerScriptModelUtils {
	protected static final LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;

	public static List<Memory> getAllMemories(LinkerScript ld) {
		return getAllMemoriesStream(ld).collect(Collectors.toList());
	}

	public static Stream<Memory> getAllMemoriesStream(LinkerScript ld) {
		return ld.getStatements().stream().filter(stmt -> stmt instanceof MemoryCommand)
				.map(stmt -> (MemoryCommand) stmt).flatMap(memcmd -> memcmd.getMemories().stream());
	}

	public static LExpression createLiteral0() {
		LNumberLiteral literal = factory.createLNumberLiteral();
		literal.setValue(0L);
		return literal;
	}

	public static <T> Optional<T> getLastOfType(List<?> list, Class<T> clazz) {
		Stream<?> stream = list.stream();
		Stream<?> thoseOfCorrectType = stream.filter(clazz::isInstance);
		Stream<T> castToType = thoseOfCorrectType.map(clazz::cast);
		Optional<T> lastOne = castToType.reduce((a, b) -> b);
		return lastOne;
	}

	public static SectionsCommand getOrCreateLastSectionsCommand(LinkerScript linkerScript) {
		Optional<SectionsCommand> lastOne = getLastOfType(linkerScript.getStatements(), SectionsCommand.class);
		return lastOne.orElseGet(() -> {
			SectionsCommand secCmd = factory.createSectionsCommand();
			linkerScript.getStatements().add(secCmd);
			return secCmd;
		});
	}

	public static MemoryCommand getOrCreateLastMemoryCommand(LinkerScript linkerScript) {
		Optional<MemoryCommand> lastOne = getLastOfType(linkerScript.getStatements(), MemoryCommand.class);
		return lastOne.orElseGet(() -> {
			MemoryCommand memCmd = factory.createMemoryCommand();
			linkerScript.getStatements().add(memCmd);
			return memCmd;
		});
	}

	public static OutputSection createOutputSection(SectionsCommand sectionsCommand) {
		Optional<OutputSection> lastOutputSection = getLastOfType(sectionsCommand.getSectionCommands(),
				OutputSection.class);

		LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;
		OutputSection outputSection = factory.createOutputSection();
		outputSection.setName(".new_section");

		lastOutputSection.ifPresent(last -> {
			outputSection.setMemory(last.getMemory());
			outputSection.setAtMemory(last.getAtMemory());
		});

		sectionsCommand.getSectionCommands().add(outputSection);
		return outputSection;
	}

	public static StatementAssignment createAssignment(SectionsCommand sectionsCommand) {
		LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;
		StatementAssignment statementAssignment = factory.createStatementAssignment();
		Assignment assignment = factory.createAssignment();
		assignment.setName("symbol");
		assignment.setFeature("=");
		LNumberLiteral literal = factory.createLNumberLiteral();
		literal.setValue(0L);
		assignment.setExp(literal);
		statementAssignment.setAssignment(assignment);

		sectionsCommand.getSectionCommands().add(statementAssignment);
		return statementAssignment;
	}

	public static Statement createStatement(SectionsCommand sectionsCommand) {
		LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;
		StatementAssert statement = factory.createStatementAssert();
		LNumberLiteral literal = factory.createLNumberLiteral();
		literal.setValue(1L);
		statement.setExp(literal);
		statement.setMessage("New statement");

		sectionsCommand.getSectionCommands().add(statement);
		return statement;
	}

	public static Memory createMemory(MemoryCommand memCmd) {
		EList<Memory> list = memCmd.getMemories();
		Memory mem = factory.createMemory();
		mem.setName("Memory");
		LNumberLiteral origin = factory.createLNumberLiteral();
		origin.setValue(0x0L);
		mem.setOrigin(origin);
		LNumberLiteral length = factory.createLNumberLiteral();
		length.setValue(0x0L);
		mem.setLength(length);

		list.add(mem);
		return mem;
	}

	public static LinkerScript getOrCreateLinkerScript(XtextResource resource) {
		EList<EObject> contents = resource.getContents();
		if (contents.isEmpty()) {
			LinkerScript linkerScript = factory.createLinkerScript();
			contents.add(linkerScript);
			return linkerScript;
		} else {
			return (LinkerScript) contents.get(0);
		}
	}

}
