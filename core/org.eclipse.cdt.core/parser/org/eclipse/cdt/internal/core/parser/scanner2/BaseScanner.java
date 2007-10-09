/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *     Anton Leherbauer (Wind River Systems)
 *     Emanuel Graf (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.CharTable;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author Doug Schaefer
 *  
 */
abstract class BaseScanner implements IScanner {
	static boolean cacheIdentifiers = true;

    protected static final char[] ONE = "1".toCharArray(); //$NON-NLS-1$

    protected static final char[] ELLIPSIS_CHARARRAY = "...".toCharArray(); //$NON-NLS-1$

    protected static final char[] VA_ARGS_CHARARRAY = "__VA_ARGS__".toCharArray(); //$NON-NLS-1$

	protected final IToken eocToken = new SimpleToken(IToken.tEOC, Integer.MAX_VALUE, null, Integer.MAX_VALUE);
	
    /**
     * @author jcamelon
     *  
     */
    protected static class InclusionData {

        public final Object inclusion;
        public final CodeReader reader;
        public final boolean includeOnce;

		/**
         * @param reader
         * @param inclusion
         * @param includeOnce
         */
        public InclusionData(CodeReader reader, Object inclusion, boolean includeOnce) {
            this.reader = reader;
            this.inclusion = inclusion;
            this.includeOnce= includeOnce;
        }
        
        public String toString() {
            return reader.toString();
        }
    }

    protected static class MacroData {
        public MacroData(int start, int end, IMacro macro) {
            this.startOffset = start;
            this.endOffset = end;
            this.macro = macro;
        }
        
        private final int startOffset;
        private final int endOffset;

        public final IMacro macro;
        
        public String toString() {
            return macro.toString();
        }
        
        public int getStartOffset() {
        	return startOffset;
        }
        public int getLength() {
        	return endOffset-startOffset;
        }
    }
    
    protected static class FunctionMacroData extends MacroData{
    	private CharArrayObjectMap arguments;
    	 public FunctionMacroData(int start, int end, IMacro macro, CharArrayObjectMap argmap) {
    		 super(start,end, macro);
    		 arguments = argmap;
    	 }
    	 
    	 public CharArrayObjectMap getActualArgs() {
    		 return arguments;
    	 }
    }
    
    protected interface IIncludeFileTester {
    	Object checkFile(String path, String fileName);
    }
    
    final private IIncludeFileTester createCodeReaderTester= new IIncludeFileTester() { 
    	public Object checkFile(String path, String fileName) {
    		return createReader(path, fileName);
    	}
    };

    protected ParserLanguage language;

    protected IParserLogService log;

    protected CharArrayObjectMap definitions = new CharArrayObjectMap(512);

    protected String[] includePaths;
    protected String[] quoteIncludePaths;

    /** Set of already included files */
    protected CharArraySet includedFiles= new CharArraySet(32);

    int count;

    protected ExpressionEvaluator expressionEvaluator;

    // The context stack
    protected static final int bufferInitialSize = 8;

    protected int bufferStackPos = -1;

    protected char[][] bufferStack = new char[bufferInitialSize][];

    protected Object[] bufferData = new Object[bufferInitialSize];

    protected int[] bufferPos = new int[bufferInitialSize];

    protected int[] bufferLimit = new int[bufferInitialSize];

    int[] lineNumbers = new int[bufferInitialSize];

    protected int[] lineOffsets = new int[bufferInitialSize];

    //branch tracking
    protected int branchStackPos = -1;

    protected int[] branches = new int[bufferInitialSize];

    //states
    final static protected int BRANCH_IF = 1;

    final static protected int BRANCH_ELIF = 2;

    final static protected int BRANCH_ELSE = 3;

    final static protected int BRANCH_END = 4;

    // Utility
    protected static String[] EMPTY_STRING_ARRAY = new String[0];

    protected static char[] EMPTY_CHAR_ARRAY = new char[0];

    protected static EndOfFileException EOF = new EndOfFileException();

    protected ParserMode parserMode;

    protected boolean isInitialized = false;
    protected boolean macroFilesInitialized = false;

    protected final char[] suffixes;

    protected final boolean support$Initializers;

    protected final boolean supportMinAndMax;

    protected boolean scanComments;
    
    protected IToken[] commentsFromInactiveCode = new IToken[0];

    protected final CharArrayIntMap additionalKeywords;

    protected final CharArrayIntMap additionalPPKeywords;

    public BaseScanner(CodeReader reader, IScannerInfo info,
            ParserMode parserMode, ParserLanguage language,
            IParserLogService log, IScannerExtensionConfiguration configuration) {
    	
        this.parserMode = parserMode;
        this.language = language;
        this.log = log;
        this.scanComments = false;
        
        if (configuration.supportAdditionalNumericLiteralSuffixes() != null)
            suffixes = configuration.supportAdditionalNumericLiteralSuffixes();
        else
            suffixes = EMPTY_CHAR_ARRAY;
        support$Initializers = configuration.support$InIdentifiers();
        supportMinAndMax = configuration.supportMinAndMaxOperators();

        if (language == ParserLanguage.C)
            keywords = ckeywords;
        else
            keywords = cppkeywords;

        additionalKeywords = configuration.getAdditionalKeywords();
        additionalPPKeywords= configuration.getAdditionalPreprocessorKeywords();

        setupBuiltInMacros(configuration);

        if (info.getDefinedSymbols() != null) {
            Map symbols = info.getDefinedSymbols();
            String[] keys = (String[]) symbols.keySet().toArray(
                    EMPTY_STRING_ARRAY);
            for (int i = 0; i < keys.length; ++i) {
                String symbolName = keys[i];
                Object value = symbols.get(symbolName);

                if (value instanceof String) {
                    if (configuration.initializeMacroValuesTo1()
                            && ((String) value).trim().equals(EMPTY_STRING))
                        addDefinition(symbolName.toCharArray(), ONE);
                    else
                        addDefinition(symbolName.toCharArray(),
                                ((String) value).toCharArray());
                }
            }
        }
        includePaths= quoteIncludePaths= info.getIncludePaths();
    }

    /**
     * @param reader
     * @param info
     */
    protected void postConstructorSetup(CodeReader reader, IScannerInfo info) {
        if (info instanceof IExtendedScannerInfo) {
            extendedScannerInfoSetup(reader, info);
        } else {
            macroFilesInitialized = true;
            pushContext(reader.buffer, reader);
            isInitialized = true;
        }
    }

    /**
     * @param reader
     * @param info
     */
    protected void extendedScannerInfoSetup(CodeReader reader, IScannerInfo info) {
        IExtendedScannerInfo einfo = (IExtendedScannerInfo) info;
        // setup separate include path for quote includes.
        String[] qip= einfo.getLocalIncludePath();
        if (qip != null && qip.length > 0) {
        	quoteIncludePaths= new String[qip.length + includePaths.length];
        	System.arraycopy(qip, 0, quoteIncludePaths, 0, qip.length);
        	System.arraycopy(includePaths, 0, quoteIncludePaths, qip.length, includePaths.length);
        }
        
        // 
        final String[] macroFiles = einfo.getMacroFiles();
		if (macroFiles != null) {
            for (int i = 0; i < macroFiles.length; ++i) {
                CodeReader r= findInclusion(macroFiles[i], true, false, null);
                if (r != null) {
                	pushContext(r.buffer, r);
                	while (true) {
                		try {
                			nextToken();
                		} catch (EndOfFileException e) {
                			finished = false;
                			break;
                		}
                	}
                }
            }
		}

        macroFilesInitialized = true;
        pushContext(reader.buffer, reader);

        final String[] preIncludeFiles= einfo.getIncludeFiles();
    	if (parserMode != ParserMode.QUICK_PARSE && preIncludeFiles != null) {
    		for (int i = 0; i < preIncludeFiles.length; i++) {
                final String file = preIncludeFiles[i];
				CodeReader r= findInclusion(file, true, false, null);
                if (r != null) {
                	int o = getCurrentOffset() + 1; 
                	int l = getLineNumber(o);
                	Object incObj = createInclusionConstruct(file.toCharArray(), r.filename, false, o,
                			l, o, o, l, o, l, true);
                	InclusionData d = new InclusionData(r, incObj, false);
                	pushContext(r.buffer, d);
            	}
    		}
    	}
        isInitialized = true;
    }

    protected void pushContext(char[] buffer) {
        if (++bufferStackPos == bufferStack.length) {
            int size = bufferStack.length * 2;

            char[][] oldBufferStack = bufferStack;
            bufferStack = new char[size][];
            System.arraycopy(oldBufferStack, 0, bufferStack, 0,
                    oldBufferStack.length);

            Object[] oldBufferData = bufferData;
            bufferData = new Object[size];
            System.arraycopy(oldBufferData, 0, bufferData, 0,
                    oldBufferData.length);

            int[] oldBufferPos = bufferPos;
            bufferPos = new int[size];
            System
                    .arraycopy(oldBufferPos, 0, bufferPos, 0,
                            oldBufferPos.length);

            int[] oldBufferLimit = bufferLimit;
            bufferLimit = new int[size];
            System.arraycopy(oldBufferLimit, 0, bufferLimit, 0,
                    oldBufferLimit.length);

            int[] oldLineNumbers = lineNumbers;
            lineNumbers = new int[size];
            System.arraycopy(oldLineNumbers, 0, lineNumbers, 0,
                    oldLineNumbers.length);

            int[] oldLineOffsets = lineOffsets;
            lineOffsets = new int[size];
            System.arraycopy(oldLineOffsets, 0, lineOffsets, 0,
                    oldLineOffsets.length);

        }

        bufferStack[bufferStackPos] = buffer;
        bufferPos[bufferStackPos] = -1;
        lineNumbers[bufferStackPos] = 1;
        lineOffsets[bufferStackPos] = 0;
        bufferLimit[bufferStackPos] = buffer.length;
    }

    protected void pushContext(char[] buffer, Object data) {
        if (data instanceof InclusionData) {
            InclusionData inclusionData= (InclusionData)data;
			if (isCircularInclusion( inclusionData ))
                return;
            if (inclusionData.includeOnce && isRepeatedInclusion(inclusionData))
            	return;
            includedFiles.put(inclusionData.reader.filename);
        }
        pushContext(buffer);
        bufferData[bufferStackPos] = data;

    }

    protected boolean isCircularInclusion(InclusionData data) {
        for (int i = 0; i < bufferStackPos; ++i) {
            if (bufferData[i] instanceof CodeReader
                    && CharArrayUtils.equals(
                            ((CodeReader) bufferData[i]).filename,
                            data.reader.filename)) {
                return true;
            } else if (bufferData[i] instanceof InclusionData
                    && CharArrayUtils
                            .equals(
                                    ((InclusionData) bufferData[i]).reader.filename,
                                    data.reader.filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given inclusion was already included before.
     * 
	 * @param inclusionData
	 * @return
	 */
	private boolean isRepeatedInclusion(InclusionData inclusionData) {
        return includedFiles.containsKey(inclusionData.reader.filename);
	}


    protected Object popContext() {
        //NOTE - do not set counters to 0 or -1 or something
        //Subclasses may require those values in their popContext()
        bufferStack[bufferStackPos] = null;

        Object result = bufferData[bufferStackPos];
        bufferData[bufferStackPos] = null;
        --bufferStackPos;

        return result;
    }

    public IMacro addDefinition(char[] key, char[] value) {
        int idx = CharArrayUtils.indexOf('(', key);
        if (idx == -1) {
        	IMacro macro = new ObjectStyleMacro(key, value);
            definitions.put(key, macro);
            return macro;
        } else {
            pushContext(key);
            bufferPos[bufferStackPos] = idx;
            char[][] args = null;
            try {
                args = extractMacroParameters(0, EMPTY_STRING_CHAR_ARRAY, false);
            } finally {
                popContext();
            }

            if (args != null) {
                key = CharArrayUtils.extract(key, 0, idx);
                return addDefinition(key, args, value);
            } else
            	return null;
        }
    }

    public IMacro addDefinition(char[] name, char[][] params, char[] expansion) {
    	IMacro macro = new FunctionStyleMacro(name, expansion, params);
        definitions.put(name, macro);
        return macro;
    }
    
    public void addDefinition(IMacro macro) {
    	definitions.put(macro.getName(), macro);
    }
    
    public int getCount() {
        return count;
    }

    public Map getDefinitions() {
        CharArrayObjectMap objMap = getRealDefinitions();
        int size = objMap.size();
        Map hashMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            hashMap.put(String.valueOf(objMap.keyAt(i)), objMap.getAt(i));
        }

        return hashMap;
    }

    public CharArrayObjectMap getRealDefinitions() {
        return definitions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getIncludePaths()
     */
    public String[] getIncludePaths() {
        return includePaths;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#isOnTopContext()
     */
    public boolean isOnTopContext() {
		for (int i = 1; i <= bufferStackPos; ++i)
			if (bufferData[i] instanceof InclusionData)
				return false;
		return true;
    }

    protected IToken lastToken;

    protected IToken nextToken;

    protected boolean finished = false;

    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    protected static final char[] EMPTY_STRING_CHAR_ARRAY = new char[0];

    protected boolean isCancelled = false;

    public synchronized void cancel() {
        isCancelled = true;
        int index = bufferStackPos < 0 ? 0 : bufferStackPos;
        bufferPos[index] = bufferLimit[index];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#nextToken()
     */
    public IToken nextToken() throws EndOfFileException {
        if (nextToken == null && !finished) {
            nextToken= doFetchToken();
            if (nextToken == null) {
                finished = true;
            }
        }

        beforeSecondFetchToken();

        if (finished) {
        	if (contentAssistMode) {
				if (lastToken != null)
					lastToken.setNext(nextToken);
        		lastToken = nextToken;
        		nextToken = eocToken;
       			return lastToken;
        	}
        	
            if (isCancelled == true)
                throw new ParseError(
                        ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);

            if (offsetBoundary == -1)
                throwEOF();
            throwOLRE();
        }

        if (lastToken != null)
            lastToken.setNext(nextToken);
        IToken oldToken = lastToken;
        lastToken = nextToken;

        nextToken = doFetchToken();

        if (nextToken == null) {
        	finished = true;
        } else if (nextToken.getType() == IToken.tCOMPLETION) {
        	finished = true;
        } else if (nextToken.getType() == IToken.tPOUNDPOUND) {
            // time for a pasting
            IToken token2 = doFetchToken();
            if (token2 == null) {
                nextToken = null;
                finished = true;
            } else {
                char[] pb = CharArrayUtils.concat(lastToken.getCharImage(),
                        token2.getCharImage());
                pushContext(pb);
                lastToken = oldToken;
                nextToken = null;
                return nextToken();
            }
        } else if (lastToken != null
                && (lastToken.getType() == IToken.tSTRING || lastToken
                        .getType() == IToken.tLSTRING)) {
            while (nextToken != null
                    && (nextToken.getType() == IToken.tSTRING || nextToken
                            .getType() == IToken.tLSTRING)) {
                // Concatenate the adjacent strings
                int tokenType = IToken.tSTRING;
                if (lastToken.getType() == IToken.tLSTRING
                        || nextToken.getType() == IToken.tLSTRING)
                    tokenType = IToken.tLSTRING;
                lastToken = newToken(tokenType, CharArrayUtils.concat(lastToken
                        .getCharImage(), nextToken.getCharImage()));
                if (oldToken != null)
                    oldToken.setNext(lastToken);
                nextToken = doFetchToken();
            }
        }

        return lastToken;
    }

	private IToken doFetchToken() throws EndOfFileException {
		IToken result= null;
		try {
		    result = fetchToken();
		} catch (OffsetLimitReachedException olre) {
			if (contentAssistMode) {
				IASTCompletionNode node= olre.getCompletionNode();
				if (node != null) {
					result= newToken(IToken.tCOMPLETION, node.getCompletionPrefix().toCharArray());
				} else {
					result= newToken(IToken.tCOMPLETION);
				}
			} else {
				throw olre;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			if (isCancelled) {
		        throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
			}
			errorHandle();
			throw e;
		} catch (RuntimeException e) {
			errorHandle();
			throw e;
		} catch (Error e) {
			errorHandle();
			throw e;
		}
		return result;
	}

    /**
     * @throws EndOfFileException
     */
    protected void throwEOF() throws EndOfFileException {
        throw EOF;
    }

    /**
     *  
     */
    protected void beforeSecondFetchToken() {
    }

    /**
     *  
     */
    protected void errorHandle() {
        if( bufferStackPos > 0 )
            ++bufferPos[bufferStackPos];
    }

    /**
     *  
     */
    protected void throwOLRE() throws OffsetLimitReachedException {
        if (lastToken != null && lastToken.getEndOffset() != offsetBoundary)
            throw new OffsetLimitReachedException((IToken) null);
        throw new OffsetLimitReachedException(lastToken);
    }

    // Return null to signify end of file
    protected IToken fetchToken() throws EndOfFileException {
        ++count;
        while (bufferStackPos >= 0) {
            if (isCancelled == true)
                throw new ParseError(
                        ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);

            //return the stored comments
			if(commentsFromInactiveCode.length > 0 && commentsFromInactiveCode[0] != null){
				IToken commentToken = commentsFromInactiveCode[0];
            	ArrayUtil.remove(commentsFromInactiveCode, commentToken);
            	return commentToken;
            }
            
            // Find the first thing we would care about
            skipOverWhiteSpaceFetchToken();

            if (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
                // We're at the end of a context, pop it off and try again
                popContext();
                continue;
            }

            // Tokens don't span buffers, stick to our current one
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];
            int pos = bufferPos[bufferStackPos];

            switch (buffer[pos]) {
            case '\r':
            case '\n':
                continue;

            case 'L':
                if (pos + 1 < limit && buffer[pos + 1] == '"')
                    return scanString();
                if (pos + 1 < limit && buffer[pos + 1] == '\'')
                    return scanCharLiteral();

                IToken t = scanIdentifier();
                if (t instanceof MacroExpansionToken)
                    continue;
                return t;

            case '"':
                return scanString();

            case '\'':
                return scanCharLiteral();

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
                t = scanIdentifier();
                if (t instanceof MacroExpansionToken)
                    continue;
                return t;

            case '\\':
                if (pos + 1 < limit
                        && (buffer[pos + 1] == 'u' || buffer[pos + 1] == 'U')) {
                    t = scanIdentifier();
                    if (t instanceof MacroExpansionToken)
                        continue;
                    return t;
                }
                handleProblem(IProblem.SCANNER_BAD_CHARACTER,
                        bufferPos[bufferStackPos], new char[] { '\\' });
                continue;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return scanNumber();

            case '.':
                if (pos + 1 < limit) {
                    switch (buffer[pos + 1]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return scanNumber();

                    case '.':
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '.') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tELLIPSIS);
                            }
                        }
                    case '*':
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tDOTSTAR);
                    }
                }
                return newToken(IToken.tDOT);

            case '#':
                if (pos + 1 < limit && buffer[pos + 1] == '#') {
                    ++bufferPos[bufferStackPos];
                    return newToken(IToken.tPOUNDPOUND);
                }

                // Should really check to make sure this is the first
                // non whitespace character on the line
                handlePPDirective(pos);
                continue;

            case '{':
                return newToken(IToken.tLBRACE);

            case '}':
                return newToken(IToken.tRBRACE);

            case '[':
                return newToken(IToken.tLBRACKET);

            case ']':
                return newToken(IToken.tRBRACKET);

            case '(':
                return newToken(IToken.tLPAREN);

            case ')':
                return newToken(IToken.tRPAREN);

            case ';':
                return newToken(IToken.tSEMI);

            case ':':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == ':'
                            && getLanguage() == ParserLanguage.CPP) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tCOLONCOLON);
                    }
                }
                return newToken(IToken.tCOLON);

            case '?':
                return newToken(IToken.tQUESTION);

            case '+':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '+') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tINCR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tPLUSASSIGN);
                    }
                }
                return newToken(IToken.tPLUS);

            case '-':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '>') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '*') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tARROWSTAR);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tARROW);
                    } else if (buffer[pos + 1] == '-') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tDECR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tMINUSASSIGN);
                    }
                }
                return newToken(IToken.tMINUS);

            case '*':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSTARASSIGN);
                    }
                }
                return newToken(IToken.tSTAR);

            case '/':
                if (pos + 1 < limit) {
					if (buffer[pos + 1] == '=') {
						++bufferPos[bufferStackPos];
						return newToken(IToken.tDIVASSIGN);
					} else if (buffer[pos + 1] == '/' || buffer[pos + 1] == '*') {
						return scanComment();
					}
				}
				return newToken(IToken.tDIV);

            case '%':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tMODASSIGN);
                    }
                }
                return newToken(IToken.tMOD);

            case '^':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tXORASSIGN);
                    }
                }
                return newToken(IToken.tXOR);

            case '&':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '&') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tAND);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tAMPERASSIGN);
                    }
                }
                return newToken(IToken.tAMPER);

            case '|':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '|') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tOR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tBITORASSIGN);
                    }
                }
                return newToken(IToken.tBITOR);

            case '~':
                return newToken(IToken.tCOMPL);

            case '!':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tNOTEQUAL);
                    }
                }
                return newToken(IToken.tNOT);

            case '=':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tEQUAL);
                    }
                }
                return newToken(IToken.tASSIGN);

            case '<':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tLTEQUAL);
                    } else if (buffer[pos + 1] == '<') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '=') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tSHIFTLASSIGN);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSHIFTL);
                    } else if (buffer[pos + 1] == '?' && supportMinAndMax) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IGCCToken.tMIN, CharArrayUtils.extract(
                                buffer, pos, 2));
                    }
                }
                return newToken(IToken.tLT);

            case '>':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tGTEQUAL);
                    } else if (buffer[pos + 1] == '>') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '=') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tSHIFTRASSIGN);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSHIFTR);
                    } else if (buffer[pos + 1] == '?' && supportMinAndMax) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IGCCToken.tMAX, CharArrayUtils.extract(
                                buffer, pos, 2));
                    }

                }
                return newToken(IToken.tGT);

            case ',':
                return newToken(IToken.tCOMMA);

            default:
                if (Character.isLetter(buffer[pos]) || buffer[pos] == '_'
                        || (support$Initializers && buffer[pos] == '$')) {
                    t = scanIdentifier();
                    if (t instanceof MacroExpansionToken)
                        continue;
                    return t;
                }

            // skip over anything we don't handle
                char [] x = new char [1];
                x[0] = buffer[pos];
                handleProblem( IASTProblem.SCANNER_BAD_CHARACTER, pos, x );
            }
        }

        // We've run out of contexts, our work is done here
        if (contentAssistMode) { 
        	return new SimpleToken(IToken.tCOMPLETION, Integer.MAX_VALUE, null, Integer.MAX_VALUE);
        }
        return null;
    }
    
    protected CharTable ident = new CharTable(1024);
    protected int idents = 0;
    
    protected IToken scanIdentifier() {
        char[] buffer = bufferStack[bufferStackPos];
        boolean escapedNewline = false;
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int len = 1;

        while (++bufferPos[bufferStackPos] < limit) {
            char c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                ++len;
                continue;
            } else if (c == '\\' && (bufferPos[bufferStackPos] + 1 < limit)) {
            	if (buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    // escaped newline
                    ++bufferPos[bufferStackPos];
                    len += 2;
                    escapedNewline = true;
                    continue;
            	}
            	if (buffer[bufferPos[bufferStackPos] + 1] == '\r') {
                    // escaped newline
            		if(buffer[bufferPos[bufferStackPos] + 2] == '\n') {
	                    ++bufferPos[bufferStackPos];
	                    ++bufferPos[bufferStackPos];
	                    len += 3;
	                    escapedNewline = true;
	                    continue;
            		}
            	}
                if ((buffer[bufferPos[bufferStackPos] + 1] == 'u')
                        || buffer[bufferPos[bufferStackPos] + 1] == 'U') {
                    ++bufferPos[bufferStackPos];
                    len += 2;
                    continue;
                }
            } else if ((support$Initializers && c == '$')) {
                ++len;
                continue;
            }
            break;
        }

        --bufferPos[bufferStackPos];
        
        if(escapedNewline) {
        	buffer = removedEscapedNewline(buffer, start, len);
        	start = 0;
        	len = buffer.length;
        }

        if (contentAssistMode && bufferStackPos == 0 && bufferPos[bufferStackPos] + 1 == limit) {
        	// return the text as a content assist token
        	if(escapedNewline)
        		return newToken(IToken.tCOMPLETION, buffer);
        	return newToken(IToken.tCOMPLETION, CharArrayUtils.extract(buffer, start, bufferPos[bufferStackPos] - start + 1));
        }

        // Check for macro expansion
        Object expObject = definitions.get(buffer, start, len);

        if (expObject != null && !isLimitReached()
                && shouldExpandMacro((IMacro) expObject)) {
            boolean expanding = true;
            if (expObject instanceof FunctionStyleMacro) {
                if (handleFunctionStyleMacro((FunctionStyleMacro) expObject,
                        true) == null)
                    expanding = false;
            } else if (expObject instanceof ObjectStyleMacro) {
                ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
                char[] expText = expMacro.getExpansion();
                if (expText.length > 0) {
                	final int endOffset= bufferPos[bufferStackPos]+1;
                	final int startOffset= endOffset - expMacro.name.length;
                    pushContext(expText, new MacroData(startOffset, endOffset, expMacro));
                }
            } else if (expObject instanceof DynamicStyleMacro) {
                DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
                char[] expText = expMacro.execute();
                if (expText.length > 0) {
                	final int endOffset= bufferPos[bufferStackPos]+1;
                	final int startOffset= endOffset - expMacro.name.length;
                    pushContext(expText, new MacroData(startOffset, endOffset, expMacro));
                }

            } else if (expObject instanceof char[]) {
                char[] expText = (char[]) expObject;
                if (expText.length > 0)
                    pushContext(expText);
            }
            if (expanding)
                return EXPANSION_TOKEN;
        }

        int tokenType = keywords.get(buffer, start, len);
        if (tokenType != keywords.undefined)  
        	return newToken(tokenType);
        
        int keyLoc = additionalKeywords.getKeyLocation(buffer, start, len);
        if (keyLoc != additionalKeywords.undefined)
        	return newToken(additionalKeywords.get(keyLoc), additionalKeywords.keyAt(keyLoc));  
        
        // we have a identifier
    	if (cacheIdentifiers) 
    		return newToken(IToken.tIDENTIFIER, ident.keyAt(ident.addIndex(buffer, start, len)));
    	else
    		return newToken(IToken.tIDENTIFIER, escapedNewline ? buffer : CharArrayUtils.extract(buffer, start, len)); 
    }

    /**
     * @param buffer
     * @param start
     * @param len
     * @param expObject
     * @return
     */
    protected boolean shouldExpandMacro(IMacro macro) {
        return shouldExpandMacro(macro, bufferStackPos, bufferData, offsetBoundary, bufferPos, bufferStack);
    }
    
    protected static boolean shouldExpandMacro(IMacro macro, int bufferStackPos, Object [] bufferData, int offsetBoundary, int [] bufferPos, char [][]bufferStack )
    {
        // but not if it has been expanded on the stack already
        // i.e. recursion avoidance
        if (macro != null && !isLimitReached(offsetBoundary, bufferStackPos, bufferPos, bufferStack ))
            for (int stackPos = bufferStackPos; stackPos >= 0; --stackPos)
                if (bufferData[stackPos] != null
                        && bufferData[stackPos] instanceof MacroData
                        && CharArrayUtils.equals(macro.getName(),
                                ((MacroData) bufferData[stackPos]).macro
                                        .getName())) {
                    return false;
                }
        return true;        
    }

    /**
     * @return
     */
    protected final boolean isLimitReached() {
        return isLimitReached(offsetBoundary, bufferStackPos, bufferPos, bufferStack);
    }
    
    /**
     * @param offsetBoundary
     * @param bufferStackPos
     * @param bufferPos
     * @param bufferStack
     * @return
     */
    protected final static boolean isLimitReached(int offsetBoundary, int bufferStackPos, int [] bufferPos, char [][]bufferStack ) {
        if (offsetBoundary == -1 || bufferStackPos != 0)
            return false;
        if (bufferPos[bufferStackPos] == offsetBoundary - 1)
            return true;
        if (bufferPos[bufferStackPos] == offsetBoundary) {
            int c = bufferStack[bufferStackPos][bufferPos[bufferStackPos]];
            if (c == '\n' || c == ' ' || c == '\t' || c == '\r')
                return true;
        }
        return false;
    }


    protected IToken scanString() {
        char[] buffer = bufferStack[bufferStackPos];

        int tokenType = IToken.tSTRING;
        if (buffer[bufferPos[bufferStackPos]] == 'L') {
            ++bufferPos[bufferStackPos];
            tokenType = IToken.tLSTRING;
        }

        int stringStart = bufferPos[bufferStackPos] + 1;
        int stringLen = 0;
        boolean escaped = false;
        boolean foundClosingQuote = false;
        while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
            ++stringLen;
            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '"') {
                if (!escaped) {
                    foundClosingQuote = true;
                    break;
                }
            } else if (c == '\\') {
                escaped = !escaped;
                continue;
            } else if (c == '\n') {
                //unescaped end of line before end of string
                if (!escaped)
                    break;
            } else if (c == '\r') {
                if (bufferPos[bufferStackPos] + 1 < bufferLimit[bufferStackPos]
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    ++bufferPos[bufferStackPos];
                    if (!escaped)
                        break;
                }
            }
            escaped = false;
        }
        --stringLen;

        // We should really throw an exception if we didn't get the terminating
        // quote before the end of buffer
        char[] result = CharArrayUtils.extract(buffer, stringStart, stringLen);
        if (!foundClosingQuote) {
            handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, stringStart,
                    result);
        }
        return newToken(tokenType, result);
    }

    protected IToken scanCharLiteral() {
        char[] buffer = bufferStack[bufferStackPos];
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int tokenType = IToken.tCHAR;
        int length = 1;
        if (buffer[bufferPos[bufferStackPos]] == 'L') {
            ++bufferPos[bufferStackPos];
            tokenType = IToken.tLCHAR;
            ++length;
        }

        if (start >= limit) {
            return newToken(tokenType, EMPTY_CHAR_ARRAY);
        }

        boolean escaped = false;
        while (++bufferPos[bufferStackPos] < limit) {
            ++length;
            int pos = bufferPos[bufferStackPos];
            if (buffer[pos] == '\'') {
                if (!escaped)
                    break;
            } else if (buffer[pos] == '\\') {
                escaped = !escaped;
                continue;
            }
            escaped = false;
        }

        if (bufferPos[bufferStackPos] == limit) {
            handleProblem(IProblem.SCANNER_BAD_CHARACTER, start, CharArrayUtils
                    .extract(buffer, start, length));
            return newToken(tokenType, EMPTY_CHAR_ARRAY);
        }

        char[] image = length > 0 ? CharArrayUtils.extract(buffer, start,
                length) : EMPTY_CHAR_ARRAY;

        return newToken(tokenType, image);
    }

    /**
     * @param scanner_bad_character
     */
    protected abstract void handleProblem(int id, int offset, char[] arg);

    /**
     * @param i
     * @return
     */
    protected int getLineNumber(int offset) {
        if (parserMode == ParserMode.COMPLETION_PARSE)
            return -1;
        int index = getCurrentFileIndex();
        if (offset >= bufferLimit[index])
            return -1;

        int lineNum = lineNumbers[index];
        int startingPoint = lineOffsets[index];

        for (int i = startingPoint; i < offset; ++i) {
            if (bufferStack[index][i] == '\n')
                ++lineNum;
        }
        if (startingPoint < offset) {
            lineNumbers[index] = lineNum;
            lineOffsets[index] = offset;
        }
        return lineNum;
    }

    protected IToken scanNumber() throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        boolean isFloat = buffer[start] == '.';
        boolean hasExponent = false;

        boolean isHex = false;
        boolean isOctal = false;
        boolean isMalformedOctal = false;

        if (buffer[start] == '0' && start + 1 < limit) {
            switch (buffer[start + 1]) {
            case 'x':
            case 'X':
                isHex = true;
                ++bufferPos[bufferStackPos];
                break;
            default:
                if (buffer[start + 1] > '0' && buffer[start + 1] < '7')
                    isOctal = true;
                else if (buffer[start + 1] == '8' || buffer[start + 1] == '9') {
                    isOctal = true;
                    isMalformedOctal = true;
                }
            }
        }

        while (++bufferPos[bufferStackPos] < limit) {
            int pos = bufferPos[bufferStackPos];
            switch (buffer[pos]) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if ((buffer[pos] == '8' || buffer[pos] == '9') && isOctal) {
                    isMalformedOctal = true;
                    break;
                }

                continue;

            case '.':
                if (isLimitReached())
                    handleNoSuchCompletion();

                if (isFloat) {
                    // second dot
                    handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, start,
                            null);
                    break;
                }

                isFloat = true;
                continue;

            case 'E':
            case 'e':
                if (isHex)
                    // a hex 'e'
                    continue;

                if (hasExponent)
                    // second e
                    break;

                if (pos + 1 >= limit)
                    // ending on the e?
                    break;

                switch (buffer[pos + 1]) {
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // looks like a good exponent
                    isFloat = true;
                    hasExponent = true;
                    ++bufferPos[bufferStackPos];
                    continue;
                default:
                    // no exponent number?
                    break;
                }
                break;

            case 'a':
            case 'A':
            case 'b':
            case 'B':
            case 'c':
            case 'C':
            case 'd':
            case 'D':
                if (isHex)
                    continue;

                // not ours
                break;

            case 'f':
            case 'F':
                if (isHex)
                    continue;

                // must be float suffix
                ++bufferPos[bufferStackPos];

                if (bufferPos[bufferStackPos] < buffer.length
                        && buffer[bufferPos[bufferStackPos]] == 'i')
                    continue; // handle GCC extension 5.10 Complex Numbers

                break; // fix for 77281 (used to be continue)

            case 'p':
            case 'P':
                // Hex float exponent prefix
                if (!isFloat || !isHex) {
                    --bufferPos[bufferStackPos];
                    break;
                }

                if (hasExponent)
                    // second p
                    break;

                if (pos + 1 >= limit)
                    // ending on the p?
                    break;

                switch (buffer[pos + 1]) {
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // looks like a good exponent
                    isFloat = true;
                    hasExponent = true;
                    ++bufferPos[bufferStackPos];
                    continue;
                default:
                    // no exponent number?
                    break;
                }
                break;

            case 'u':
            case 'U':
            case 'L':
            case 'l':
                // unsigned suffix
                suffixLoop: while (++bufferPos[bufferStackPos] < limit) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case 'U':
                    case 'u':
                    case 'l':
                    case 'L':
                        break;
                    default:

                        break suffixLoop;
                    }
                }
                break;

            default:
                boolean success = false;
                for (int iter = 0; iter < suffixes.length; iter++)
                    if (buffer[pos] == suffixes[iter]) {
                        success = true;
                        break;
                    }
                if (success)
                    continue;
            }

            // If we didn't continue in the switch, we're done
            break;
        }

        --bufferPos[bufferStackPos];

        char[] result = CharArrayUtils.extract(buffer, start,
                bufferPos[bufferStackPos] - start + 1);
        int tokenType = isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER;

        if (tokenType == IToken.tINTEGER && isHex && result.length == 2) {
            handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT, start, result);
        } else if (tokenType == IToken.tINTEGER && isOctal && isMalformedOctal) {
            handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, start, result);
        }

        return newToken(tokenType, result);
    }

    protected boolean branchState(int state) {
        if (state != BRANCH_IF && branchStackPos == -1)
            return false;

        switch (state) {
        case BRANCH_IF:
            if (++branchStackPos == branches.length) {
                int[] temp = new int[branches.length << 1];
                System.arraycopy(branches, 0, temp, 0, branches.length);
                branches = temp;
            }
            branches[branchStackPos] = BRANCH_IF;
            return true;
        case BRANCH_ELIF:
        case BRANCH_ELSE:
            switch (branches[branchStackPos]) {
            case BRANCH_IF:
            case BRANCH_ELIF:
                branches[branchStackPos] = state;
                return true;
            default:
                return false;
            }
        case BRANCH_END:
            switch (branches[branchStackPos]) {
            case BRANCH_IF:
            case BRANCH_ELSE:
            case BRANCH_ELIF:
                --branchStackPos;
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    protected void handlePPDirective(int pos) throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int startingLineNumber = getLineNumber(pos);
        skipOverWhiteSpace();
        if (isLimitReached())
            handleCompletionOnPreprocessorDirective("#"); //$NON-NLS-1$

        // find the directive
        int start = ++bufferPos[bufferStackPos];

        // if new line or end of buffer, we're done
        if (start >= limit || buffer[start] == '\n')
            return;

        char c = buffer[start];
        if ((c >= 'a' && c <= 'z')) {
            while (++bufferPos[bufferStackPos] < limit) {
                c = buffer[bufferPos[bufferStackPos]];
                if ((c >= 'a' && c <= 'z') || c == '_')
                    continue;
                break;
            }
            --bufferPos[bufferStackPos];
            int len = bufferPos[bufferStackPos] - start + 1;
            if (isLimitReached())
                handleCompletionOnPreprocessorDirective(new String(buffer, pos,
                        len + 1));
            
            int end;
            int type = ppKeywords.get(buffer, start, len);
            if (type == ppKeywords.undefined && additionalPPKeywords != null) {
            	type= additionalPPKeywords.get(buffer, start, len);
            }
            if (type != IPreprocessorDirective.ppInvalid) {
                switch (type) {
                case IPreprocessorDirective.ppInclude:
                    handlePPInclude(pos, false, startingLineNumber, true);
                    return;
                case IPreprocessorDirective.ppInclude_next:
                    handlePPInclude(pos, true, startingLineNumber, true);
                    return;
                case IPreprocessorDirective.ppImport:
                    handlePPInclude(pos, false, startingLineNumber, true);
                    return;
                case IPreprocessorDirective.ppDefine:
                    handlePPDefine(pos, startingLineNumber);
                    return;
                case IPreprocessorDirective.ppUndef:
                    handlePPUndef(pos);
                    return;
                case IPreprocessorDirective.ppIfdef:
                    handlePPIfdef(pos, true);
                    return;
                case IPreprocessorDirective.ppIfndef:
                    handlePPIfdef(pos, false);
                    return;
                case IPreprocessorDirective.ppIf: 
                    start = bufferPos[bufferStackPos]+1;
                    skipToNewLine();
                    end= bufferPos[bufferStackPos]+1;
                    len = end - start;
                    if (isLimitReached())
                        handleCompletionOnExpression(CharArrayUtils.extract(
                                buffer, start, len));
                    branchState(BRANCH_IF);
                    
                    if (expressionEvaluator.evaluate(buffer, start, len,
                            definitions,
                            getLineNumber(start),
                            getCurrentFilename()) == 0) {
                    	processIf(pos, end, false);
                        skipOverConditionalCode(true);
                        if (isLimitReached())
                            handleInvalidCompletion();
                    } else {
                    	processIf(pos, end, true);
                    }
                    return;
                case IPreprocessorDirective.ppElse:
                case IPreprocessorDirective.ppElif:
                    // Condition must have been true, skip over the rest

                    if (branchState(type == IPreprocessorDirective.ppElse ? BRANCH_ELSE : BRANCH_ELIF)) {
                    	skipToNewLine();
                        if (type == IPreprocessorDirective.ppElse)
                            processElse(pos, bufferPos[bufferStackPos] + 1,
                                    false);
                        else
                            processElsif(pos, bufferPos[bufferStackPos] + 1, false);
                        skipOverConditionalCode(false);
                    } else {
                        handleProblem(
                                IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                start, ppKeywords.findKey(buffer, start, len));
                        skipToNewLine();
                    }

                    if (isLimitReached())
                        handleInvalidCompletion();
                    return;
                case IPreprocessorDirective.ppError:
                case IPreprocessorDirective.ppWarning:
                    skipOverWhiteSpace();
                    start = bufferPos[bufferStackPos] + 1;
                    skipToNewLine();
                    end= bufferPos[bufferStackPos] + 1;
                    boolean isWarning= type == IPreprocessorDirective.ppWarning;
                    handleProblem(isWarning ? IProblem.PREPROCESSOR_POUND_WARNING : IProblem.PREPROCESSOR_POUND_ERROR, start,
                            CharArrayUtils.extract(buffer, start, end-start));
                    if (isWarning) {
                    	processWarning(pos, end);
                    } else {
                    	processError(pos, end);
                    }
                    return;
                case IPreprocessorDirective.ppEndif:
                    skipToNewLine();
                    if (branchState(BRANCH_END)) {
                        processEndif(pos, bufferPos[bufferStackPos] + 1);
                    }
                    else {
                        handleProblem(
                                IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                start, ppKeywords.findKey(buffer, start, len));
                    }
                    return;
                case IPreprocessorDirective.ppPragma:
                    skipToNewLine();
                    processPragma(pos, bufferPos[bufferStackPos]+1);
                    return;
                case IPreprocessorDirective.ppIgnore:
                    skipToNewLine();
                    return;
                }
            }
        } else {
        	// ignore preprocessor output lines of the form
        	// # <linenum> "<filename>" flags
            if (c >= '0' && c <= '9' && start > pos+1) {
                while (++bufferPos[bufferStackPos] < limit) {
                    c = buffer[bufferPos[bufferStackPos]];
                    if ((c >= '0' && c <= '9'))
                        continue;
                    break;
                }
                if (bufferPos[bufferStackPos] < limit) {
	                c = buffer[bufferPos[bufferStackPos]];
	                if (c == ' ' || c == '\t') {
	                	// now we have # <linenum> 
	                	// skip the rest
	                    skipToNewLine();
	                    return;
	                }
                }
            	--bufferPos[bufferStackPos];
            }
        }
        // directive was not handled, create a problem
        handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, start, 
        		new String(buffer, start, getCurrentOffset() - start + 1).toCharArray());
        skipToNewLine();
    }

    /**
     * @param startPos
     * @param endPos
     */
    protected abstract void processPragma(int startPos, int endPos);

    /**
     * @param pos
     * @param i
     */
    protected abstract void processEndif(int pos, int i);

    /**
     * @param startPos
     * @param endPos
     */
    protected abstract void processError(int startPos, int endPos);

    /**
     * Process #warning directive.
     * 
     * @param startPos
     * @param endPos
     */
    protected void processWarning(int startPos, int endPos) {
    	// default: do nothing
    }

    protected abstract void processElsif(int startPos, int endPos, boolean taken);

    protected abstract void processElse(int startPos, int endPos, boolean taken);

    /**
     * @param pos
     * @param i
     * @param b
     */
    protected abstract void processIf(int startPos, int endPos, boolean taken);

    protected void handlePPInclude(int pos2, boolean include_next, int startingLineNumber, boolean active) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        skipOverWhiteSpace();
        int startOffset = pos2;
        int pos = ++bufferPos[bufferStackPos];
        if (pos >= limit)
            return;

        boolean local = false;
        String filename = null;

        int endOffset = startOffset;
        int nameOffset = 0;
        int nameEndOffset = 0;

        int nameLine = 0, endLine = 0;
        char c = buffer[pos];
        int start;
        int length;

        switch (c) {
        case '\n':
            return;
        case '"':
            nameLine = getLineNumber(bufferPos[bufferStackPos]);
            local = true;
            start = bufferPos[bufferStackPos] + 1;
            length = 0;
            for (length=0; ++bufferPos[bufferStackPos] < limit; length++) {
                c = buffer[bufferPos[bufferStackPos]];
                if (c == '"') {
                	filename = new String(buffer, start, length);
                	break;
                } 
                else if (c == '\n' || c == '\r') {
                	break;
                }
            }

            nameOffset = start;
            nameEndOffset = start + length;
            endOffset = start + length + 1;
            break;
        case '<':
            nameLine = getLineNumber(bufferPos[bufferStackPos]);
            local = false;
            start = bufferPos[bufferStackPos] + 1;

            for (length=0; ++bufferPos[bufferStackPos] < limit; length++) {
                c = buffer[bufferPos[bufferStackPos]];
            	if (c == '>') {
            		filename= new String(buffer, start, length);
            		break;
            	}
            	else if (c == '\n' || c == '\r') {
            		break;
            	}
            }
            endOffset = start + length + 1;
            nameOffset = start;
            nameEndOffset = start + length;
            break;
        default:
            // handle macro expansions
            while (++bufferPos[bufferStackPos] < limit) {
                c = buffer[bufferPos[bufferStackPos]];
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                        || c == '_' || (c >= '0' && c <= '9')
                        || Character.isUnicodeIdentifierPart(c)) {
                    continue;
                } else if (c == '\\'
                        && bufferPos[bufferStackPos] + 1 < buffer.length
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    // escaped newline
                    ++bufferPos[bufferStackPos];
                    continue;
                }
                break;
            }
        	nameOffset= pos;
        	int len= bufferPos[bufferStackPos] - nameOffset;
        	nameEndOffset= nameOffset + len;
        	endOffset= nameEndOffset;
        	bufferPos[bufferStackPos]--;
        	
            Object expObject = definitions.get(buffer, nameOffset, len);

            if (expObject != null) {
                char[] t = null;
                if (expObject instanceof FunctionStyleMacro) {
                    t = handleFunctionStyleMacro(
                            (FunctionStyleMacro) expObject, false);
                } else if (expObject instanceof ObjectStyleMacro) {
                    t = ((ObjectStyleMacro) expObject).getExpansion();
                }
                if (t != null) {
                    t = replaceArgumentMacros(t);
                    if (t.length >= 2) {
                    	if (t[0] == '"' && t[t.length-1] == '"') {
                    		local = true;
                    		filename = new String(t, 1, t.length-2);
                    	} else if (t[0] == '<' && t[t.length-1] == '>') {
                    		local = false;
                    		filename = new String(t, 1, t.length-2);
                    	}
                    }
                }
            }
            break;
        }

        if (filename == null || filename == EMPTY_STRING) {
        	if (active) {
	            handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, startOffset,
	                    new String(buffer, startOffset, nameEndOffset - startOffset).toCharArray());
	            return;
        	}
        	filename= new String(buffer, nameOffset, nameEndOffset - nameOffset);
        }
        char[] fileNameArray = filename.toCharArray();

        // TODO else we need to do macro processing on the rest of the line
        endLine = getLineNumber(bufferPos[bufferStackPos]);
        skipToNewLine();

        if (parserMode == ParserMode.QUICK_PARSE && active) {
        	final Object inc= createInclusionConstruct(
        			fileNameArray, EMPTY_CHAR_ARRAY, local, startOffset, startingLineNumber, 
        			nameOffset, nameEndOffset, nameLine, endOffset, endLine, false);
        	quickParsePushPopInclusion(inc);
        }
        else {
        	CodeReader reader= null;
        	if (active) {
        		final File currentDir= local || include_next ? new File(String.valueOf(getCurrentFilename())).getParentFile() : null;
        		reader= findInclusion(filename, local, include_next, currentDir);
        		if (reader != null) {
        			final Object inc = createInclusionConstruct(
        					fileNameArray, reader.filename, local, startOffset,	startingLineNumber, 
        					nameOffset, nameEndOffset, nameLine, endOffset, endLine, false);
        			pushContext(reader.buffer, new InclusionData(reader, inc, false));
        		}
        	}
        	if (reader == null) {
        		processInclude(fileNameArray, local, include_next, active, startOffset, nameOffset, nameEndOffset, endOffset, startingLineNumber, nameLine, endLine);
        		if (active) {
        			handleProblem(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, startOffset, fileNameArray);
        		}
        	}
        }
    }

    /**
     * Process an include directive without following the inclusion.
	 */
	protected void processInclude(char[] fileName, boolean local, boolean include_next, boolean active, int startOffset, int nameOffset,
			int nameEndOffset, int endOffset, int startingLineNumber, int nameLine, int endLine) {
		// default: do nothing
	}

    private CodeReader findInclusion(final String filename, final boolean quoteInclude, 
    		final boolean includeNext, final File currentDir) {
    	return (CodeReader) findInclusion(filename, quoteInclude, includeNext, currentDir, createCodeReaderTester);
    }

    protected Object findInclusion(final String filename, final boolean quoteInclude, 
    		final boolean includeNext, final File currentDirectory, final IIncludeFileTester tester) {
        Object reader = null;
		// filename is an absolute path or it is a Linux absolute path on a windows machine
		if (new File(filename).isAbsolute() || filename.startsWith("/")) { //$NON-NLS-1$
		    return tester.checkFile( EMPTY_STRING, filename );
		}
                
        if (currentDirectory != null && quoteInclude && !includeNext) {
            // Check to see if we find a match in the current directory
        	String absolutePath = currentDirectory.getAbsolutePath();
        	reader = tester.checkFile(absolutePath, filename);
        	if (reader != null) {
        		return reader;
        	}
        }
        
        // if we're not include_next, then we are looking for the first occurrence of 
        // the file, otherwise, we ignore all the paths before the current directory
        String[] includePathsToUse = quoteInclude ? quoteIncludePaths : includePaths;
        if (includePathsToUse != null ) {
            int startpos = 0;
            if (includeNext && currentDirectory != null) {
                startpos = findIncludePos(includePathsToUse, currentDirectory) + 1;
            }
            for (int i = startpos; i < includePathsToUse.length; ++i) {
                reader = tester.checkFile(includePathsToUse[i], filename);
                if (reader != null) {
                	return reader;
                }
            }
        }
        return null;
    }

	protected abstract CodeReader createReader(String path, String fileName);
    

    private int findIncludePos(String[] paths, File currentDirectory) {
        for (int i = 0; i < paths.length; ++i)
            try {
                String path = new File(paths[i]).getCanonicalPath();
                String parent = currentDirectory.getCanonicalPath();
                if (path.equals(parent))
                    return i;
            } catch (IOException e) {
            }

        return -1;
    }

    /**
     * @param finalPath
     * @return
     */
    protected abstract CodeReader createReaderDuple(String finalPath);

    /**
     * @param inclusion
     */
    protected abstract void quickParsePushPopInclusion(Object inclusion);

    /**
     * @param fileName
     * @param local
     * @param startOffset
     * @param startingLineNumber
     * @param nameOffset
     * @param nameEndOffset
     * @param nameLine
     * @param endOffset
     * @param endLine
     * @param isForced
     * @param reader
     * @return
     */
    protected abstract Object createInclusionConstruct(char[] fileName,
            char[] filenamePath, boolean local, int startOffset,
            int startingLineNumber, int nameOffset, int nameEndOffset,
            int nameLine, int endOffset, int endLine, boolean isForced);

    static int countIt = 0;
    protected void handlePPDefine(int pos2, int startingLineNumber) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int startingOffset = pos2;
        int endingLine = 0, nameLine = 0;
        skipOverWhiteSpace();

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, idstart,
                    null);
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                ++idlen;
                continue;
            }
            break;
        }
        --bufferPos[bufferStackPos];
        nameLine = getLineNumber(bufferPos[bufferStackPos]);
        char[] name = new char[idlen];
        System.arraycopy(buffer, idstart, name, 0, idlen);

        // Now check for function style macro to store the arguments
        char[][] arglist = null;
        int pos = bufferPos[bufferStackPos];
        if (pos + 1 < limit && buffer[pos + 1] == '(') {
            ++bufferPos[bufferStackPos];
            arglist = extractMacroParameters(idstart, name, true);
            if (arglist == null)
                return;
        }

        // Capture the replacement text
        
        // Set the offsets to the current position in case there 
        // is no replacement sequence (bug #184804)
        int textend = bufferPos[bufferStackPos];
        int textstart = textend + 1;
        
        int varArgDefinitionInd = -1;
        skipOverWhiteSpace();
        
        // if there is a replacement sequence then adjust the offsets accordingly
        if(bufferPos[bufferStackPos] + 1 < limit
           && buffer[bufferPos[bufferStackPos] + 1] != '\n') {
        	textend = bufferPos[bufferStackPos];
            textstart = textend + 1;
        }

        boolean encounteredComment = false;
        boolean usesVarArgInDefinition = false;
        while (bufferPos[bufferStackPos] + 1 < limit
                && buffer[bufferPos[bufferStackPos] + 1] != '\n') {

            if (CharArrayUtils.equals(buffer, bufferPos[bufferStackPos] + 1,
                    VA_ARGS_CHARARRAY.length, VA_ARGS_CHARARRAY)) {
                usesVarArgInDefinition = true; // __VA_ARGS__ is in definition,
                                               // used
                // to check C99 6.10.3-5
                varArgDefinitionInd = bufferPos[bufferStackPos] + 1;
            }

            //16.3.2-1 Each # preprocessing token in the replacement list for a
            // function-like-macro shall
            //be followed by a parameter as the next preprocessing token
            if (arglist != null && !skipOverNonWhiteSpace(true)) {
                ++bufferPos[bufferStackPos]; //advances us to the #
                if (skipOverWhiteSpace())
                    encounteredComment = true;

                boolean isArg = false;
                if (bufferPos[bufferStackPos] + 1 < limit) {
                    ++bufferPos[bufferStackPos]; //advances us past the # (or
                                                 // last
                    // whitespace)
                    for (int i = 0; i < arglist.length && arglist[i] != null; i++) {
                        if (bufferPos[bufferStackPos] + arglist[i].length - 1 < limit) {
                            if (arglist[i].length > 3
                                    && arglist[i][arglist[i].length - 3] == '.'
                                    && arglist[i][arglist[i].length - 2] == '.'
                                    && arglist[i][arglist[i].length - 3] == '.') {
                                char[] varArgName = new char[arglist[i].length - 3];
                                System.arraycopy(arglist[i], 0, varArgName, 0,
                                        arglist[i].length - 3);
                                if (CharArrayUtils.equals(buffer,
                                        bufferPos[bufferStackPos],
                                        varArgName.length, varArgName)) {
                                    isArg = true;
                                    //advance us to the end of the arg
                                    bufferPos[bufferStackPos] += arglist[i].length - 4;
                                    break;
                                }
                            } else if (CharArrayUtils.equals(buffer,
                                    bufferPos[bufferStackPos],
                                    arglist[i].length, arglist[i])
                                    || (CharArrayUtils.equals(arglist[i],
                                            ELLIPSIS_CHARARRAY) && CharArrayUtils
                                            .equals(buffer,
                                                    bufferPos[bufferStackPos],
                                                    VA_ARGS_CHARARRAY.length,
                                                    VA_ARGS_CHARARRAY))) {
                                isArg = true;
                                //advance us to the end of the arg
                                bufferPos[bufferStackPos] += arglist[i].length - 1;
                                break;
                            }
                        }
                    }
                }
                if (!isArg)
                    handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR,
                            bufferPos[bufferStackPos], null);
            } else {
                skipOverNonWhiteSpace();
            }
            textend = bufferPos[bufferStackPos];
            if(scanComments && (buffer[textend+1]=='/' 
            	&& (buffer[textend+2]=='/'||buffer[textend+2]=='*'))) {

            	if (skipOverWhiteSpaceAndParseComments())
            		encounteredComment = true;
            } else {
            	if (skipOverWhiteSpace())
            		encounteredComment = true;
            }
        }

        int textlen = textend - textstart + 1;
        endingLine = getLineNumber(bufferPos[bufferStackPos]);
        char[] text = EMPTY_CHAR_ARRAY;
        if (textlen > 0) {
            text = new char[textlen];
            System.arraycopy(buffer, textstart, text, 0, textlen);
//            countIt++;
//            System.out.println(countIt);
        }

        if (encounteredComment)
            text = removeCommentFromBuffer(text);
        text = removedEscapedNewline(text, 0, text.length);

        IMacro result = null;
        if (arglist == null)
            result = new ObjectStyleMacro(name, text);
        else
            result = new FunctionStyleMacro(name, text, arglist);

        // Throw it in
        definitions.put(name, result);

        if (usesVarArgInDefinition
                && definitions.get(name) instanceof FunctionStyleMacro
                && !((FunctionStyleMacro) definitions.get(name)).hasVarArgs())
            handleProblem(IProblem.PREPROCESSOR_INVALID_VA_ARGS,
                    varArgDefinitionInd, null);

        int idend = idstart + idlen;
        int textEnd = textstart + textlen;
        processMacro(name, startingOffset, startingLineNumber, idstart, idend,
                nameLine, textEnd, endingLine, result);
    }

    /**
     * @param name
     * @param startingOffset
     * @param startingLineNumber
     * @param idstart
     * @param idend
     * @param nameLine
     * @param textEnd
     * @param endingLine
     * @param macro
     *            TODO
     */
    protected abstract void processMacro(char[] name, int startingOffset,
            int startingLineNumber, int idstart, int idend, int nameLine,
            int textEnd, int endingLine,
            org.eclipse.cdt.core.parser.IMacro macro);

    protected char[][] extractMacroParameters(int idstart, char[] name,
            boolean reportProblems) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        if (bufferPos[bufferStackPos] >= limit
                || buffer[bufferPos[bufferStackPos]] != '(')
            return null;

        char c;
        char[][] arglist = new char[4][];
        int currarg = -1;
        while (bufferPos[bufferStackPos] < limit) {
            skipOverWhiteSpace();
            if (++bufferPos[bufferStackPos] >= limit) {
            	if (reportProblems) {
                    handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN,
                            idstart, name);
            	}
                return null;
            }
            c = buffer[bufferPos[bufferStackPos]];
            int argstart = bufferPos[bufferStackPos];
            if (c == ')') {
                break;
            } else if (c == ',') {
                continue;
            } else if (c == '.' && bufferPos[bufferStackPos] + 1 < limit
                    && buffer[bufferPos[bufferStackPos] + 1] == '.'
                    && bufferPos[bufferStackPos] + 2 < limit
                    && buffer[bufferPos[bufferStackPos] + 2] == '.') {
                bufferPos[bufferStackPos]--; // move back and let
                                             // skipOverIdentifier
                // handle the ellipsis
            } else if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || c == '_' || Character.isUnicodeIdentifierPart(c))) {
                if (reportProblems) {
                    handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN,
                            idstart, name);

                    // yuck
                    skipToNewLine();
                    return null;
                }
            }

            skipOverIdentifier();
            if (++currarg == arglist.length) {
                char[][] oldarglist = arglist;
                arglist = new char[oldarglist.length * 2][];
                System.arraycopy(oldarglist, 0, arglist, 0, oldarglist.length);
            }
            int arglen = bufferPos[bufferStackPos] - argstart + 1;
            char[] arg = new char[arglen];
            System.arraycopy(buffer, argstart, arg, 0, arglen);
            arglist[currarg] = arg;
        }

        return arglist;
    }

    /**
     * @param text
     * @return
     */
    protected char[] removedEscapedNewline(char[] text, int start, int len) {
        if (CharArrayUtils.indexOf('\n', text, start, start + len) == -1)
            return text;
        char[] result = new char[len];
        Arrays.fill(result, ' ');
        int counter = 0;
        for (int i = start; i < start + len; ++i) {
            if (text[i] == '\\' && i + 1 < text.length && text[i + 1] == '\n')
                ++i;
            else if (text[i] == '\\' && i + 1 < text.length
                    && text[i + 1] == '\r' && i + 2 < text.length
                    && text[i + 2] == '\n')
                i += 2;
            else
                result[counter++] = text[i];
        }
        return CharArrayUtils.trim(result);
    }

    /**
     * Remove line and block comments from the given char array.
     * @param text  the char array
     * @return a char array without comment
     */
    protected char[] removeCommentFromBuffer(char[] text) {
        char[] result = new char[text.length];
        Arrays.fill(result, ' ');
        int resultCount = 0;
        boolean insideString= false;
        boolean insideSingleQuote= false;
        boolean escaped= false;
        // either a single-line or multi-line comment was found
        forLoop: for (int i = 0; i < text.length; ++i) {
            final char c= text[i];
        	switch (c) {
        	case '/':
    			if (!insideString && !insideSingleQuote && i + 1 < text.length) {
    				final char c2= text[i + 1];
    				if (c2 == '/') {
    					// done
    					break forLoop;
    				} else if (c2 == '*') {
    					i += 2;
    					while (i < text.length
    							&& !(text[i] == '*' && i + 1 < text.length && text[i + 1] == '/'))
    						++i;
    					++i;
    					continue;
    				}
    			}
    			escaped= false;
                break;
            case '\\':
           		escaped = !escaped;
                break;
            case '"':
            	if (!insideSingleQuote) {
            		insideString= insideString ? escaped : true;
            	}
            	escaped= false;
            	break;
            case '\'':
            	if (!insideString) {
            		insideSingleQuote= insideSingleQuote ? escaped : true;
            	}
            	escaped= false;
            	break;
            case '\t':
            	if (!insideString && !insideSingleQuote) {
            		result[resultCount++]= ' ';
                	continue;
            	}
        		escaped= false;
            	break;
        	default:
        		escaped= false;
        	}
            result[resultCount++] = c;
        }
        return CharArrayUtils.trim(result);
    }

    protected void handlePPUndef(int pos) throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        skipOverWhiteSpace();

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '_'
                    || (c >= '0' && c <= '9' || Character
                            .isUnicodeIdentifierPart(c))) {
                ++idlen;
                continue;
            }
            break;

        }
        --bufferPos[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(new String(buffer, idstart, idlen));

        skipToNewLine();
        

        Object definition = definitions.remove(buffer, idstart, idlen);
        processUndef(pos, bufferPos[bufferStackPos]+1, CharArrayUtils.extract(buffer, idstart, idlen ), idstart, definition);
    }

    /**
     * @param pos
     * @param endPos
     * @param symbol TODO
     * @param namePos TODO
     * @param definition TODO
     */
    protected abstract void processUndef(int pos, int endPos, char[] symbol, int namePos, Object definition);

    protected void handlePPIfdef(int pos, boolean positive)
            throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(EMPTY_STRING);

        skipOverWhiteSpace();

        if (isLimitReached())
            handleCompletionOnDefinition(EMPTY_STRING);

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '_'
                    || (c >= '0' && c <= '9' || Character
                            .isUnicodeIdentifierPart(c))) {
                ++idlen;
                continue;
            }
            break;

        }
        --bufferPos[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(new String(buffer, idstart, idlen));

        skipToNewLine();

        branchState(BRANCH_IF);

        if ((definitions.get(buffer, idstart, idlen) != null) == positive) {
            processIfdef(pos, bufferPos[bufferStackPos]+1, positive, true);
            return;
        }

        processIfdef(pos, bufferPos[bufferStackPos]+1, positive, false);
        // skip over this group
        skipOverConditionalCode(true);
        if (isLimitReached())
            handleInvalidCompletion();
    }

    protected abstract void processIfdef(int startPos, int endPos,
            boolean positive, boolean taken);

    // checkelse - if potential for more, otherwise skip to endif
    protected void skipOverConditionalCode(boolean checkelse) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int nesting = 0;

        while (bufferPos[bufferStackPos] < limit) {

            skipOverWhiteSpaceFetchToken();

            if (++bufferPos[bufferStackPos] >= limit)
                return;

            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '#') {
                int startPos = bufferPos[bufferStackPos];
                skipOverWhiteSpace();

                // find the directive
                int start = ++bufferPos[bufferStackPos];

                // if new line or end of buffer, we're done
                if (start >= limit || buffer[start] == '\n')
                    continue;

                c = buffer[start];
                if ((c >= 'a' && c <= 'z')) {
                    while (++bufferPos[bufferStackPos] < limit) {
                        c = buffer[bufferPos[bufferStackPos]];
                        if ((c >= 'a' && c <= 'z'))
                            continue;
                        break;
                    }
                    --bufferPos[bufferStackPos];
                    int len = bufferPos[bufferStackPos] - start + 1;
                    int type = ppKeywords.get(buffer, start, len);
                    if (type != ppKeywords.undefined) {
                        switch (type) {
                        case IPreprocessorDirective.ppIfdef:
                        case IPreprocessorDirective.ppIfndef:
                        case IPreprocessorDirective.ppIf:
                            ++nesting;
                            branchState(BRANCH_IF);
                            skipToNewLine();
                            if (type == IPreprocessorDirective.ppIfdef)
                                processIfdef(startPos,
                                        bufferPos[bufferStackPos]+1, true, false);
                            else if (type == IPreprocessorDirective.ppIfndef)
                                processIfdef(startPos,
                                        bufferPos[bufferStackPos]+1, false, false);
                            else
                                processIf(startPos, bufferPos[bufferStackPos]+1,
                                        false);
                            break;
                        case IPreprocessorDirective.ppElse:
                            if (branchState(BRANCH_ELSE)) {
                                skipToNewLine();
                                if (checkelse && nesting == 0) {
                                    processElse(startPos,
                                            bufferPos[bufferStackPos]+1, true);
                                    return;
                                }
                                processElse(startPos,
                                        bufferPos[bufferStackPos]+1, false);
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        case IPreprocessorDirective.ppElif:
                            if (branchState(BRANCH_ELIF)) {
                                if (checkelse && nesting == 0) {
                                    // check the condition
                                    start = bufferPos[bufferStackPos] + 1;
                                    skipToNewLine();
                                    int end= bufferPos[bufferStackPos] + 1;
                                    len= end - start;
                                    if (expressionEvaluator
                                            .evaluate(
                                                    buffer,
                                                    start,
                                                    len,
                                                    definitions,
                                                    getLineNumber(start),
                                                    getCurrentFilename()) != 0) {
										// condition passed, we're good
                                        processElsif(startPos, end, true);
                                        return;
                                    }
                                    processElsif(startPos, end, false);
                                } else {
                                    skipToNewLine();
                                    processElsif(startPos, bufferPos[bufferStackPos] + 1, false);
                                }
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        case IPreprocessorDirective.ppEndif:
                            if (branchState(BRANCH_END)) {
                                processEndif(startPos,
                                        bufferPos[bufferStackPos] + 1);
                                if (nesting > 0) {
                                    --nesting;
                                } else {
                                    skipToNewLine();
                                    return;
                                }
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        case IPreprocessorDirective.ppInclude:
                            handlePPInclude(startPos, false, getLineNumber(startPos), false);
                            break;
                        case IPreprocessorDirective.ppInclude_next:
                            handlePPInclude(startPos, true, getLineNumber(startPos), false);
                            break;
                        case IPreprocessorDirective.ppImport:
                            handlePPInclude(startPos, true, getLineNumber(startPos), false);
                            break;
                        }
                    }
                }
            } else if (c != '\n')
            	if(scanComments){
            		skipToNewLineAndCollectComments();
            	}else{
            		skipToNewLine();
            	}
        }
    }

    protected boolean skipOverWhiteSpace() {
    	return skipOverWhiteSpaceAndParseComments();    	
    }
    
    protected boolean skipOverWhiteSpaceFetchToken() {

        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int pos = bufferPos[bufferStackPos];        

        boolean encounteredComment = false;
        while (++bufferPos[bufferStackPos] < limit) {
            pos = bufferPos[bufferStackPos];
            switch (buffer[pos]) {
            case ' ':
            case '\t':
            case '\r':
                continue;
            case '/':
                if (!scanComments) {
              	   if (pos + 1 < limit) {
              		   if (buffer[pos + 1] == '/') {
  							// C++ comment, skip rest of line
  							skipToNewLine(true); 
  							return false;
  						} else if (buffer[pos + 1] == '*') {
  							// C comment, find closing */
  							for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
  								pos = bufferPos[bufferStackPos];
  								if (buffer[pos] == '*' && pos + 1 < limit
  										&& buffer[pos + 1] == '/') {
  									++bufferPos[bufferStackPos];
  									encounteredComment = true;
  									break;
  								}
  							}
  							continue;
  						}
  					}
  				}
                break;
            case '\\':
                if (pos + 1 < limit && buffer[pos + 1] == '\n') {
                    // \n is a whitespace
                    ++bufferPos[bufferStackPos];
                    continue;
                }
                if (pos + 1 < limit && buffer[pos + 1] == '\r') {
                    if (pos + 2 < limit && buffer[pos + 2] == '\n') {
                        bufferPos[bufferStackPos] += 2;
                        continue;
                    }
                }
                break;
            }

            // fell out of switch without continuing, we're done
            --bufferPos[bufferStackPos];
            return encounteredComment;
        }
        --bufferPos[bufferStackPos];
        return encounteredComment;
    }
    
    protected boolean skipOverWhiteSpaceAndParseComments() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
	
        int pos = bufferPos[bufferStackPos];
        //		if( pos > 0 && pos < limit && buffer[pos] == '\n')
        //			return false;
        boolean encounteredComment = false;
        while (++bufferPos[bufferStackPos] < limit) {
        	pos = bufferPos[bufferStackPos];
        	switch (buffer[pos]) {
        	case ' ':
        	case '\t':
        	case '\r':
        		continue;
        	case '/':
        		if (pos + 1 < limit) {
        			if (buffer[pos + 1] == '/' || buffer[pos + 1] == '*') {
        				IToken comment = scanComment();	
        				if(comment.getType() == IToken.tBLOCKCOMMENT){
        					encounteredComment=true;
        				}
        				continue;
        			}
        		}
        		break;
        	case '\\':
        		if (pos + 1 < limit && buffer[pos + 1] == '\n') {
        			// \n is a whitespace
        			++bufferPos[bufferStackPos];
        			continue;
        		}
        		if (pos + 1 < limit && buffer[pos + 1] == '\r') {
        			if (pos + 2 < limit && buffer[pos + 2] == '\n') {
        				bufferPos[bufferStackPos] += 2;
        				continue;
        			}
        		}
        		break;
        	}
        	// fell out of switch without continuing, we're done
        	--bufferPos[bufferStackPos];
        	return encounteredComment;
        }
        --bufferPos[bufferStackPos];
        return encounteredComment;
    }

    protected int indexOfNextNonWhiteSpace(char[] buffer, int start, int limit) {
        if (start < 0 || start >= buffer.length || limit > buffer.length)
            return -1;

        int pos = start + 1;
        while (pos < limit) {
            switch (buffer[pos++]) {
            case ' ':
            case '\t':
            case '\r':
                continue;
            case '/':
                if (pos < limit) {
                    if (buffer[pos] == '/') {
                        // C++ comment, skip rest of line
                        while (++pos < limit) {
                            switch (buffer[pos]) {
                            case '\\':
                                ++pos;
                                break;
                            case '\n':
                                break;
                            }
                        }
                    } else if (buffer[pos] == '*') {
                        // C comment, find closing */
                        while (++pos < limit) {
                            if (buffer[pos] == '*' && pos + 1 < limit
                                    && buffer[pos + 1] == '/') {
                                pos += 2;
                                break;
                            }
                        }
                    }
                }
                continue;
            case '\\':
                if (pos < limit && (buffer[pos] == '\n' || buffer[pos] == '\r')) {
                    ++pos;
                    continue;
                }
            }
            // fell out of switch without continuing, we're done
            return --pos;
        }
        return pos;
    }

    protected void skipOverNonWhiteSpace() {
        skipOverNonWhiteSpace(false);
    }

    protected boolean skipOverNonWhiteSpace(boolean stopAtPound) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                --bufferPos[bufferStackPos];
                return true;
            case '/':
                int pos = bufferPos[bufferStackPos];
                if (pos + 1 < limit && (buffer[pos + 1] == '/')
                        || (buffer[pos + 1] == '*')) {
                    --bufferPos[bufferStackPos];
                    return true;
                }
                break;

            case '\\':
                pos = bufferPos[bufferStackPos];
                if (pos + 1 < limit && buffer[pos + 1] == '\n') {
                    // \n is whitespace
                    --bufferPos[bufferStackPos];
                    return true;
                }
                if (pos + 1 < limit && buffer[pos + 1] == '\r') {
                    if (pos + 2 < limit && buffer[pos + 2] == '\n') {
                        bufferPos[bufferStackPos] += 2;
                        continue;
                    }
                }
                break;
            case '"':
                boolean escaped = false;
                if (bufferPos[bufferStackPos] - 1 > 0
                        && buffer[bufferPos[bufferStackPos] - 1] == '\\')
                    escaped = true;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '"':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    case '\n':
                        if (!escaped)
                            break loop;
                    case '/':
                        if (escaped
                                && (bufferPos[bufferStackPos] + 1 < limit)
                                && (buffer[bufferPos[bufferStackPos] + 1] == '/' || buffer[bufferPos[bufferStackPos] + 1] == '*')) {
                            --bufferPos[bufferStackPos];
                            return true;
                        }

                    default:
                        escaped = false;
                    }
                }
                //if we hit the limit here, then the outer while loop will
                // advance
                //us 2 past the end and we'll back up one and still be past the
                // end,
                //so back up here as well to leave us at the last char.
                if (bufferPos[bufferStackPos] == bufferLimit[bufferStackPos])
                    bufferPos[bufferStackPos]--;
                break;
            case '\'':
                escaped = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '\'':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    default:
                        escaped = false;
                    }
                }
                if (bufferPos[bufferStackPos] == bufferLimit[bufferStackPos])
                    bufferPos[bufferStackPos]--;

                break;
            case '#':
                if (stopAtPound) {
                    if (bufferPos[bufferStackPos] + 1 >= limit
                            || buffer[bufferPos[bufferStackPos] + 1] != '#') {
                        --bufferPos[bufferStackPos];
                        return false;
                    }
                    ++bufferPos[bufferStackPos];
                }
                break;
            }
        }
        --bufferPos[bufferStackPos];
        return true;
    }

    protected int skipOverMacroArg() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int argEnd = bufferPos[bufferStackPos]--;
        int nesting = 0;
        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case '(':
                ++nesting;
                break;
            case ')':
                if (nesting == 0) {
                    --bufferPos[bufferStackPos];
                    return argEnd;
                }
                --nesting;
                break;
            case ',':
                if (nesting == 0) {
                    --bufferPos[bufferStackPos];
                    return argEnd;
                }
                break;
            // fix for 95119
            case '\'':
                boolean escapedChar = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escapedChar = !escapedChar;
                        continue;
                    case '\'':
                        if (escapedChar) {
                            escapedChar = false;
                            continue;
                        }
                        break loop;
                    default:
                       escapedChar = false;
                    }
                }
                break;
            case '"':
                boolean escaped = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '"':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    default:
                        escaped = false;
                    }
                }
                break;
            }
            argEnd = bufferPos[bufferStackPos];
            skipOverWhiteSpace();
        }
        --bufferPos[bufferStackPos];
        // correct argEnd when reaching limit, (bug 179383)
        if (argEnd==limit) {
        	argEnd--;
        }
        return argEnd;
    }

    protected void skipOverIdentifier() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '.' && bufferPos[bufferStackPos] + 1 < limit
                    && buffer[bufferPos[bufferStackPos] + 1] == '.'
                    && bufferPos[bufferStackPos] + 2 < limit
                    && buffer[bufferPos[bufferStackPos] + 2] == '.') {
                // encountered "..." make sure it's the last argument, if not
                // raise
                // IProblem

                bufferPos[bufferStackPos] += 2;
                int end = bufferPos[bufferStackPos];

                while (++bufferPos[bufferStackPos] < limit) {
                    char c2 = buffer[bufferPos[bufferStackPos]];

                    if (c2 == ')') { // good
                        bufferPos[bufferStackPos] = end; // point at the end of
                                                         // ... to
                        // get the argument
                        return;
                    }

                    switch (c2) {
                    case ' ':
                    case '\t':
                    case '\r':
                        continue;
                    case '\\':
                        if (bufferPos[bufferStackPos] + 1 < limit
                                && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                            // \n is a whitespace
                            ++bufferPos[bufferStackPos];
                            continue;
                        }
                        if (bufferPos[bufferStackPos] + 1 < limit
                                && buffer[bufferPos[bufferStackPos] + 1] == '\r') {
                            if (bufferPos[bufferStackPos] + 2 < limit
                                    && buffer[bufferPos[bufferStackPos] + 2] == '\n') {
                                bufferPos[bufferStackPos] += 2;
                                continue;
                            }
                        }
                        break;
                    default:
                        // bad
                        handleProblem(
                                IProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST,
                                bufferPos[bufferStackPos], String.valueOf(c2)
                                        .toCharArray());
                        return;
                    }
                }
                // "..." was the last macro argument
                break;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || c == '_' || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                continue;
            }
            break; // found the end of the argument
        }

        --bufferPos[bufferStackPos];
    }

    protected void skipToNewLine() {
    	skipToNewLine(false);
    }
    
    /** 
     * Skips everything up to the next newline. 
     */
    protected void skipToNewLine(boolean insideComment) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int pos = bufferPos[bufferStackPos];
        
        boolean escaped = false;
        boolean insideString= false;
        boolean insideSingleQuote= false;
        while (++pos < limit) {
        	char ch= buffer[pos];
            switch (ch) {
            case '/':
            	if (insideComment || insideString || insideSingleQuote) {
            		break;
            	}
                if (pos + 1 < limit) {
                	char c= buffer[pos + 1];
                	if (c == '*') {
                        pos+=2;
                        while (++pos < limit) {
                            if (buffer[pos-1] == '*' && buffer[pos] == '/') {
                            	pos++;
                                break;
                            }
                        }
                        pos--;
                        break;
                	}
                	else if (c == '/') {
                		insideComment= true;
                	}
                }
                break;
            case '\\':
                escaped = !escaped;
                continue;
            case '"': 
            	if (!insideComment && !insideSingleQuote) {
            		insideString= insideString ? escaped : true;
            	}
            	break;
            case '\'':
            	if (!insideComment && !insideString) {
            		insideSingleQuote= insideSingleQuote ? escaped : true;
            	}
            	break;
            case '\n':
                if (escaped) {
                    break;
                }
                bufferPos[bufferStackPos]= pos-1;
                return;
            case '\r':
            	if (pos+1 < limit && buffer[pos+1] == '\n') {
            		if (escaped) {
            			pos++;
            			break;
            		}
                	bufferPos[bufferStackPos]= pos-1;
                    return;
                }
                break;
            default:
            	break;
            }
            escaped = false;
        }
        bufferPos[bufferStackPos]= pos-1;
    }
    
    protected void skipToNewLineAndCollectComments() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];   
        int pos = bufferPos[bufferStackPos];
        
        boolean escaped = false;
        boolean insideString= false;
        boolean insideSingleQuote= false;
        for (;pos < limit;++pos) {
        	char ch= buffer[pos];
            switch (ch) {
            case '/':
            	if (insideString || insideSingleQuote) {
            		break;
            	}
                if (pos + 1 < limit) {
                	char c= buffer[pos + 1];
                	if (c == '*'||c == '/') {
                		bufferPos[bufferStackPos] = pos;
                        IToken comment = scanComment();
                        commentsFromInactiveCode = (IToken[]) ArrayUtil.append(comment.getClass(), commentsFromInactiveCode, comment);
                        pos = bufferPos[bufferStackPos];
                	}
                }
                break;
            case '\\':
                escaped = !escaped;
                continue;
            case '"': 
            	if (!insideSingleQuote) {
            		insideString= insideString ? escaped : true;
            	}
            	break;
            case '\'':
            	if (!insideString) {
            		insideSingleQuote= insideSingleQuote ? escaped : true;
            	}
            	break;
            case '\n':
                if (escaped) {
                    break;
                }
                bufferPos[bufferStackPos]= pos;
                return;
            case '\r':
            	if (pos+1 < limit && buffer[pos+1] == '\n') {
            		if (escaped) {
            			pos++;
            			break;
            		}
                	bufferPos[bufferStackPos]= pos;
                    return;
                }
                break;
            default:
            	break;
            }
            escaped = false;
        }
        bufferPos[bufferStackPos]= pos;
	}

    protected char[] handleFunctionStyleMacro(FunctionStyleMacro macro,
            boolean pushContext) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int start = bufferPos[bufferStackPos] - macro.name.length + 1;
        skipOverWhiteSpace();
        while (bufferPos[bufferStackPos] < limit
                && buffer[bufferPos[bufferStackPos]] == '\\'
                && bufferPos[bufferStackPos] + 1 < buffer.length
                && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
            bufferPos[bufferStackPos] += 2;
            skipOverWhiteSpace();
        }

        if (++bufferPos[bufferStackPos] >= limit) {
            //allow a macro boundary cross here, but only if the caller was
            // prepared to accept a bufferStackPos change
            if (pushContext) {
                int idx = -1;
                int stackpPos = bufferStackPos;
                while (bufferData[stackpPos] != null
                        && bufferData[stackpPos] instanceof MacroData) {
                    stackpPos--;
                    if (stackpPos < 0)
                        return EMPTY_CHAR_ARRAY;
                    idx = indexOfNextNonWhiteSpace(bufferStack[stackpPos],
                            bufferPos[stackpPos], bufferLimit[stackpPos]);
                    if (idx >= bufferLimit[stackpPos])
                        continue;
                    if (idx > 0 && bufferStack[stackpPos][idx] == '(')
                        break;
                    bufferPos[bufferStackPos]--;
                    return null;
                }
                if (idx == -1) {
                    bufferPos[bufferStackPos]--;
                    return null;
                }

                MacroData data;
                IMacro popMacro= macro;
                do {
                	data= (MacroData) bufferData[bufferStackPos];
                    popContextForFunctionMacroName(popMacro);
                	popMacro= data.macro;
                } while (bufferStackPos > stackpPos);

                bufferPos[bufferStackPos] = idx;
                buffer = bufferStack[bufferStackPos];
                limit = bufferLimit[bufferStackPos];
                start = data.startOffset;
            } else {
                bufferPos[bufferStackPos]--;
                return null;
            }
        }

        // fix for 107150: the scanner stops at the \n or \r after skipOverWhiteSpace() take that into consideration
        while (bufferPos[bufferStackPos] + 1 < limit && (buffer[bufferPos[bufferStackPos]] == '\n' || buffer[bufferPos[bufferStackPos]] == '\r')) {
        	bufferPos[bufferStackPos]++; // skip \n or \r
        	skipOverWhiteSpace(); // skip any other spaces after the \n
        	
        	if (bufferPos[bufferStackPos] + 1 < limit && buffer[bufferPos[bufferStackPos]] != '(' && buffer[bufferPos[bufferStackPos] + 1] == '(')
        		bufferPos[bufferStackPos]++; // advance to ( if necessary
        }

        if (buffer[bufferPos[bufferStackPos]] != '(') {
            bufferPos[bufferStackPos]--;
            return null;
        }

        char[][] arglist = macro.arglist;
        int currarg = 0;
        CharArrayObjectMap argmap = new CharArrayObjectMap(arglist.length);

        while (bufferPos[bufferStackPos] < limit) {
            skipOverWhiteSpace();

            if (bufferPos[bufferStackPos] + 1 >= limit)
            	break;
            
            if (buffer[++bufferPos[bufferStackPos]] == ')') {
                if (currarg > 0 && argmap.size() <= currarg) {
                    argmap.put(arglist[currarg], EMPTY_CHAR_ARRAY);
                }
                break;	// end of macro
            }
            if (buffer[bufferPos[bufferStackPos]] == ',') {
                if (argmap.size() <= currarg) {
                    argmap.put(arglist[currarg], EMPTY_CHAR_ARRAY);
                }
            	currarg++;
                continue;
            }

            if ((currarg >= arglist.length || arglist[currarg] == null)
                    && !macro.hasVarArgs() && !macro.hasGCCVarArgs()) {
                // too many args and no variable argument
                handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
                        bufferPos[bufferStackPos], macro.name);
                break;
            }

            int argstart = bufferPos[bufferStackPos];

            int argend = -1;
            if ((macro.hasGCCVarArgs() || macro.hasVarArgs()) && currarg == macro.getVarArgsPosition()) {
                // there are varargs and the other parameters have been accounted
                // for, the rest will replace __VA_ARGS__ or name where
                // "name..." is the parameter
                for (;;) {
                	argend= skipOverMacroArg();
                    skipOverWhiteSpace();
                    // to continue we need at least a comma and another char.
                    if (bufferPos[bufferStackPos]+2 >= limit) { 
                    	break;
                    }
                    if (buffer[++bufferPos[bufferStackPos]] == ')') {
                    	bufferPos[bufferStackPos]--;
                    	break;
                    }
                    // it's a comma
                    bufferPos[bufferStackPos]++;
                } 
            } else {
                argend = skipOverMacroArg();
            }
            
            char[] arg = EMPTY_CHAR_ARRAY;
            int arglen = argend - argstart + 1;
            if (arglen > 0) {
                arg = new char[arglen];
                System.arraycopy(buffer, argstart, arg, 0, arglen);
            }

            argmap.put(arglist[currarg], arg);
        }

        int numRequiredArgs = arglist.length;
        for (int i = 0; i < arglist.length; i++) {
            if (arglist[i] == null) {
            	numRequiredArgs = i;
                break;
            }
        }

        /* Don't require a match for the vararg placeholder */
        /* Workaround for bugzilla 94365 */
        if (macro.hasGCCVarArgs()|| macro.hasVarArgs())
        	numRequiredArgs--;
 
        if (argmap.size() < numRequiredArgs) {
            handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
                    bufferPos[bufferStackPos], macro.name);
        }

        char[] result = null;
        if (macro instanceof DynamicFunctionStyleMacro) {
            result = ((DynamicFunctionStyleMacro) macro).execute(argmap);
        } else {
            CharArrayObjectMap replacedArgs = new CharArrayObjectMap(argmap
                    .size());
            int size = expandFunctionStyleMacro(macro.getExpansion(), argmap,
                    replacedArgs, null);
            result = new char[size];
            expandFunctionStyleMacro(macro.getExpansion(), argmap, replacedArgs,
                    result);
        }
        if (pushContext)
        {
            pushContext(result, new FunctionMacroData(start, bufferPos[bufferStackPos] + 1,
        		macro, argmap));
        }
        return result;
    }

    /**
     * Called when the buffer limit is reached while expanding a function style macro.
     * This special case might be handled differently by subclasses.
     * 
	 * @param macro
	 */
	protected void popContextForFunctionMacroName(IMacro macro) {
		// do the default
		popContext();
	}

	protected char[] replaceArgumentMacros(char[] arg) {
        int limit = arg.length;
        int start = -1, end = -1;
        Object expObject = null;
        for (int pos = 0; pos < limit; pos++) {
            char c = arg[pos];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || Character.isLetter(c)
                    || (support$Initializers && c == '$')) {
                start = pos;
                while (++pos < limit) {
                    c = arg[pos];
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || c == '_' || (c >= '0' && c <= '9')
                            || (support$Initializers && c == '$')
                            || Character.isUnicodeIdentifierPart(c)) {
                        continue;
                    }
                    break;
                }
                end = pos - 1;
            }
            else if (c == '"') {
            	boolean escaped= false;
                while (++pos < limit) {
                    c = arg[pos];
                    if (!escaped && c == '"') {
                    	break;
                    }
                    if (c == '\\') {
                    	escaped= !escaped;
                    }
                    else {
                    	escaped= false;
                    }
                }
            }
            else if (c == '\'') {
            	boolean escaped= false;
                while (++pos < limit) {
                    c = arg[pos];
                    if (!escaped && c == '\'') {
                    	break;
                    }
                    if (c == '\\') {
                    	escaped= !escaped;
                    }
                    else {
                    	escaped= false;
                    }
                }
            }

            if (start != -1 && end >= start) {
                //Check for macro expansion
                expObject = definitions.get(arg, start, (end - start + 1));
                if (expObject == null || !shouldExpandMacro((IMacro) expObject)) {
                    expObject = null;
                    start = -1;
                    continue;
                }
                //else, break and expand macro
                break;
            }
        }

        if (expObject == null)
        {
            return arg;
        }
        

        char[] expansion = null;
        if (expObject instanceof FunctionStyleMacro) {
            FunctionStyleMacro expMacro = (FunctionStyleMacro) expObject;
            pushContext((start == 0) ? arg : CharArrayUtils.extract(arg, start,
                    arg.length - start));
            bufferPos[bufferStackPos] += end - start + 1;
            expansion = handleFunctionStyleMacro(expMacro, false);
            end = bufferPos[bufferStackPos] + start;
            popContext();
        } else if (expObject instanceof ObjectStyleMacro) {
            ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
            expansion = expMacro.getExpansion();
        } else if (expObject instanceof char[]) {
            expansion = (char[]) expObject;
        } else if (expObject instanceof DynamicStyleMacro) {
            DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
            expansion = expMacro.execute();
        }

        if (expansion != null) {
            int newlength = start + expansion.length + (limit - end - 1);
            char[] result = new char[newlength];
            System.arraycopy(arg, 0, result, 0, start);
            System.arraycopy(expansion, 0, result, start, expansion.length);
            if (arg.length > end + 1)
                System.arraycopy(arg, end + 1, result,
                        start + expansion.length, limit - end - 1);

            
            beforeReplaceAllMacros();
            //we need to put the macro on the context stack in order to detect
            // recursive macros
            pushContext(EMPTY_CHAR_ARRAY,
                    new MacroData(start, start
                            + ((IMacro) expObject).getName().length,
                            (IMacro) expObject));
            arg = replaceArgumentMacros(result); //rescan for more macros
            popContext();
            afterReplaceAllMacros();
        }
        
        return arg;
    }


    /**
     * Hook for subclasses.
     */
    protected void afterReplaceAllMacros() {
        // TODO Auto-generated method stub
        
    }

    /**
     * Hook for subclasses.
     */
    protected void beforeReplaceAllMacros() {
        // TODO Auto-generated method stub
        
    }

    protected int expandFunctionStyleMacro(char[] expansion,
            CharArrayObjectMap argmap, CharArrayObjectMap replacedArgs,
            char[] result) {

        // The current position in the expansion string that we are looking at
        int pos = -1;
        // The last position in the expansion string that was copied over
        int lastcopy = -1;
        // The current write offset in the result string - also tells us the
        // length of the result string
        int outpos = 0;
        // The first character in the current block of white space - there are
        // times when we don't
        // want to copy over the whitespace
        int wsstart = -1;
        //whether or not we are on the second half of the ## operator
        boolean prevConcat = false;
        //for handling ##
        char[] prevArg = null;
        int prevArgStart = -1;
        int prevArgLength = -1;
        int prevArgTarget = 0;

        int limit = expansion.length;

        while (++pos < limit) {
            char c = expansion[pos];

            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c < '9')
                    || Character.isUnicodeIdentifierPart(c)) {

                wsstart = -1;
                int idstart = pos;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || (c >= '0' && c <= '9') || c == '_' || Character
                            .isUnicodeIdentifierPart(c))) {
                        break;
                    }
                }
                --pos;

                char[] repObject = (char[]) argmap.get(expansion, idstart, pos
                        - idstart + 1);

                int next = indexOfNextNonWhiteSpace(expansion, pos, limit);
                boolean nextIsPoundPound = (next + 1 < limit
                        && expansion[next] == '#' && expansion[next + 1] == '#');

                if (prevConcat && prevArgStart > -1 && prevArgLength > 0) {
                    int l1 = prevArg != null ? prevArg.length : prevArgLength;
                    int l2 = repObject != null ? repObject.length : pos
                            - idstart + 1;
                    char[] newRep = new char[l1 + l2];
                    if (prevArg != null)
                        System.arraycopy(prevArg, 0, newRep, 0, l1);
                    else
                        System
                                .arraycopy(expansion, prevArgStart, newRep, 0,
                                        l1);

                    if (repObject != null)
                        System.arraycopy(repObject, 0, newRep, l1, l2);
                    else
                        System.arraycopy(expansion, idstart, newRep, l1, l2);
                    idstart = prevArgStart;
                    repObject = newRep;
                }
                if (repObject != null) {
                    // copy what we haven't so far
                    if (++lastcopy < idstart) {
                        int n = idstart - lastcopy;
                        if (result != null) {
                        	// the outpos may be set back when prevConcat is true, so make sure we
                        	// stay in bounds.
                            if (prevConcat && outpos+n > result.length) {
                            	n= result.length- outpos;
                            }
                            System.arraycopy(expansion, lastcopy, result,
                                    outpos, n);
                        }
                        outpos += n;
                    }

                    if (prevConcat)
                        outpos = prevArgTarget;

                    if (!nextIsPoundPound) {
                        //16.3.1 completely macro replace the arguments before
                        // substituting them in
                        char[] rep = (char[]) ((replacedArgs != null) ? replacedArgs
                                .get(repObject)
                                : null);
                        
                        if (rep != null)
                            repObject = rep;
                        else {
                            rep = replaceArgumentMacros(repObject);
                            if (replacedArgs != null)
                                replacedArgs.put(repObject, rep);
                            repObject = rep;
                        }
                      
                        if (result != null )
                            System.arraycopy(repObject, 0, result, outpos, repObject.length);
                    }
                    outpos += repObject.length;

                    lastcopy = pos;
                }

                prevArg = repObject;
                prevArgStart = idstart;
                prevArgLength = pos - idstart + 1;
                prevArgTarget = repObject != null ? outpos - repObject.length
                        : outpos + idstart - lastcopy - 1;
                prevConcat = false;
            } else if (c == '"') {

                // skip over strings
                wsstart = -1;
                boolean escaped = false;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (c == '"') {
                        if (!escaped)
                            break;
                    } else if (c == '\\') {
                        escaped = !escaped;
                    }
                    escaped = false;
                }
                prevConcat = false;
            } else if (c == '\'') {

                // skip over character literals
                wsstart = -1;
                boolean escaped = false;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (c == '\'') {
                        if (!escaped)
                            break;
                    } else if (c == '\\') {
                        escaped = !escaped;
                    }
                    escaped = false;
                }
                prevConcat = false;
            } else if (c == ' ' || c == '\t') {
                // obvious whitespace
                if (wsstart < 0)
                    wsstart = pos;
            } else if (c == '/' && pos + 1 < limit) {

                // less than obvious, comments are whitespace
                c = expansion[pos+1];
                if (c == '/') {
                    // copy up to here or before the last whitespace
                	++pos;
                    ++lastcopy;
                    int n = wsstart < 0 ? pos - 1 - lastcopy : wsstart
                            - lastcopy;
                    if (result != null)
                        System
                                .arraycopy(expansion, lastcopy, result, outpos,
                                        n);
                    outpos += n;

                    // skip the rest
                    lastcopy = expansion.length - 1;
                } else if (c == '*') {
                	++pos;
                    if (wsstart < 1)
                        wsstart = pos - 1;
                    while (++pos < limit) {
                        if (expansion[pos] == '*' && pos + 1 < limit
                                && expansion[pos + 1] == '/') {
                            ++pos;
                            break;
                        }
                    }
                } else
                    wsstart = -1;

            } else if (c == '\\' && pos + 1 < limit
                    && expansion[pos + 1] == 'n') {
                // skip over this
                ++pos;

            } else if (c == '#') {

                if (pos + 1 < limit && expansion[pos + 1] == '#') {
                    prevConcat = true;
                    ++pos;
                    // skip whitespace
                    if (wsstart < 0)
                        wsstart = pos - 1;
                    while (++pos < limit) {
                        switch (expansion[pos]) {
                        case ' ':
                        case '\t':
                            continue;

                        case '/':
                            if (pos + 1 < limit) {
                                c = expansion[pos + 1];
                                if (c == '/')
                                    // skip over everything
                                    pos = expansion.length;
                                else if (c == '*') {
                                    ++pos;
                                    while (++pos < limit) {
                                        if (expansion[pos] == '*'
                                                && pos + 1 < limit
                                                && expansion[pos + 1] == '/') {
                                            ++pos;
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                    --pos;
                } else {
                    prevConcat = false;
                    // stringify

                    // copy what we haven't so far
                    if (++lastcopy < pos) {
                        int n = pos - lastcopy;
                        if (result != null)
                            System.arraycopy(expansion, lastcopy, result,
                                    outpos, n);
                        outpos += n;
                    }

                    // skip whitespace
                    while (++pos < limit) {
                        switch (expansion[pos]) {
                        case ' ':
                        case '\t':
                            continue;
                        case '/':
                            if (pos + 1 < limit) {
                                c = expansion[pos + 1];
                                if (c == '/')
                                    // skip over everything
                                    pos = expansion.length;
                                else if (c == '*') {
                                    ++pos;
                                    while (++pos < limit) {
                                        if (expansion[pos] == '*'
                                                && pos + 1 < limit
                                                && expansion[pos + 1] == '/') {
                                            ++pos;
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        //TODO handle comments
                        }
                        break;
                    }

                    // grab the identifier
                    c = expansion[pos];
                    int idstart = pos;
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X')
                            || c == '_' || Character.isUnicodeIdentifierPart(c)) {
                        while (++pos < limit) {
                            c = expansion[pos];
                            if (!((c >= 'a' && c <= 'z')
                                    || (c >= 'A' && c <= 'X')
                                    || (c >= '0' && c <= '9') || c == '_' || Character
                                    .isUnicodeIdentifierPart(c)))
                                break;
                        }
                    } // else TODO something
                    --pos;
                    int idlen = pos - idstart + 1;
                    char[] argvalue = (char[]) argmap.get(expansion, idstart,
                            idlen);
                    if (argvalue != null) {
                        //16.3.2-2 ... a \ character is inserted before each "
                        // and \
                        // character
                        //of a character literal or string literal

                        //technically, we are also supposed to replace each
                        // occurence
                        // of whitespace
                        //(including comments) in the argument with a single
                        // space.
                        // But, at this time
                        //we don't really care what the contents of the string
                        // are,
                        // just that we get the string
                        //so we won't bother doing that
                        if (result != null) {
                            result[outpos++] = '"';
                            for (int i = 0; i < argvalue.length; i++) {
                                if (argvalue[i] == '"' || argvalue[i] == '\\')
                                    result[outpos++] = '\\';
                                if (argvalue[i] == '\r' || argvalue[i] == '\n')
                                    result[outpos++] = ' ';
                                else
                                    result[outpos++] = argvalue[i];
                            }
                            result[outpos++] = '"';
                        } else {
                            for (int i = 0; i < argvalue.length; i++) {
                                if (argvalue[i] == '"' || argvalue[i] == '\\')
                                    ++outpos;
                                ++outpos;
                            }
                            outpos += 2;
                        }
                    }
                    lastcopy = pos;
                    wsstart = -1;
                }
            } else {
                prevConcat = false;
                // not sure what it is but it sure ain't whitespace
                wsstart = -1;
            }

        }

        if (wsstart < 0 && ++lastcopy < expansion.length) {
            int n = expansion.length - lastcopy;
            if (result != null)
                System.arraycopy(expansion, lastcopy, result, outpos, n);
            outpos += n;
        }

        return outpos;
    }

    // standard built-ins
    protected static final ObjectStyleMacro __cplusplus = new ObjectStyleMacro(
            "__cplusplus".toCharArray(), ONE);   //$NON-NLS-1$

    protected static final ObjectStyleMacro __STDC__ = new ObjectStyleMacro(
            "__STDC__".toCharArray(), ONE);  //$NON-NLS-1$

    protected static final ObjectStyleMacro __STDC_HOSTED__ = new ObjectStyleMacro(
            "__STDC_HOSTED_".toCharArray(), ONE);  //$NON-NLS-1$

    protected static final ObjectStyleMacro __STDC_VERSION__ = new ObjectStyleMacro(
            "__STDC_VERSION_".toCharArray(), "199901L".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    protected final DynamicStyleMacro __FILE__ = new DynamicStyleMacro(
            "__FILE__".toCharArray()) { //$NON-NLS-1$

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            buffer.append(getCurrentFilename());
            buffer.append('\"');
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __DATE__ = new DynamicStyleMacro(
            "__DATE__".toCharArray()) { //$NON-NLS-1$

        protected final void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            buffer.append(cal.get(Calendar.MONTH));
            buffer.append(" "); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.DAY_OF_MONTH));
            buffer.append(" "); //$NON-NLS-1$
            buffer.append(cal.get(Calendar.YEAR));
            buffer.append("\""); //$NON-NLS-1$
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __TIME__ = new DynamicStyleMacro(
            "__TIME__".toCharArray()) { //$NON-NLS-1$

        protected final void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            append(buffer, cal.get(Calendar.HOUR));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.MINUTE));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.SECOND));
            buffer.append("\""); //$NON-NLS-1$
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __LINE__ = new DynamicStyleMacro(
            "__LINE__".toCharArray()) { //$NON-NLS-1$

        public char[] execute() {
            int lineNumber = lineNumbers[bufferStackPos];
            return Long.toString(lineNumber).toCharArray();
        }
    };

    protected int offsetBoundary = -1;
    
    protected boolean contentAssistMode = false;

    protected void setupBuiltInMacros(IScannerExtensionConfiguration config) {

        definitions.put(__STDC__.name, __STDC__);
        definitions.put(__FILE__.name, __FILE__);
        definitions.put(__DATE__.name, __DATE__);
        definitions.put(__TIME__.name, __TIME__);
        definitions.put(__LINE__.name, __LINE__);

        if (language == ParserLanguage.CPP)
            definitions.put(__cplusplus.name, __cplusplus);
        else {
            definitions.put(__STDC_HOSTED__.name, __STDC_HOSTED__);
            definitions.put(__STDC_VERSION__.name, __STDC_VERSION__);
        }

        CharArrayObjectMap toAdd = config.getAdditionalMacros();
        for (int i = 0; i < toAdd.size(); ++i)
            definitions.put(toAdd.keyAt(i), toAdd.getAt(i));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
     */
    public final void setOffsetBoundary(int offset) {
        offsetBoundary = offset;
        bufferLimit[0] = offset;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setContentAssistMode(int)
	 */
	public void setContentAssistMode(int offset) {
		setOffsetBoundary(offset);
		contentAssistMode = true;
	}
	
	/**
	 * Turns on/off comment parsing.
	 * @since 4.0
	 */
	public void setScanComments(boolean val) {
		scanComments= val;
	}
	
    protected ParserLanguage getLanguage() {
        return language;
    }

    protected CodeReader getMainReader() {
        if (bufferData != null && bufferData[0] != null
                && bufferData[0] instanceof CodeReader)
            return ((CodeReader) bufferData[0]);
        return null;
    }

    public char[] getMainFilename() {
        if (bufferData != null && bufferData[0] != null
                && bufferData[0] instanceof CodeReader)
            return ((CodeReader) bufferData[0]).filename;

        return EMPTY_CHAR_ARRAY;
    }

    protected final char[] getCurrentFilename() {
        for (int i = bufferStackPos; i >= 0; --i) {
            if (bufferData[i] instanceof InclusionData)
                return ((InclusionData) bufferData[i]).reader.filename;
            if (bufferData[i] instanceof CodeReader)
                return ((CodeReader) bufferData[i]).filename;
        }
        return EMPTY_CHAR_ARRAY;
    }

    protected final int getCurrentFileIndex() {
        for (int i = bufferStackPos; i >= 0; --i) {
            if (bufferData[i] instanceof InclusionData
                    || bufferData[i] instanceof CodeReader)
                return i;
        }
        return 0;
    }

    protected final CharArrayIntMap keywords;

    protected static CharArrayIntMap ckeywords;

    protected static CharArrayIntMap cppkeywords;

    protected static CharArrayIntMap ppKeywords;

    protected static final char[] TAB = { '\t' };

    protected static final char[] SPACE = { ' ' };

    private static final MacroExpansionToken EXPANSION_TOKEN = new MacroExpansionToken();

    static {
        CharArrayIntMap words = new CharArrayIntMap(40, -1);

        // Common keywords
        words.put(Keywords.cAUTO, IToken.t_auto);
        words.put(Keywords.cBREAK, IToken.t_break);
        words.put(Keywords.cCASE, IToken.t_case); 
        words.put(Keywords.cCHAR, IToken.t_char); 
        words.put(Keywords.cCONST, IToken.t_const); 
        words.put(Keywords.cCONTINUE, IToken.t_continue); 
        words.put(Keywords.cDEFAULT, IToken.t_default); 
        words.put(Keywords.cDO, IToken.t_do); 
        words.put(Keywords.cDOUBLE, IToken.t_double); 
        words.put(Keywords.cELSE, IToken.t_else); 
        words.put(Keywords.cENUM, IToken.t_enum); 
        words.put(Keywords.cEXTERN, IToken.t_extern); 
        words.put(Keywords.cFLOAT, IToken.t_float); 
        words.put(Keywords.cFOR, IToken.t_for); 
        words.put(Keywords.cGOTO, IToken.t_goto); 
        words.put(Keywords.cIF, IToken.t_if); 
        words.put(Keywords.cINLINE, IToken.t_inline); 
        words.put(Keywords.cINT, IToken.t_int); 
        words.put(Keywords.cLONG, IToken.t_long); 
        words.put(Keywords.cREGISTER, IToken.t_register); 
        words.put(Keywords.cRETURN, IToken.t_return); 
        words.put(Keywords.cSHORT, IToken.t_short); 
        words.put(Keywords.cSIGNED, IToken.t_signed); 
        words.put(Keywords.cSIZEOF, IToken.t_sizeof); 
        words.put(Keywords.cSTATIC, IToken.t_static); 
        words.put(Keywords.cSTRUCT, IToken.t_struct); 
        words.put(Keywords.cSWITCH, IToken.t_switch); 
        words.put(Keywords.cTYPEDEF, IToken.t_typedef); 
        words.put(Keywords.cUNION, IToken.t_union); 
        words.put(Keywords.cUNSIGNED, IToken.t_unsigned); 
        words.put(Keywords.cVOID, IToken.t_void); 
        words.put(Keywords.cVOLATILE, IToken.t_volatile); 
        words.put(Keywords.cWHILE, IToken.t_while); 
        words.put(Keywords.cASM, IToken.t_asm); 

        // ANSI C keywords
        ckeywords = (CharArrayIntMap) words.clone();
        ckeywords.put(Keywords.cRESTRICT, IToken.t_restrict); 
        ckeywords.put(Keywords.c_BOOL, IToken.t__Bool); 
        ckeywords.put(Keywords.c_COMPLEX, IToken.t__Complex); 
        ckeywords.put(Keywords.c_IMAGINARY, IToken.t__Imaginary); 

        // C++ Keywords
        cppkeywords = words;
        cppkeywords.put(Keywords.cBOOL, IToken.t_bool); 
        cppkeywords.put(Keywords.cCATCH, IToken.t_catch); 
        cppkeywords.put(Keywords.cCLASS, IToken.t_class); 
        cppkeywords.put(Keywords.cCONST_CAST, IToken.t_const_cast); 
        cppkeywords.put(Keywords.cDELETE, IToken.t_delete); 
        cppkeywords.put(Keywords.cDYNAMIC_CAST, IToken.t_dynamic_cast); 
        cppkeywords.put(Keywords.cEXPLICIT, IToken.t_explicit); 
        cppkeywords.put(Keywords.cEXPORT, IToken.t_export); 
        cppkeywords.put(Keywords.cFALSE, IToken.t_false); 
        cppkeywords.put(Keywords.cFRIEND, IToken.t_friend); 
        cppkeywords.put(Keywords.cMUTABLE, IToken.t_mutable); 
        cppkeywords.put(Keywords.cNAMESPACE, IToken.t_namespace); 
        cppkeywords.put(Keywords.cNEW, IToken.t_new); 
        cppkeywords.put(Keywords.cOPERATOR, IToken.t_operator); 
        cppkeywords.put(Keywords.cPRIVATE, IToken.t_private); 
        cppkeywords.put(Keywords.cPROTECTED, IToken.t_protected); 
        cppkeywords.put(Keywords.cPUBLIC, IToken.t_public); 
        cppkeywords.put(Keywords.cREINTERPRET_CAST, IToken.t_reinterpret_cast); 
        cppkeywords.put(Keywords.cSTATIC_CAST, IToken.t_static_cast); 
        cppkeywords.put(Keywords.cTEMPLATE, IToken.t_template); 
        cppkeywords.put(Keywords.cTHIS, IToken.t_this); 
        cppkeywords.put(Keywords.cTHROW, IToken.t_throw); 
        cppkeywords.put(Keywords.cTRUE, IToken.t_true); 
        cppkeywords.put(Keywords.cTRY, IToken.t_try); 
        cppkeywords.put(Keywords.cTYPEID, IToken.t_typeid); 
        cppkeywords.put(Keywords.cTYPENAME, IToken.t_typename); 
        cppkeywords.put(Keywords.cUSING, IToken.t_using); 
        cppkeywords.put(Keywords.cVIRTUAL, IToken.t_virtual); 
        cppkeywords.put(Keywords.cWCHAR_T, IToken.t_wchar_t); 

        // C++ operator alternative
        cppkeywords.put(Keywords.cAND, IToken.t_and); 
        cppkeywords.put(Keywords.cAND_EQ, IToken.t_and_eq); 
        cppkeywords.put(Keywords.cBITAND, IToken.t_bitand); 
        cppkeywords.put(Keywords.cBITOR, IToken.t_bitor); 
        cppkeywords.put(Keywords.cCOMPL, IToken.t_compl); 
        cppkeywords.put(Keywords.cNOT, IToken.t_not); 
        cppkeywords.put(Keywords.cNOT_EQ, IToken.t_not_eq); 
        cppkeywords.put(Keywords.cOR, IToken.t_or); 
        cppkeywords.put(Keywords.cOR_EQ, IToken.t_or_eq); 
        cppkeywords.put(Keywords.cXOR, IToken.t_xor); 
        cppkeywords.put(Keywords.cXOR_EQ, IToken.t_xor_eq); 

        // Preprocessor keywords
        ppKeywords = new CharArrayIntMap(16, IPreprocessorDirective.ppInvalid);
        ppKeywords.put(Keywords.cIF, IPreprocessorDirective.ppIf); 
        ppKeywords.put(Keywords.cIFDEF, IPreprocessorDirective.ppIfdef); 
        ppKeywords.put(Keywords.cIFNDEF, IPreprocessorDirective.ppIfndef); 
        ppKeywords.put(Keywords.cELIF, IPreprocessorDirective.ppElif); 
        ppKeywords.put(Keywords.cELSE, IPreprocessorDirective.ppElse); 
        ppKeywords.put(Keywords.cENDIF, IPreprocessorDirective.ppEndif); 
        ppKeywords.put(Keywords.cINCLUDE, IPreprocessorDirective.ppInclude); 
        ppKeywords.put(Keywords.cDEFINE, IPreprocessorDirective.ppDefine); 
        ppKeywords.put(Keywords.cUNDEF, IPreprocessorDirective.ppUndef); 
        ppKeywords.put(Keywords.cERROR, IPreprocessorDirective.ppError); 
        ppKeywords.put(Keywords.cPRAGMA, IPreprocessorDirective.ppPragma); 
        ppKeywords.put(Keywords.cLINE, IPreprocessorDirective.ppIgnore);
    }

    /**
     * @param definition
     */
    protected void handleCompletionOnDefinition(String definition)
            throws EndOfFileException {
        IASTCompletionNode node = new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.MACRO_REFERENCE, null, null,
                definition, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null);

        throw new OffsetLimitReachedException(node);
    }

    /**
     * @param expression2
     */
    protected void handleCompletionOnExpression(char[] buffer)
            throws EndOfFileException {

        IASTCompletionNode.CompletionKind kind = IASTCompletionNode.CompletionKind.MACRO_REFERENCE;
        int lastSpace = CharArrayUtils.lastIndexOf(SPACE, buffer);
        int lastTab = CharArrayUtils.lastIndexOf(TAB, buffer);
        int max = lastSpace > lastTab ? lastSpace : lastTab;

        char[] prefix = CharArrayUtils.trim(CharArrayUtils.extract(buffer, max,
                buffer.length - max));
        for (int i = 0; i < prefix.length; ++i) {
            char c = prefix[i];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c))
                continue;
            handleInvalidCompletion();
        }
        IASTCompletionNode node = new ASTCompletionNode(
                kind,
                null,
                null,
                new String(prefix),
                KeywordSets
                        .getKeywords(
                                ((kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND) ? KeywordSetKey.EMPTY
                                        : KeywordSetKey.MACRO), language),
                EMPTY_STRING, null);

        throw new OffsetLimitReachedException(node);
    }

    protected void handleNoSuchCompletion() throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.NO_SUCH_KIND, null, null,
                EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null));
    }

    protected void handleInvalidCompletion() throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.UNREACHABLE_CODE, null, null,
                EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null));
    }

    protected void handleCompletionOnPreprocessorDirective(String prefix)
            throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.PREPROCESSOR_DIRECTIVE, null,
                null, prefix, KeywordSets.getKeywords(
                        KeywordSetKey.PP_DIRECTIVE, language), EMPTY_STRING,
                null));
    }

    protected int getCurrentOffset() {
        return bufferPos[bufferStackPos];
    }
    protected IToken scanComment() {
    		char[] buffer = bufferStack[bufferStackPos];
    		final int limit = bufferLimit[bufferStackPos];
    
    		int pos = bufferPos[bufferStackPos];
    		if (pos + 1 < limit) {
    			if (buffer[pos + 1] == '/') {
    				// C++ comment
    				int commentLength = 0;
    				while (++bufferPos[bufferStackPos] < limit) {
    					if (buffer[bufferPos[bufferStackPos]] == '\n'||buffer[bufferPos[bufferStackPos]] == '\r') {
    						break;
    					}
    					++commentLength;
    				}
   				// leave the new line there
   				--bufferPos[bufferStackPos];
    			return newToken(IToken.tCOMMENT, CharArrayUtils.extract(buffer,
    					pos, bufferPos[bufferStackPos] - pos + 1));
    		} else if (buffer[pos + 1] == '*') {
    			// C comment, find closing */
    			int start = pos;
    			for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos]+1 < limit; ++bufferPos[bufferStackPos]) {
    				pos = bufferPos[bufferStackPos];
    				if (buffer[pos] == '*' && buffer[pos + 1] == '/') {
    					++bufferPos[bufferStackPos];
    					break;
    				}
    			}
    			return newToken(IToken.tBLOCKCOMMENT, CharArrayUtils.extract(
    					buffer, start, bufferPos[bufferStackPos] - start + 1));
    		}
    	}
    	return null;
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Scanner @ file:"); //$NON-NLS-1$
        buffer.append(getCurrentFilename());
        buffer.append(" line: "); //$NON-NLS-1$
        buffer.append(getLineNumber(getCurrentOffset()));
        return buffer.toString();
    }

    protected abstract IToken newToken(int signal);

    protected abstract IToken newToken(int signal, char[] buffer);

}
