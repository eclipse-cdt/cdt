/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author jcamelon
 */
public interface IScannerPreprocessorLog {

    public void startTranslationUnit();

    public void endTranslationUnit(int finalOffset);

    public void startInclusion(char[] includePath, int offset);

    public void endInclusion(char[] includePath, int offset);

    public void enterObjectStyleMacroExpansion(char[] name, char[] expansion,
            int offset);

    public void exitObjectStyleMacroExpansion(char[] name, int offset);

    public void enterFunctionStyleExpansion(char[] name, char[][] parameters,
            char[] expansion, int offset);

    public void exitFunctionStyleExpansion(char[] name, int offset);

    public void defineObjectStyleMacro(ObjectStyleMacro m, int startOffset,
            int nameOffset, int nameEndOffset, int endOffset);

    public void defineFunctionStyleMacro(FunctionStyleMacro m, int startOffset,
            int nameOffset, int nameEndOffset, int endOffset);

    public void encounterPoundIf(int startOffset, int endOffset);

    public void encounterPoundPragma(int startOffset, int endOffset);

    public void encounterPoundError(int startOffset, int endOffset);

    public void encounterPoundIfdef(int startOffset, int endOffset);

    public void encounterPoundUndef(int startOffset, int endOffset);

    public void encounterPoundElse(int startOffset, int endOffset);

    public void encounterPoundElif(int startOffset, int endOffset);

    public void encounterPoundEndIf(int startOffset, int endOffset);
}