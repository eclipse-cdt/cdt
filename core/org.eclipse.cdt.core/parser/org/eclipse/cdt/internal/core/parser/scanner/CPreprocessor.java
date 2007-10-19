/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.MacroDefinitionParser.InvalidMacroDefinitionException;
import org.eclipse.cdt.internal.core.parser.scanner2.IIndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerUtility;

/**
 * C-Preprocessor providing tokens for the parsers. The class should not be used directly, rather than that 
 * you should be using the {@link IScanner} interface.
 * @since 5.0
 */
public class CPreprocessor implements ILexerLog, IScanner {
	public static final int tDEFINED= IToken.FIRST_RESERVED_PREPROCESSOR;

	private static final int ORIGIN_PREPROCESSOR_DIRECTIVE = OffsetLimitReachedException.ORIGIN_PREPROCESSOR_DIRECTIVE;
	private static final int ORIGIN_INACTIVE_CODE = OffsetLimitReachedException.ORIGIN_INACTIVE_CODE;
//	private static final int ORIGIN_MACRO_EXPANSION = OffsetLimitReachedException.ORIGIN_MACRO_EXPANSION;
	
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private static final char[] ONE = "1".toCharArray(); //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final EndOfFileException EOF = new EndOfFileException();


    // standard built-ins
    private static final ObjectStyleMacro __cplusplus = new ObjectStyleMacro("__cplusplus".toCharArray(), ONE);   //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC__ = new ObjectStyleMacro("__STDC__".toCharArray(), ONE);  //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC_HOSTED__ = new ObjectStyleMacro("__STDC_HOSTED_".toCharArray(), ONE);  //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC_VERSION__ = new ObjectStyleMacro("__STDC_VERSION_".toCharArray(), "199901L".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final ILexerLog LEXERLOG_NULL= new ILexerLog() {
		public void handleComment(boolean isBlockComment, int offset, int endOffset) {}
		public void handleProblem(int problemID, char[] source, int offset, int endOffset) {}
    };

	private interface IIncludeFileTester {
    	Object checkFile(String path, String fileName);
    }
    
    final private IIncludeFileTester createCodeReaderTester= new IIncludeFileTester() { 
    	public Object checkFile(String path, String fileName) {
    		return createReader(path, fileName);
    	}
    };
    
    final private IIncludeFileTester createPathTester= new IIncludeFileTester() { 
    	public Object checkFile(String path, String fileName) {
    		path= ScannerUtility.createReconciledPath(path, fileName);
    		if (new File(path).exists()) {
    			return path;
    		}
    		return null;
    	}
    };

    // standard built-ins
    final private DynamicStyleMacro __FILE__= new DynamicStyleMacro("__FILE__".toCharArray()) { //$NON-NLS-1$
        public Token execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            buffer.append(getCurrentFilename());
            buffer.append('\"');
            return new ImageToken(IToken.tSTRING, 0, 0, buffer.toString().toCharArray());
        }
    };
    final private DynamicStyleMacro __DATE__= new DynamicStyleMacro("__DATE__".toCharArray()) { //$NON-NLS-1$
        final private void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public Token execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            buffer.append(cal.get(Calendar.MONTH));
            buffer.append(" "); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.DAY_OF_MONTH));
            buffer.append(" "); //$NON-NLS-1$
            buffer.append(cal.get(Calendar.YEAR));
            buffer.append("\""); //$NON-NLS-1$
            return new ImageToken(IToken.tSTRING, 0, 0, buffer.toString().toCharArray());
        }
    };

    final private DynamicStyleMacro __TIME__ = new DynamicStyleMacro("__TIME__".toCharArray()) { //$NON-NLS-1$
        final private void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public Token execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            append(buffer, cal.get(Calendar.HOUR));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.MINUTE));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.SECOND));
            buffer.append("\""); //$NON-NLS-1$
            return new ImageToken(IToken.tSTRING, 0, 0, buffer.toString().toCharArray());
        }
    };

    final private DynamicStyleMacro __LINE__ = new DynamicStyleMacro("__LINE__".toCharArray()) { //$NON-NLS-1$
        public Token execute() {
            int lineNumber= fLocationMap.getCurrentLineNumber(fCurrentContext.currentPPToken().getOffset());
            return new ImageToken(IToken.tINTEGER, 0, 0, Long.toString(lineNumber).toCharArray());
        }
    };

    final private IParserLogService fLog;
    final private ICodeReaderFactory fCodeReaderFactory;
    private final ExpressionEvaluator fExpressionEvaluator;
	private final MacroDefinitionParser fMacroDefinitionParser= new MacroDefinitionParser();

    // configuration
    final private ParserLanguage fLanguage;
    final private LexerOptions fLexOptions= new LexerOptions();
    final private boolean fCheckNumbers;
    final private char[] fAdditionalNumericLiteralSuffixes;
    final private CharArrayIntMap fKeywords;
    final private CharArrayIntMap fPPKeywords;
    final private String[] fIncludePaths;
    final private String[] fQuoteIncludePaths;

    private int fContentAssistLimit;

    // state information
    private final CharArrayObjectMap fMacroDictionary = new CharArrayObjectMap(512);
    private final LocationMap fLocationMap = new LocationMap();

    /** Set of already included files */
    private final CharArraySet includedFiles= new CharArraySet(32);
    private int fTokenCount;

	private final Lexer fRootLexer;
	private final ScannerContext fRootContext;
	private ScannerContext fCurrentContext;

    private boolean isCancelled = false;

    private Token fPrefetchedToken;
    private Token fLastToken;



    public CPreprocessor(CodeReader reader, IScannerInfo info, ParserLanguage language, IParserLogService log,
            IScannerExtensionConfiguration configuration, ICodeReaderFactory readerFactory) {
        fLanguage= language;
        fLog = log;
        fCheckNumbers= true; // mstodo room for optimization.
        fAdditionalNumericLiteralSuffixes= nonNull(configuration.supportAdditionalNumericLiteralSuffixes());
        fLexOptions.fSupportDollarInitializers= configuration.support$InIdentifiers();
        fLexOptions.fSupportMinAndMax = configuration.supportMinAndMaxOperators();
        fKeywords= new CharArrayIntMap(40, -1);
        fPPKeywords= new CharArrayIntMap(40, -1);
        configureKeywords(language, configuration);

    	fIncludePaths= info.getIncludePaths();
    	fQuoteIncludePaths= getQuoteIncludePath(info);
    	
        fExpressionEvaluator= new ExpressionEvaluator();
        fCodeReaderFactory= readerFactory;

        setupMacroDictionary(configuration, info);		
                
        ILocationCtx ctx= fLocationMap.pushTranslationUnit(new String(reader.filename), reader.buffer);	
        fRootLexer= new Lexer(reader.buffer, (LexerOptions) fLexOptions.clone(), this);
        fRootContext= fCurrentContext= new ScannerContextFile(ctx, null, fRootLexer);

        if (info instanceof IExtendedScannerInfo) {
        	final IExtendedScannerInfo einfo= (IExtendedScannerInfo) info;
        	
        	// files provided on command line (-imacros, -include)
        	registerPreIncludedFiles(einfo.getMacroFiles(), einfo.getIncludeFiles());
        }
    }
    
	public void setContentAssistMode(int offset) {
		fRootLexer.setContentAssistMode(offset);
	}


    // mstodo keywords should be provided directly by the language
	private void configureKeywords(ParserLanguage language,	IScannerExtensionConfiguration configuration) {
		if (language == ParserLanguage.C) {
        	Keywords.addKeywordsC(fKeywords);
        }
        else {
        	Keywords.addKeywordsCpp(fKeywords);
        }
        CharArrayIntMap additionalKeywords= configuration.getAdditionalKeywords();
        if (additionalKeywords != null) {
        	fKeywords.putAll(additionalKeywords);
        }
        additionalKeywords= configuration.getAdditionalPreprocessorKeywords();
        if (additionalKeywords != null) {
        	fPPKeywords.putAll(additionalKeywords);
        }
	}

    protected String getCurrentFilename() {
		return fLocationMap.getCurrentFilename();
	}

	private char[] nonNull(char[] array) {
		return array == null ? EMPTY_CHAR_ARRAY : array;
	}

    private String[] getQuoteIncludePath(IScannerInfo info) {
        if (info instanceof IExtendedScannerInfo) {
        	final IExtendedScannerInfo einfo= (IExtendedScannerInfo) info;
            final String[] qip= einfo.getLocalIncludePath();
            if (qip != null && qip.length > 0) {
            	final String[] result= new String[qip.length + fIncludePaths.length];
            	System.arraycopy(qip, 0, result, 0, qip.length);
            	System.arraycopy(fIncludePaths, 0, result, qip.length, fIncludePaths.length);
            	return result;
            }
        }
        return info.getIncludePaths();
	}

    private void setupMacroDictionary(IScannerExtensionConfiguration config, IScannerInfo info) {
    	// built in macros
        fMacroDictionary.put(__STDC__.getNameCharArray(), __STDC__);
        fMacroDictionary.put(__FILE__.getNameCharArray(), __FILE__);
        fMacroDictionary.put(__DATE__.getNameCharArray(), __DATE__);
        fMacroDictionary.put(__TIME__.getNameCharArray(), __TIME__);
        fMacroDictionary.put(__LINE__.getNameCharArray(), __LINE__);

        if (fLanguage == ParserLanguage.CPP)
            fMacroDictionary.put(__cplusplus.getNameCharArray(), __cplusplus);
        else {
            fMacroDictionary.put(__STDC_HOSTED__.getNameCharArray(), __STDC_HOSTED__);
            fMacroDictionary.put(__STDC_VERSION__.getNameCharArray(), __STDC_VERSION__);
        }

        CharArrayObjectMap toAdd = config.getAdditionalMacros();
        for (int i = 0; i < toAdd.size(); ++i) {
        	addDefinition((IMacro) toAdd.getAt(i));
        }
        
        // macros provided on command-line (-D)
        final boolean initEmptyMacros= config.initializeMacroValuesTo1();
        final Map macroDict= info.getDefinedSymbols();
        if (macroDict != null) {
        	for (Iterator iterator = macroDict.entrySet().iterator(); iterator.hasNext();) {
				final Map.Entry entry = (Map.Entry) iterator.next();
				final String key= (String) entry.getKey();
				final String value= ((String) entry.getValue()).trim();
				if (initEmptyMacros && value.length() == 0) {
					addMacroDefinition(key.toCharArray(), ONE);
				}
				else {
					addMacroDefinition(key.toCharArray(), value.toCharArray());
                }
            }
        }
        
        Object[] predefined= fMacroDictionary.valueArray();
        for (int i = 0; i < predefined.length; i++) {
        	fLocationMap.registerPredefinedMacro((PreprocessorMacro) predefined[i]);
		}
    }

    private void registerPreIncludedFiles(final String[] macroFiles, final String[] preIncludedFiles) {
    	if (preIncludedFiles != null && preIncludedFiles.length > 0) {
    		final char[] buffer= createSyntheticFile(preIncludedFiles);
    		ILocationCtx ctx= fLocationMap.pushPreInclusion(buffer, 0, false);
    		fCurrentContext= new ScannerContextFile(ctx, fCurrentContext, new Lexer(buffer, fLexOptions, this));
    	}
    	
    	if (macroFiles != null && macroFiles.length > 0) {
    		final char[] buffer= createSyntheticFile(macroFiles);
    		ILocationCtx ctx= fLocationMap.pushPreInclusion(buffer, 0, true);
    		fCurrentContext= new ScannerContextMacroFile(this, ctx, fCurrentContext, new Lexer(buffer, fLexOptions, this));
    	}
    }

	private char[] createSyntheticFile(String[] files) {
		int totalLength= 0;
    	final char[] instruction= "#include <".toCharArray(); //$NON-NLS-1$
    	for (int i = 0; i < files.length; i++) {
    		totalLength+= instruction.length + 2 + files[i].length();
    	}
    	final char[] buffer= new char[totalLength];
    	int pos= 0;
    	for (int i = 0; i < files.length; i++) {
    		final char[] fileName= files[i].toCharArray();
    		System.arraycopy(instruction, 0, buffer, pos, instruction.length);
    		pos+= instruction.length;
    		System.arraycopy(fileName, 0, buffer, pos, fileName.length);
    		pos+= fileName.length;
    		buffer[pos++]= '>';
    		buffer[pos++]= '\n';
    	}
    	return buffer;
	}
    
//    private boolean isCircularInclusion(InclusionData data) {
//    	// mstodo
//        for (int i = 0; i < bufferStackPos; ++i) {
//            if (bufferData[i] instanceof CodeReader
//                    && CharArrayUtils.equals(
//                            ((CodeReader) bufferData[i]).filename,
//                            data.reader.filename)) {
//                return true;
//            } else if (bufferData[i] instanceof InclusionData
//                    && CharArrayUtils
//                            .equals(
//                                    ((InclusionData) bufferData[i]).reader.filename,
//                                    data.reader.filename)) {
//                return true;
//            }
//        }
//        return false;
//    }



	/**
     * Check if the given inclusion was already included before.
     * 
	 * @param inclusionData
	 * @return
	 */
//	private boolean isRepeatedInclusion(InclusionData inclusionData) {
//        return includedFiles.containsKey(inclusionData.reader.filename);
//	}

    public PreprocessorMacro addMacroDefinition(char[] key, char[] value) {
     	final Lexer lex= new Lexer(key, fLexOptions, LEXERLOG_NULL);
    	try {
    		PreprocessorMacro result= fMacroDefinitionParser.parseMacroDefinition(lex, LEXERLOG_NULL, value);
    		fLocationMap.registerPredefinedMacro(result);
	    	fMacroDictionary.put(result.getNameCharArray(), result);
	    	return result;
    	}
    	catch (Exception e) {
    		fLog.traceLog("Invalid macro definition: '" + String.valueOf(key) + "'");     //$NON-NLS-1$//$NON-NLS-2$
    		return null;
    	}
    }
  
    public int getCount() {
        return fTokenCount;
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
        return fMacroDictionary;
    }

    public String[] getIncludePaths() {
        return fIncludePaths;
    }

    public boolean isOnTopContext() {
    	return fCurrentContext == fRootContext;
    }

    public synchronized void cancel() {
    	isCancelled= true;
    }

    /**
     * Returns next token for the parser. 
     * @throws OffsetLimitReachedException 
     */
    public IToken nextToken() throws EndOfFileException {
        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }
        
        // use prefetched token or get a new one.
    	Token t1= fPrefetchedToken;
    	if (t1 == null) {
    		t1= fetchTokenFromPreprocessor();
    	}
    	else {
    		fPrefetchedToken= null;
    	}
    	
    	final int tt1= t1.getType();
    	switch(tt1) {
    	case Lexer.tEND_OF_INPUT:
    		if (fContentAssistLimit < 0) {
    			throw EOF;
    		}
			t1= new SimpleToken(IToken.tEOC, fContentAssistLimit, fContentAssistLimit);
    		break;
    	case IToken.tSTRING:
    	case IToken.tLSTRING:
    		boolean isWide= tt1 == IToken.tLSTRING;
    		Token t2;
    		StringBuffer buf= null;
    		int endOffset= 0;
    		loop: while(true) {
    			t2= fetchTokenFromPreprocessor();
    			final int tt2= t2.getType();
    			switch(tt2) {
    			case IToken.tLSTRING:
    				isWide= true;
    				// no break;
    			case IToken.tSTRING:
    				if (buf == null) {
    					buf= new StringBuffer();
    					appendStringContent(buf, t1);
    				}
    				appendStringContent(buf, t2);
    				endOffset= t2.getEndOffset();
    				continue loop;
    				
    			default:
    				break loop;
    			}
    		}
    		fPrefetchedToken= t2;
    		if (buf != null) {
    			char[] image= new char[buf.length() + (isWide ? 3 : 2)];
    			int off= -1;
    			if (isWide) {
    				image[++off]= 'L';
    			}
    			image[++off]= '"';
    			buf.getChars(0, buf.length(), image, ++off);
    			image[image.length-1]= '"';
    			t1= new ImageToken((isWide ? IToken.tLSTRING : IToken.tSTRING), t1.getOffset(), endOffset, image);
    		}
    	}

    	if (fLastToken != null) {
    		fLastToken.setNext(t1);
    	}
    	fLastToken= t1;
    	return t1;
    }

    private void appendStringContent(StringBuffer buf, Token t1) {
    	final char[] image= t1.getCharImage();
    	final int start= image[0]=='"' ? 1 : 2;
    	buf.append(image, start, image.length-start);
	}

	Token fetchTokenFromPreprocessor() throws OffsetLimitReachedException {
        ++fTokenCount;
        Token ppToken= fCurrentContext.currentPPToken();
        while(true) {
			switch(ppToken.getType()) {
        	case Lexer.tBEFORE_INPUT:
        	case Lexer.tNEWLINE:
        		ppToken= fCurrentContext.nextPPToken();
        		continue;

        	case Lexer.tEND_OF_INPUT:
            	final ILocationCtx locationCtx = fCurrentContext.getLocationCtx();
            	if (locationCtx != null) {
            		fLocationMap.popContext(locationCtx);
            	}
        		fCurrentContext= fCurrentContext.getParent();
        		if (fCurrentContext == null) {
        			return ppToken;
        		}
            	
        		ppToken= fCurrentContext.currentPPToken();
        		continue;

            case IToken.tPOUND:
            	final Lexer lexer= fCurrentContext.getLexerForPPDirective();
            	if (lexer != null) {
            		executeDirective(lexer, ppToken.getOffset());
            		continue;
            	}
            	break;
        	
        	case IToken.tIDENTIFIER:
        		if (fCurrentContext.expandsMacros() && expandMacro(ppToken, true)) {
        			continue;
        		}

        		final char[] name= ppToken.getCharImage();
        		int tokenType = fKeywords.get(name);
            	if (tokenType != fKeywords.undefined) {
            		ppToken.setType(tokenType);
            	}
            	break;
        		
        	case IToken.tINTEGER:
        		if (fCheckNumbers) {
        			checkNumber(ppToken, false);
        		}
        		break;

        	case IToken.tFLOATINGPT:
        		if (fCheckNumbers) {
        			checkNumber(ppToken, true);
        		}
        		break;
        	}
        	fCurrentContext.nextPPToken();
        	return ppToken;
        }
    }

    private void checkNumber(Token number, final boolean isFloat) {
        final char[] image= number.getCharImage();
        boolean hasExponent = false;

        boolean isHex = false;
        boolean isOctal = false;
        boolean hasDot= false;

        int pos= 0;
        if (image.length > 1) {
        	if (image[0] == '0') {
        		switch (image[++pos]) {
        		case 'x':
        		case 'X':
        			isHex = true;
        			++pos;
        			break;
        		case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
        			isOctal = true;
        			++pos;
        			break;
        		case '8': case '9':
        			handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, image, number.getOffset(), number.getEndOffset());
        			return;
        		}
        	}
        }

        loop: for (; pos < image.length; pos++) {
            switch (image[pos]) {
            // octal digits
            case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': 
            	continue;
            	
            // decimal digits
            case '8': case '9': 
            	if (isOctal) {
        			handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, image, number.getOffset(), number.getEndOffset());
        			return;
            	}
                continue;
                
            // hex digits
            case 'a': case 'A': case 'b': case 'B': case 'c': case 'C': case 'd': case 'D': case 'f': case 'F':
                if (isHex && !hasExponent) {
                    continue;
                }
                break loop;

            case '.':
                if (hasDot) {
                    handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, image, number.getOffset(), number.getEndOffset());
                    return;
                }
                hasDot= true;
                continue;

            // check for exponent or hex digit
            case 'E': case 'e':
                if (isHex && !hasExponent) {
                    continue;
                }
                if (isFloat && !isHex && !hasExponent && pos+1 >= image.length) {
                	switch (image[pos+1]) {
                	case '+': case '-':
                	case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                		hasExponent = true;
                		++pos;
                		continue;
                	}
                }
                break loop;

            // check for hex float exponent
            case 'p': case 'P':
                if (isFloat && isHex && !hasExponent && pos+1 >= image.length) {
                	switch (image[pos+1]) {
                	case '+': case '-':
                	case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                		hasExponent = true;
                		++pos;
                		continue;
                	}
                }
                break loop;

            default:
            	break loop;
            }
        }
        
        // check the suffix
        loop: for (; pos < image.length; pos++) {
        	final char c= image[pos];
            switch (c) {
            case 'u': case 'U': case 'L': case 'l':
            	continue;
            case 'f': case 'F':
            	if (isFloat) {
            		continue loop;
            	}
            }
            for (int i=0; i<fAdditionalNumericLiteralSuffixes.length; i++) {
				if (fAdditionalNumericLiteralSuffixes[i] == c) {
					continue loop;
				}
			}
            if (isFloat) {
            	handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, image, number.getOffset(), number.getEndOffset());
            }
            else if (isHex) {
            	handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT, image, number.getOffset(), number.getEndOffset());
            }
            else if (isOctal) {
            	handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, image, number.getOffset(), number.getEndOffset());
            }
            else {
            	handleProblem(IProblem.SCANNER_BAD_DECIMAL_FORMAT, image, number.getOffset(), number.getEndOffset());
            }
            return;
        }
    }

    private CodeReader findInclusion(final String filename, final boolean quoteInclude, 
    		final boolean includeNext, final File currentDir) {
    	return (CodeReader) findInclusion(filename, quoteInclude, includeNext, currentDir, createCodeReaderTester);
    }

    private Object findInclusion(final String filename, final boolean quoteInclude, 
    		final boolean includeNext, final File currentDirectory, final IIncludeFileTester tester) {
        Object reader = null;
		// filename is an absolute path or it is a Linux absolute path on a windows machine
		if (new File(filename).isAbsolute() || filename.startsWith("/")) {  //$NON-NLS-1$
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
        String[] includePathsToUse = quoteInclude ? fQuoteIncludePaths : fIncludePaths;
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

    public String toString() {
        StringBuffer buffer = new StringBuffer("Scanner @ file:");  //$NON-NLS-1$
        buffer.append(fCurrentContext.toString());
        buffer.append(" line: ");  //$NON-NLS-1$
        buffer.append(fLocationMap.getCurrentLineNumber(fCurrentContext.currentPPToken().getOffset()));
        return buffer.toString();
    }
	
    
    public Object createMacro(char[] name, char[][] parameters, char[] expansion) {
    	return fMacroDefinitionParser.parseMacroDefinition(name, parameters, expansion);
    }
    
    public void addMacroDefinition(Object macro, String filename, int nameOffset, int nameEndOffset, int expansionOffset) {
    	if (!(macro instanceof PreprocessorMacro)) {
    		throw new IllegalArgumentException();
    	}
    	PreprocessorMacro pm= (PreprocessorMacro) macro;
    	fLocationMap.registerMacroFromIndex(pm, filename, nameOffset, nameEndOffset, expansionOffset);
    	fMacroDictionary.put(pm.getNameCharArray(), pm);
    }
    
    public ILocationResolver getLocationMap() {
    	return fLocationMap;
    }

	public void handleComment(boolean isBlockComment, int offset, int endOffset) {
		fLocationMap.encounteredComment(offset, endOffset, isBlockComment);
	}

    public void handleProblem(int id,  char[] arg, int offset, int endOffset) {
        fLocationMap.encounterProblem(id, arg, offset, endOffset);
    }

    private CodeReader createReader(String path, String fileName){
        String finalPath = ScannerUtility.createReconciledPath(path, fileName);
        CodeReader reader = fCodeReaderFactory.createCodeReaderForInclusion(this, finalPath);
        return reader;
    }
    
	
    /**
     * Assumes that the pound token has not yet been consumed
     * @param ppdCtx 
     * @param startOffset offset in current file
     * @since 5.0
     */
    private void executeDirective(final Lexer lexer, final int startOffset) throws OffsetLimitReachedException {
    	final Token ident= lexer.nextToken();
    	switch (ident.getType()) {
    	case IToken.tCOMPLETION:
    		lexer.nextToken();
    		throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, ident);
    		
    	case Lexer.tNEWLINE:
    		return;

    	case Lexer.tEND_OF_INPUT:
    	case IToken.tINTEGER:
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		return;

    	case IToken.tIDENTIFIER:
    		break;

    	default:
    		int endOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, ident.getCharImage(), startOffset, endOffset);
    		return;
    	}

    	// we have an identifier
    	final char[] name = ident.getCharImage();
    	final int type = fPPKeywords.get(name);
    	switch (type) {
    	case IPreprocessorDirective.ppImport:
    	case IPreprocessorDirective.ppInclude:
    		executeInclude(lexer, startOffset, false, true);
    		break;
    	case IPreprocessorDirective.ppInclude_next:
    		executeInclude(lexer, startOffset, true, true);
    		break;
    	case IPreprocessorDirective.ppDefine:
    		executeDefine(lexer, startOffset);
    		break;
    	case IPreprocessorDirective.ppUndef:
    		executeUndefine(lexer, startOffset);
    		break;
    	case IPreprocessorDirective.ppIfdef:
    		executeIfdef(lexer, startOffset, true);
    		break;
    	case IPreprocessorDirective.ppIfndef:
    		executeIfdef(lexer, startOffset, false);
    		break;
    	case IPreprocessorDirective.ppIf: 
    		executeIf(lexer, startOffset);
    		break;
    	case IPreprocessorDirective.ppElse: 
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		int endOffset= lexer.currentToken().getEndOffset();
    		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_ELSE)) {
    			fLocationMap.encounterPoundElse(startOffset, endOffset, false);
    			skipOverConditionalCode(lexer, false);
    		} 
    		else {
    			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, startOffset, endOffset);
    		}
    		break;
    	case IPreprocessorDirective.ppElif: 
    		int condOffset= lexer.nextToken().getOffset();
    		int condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		endOffset= lexer.currentToken().getEndOffset();
    		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_ELIF)) {
    			fLocationMap.encounterPoundElif(startOffset, condOffset, condEndOffset, endOffset, false);
    			skipOverConditionalCode(lexer, false);
    		} 
    		else {
    			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, startOffset, endOffset);
    		}
    		break;
    	case IPreprocessorDirective.ppEndif:
    		condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		endOffset= lexer.currentToken().getEndOffset();
    		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_END)) {
    			fLocationMap.encounterPoundEndIf(startOffset, endOffset);
    		} 
    		else {
    			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, startOffset, endOffset);
    		}
    		break;
    	case IPreprocessorDirective.ppWarning: 
    	case IPreprocessorDirective.ppError:
    		condOffset= lexer.nextToken().getOffset();
    		condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		endOffset= lexer.currentToken().getEndOffset();
    		final char[] warning= lexer.getInputChars(condOffset, endOffset);
    		final int id= type == IPreprocessorDirective.ppError 
    				? IProblem.PREPROCESSOR_POUND_ERROR 
    				: IProblem.PREPROCESSOR_POUND_WARNING;
    		handleProblem(id, warning, startOffset, endOffset); 
    		fLocationMap.encounterPoundError(startOffset, condOffset, condEndOffset, endOffset);
    		break;
    	case IPreprocessorDirective.ppPragma: 
    		condOffset= lexer.nextToken().getOffset();
    		condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		endOffset= lexer.currentToken().getEndOffset();
    		fLocationMap.encounterPoundPragma(startOffset, condOffset, condEndOffset, endOffset);
    		break;
    	case IPreprocessorDirective.ppIgnore:
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		break;
    	default:
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			endOffset= lexer.currentToken().getEndOffset();
    		handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, name, startOffset, endOffset);
    		break;
    	}
    }

	private void executeInclude(final Lexer lexer, int poundOffset, boolean include_next, boolean active) throws OffsetLimitReachedException {
		char[] headerName= null;
		boolean userInclude= true;

		lexer.setInsideIncludeDirective();
		final Token header= lexer.nextToken();
		final int nameOffset= header.getOffset();
		int nameEndOffset= header.getEndOffset();
		int endOffset;
		
		switch(header.getType()) {
		case Lexer.tSYSTEM_HEADER_NAME:
			userInclude= false;
			char[] image= header.getCharImage();
			headerName= new char[image.length-2];
			System.arraycopy(image, 1, headerName, 0, headerName.length);
			lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			endOffset= lexer.currentToken().getEndOffset();
			break;
			
		case Lexer.tQUOTE_HEADER_NAME:
			image= header.getCharImage();
			headerName= new char[image.length-2];
			System.arraycopy(image, 1, headerName, 0, headerName.length);
			lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			endOffset= lexer.currentToken().getEndOffset();
			break;

		case IToken.tCOMPLETION:
			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, header);
			
		case IToken.tIDENTIFIER: 
			Token tokens= new SimpleToken(0,0,0);
			nameEndOffset= getPreprocessedTokensOfLine(lexer, tokens);
			endOffset= lexer.currentToken().getEndOffset();
			tokens= (Token) tokens.getNext();
			if (tokens != null) {
				switch(tokens.getType()) {
				case IToken.tSTRING:
					image= tokens.getCharImage();
					headerName= new char[image.length-2];
					System.arraycopy(image, 1, headerName, 0, headerName.length);
					break;
				case IToken.tLT:
					boolean complete= false;
					StringBuffer buf= new StringBuffer();
					tokens= (Token) tokens.getNext();
					while (tokens != null) {
						if (tokens.getType() == IToken.tGT) {
							complete= true;
							break;
						}
						buf.append(tokens.getImage());
						tokens= (Token) tokens.getNext();
					}
					if (!complete) {
						throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, null);
					}
					headerName= new char[buf.length()];
					buf.getChars(0, buf.length(), headerName, 0);
				}
			}
			break;
			
		default:
			endOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			break;
		}
		if (headerName == null || headerName.length==0) {
	    	if (active) {
	            handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE,
	            		lexer.getInputChars(poundOffset, endOffset), poundOffset, endOffset);
	    	}
			return;
		}

		CodeReader reader= null;
		if (active) {
			final File currentDir= userInclude || include_next ? new File(String.valueOf(getCurrentFilename())).getParentFile() : null;
			reader= findInclusion(new String(headerName), userInclude, include_next, currentDir);
			if (reader != null) {
				ILocationCtx ctx= fLocationMap.pushInclusion(poundOffset, nameOffset, nameEndOffset, endOffset, reader.buffer, new String(reader.filename), headerName, userInclude);
				ScannerContextFile fctx= new ScannerContextFile(ctx, fCurrentContext, new Lexer(reader.buffer, fLexOptions, this));
				fCurrentContext= fctx;
			}
			else {
				handleProblem(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, headerName, poundOffset, endOffset);
			}
		}
		else {
			final File currentDir= userInclude || include_next ? new File(String.valueOf(getCurrentFilename())).getParentFile() : null;
			String path= (String) findInclusion(new String(headerName), userInclude, include_next, currentDir, createPathTester);
			if (path != null) {
				if (fCodeReaderFactory instanceof IIndexBasedCodeReaderFactory) {
					// fast indexer
					if (!((IIndexBasedCodeReaderFactory) fCodeReaderFactory).hasFileBeenIncludedInCurrentTranslationUnit(path)) {
						path= null;
					}
				}
				else {
					// full indexer
					if (!includedFiles.containsKey(path.toCharArray())) {
						path= null;
					}
				}
			}
			fLocationMap.encounterPoundInclude(poundOffset, nameOffset, nameEndOffset, endOffset, headerName, path, !userInclude, active); 
		}
	}

    private void executeDefine(final Lexer lexer, int startOffset) throws OffsetLimitReachedException {
		try {
			ObjectStyleMacro macrodef = fMacroDefinitionParser.parseMacroDefinition(lexer, this);
			fMacroDictionary.put(macrodef.getNameCharArray(), macrodef);
			final Token name= fMacroDefinitionParser.getNameToken();
			fLocationMap.encounterPoundDefine(startOffset, name.getOffset(), name.getEndOffset(), 
					fMacroDefinitionParser.getExpansionOffset(), fMacroDefinitionParser.getExpansionEndOffset(),
					macrodef);
		} catch (InvalidMacroDefinitionException e) {
			int end= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, e.fName, startOffset, end);
		}
    }

    private void executeUndefine(Lexer lexer, int startOffset) throws OffsetLimitReachedException {
		final Token name= lexer.nextToken();
    	final int tt= name.getType();
    	if (tt != IToken.tIDENTIFIER) {
    		if (tt == IToken.tCOMPLETION) {
    			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, name);
    		}
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, name.getCharImage(), startOffset, name.getEndOffset());
    		return;
    	}
		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    	final int endOffset= lexer.currentToken().getEndOffset();
    	final char[] namechars= name.getCharImage();
    	PreprocessorMacro definition= (PreprocessorMacro) fMacroDictionary.remove(namechars, 0, namechars.length);
    	fLocationMap.encounterPoundUndef(definition, startOffset, name.getOffset(), name.getEndOffset(), endOffset, namechars);
    }

    private void executeIfdef(Lexer lexer, int startOffset, boolean positive) throws OffsetLimitReachedException {
		final Token name= lexer.nextToken();
    	final int tt= name.getType();
    	if (tt != IToken.tIDENTIFIER) {
    		if (tt == IToken.tCOMPLETION) {
    			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, name);
    		}
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		handleProblem(IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND, name.getCharImage(), startOffset, name.getEndOffset());
    		return;
    	}
		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
		final int endOffset= lexer.currentToken().getEndOffset();
    	final char[] namechars= name.getCharImage();
        boolean isActive= (fMacroDictionary.get(namechars) != null) == positive;
        
    	fCurrentContext.changeBranch(ScannerContext.BRANCH_IF);        
        if (positive) {
        	fLocationMap.encounterPoundIfdef(startOffset, name.getOffset(), name.getEndOffset(), endOffset, isActive);
        }
        else {
        	fLocationMap.encounterPoundIfndef(startOffset, name.getOffset(), name.getEndOffset(), endOffset, isActive);
        }

        if (!isActive) {
        	skipOverConditionalCode(lexer, true);
        }
    }

    private void executeIf(Lexer lexer, int startOffset) throws OffsetLimitReachedException {
    	boolean isActive= false;
    	Token condition= new SimpleToken(0,0,0);
    	final int condOffset= lexer.nextToken().getOffset();
    	final int condEndOffset= getPreprocessedTokensOfLine(lexer, condition);
    	final int endOffset= lexer.currentToken().getEndOffset();
    	
    	condition= (Token) condition.getNext();
		if (condition != null) {
    		try {
				isActive= fExpressionEvaluator.evaluate(condition, fMacroDictionary);
			} catch (EvalException e) {
				handleProblem(e.getProblemID(), e.getProblemArg(), condOffset, endOffset);
			}
    	}

		fCurrentContext.changeBranch(ScannerContext.BRANCH_IF);
    	fLocationMap.encounterPoundIf(startOffset, condOffset, condEndOffset, endOffset, isActive);
		
    	if (!isActive) {
    		skipOverConditionalCode(lexer, true);
    	} 
    }
    
    /**
     * Runs the preprocessor on the rest of the line, storing the tokens in the holder supplied.
     * Macro expansion is reported to the location map.
     * Returns the end-offset of the last token used from the input.
     */
    private int getPreprocessedTokensOfLine(Lexer lexer, Token resultHolder) throws OffsetLimitReachedException {
		final ScannerContext sctx= fCurrentContext;
		final ScannerContextPPDirective ppdCtx= new ScannerContextPPDirective(lexer, true);
		fCurrentContext= ppdCtx;
		try {
			Token t= fetchTokenFromPreprocessor();
			while (t.getType() != Lexer.tEND_OF_INPUT) {
				resultHolder.setNext(t);
				resultHolder= t;
				t= fetchTokenFromPreprocessor();
			}
			// make sure an exception is thrown if we are running content assist at the end of the line
			lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			return ppdCtx.getLastEndOffset();
		}
		finally {
			fCurrentContext= sctx;
		}
    }
    
    private void skipOverConditionalCode(final Lexer lexer, boolean takeElseBranch) throws OffsetLimitReachedException {
    	int nesting= 0;
    	while(true) {
    		final Token pound= lexer.nextDirective();
    		int tt= pound.getType();
    		if (tt != IToken.tPOUND) {
    			if (tt == IToken.tCOMPLETION) {
    				throw new OffsetLimitReachedException(ORIGIN_INACTIVE_CODE, pound); // completion in inactive code
    			}
    			return;
    		}
        	final Token ident= lexer.nextToken();
        	tt= ident.getType();
        	if (tt != IToken.tIDENTIFIER) {
        		if (tt == IToken.tCOMPLETION) {
        			throw new OffsetLimitReachedException(ORIGIN_INACTIVE_CODE, ident);	// completion in inactive directive
        		}
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		continue;
        	}
        	
        	// we have an identifier
        	final char[] name = ident.getCharImage();
        	final int type = fPPKeywords.get(name);
        	switch (type) {
        	case IPreprocessorDirective.ppImport:
        	case IPreprocessorDirective.ppInclude:
        		executeInclude(lexer, ident.getOffset(), false, false);
        		break;
        	case IPreprocessorDirective.ppInclude_next:
        		executeInclude(lexer, ident.getOffset(), true, false);
        		break;
        	case IPreprocessorDirective.ppIfdef:
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		int endOffset= lexer.currentToken().getEndOffset();
        		nesting++;
        		fCurrentContext.changeBranch(ScannerContext.BRANCH_IF);
        		fLocationMap.encounterPoundIfdef(pound.getOffset(), ident.getOffset(), ident.getEndOffset(), endOffset, false);
        		break;
        	case IPreprocessorDirective.ppIfndef:
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		endOffset= lexer.currentToken().getEndOffset();
        		nesting++;
        		fCurrentContext.changeBranch(ScannerContext.BRANCH_IF);
        		fLocationMap.encounterPoundIfndef(pound.getOffset(), ident.getOffset(), ident.getEndOffset(), endOffset, false);
        		break;
        	case IPreprocessorDirective.ppIf: 
        		int condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		endOffset= lexer.currentToken().getEndOffset();
        		nesting++;
        		fCurrentContext.changeBranch(ScannerContext.BRANCH_IF);
        		fLocationMap.encounterPoundIf(pound.getOffset(), ident.getOffset(), condEndOffset, endOffset, false);
        		break;
        	case IPreprocessorDirective.ppElse: 
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		endOffset= lexer.currentToken().getEndOffset();
        		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_ELSE)) {
        			boolean isActive= nesting == 0 && takeElseBranch;
        			fLocationMap.encounterPoundElse(pound.getOffset(), endOffset, isActive);
        			if (isActive) {
        				return;
        			}
        		} 
        		else {
        			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, pound.getOffset(), endOffset);
        		}
        		break;
        	case IPreprocessorDirective.ppElif: 
        		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_ELIF)) {
        	    	boolean isActive= false;
        	    	int condOffset= lexer.nextToken().getOffset();
        			if (nesting == 0 && takeElseBranch) {
            	    	Token condition= new SimpleToken(0,0,0);
        				condEndOffset= getPreprocessedTokensOfLine(lexer, condition);
        				condition= (Token) condition.getNext();
        				if (condition != null) {
        					try {
        						isActive= fExpressionEvaluator.evaluate(condition, fMacroDictionary);
        					} catch (EvalException e) {
        						handleProblem(e.getProblemID(), e.getProblemArg(), condOffset, condEndOffset);
        					}
        				}
        			}
        			else {
        				condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        			}
        			endOffset= lexer.currentToken().getEndOffset();
        			fCurrentContext.changeBranch(ScannerContext.BRANCH_ELIF);
        	    	fLocationMap.encounterPoundElif(pound.getOffset(), condOffset, condEndOffset, endOffset, isActive);
        			
        	    	if (isActive) {
        	    		return;
        	    	} 
        		} 
        		else {
            		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
            		endOffset= lexer.currentToken().getEndOffset();
        			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, pound.getOffset(), endOffset);
        		}
        		break;
        	case IPreprocessorDirective.ppEndif:
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		endOffset= lexer.currentToken().getEndOffset();
        		if (fCurrentContext.changeBranch(ScannerContext.BRANCH_END)) {
        			fLocationMap.encounterPoundEndIf(pound.getOffset(), endOffset);
            		if (nesting == 0) {
            			return;
            		}
            		--nesting;
        		} 
        		else {
        			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, pound.getOffset(), endOffset);
        		}
        		break;
        	default:
            	lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
        		break;
        	}
    	}
    }
    
	/**
	 * The method assumes that the identifier is not yet consumed.
	 * <p>
	 * Checks whether the identifier causes a macro expansion. May advance the current lexer
	 * to check for the opening bracket succeeding the identifier.
	 * <p>
	 * If applicable the macro is expanded and the resulting tokens are put into a scanner context. 
	 * @param identifier the token where macro expansion may occur.
	 * @param multiline whether we are allowed to check subsequent lines for macro arguments.
	 * @return
	 * @throws OffsetLimitReachedException 
	 */
	private boolean expandMacro(final Token identifier, boolean multiline) throws OffsetLimitReachedException {
		final char[] name= identifier.getCharImage();
        PreprocessorMacro macro= (PreprocessorMacro) fMacroDictionary.get(name);
        if (macro == null) {
        	return false;
        }
        
        Token bracket= null;
        if (macro instanceof FunctionStyleMacro) {
        	bracket= fCurrentContext.nextPPToken();
        	if (multiline) {
        		while(bracket.getType() == Lexer.tNEWLINE) {
        			bracket= fCurrentContext.nextPPToken();
        		}
        	}
        	if (bracket.getType() != IToken.tLPAREN) {
        		return false;
        	}
        }
        
        // mstodo really expand the macro
        return true;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(fLocationMap.getClass())) {
			return fLocationMap;
		}
		return null;
	}

	// stuff to be removed
	public IMacro addDefinition(char[] key, char[] value) {
    	throw new UnsupportedOperationException();
	}
	public void setScanComments(boolean val) {
    	throw new UnsupportedOperationException();
	}
	public char[] getMainFilename() {
    	throw new UnsupportedOperationException();
	}
    public void addDefinition(IMacro macro) {
    	throw new UnsupportedOperationException();
    }
	public IMacro addDefinition(char[] name, char[][] params, char[] expansion) {
    	throw new UnsupportedOperationException();
    }
	public void setASTFactory(IASTFactory f) {
    	throw new UnsupportedOperationException();
	}
	public org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver getLocationResolver() {
    	throw new UnsupportedOperationException();
	}
	public void setOffsetBoundary(int offset) {
    	throw new UnsupportedOperationException();
	}

//    private int skipOverMacroArg() {
//        char[] buffer = bufferStack[bufferStackPos];
//        int limit = bufferLimit[bufferStackPos];
//        int argEnd = bufferPos[bufferStackPos]--;
//        int nesting = 0;
//        while (++bufferPos[bufferStackPos] < limit) {
//            switch (buffer[bufferPos[bufferStackPos]]) {
//            case '(':
//                ++nesting;
//                break;
//            case ')':
//                if (nesting == 0) {
//                    --bufferPos[bufferStackPos];
//                    return argEnd;
//                }
//                --nesting;
//                break;
//            case ',':
//                if (nesting == 0) {
//                    --bufferPos[bufferStackPos];
//                    return argEnd;
//                }
//                break;
//            // fix for 95119
//            case '\'':
//                boolean escapedChar = false;
//                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
//                    switch (buffer[bufferPos[bufferStackPos]]) {
//                    case '\\':
//                        escapedChar = !escapedChar;
//                        continue;
//                    case '\'':
//                        if (escapedChar) {
//                            escapedChar = false;
//                            continue;
//                        }
//                        break loop;
//                    default:
//                       escapedChar = false;
//                    }
//                }
//                break;
//            case '"':
//                boolean escaped = false;
//                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
//                    switch (buffer[bufferPos[bufferStackPos]]) {
//                    case '\\':
//                        escaped = !escaped;
//                        continue;
//                    case '"':
//                        if (escaped) {
//                            escaped = false;
//                            continue;
//                        }
//                        break loop;
//                    default:
//                        escaped = false;
//                    }
//                }
//                break;
//            }
//            argEnd = bufferPos[bufferStackPos];
//            skipOverWhiteSpace();
//        }
//        --bufferPos[bufferStackPos];
//        // correct argEnd when reaching limit, (bug 179383)
//        if (argEnd==limit) {
//        	argEnd--;
//        }
//        return argEnd;
//    }
//
//
//    private char[] expandFunctionStyleMacro(FunctionStyleMacro macro) {
//        char[] buffer = bufferStack[bufferStackPos];
//        int limit = bufferLimit[bufferStackPos];
//        int start = bufferPos[bufferStackPos] - macro.name.length + 1;
//        skipOverWhiteSpace();
//        while (bufferPos[bufferStackPos] < limit
//                && buffer[bufferPos[bufferStackPos]] == '\\'
//                && bufferPos[bufferStackPos] + 1 < buffer.length
//                && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
//            bufferPos[bufferStackPos] += 2;
//            skipOverWhiteSpace();
//        }
//
//        if (++bufferPos[bufferStackPos] >= limit) {
//            //allow a macro boundary cross here, but only if the caller was
//            // prepared to accept a bufferStackPos change
//            if (pushContext) {
//                int idx = -1;
//                int stackpPos = bufferStackPos;
//                while (bufferData[stackpPos] != null
////                        && bufferData[stackpPos] instanceof MacroData
//                        ) {
//                    stackpPos--;
//                    if (stackpPos < 0)
//                        return EMPTY_CHAR_ARRAY;
//                    idx = indexOfNextNonWhiteSpace(bufferStack[stackpPos],
//                            bufferPos[stackpPos], bufferLimit[stackpPos]);
//                    if (idx >= bufferLimit[stackpPos])
//                        continue;
//                    if (idx > 0 && bufferStack[stackpPos][idx] == '(')
//                        break;
//                    bufferPos[bufferStackPos]--;
//                    return null;
//                }
//                if (idx == -1) {
//                    bufferPos[bufferStackPos]--;
//                    return null;
//                }
//
////                MacroData data;
//                IMacro popMacro= macro;
//                do {
////                	data= (MacroData) bufferData[bufferStackPos];
//                    popContextForFunctionMacroName(popMacro);
//                	popMacro= data.macro;
//                } while (bufferStackPos > stackpPos);
//
//                bufferPos[bufferStackPos] = idx;
//                buffer = bufferStack[bufferStackPos];
//                limit = bufferLimit[bufferStackPos];
//                start = data.startOffset;
//            } else {
//                bufferPos[bufferStackPos]--;
//                return null;
//            }
//        }
//
//        // fix for 107150: the scanner stops at the \n or \r after skipOverWhiteSpace() take that into consideration
//        while (bufferPos[bufferStackPos] + 1 < limit && (buffer[bufferPos[bufferStackPos]] == '\n' || buffer[bufferPos[bufferStackPos]] == '\r')) {
//        	bufferPos[bufferStackPos]++; // skip \n or \r
//        	skipOverWhiteSpace(); // skip any other spaces after the \n
//        	
//        	if (bufferPos[bufferStackPos] + 1 < limit && buffer[bufferPos[bufferStackPos]] != '(' && buffer[bufferPos[bufferStackPos] + 1] == '(')
//        		bufferPos[bufferStackPos]++; // advance to ( if necessary
//        }
//
//        if (buffer[bufferPos[bufferStackPos]] != '(') {
//            bufferPos[bufferStackPos]--;
//            return null;
//        }
//
//        char[][] arglist = macro.arglist;
//        int currarg = 0;
//        CharArrayObjectMap argmap = new CharArrayObjectMap(arglist.length);
//
//        while (bufferPos[bufferStackPos] < limit) {
//            skipOverWhiteSpace();
//
//            if (bufferPos[bufferStackPos] + 1 >= limit)
//            	break;
//            
//            if (buffer[++bufferPos[bufferStackPos]] == ')') {
//                if (currarg > 0 && argmap.size() <= currarg) {
//                    argmap.put(arglist[currarg], EMPTY_CHAR_ARRAY);
//                }
//                break;	// end of macro
//            }
//            if (buffer[bufferPos[bufferStackPos]] == ',') {
//                if (argmap.size() <= currarg) {
//                    argmap.put(arglist[currarg], EMPTY_CHAR_ARRAY);
//                }
//            	currarg++;
//                continue;
//            }
//
//            if ((currarg >= arglist.length || arglist[currarg] == null)
//                    && !macro.hasVarArgs() && !macro.hasGCCVarArgs()) {
//                // too many args and no variable argument
//                handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
//                        bufferPos[bufferStackPos], macro.name);
//                break;
//            }
//
//            int argstart = bufferPos[bufferStackPos];
//
//            int argend = -1;
//            if ((macro.hasGCCVarArgs() || macro.hasVarArgs()) && currarg == macro.getVarArgsPosition()) {
//                // there are varargs and the other parameters have been accounted
//                // for, the rest will replace __VA_ARGS__ or name where
//                // "name..." is the parameter
//                for (;;) {
//                	argend= skipOverMacroArg();
//                    skipOverWhiteSpace();
//                    // to continue we need at least a comma and another char.
//                    if (bufferPos[bufferStackPos]+2 >= limit) { 
//                    	break;
//                    }
//                    if (buffer[++bufferPos[bufferStackPos]] == ')') {
//                    	bufferPos[bufferStackPos]--;
//                    	break;
//                    }
//                    // it's a comma
//                    bufferPos[bufferStackPos]++;
//                } 
//            } else {
//                argend = skipOverMacroArg();
//            }
//            
//            char[] arg = EMPTY_CHAR_ARRAY;
//            int arglen = argend - argstart + 1;
//            if (arglen > 0) {
//                arg = new char[arglen];
//                System.arraycopy(buffer, argstart, arg, 0, arglen);
//            }
//
//            argmap.put(arglist[currarg], arg);
//        }
//
//        int numRequiredArgs = arglist.length;
//        for (int i = 0; i < arglist.length; i++) {
//            if (arglist[i] == null) {
//            	numRequiredArgs = i;
//                break;
//            }
//        }
//
//        /* Don't require a match for the vararg placeholder */
//        /* Workaround for bugzilla 94365 */
//        if (macro.hasGCCVarArgs()|| macro.hasVarArgs())
//        	numRequiredArgs--;
// 
//        if (argmap.size() < numRequiredArgs) {
//            handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
//                    bufferPos[bufferStackPos], macro.name);
//        }
//
//        char[] result = null;
//        if (macro instanceof DynamicFunctionStyleMacro) {
//            result = ((DynamicFunctionStyleMacro) macro).execute(argmap);
//        } else {
//            CharArrayObjectMap replacedArgs = new CharArrayObjectMap(argmap
//                    .size());
//            int size = expandFunctionStyleMacro(macro.getExpansion(), argmap,
//                    replacedArgs, null);
//            result = new char[size];
//            expandFunctionStyleMacro(macro.getExpansion(), argmap, replacedArgs,
//                    result);
//        }
//        if (pushContext)
//        {
////            pushContext(result, new FunctionMacroData(start, bufferPos[bufferStackPos] + 1,
////        		macro, argmap));
//        }
//        return result;
//    }
//
//	private char[] replaceArgumentMacros(char[] arg) {
//        int limit = arg.length;
//        int start = -1, end = -1;
//        Object expObject = null;
//        for (int pos = 0; pos < limit; pos++) {
//            char c = arg[pos];
//            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
//                    || Character.isLetter(c)
//                    || (support$Initializers && c == '$')) {
//                start = pos;
//                while (++pos < limit) {
//                    c = arg[pos];
//                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
//                            || c == '_' || (c >= '0' && c <= '9')
//                            || (support$Initializers && c == '$')
//                            || Character.isUnicodeIdentifierPart(c)) {
//                        continue;
//                    }
//                    break;
//                }
//                end = pos - 1;
//            }
//            else if (c == '"') {
//            	boolean escaped= false;
//                while (++pos < limit) {
//                    c = arg[pos];
//                    if (!escaped && c == '"') {
//                    	break;
//                    }
//                    if (c == '\\') {
//                    	escaped= !escaped;
//                    }
//                    else {
//                    	escaped= false;
//                    }
//                }
//            }
//            else if (c == '\'') {
//            	boolean escaped= false;
//                while (++pos < limit) {
//                    c = arg[pos];
//                    if (!escaped && c == '\'') {
//                    	break;
//                    }
//                    if (c == '\\') {
//                    	escaped= !escaped;
//                    }
//                    else {
//                    	escaped= false;
//                    }
//                }
//            }
//
//            if (start != -1 && end >= start) {
//                //Check for macro expansion
//                expObject = fMacroDictionary.get(arg, start, (end - start + 1));
//                if (expObject == null || !shouldExpandMacro((IMacro) expObject)) {
//                    expObject = null;
//                    start = -1;
//                    continue;
//                }
//                //else, break and expand macro
//                break;
//            }
//        }
//
//        if (expObject == null)
//        {
//            return arg;
//        }
//        
//
//        char[] expansion = null;
//        if (expObject instanceof FunctionStyleMacro) {
//            FunctionStyleMacro expMacro = (FunctionStyleMacro) expObject;
//            pushContext((start == 0) ? arg : CharArrayUtils.extract(arg, start,
//                    arg.length - start));
//            bufferPos[bufferStackPos] += end - start + 1;
//            expansion = handleFunctionStyleMacro(expMacro, false);
//            end = bufferPos[bufferStackPos] + start;
//            popContext();
//        } else if (expObject instanceof ObjectStyleMacro) {
//            ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
//            expansion = expMacro.getExpansion();
//        } else if (expObject instanceof char[]) {
//            expansion = (char[]) expObject;
//        } else if (expObject instanceof DynamicStyleMacro) {
//            DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
//            expansion = expMacro.execute();
//        }
//
//        if (expansion != null) {
//            int newlength = start + expansion.length + (limit - end - 1);
//            char[] result = new char[newlength];
//            System.arraycopy(arg, 0, result, 0, start);
//            System.arraycopy(expansion, 0, result, start, expansion.length);
//            if (arg.length > end + 1)
//                System.arraycopy(arg, end + 1, result,
//                        start + expansion.length, limit - end - 1);
//
//            
//            beforeReplaceAllMacros();
//            //we need to put the macro on the context stack in order to detect
//            // recursive macros
////            pushContext(EMPTY_CHAR_ARRAY,
////                    new MacroData(start, start
////                            + ((IMacro) expObject).getName().length,
////                            (IMacro) expObject)
////            );
//            arg = replaceArgumentMacros(result); //rescan for more macros
//            popContext();
//            afterReplaceAllMacros();
//        }
//        
//        return arg;
//    }
//
//	
//    private int expandFunctionStyleMacro(char[] expansion,
//            CharArrayObjectMap argmap, CharArrayObjectMap replacedArgs,
//            char[] result) {
//
//        // The current position in the expansion string that we are looking at
//        int pos = -1;
//        // The last position in the expansion string that was copied over
//        int lastcopy = -1;
//        // The current write offset in the result string - also tells us the
//        // length of the result string
//        int outpos = 0;
//        // The first character in the current block of white space - there are
//        // times when we don't
//        // want to copy over the whitespace
//        int wsstart = -1;
//        //whether or not we are on the second half of the ## operator
//        boolean prevConcat = false;
//        //for handling ##
//        char[] prevArg = null;
//        int prevArgStart = -1;
//        int prevArgLength = -1;
//        int prevArgTarget = 0;
//
//        int limit = expansion.length;
//
//        while (++pos < limit) {
//            char c = expansion[pos];
//
//            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
//                    || (c >= '0' && c < '9')
//                    || Character.isUnicodeIdentifierPart(c)) {
//
//                wsstart = -1;
//                int idstart = pos;
//                while (++pos < limit) {
//                    c = expansion[pos];
//                    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
//                            || (c >= '0' && c <= '9') || c == '_' || Character
//                            .isUnicodeIdentifierPart(c))) {
//                        break;
//                    }
//                }
//                --pos;
//
//                char[] repObject = (char[]) argmap.get(expansion, idstart, pos
//                        - idstart + 1);
//
//                int next = indexOfNextNonWhiteSpace(expansion, pos, limit);
//                boolean nextIsPoundPound = (next + 1 < limit
//                        && expansion[next] == '#' && expansion[next + 1] == '#');
//
//                if (prevConcat && prevArgStart > -1 && prevArgLength > 0) {
//                    int l1 = prevArg != null ? prevArg.length : prevArgLength;
//                    int l2 = repObject != null ? repObject.length : pos
//                            - idstart + 1;
//                    char[] newRep = new char[l1 + l2];
//                    if (prevArg != null)
//                        System.arraycopy(prevArg, 0, newRep, 0, l1);
//                    else
//                        System
//                                .arraycopy(expansion, prevArgStart, newRep, 0,
//                                        l1);
//
//                    if (repObject != null)
//                        System.arraycopy(repObject, 0, newRep, l1, l2);
//                    else
//                        System.arraycopy(expansion, idstart, newRep, l1, l2);
//                    idstart = prevArgStart;
//                    repObject = newRep;
//                }
//                if (repObject != null) {
//                    // copy what we haven't so far
//                    if (++lastcopy < idstart) {
//                        int n = idstart - lastcopy;
//                        if (result != null) {
//                        	// the outpos may be set back when prevConcat is true, so make sure we
//                        	// stay in bounds.
//                            if (prevConcat && outpos+n > result.length) {
//                            	n= result.length- outpos;
//                            }
//                            System.arraycopy(expansion, lastcopy, result,
//                                    outpos, n);
//                        }
//                        outpos += n;
//                    }
//
//                    if (prevConcat)
//                        outpos = prevArgTarget;
//
//                    if (!nextIsPoundPound) {
//                        //16.3.1 completely macro replace the arguments before
//                        // substituting them in
//                        char[] rep = (char[]) ((replacedArgs != null) ? replacedArgs
//                                .get(repObject)
//                                : null);
//                        
//                        if (rep != null)
//                            repObject = rep;
//                        else {
//                            rep = replaceArgumentMacros(repObject);
//                            if (replacedArgs != null)
//                                replacedArgs.put(repObject, rep);
//                            repObject = rep;
//                        }
//                      
//                        if (result != null )
//                            System.arraycopy(repObject, 0, result, outpos, repObject.length);
//                    }
//                    outpos += repObject.length;
//
//                    lastcopy = pos;
//                }
//
//                prevArg = repObject;
//                prevArgStart = idstart;
//                prevArgLength = pos - idstart + 1;
//                prevArgTarget = repObject != null ? outpos - repObject.length
//                        : outpos + idstart - lastcopy - 1;
//                prevConcat = false;
//            } else if (c == '"') {
//
//                // skip over strings
//                wsstart = -1;
//                boolean escaped = false;
//                while (++pos < limit) {
//                    c = expansion[pos];
//                    if (c == '"') {
//                        if (!escaped)
//                            break;
//                    } else if (c == '\\') {
//                        escaped = !escaped;
//                    }
//                    escaped = false;
//                }
//                prevConcat = false;
//            } else if (c == '\'') {
//
//                // skip over character literals
//                wsstart = -1;
//                boolean escaped = false;
//                while (++pos < limit) {
//                    c = expansion[pos];
//                    if (c == '\'') {
//                        if (!escaped)
//                            break;
//                    } else if (c == '\\') {
//                        escaped = !escaped;
//                    }
//                    escaped = false;
//                }
//                prevConcat = false;
//            } else if (c == ' ' || c == '\t') {
//                // obvious whitespace
//                if (wsstart < 0)
//                    wsstart = pos;
//            } else if (c == '/' && pos + 1 < limit) {
//
//                // less than obvious, comments are whitespace
//                c = expansion[pos+1];
//                if (c == '/') {
//                    // copy up to here or before the last whitespace
//                	++pos;
//                    ++lastcopy;
//                    int n = wsstart < 0 ? pos - 1 - lastcopy : wsstart
//                            - lastcopy;
//                    if (result != null)
//                        System
//                                .arraycopy(expansion, lastcopy, result, outpos,
//                                        n);
//                    outpos += n;
//
//                    // skip the rest
//                    lastcopy = expansion.length - 1;
//                } else if (c == '*') {
//                	++pos;
//                    if (wsstart < 1)
//                        wsstart = pos - 1;
//                    while (++pos < limit) {
//                        if (expansion[pos] == '*' && pos + 1 < limit
//                                && expansion[pos + 1] == '/') {
//                            ++pos;
//                            break;
//                        }
//                    }
//                } else
//                    wsstart = -1;
//
//            } else if (c == '\\' && pos + 1 < limit
//                    && expansion[pos + 1] == 'n') {
//                // skip over this
//                ++pos;
//
//            } else if (c == '#') {
//
//                if (pos + 1 < limit && expansion[pos + 1] == '#') {
//                    prevConcat = true;
//                    ++pos;
//                    // skip whitespace
//                    if (wsstart < 0)
//                        wsstart = pos - 1;
//                    while (++pos < limit) {
//                        switch (expansion[pos]) {
//                        case ' ':
//                        case '\t':
//                            continue;
//
//                        case '/':
//                            if (pos + 1 < limit) {
//                                c = expansion[pos + 1];
//                                if (c == '/')
//                                    // skip over everything
//                                    pos = expansion.length;
//                                else if (c == '*') {
//                                    ++pos;
//                                    while (++pos < limit) {
//                                        if (expansion[pos] == '*'
//                                                && pos + 1 < limit
//                                                && expansion[pos + 1] == '/') {
//                                            ++pos;
//                                            break;
//                                        }
//                                    }
//                                    continue;
//                                }
//                            }
//                        }
//                        break;
//                    }
//                    --pos;
//                } else {
//                    prevConcat = false;
//                    // stringify
//
//                    // copy what we haven't so far
//                    if (++lastcopy < pos) {
//                        int n = pos - lastcopy;
//                        if (result != null)
//                            System.arraycopy(expansion, lastcopy, result,
//                                    outpos, n);
//                        outpos += n;
//                    }
//
//                    // skip whitespace
//                    while (++pos < limit) {
//                        switch (expansion[pos]) {
//                        case ' ':
//                        case '\t':
//                            continue;
//                        case '/':
//                            if (pos + 1 < limit) {
//                                c = expansion[pos + 1];
//                                if (c == '/')
//                                    // skip over everything
//                                    pos = expansion.length;
//                                else if (c == '*') {
//                                    ++pos;
//                                    while (++pos < limit) {
//                                        if (expansion[pos] == '*'
//                                                && pos + 1 < limit
//                                                && expansion[pos + 1] == '/') {
//                                            ++pos;
//                                            break;
//                                        }
//                                    }
//                                    continue;
//                                }
//                            }
//                        //TODO handle comments
//                        }
//                        break;
//                    }
//
//                    // grab the identifier
//                    c = expansion[pos];
//                    int idstart = pos;
//                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X')
//                            || c == '_' || Character.isUnicodeIdentifierPart(c)) {
//                        while (++pos < limit) {
//                            c = expansion[pos];
//                            if (!((c >= 'a' && c <= 'z')
//                                    || (c >= 'A' && c <= 'X')
//                                    || (c >= '0' && c <= '9') || c == '_' || Character
//                                    .isUnicodeIdentifierPart(c)))
//                                break;
//                        }
//                    } // else TODO something
//                    --pos;
//                    int idlen = pos - idstart + 1;
//                    char[] argvalue = (char[]) argmap.get(expansion, idstart,
//                            idlen);
//                    if (argvalue != null) {
//                        //16.3.2-2 ... a \ character is inserted before each "
//                        // and \
//                        // character
//                        //of a character literal or string literal
//
//                        //technically, we are also supposed to replace each
//                        // occurence
//                        // of whitespace
//                        //(including comments) in the argument with a single
//                        // space.
//                        // But, at this time
//                        //we don't really care what the contents of the string
//                        // are,
//                        // just that we get the string
//                        //so we won't bother doing that
//                        if (result != null) {
//                            result[outpos++] = '"';
//                            for (int i = 0; i < argvalue.length; i++) {
//                                if (argvalue[i] == '"' || argvalue[i] == '\\')
//                                    result[outpos++] = '\\';
//                                if (argvalue[i] == '\r' || argvalue[i] == '\n')
//                                    result[outpos++] = ' ';
//                                else
//                                    result[outpos++] = argvalue[i];
//                            }
//                            result[outpos++] = '"';
//                        } else {
//                            for (int i = 0; i < argvalue.length; i++) {
//                                if (argvalue[i] == '"' || argvalue[i] == '\\')
//                                    ++outpos;
//                                ++outpos;
//                            }
//                            outpos += 2;
//                        }
//                    }
//                    lastcopy = pos;
//                    wsstart = -1;
//                }
//            } else {
//                prevConcat = false;
//                // not sure what it is but it sure ain't whitespace
//                wsstart = -1;
//            }
//
//        }
//
//        if (wsstart < 0 && ++lastcopy < expansion.length) {
//            int n = expansion.length - lastcopy;
//            if (result != null)
//                System.arraycopy(expansion, lastcopy, result, outpos, n);
//            outpos += n;
//        }
//
//        return outpos;
//    }
}
