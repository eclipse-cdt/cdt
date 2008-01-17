/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Performs step by step macro expansion for an exact macro expansion location.
 * @since 5.0
 */
public class SingleMacroExpansionExplorer extends MacroExpansionExplorer {

	private static final LexerOptions LEX_OPTIONS= new LexerOptions();
	static {
		LEX_OPTIONS.fCreateImageLocations= false;
	}
	
	private final String fInput;
	private final CharArrayMap<PreprocessorMacro> fDictionary;
	private MacroExpansionStep fFullExpansion;
	private int fExpansionCount;
	private String fFilePath;
	private int fLineNumber;

	public SingleMacroExpansionExplorer(String input, IASTName ref, IASTName[] implicitRefs, String filePath, int lineNumber) {
		fInput= input;
		fDictionary= createDictionary(ref, implicitRefs);
		fFilePath= filePath;
		fLineNumber= lineNumber;
	}

	private CharArrayMap<PreprocessorMacro> createDictionary(IASTName ref, IASTName[] implicitRefs) {
		CharArrayMap<PreprocessorMacro> map= new CharArrayMap<PreprocessorMacro>(implicitRefs.length+1);
		addMacroDefinition(map, ref);
		for (IASTName name : implicitRefs) {
			addMacroDefinition(map, name);
		}
		return map;
	}

	private void addMacroDefinition(CharArrayMap<PreprocessorMacro> map, IASTName name) {
		IBinding binding= name.getBinding();
		if (binding instanceof PreprocessorMacro) {
			map.put(name.toCharArray(), (PreprocessorMacro) binding);
		}
	}

	@Override
	public IMacroExpansionStep getFullExpansion() {
		computeExpansion();
		return fFullExpansion;
	}

	@Override
	public int getExpansionStepCount() {
		computeExpansion();
		return fExpansionCount;
	}

	private void computeExpansion() {
		MacroExpander expander= new MacroExpander(ILexerLog.NULL, fDictionary, null, LEX_OPTIONS);
		MacroExpansionTracker tracker= new MacroExpansionTracker(Integer.MAX_VALUE);
		expander.expand(fInput, tracker, fFilePath, fLineNumber);
		
		fExpansionCount= tracker.getStepCount();
		ReplaceEdit r= tracker.getReplacement();
		ReplaceEdit[] replacements= r==null ? new ReplaceEdit[0] : new ReplaceEdit[]{r};
		fFullExpansion= new MacroExpansionStep(fInput, null, replacements);
	}

	@Override
	public MacroExpansionStep getExpansionStep(int step) throws IndexOutOfBoundsException {
		computeExpansion();
		if (step < 0 || step >= fExpansionCount) {
			throw new IndexOutOfBoundsException();
		}
		MacroExpander expander= new MacroExpander(ILexerLog.NULL, fDictionary, null, LEX_OPTIONS);
		MacroExpansionTracker tracker= new MacroExpansionTracker(step);
		expander.expand(fInput, tracker, fFilePath, fLineNumber);
		
		fExpansionCount= tracker.getStepCount();
		ReplaceEdit r= tracker.getReplacement();
		ReplaceEdit[] replacements= r==null ? new ReplaceEdit[0] : new ReplaceEdit[]{r};
		return new MacroExpansionStep(tracker.getCodeBeforeStep(), tracker.getExpandedMacro(), replacements);
	}
}
