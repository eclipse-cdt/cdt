/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * The standard CDT scanner is CPreprocessor, which uses a Lexer to read from a file.  The
 * relationships look like:
 * <br>
 * GNUCPPSourceParser - CPreprocessor - Lexer
 * <p>
 * The implementation of CPreprocessor depends on reading from a file.  It might be possible
 * to configure it to get content from a String instead, but it seems like a complex change.
 * This simpler solution replaces the CPreprocessor with a simple scanner.  In this context,
 * the only part of CPreprocessor that seems to be needed is replacing the token type for
 * keywords.  In this case the relationships look like:
 * <br>
 * QtParser - StringScanner - Lexer
 */
@SuppressWarnings("restriction")
public class StringScanner implements IScanner {

	private final Lexer lexer;
	private final CharArrayIntMap keywords;

	public StringScanner(String str) {
		this.lexer = new Lexer(str.toCharArray(), new LexerOptions(), ILexerLog.NULL, null);
		keywords = new CharArrayIntMap(40, -1);
    	Keywords.addKeywordsCpp(keywords);
	}

	@Override
	public IToken nextToken() throws EndOfFileException {

		IToken token = lexer.nextToken();
		if (token.getType() != IToken.tIDENTIFIER)
			return token;

    	char[] name= token.getCharImage();
		int tokenType = keywords.get(name);
		if (tokenType != keywords.undefined)
			token.setType(tokenType);

		return token;
	}

	@Override
	public Map<String, IMacroBinding> getMacroDefinitions() {
		return null;
	}

	@Override
	public boolean isOnTopContext() {
		return false;
	}

	@Override
	public void cancel() {
	}

	@Override
	public ILocationResolver getLocationResolver() {
		return null;
	}

	@Override
	public void setTrackIncludeExport(IncludeExportPatterns patterns) {
    }

	@Override
	public void setContentAssistMode(int offset) {
	}

	@Override
	public void setSplitShiftROperator(boolean val) {
	}

	@Override
	public void setComputeImageLocations(boolean val) {
	}

	@Override
	public void setProcessInactiveCode(boolean val) {
	}

	@Override
	public void skipInactiveCode() throws OffsetLimitReachedException {
	}

	@Override
	public int getCodeBranchNesting() {
		return 0;
	}

	@Override
	@Deprecated
	public void setScanComments(boolean val) {
	}

	@Override
	public char[] getAdditionalNumericLiteralSuffixes() {
		return new char[] {};
	}
}
