/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.EmptyFilesProvider;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.MacroDefinitionParser.InvalidMacroDefinitionException;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerContext.BranchKind;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerContext.CodeState;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerContext.Conditional;
import org.eclipse.core.runtime.IAdaptable;

/**
 * C-Preprocessor providing tokens for the parsers. The class should not be used directly, rather than that
 * you should be using the {@link IScanner} interface.
 * @since 5.0
 */
public class CPreprocessor implements ILexerLog, IScanner, IAdaptable {
	public static final String PROP_VALUE = "CPreprocessor"; //$NON-NLS-1$

	public static final int tDEFINED= IToken.FIRST_RESERVED_PREPROCESSOR;
	public static final int tEXPANDED_IDENTIFIER= IToken.FIRST_RESERVED_PREPROCESSOR+1;
	public static final int tSCOPE_MARKER= IToken.FIRST_RESERVED_PREPROCESSOR+2;
	public static final int tSPACE= IToken.FIRST_RESERVED_PREPROCESSOR+3;
	public static final int tNOSPACE= IToken.FIRST_RESERVED_PREPROCESSOR+4;
	public static final int tMACRO_PARAMETER= IToken.FIRST_RESERVED_PREPROCESSOR+5;

	private static final int ORIGIN_PREPROCESSOR_DIRECTIVE = OffsetLimitReachedException.ORIGIN_PREPROCESSOR_DIRECTIVE;
	private static final int ORIGIN_INACTIVE_CODE = OffsetLimitReachedException.ORIGIN_INACTIVE_CODE;
	
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private static final char[] ONE = "1".toCharArray(); //$NON-NLS-1$


    // standard built-ins
    private static final ObjectStyleMacro __CDT_PARSER__= new ObjectStyleMacro("__CDT_PARSER__".toCharArray(), ONE);   //$NON-NLS-1$
    private static final ObjectStyleMacro __cplusplus = new ObjectStyleMacro("__cplusplus".toCharArray(), ONE);   //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC__ = new ObjectStyleMacro("__STDC__".toCharArray(), ONE);  //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC_HOSTED__ = new ObjectStyleMacro("__STDC_HOSTED__".toCharArray(), ONE);  //$NON-NLS-1$
    private static final ObjectStyleMacro __STDC_VERSION__ = new ObjectStyleMacro("__STDC_VERSION__".toCharArray(), "199901L".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    private static final DynamicMacro __FILE__= new FileMacro("__FILE__".toCharArray()); //$NON-NLS-1$
    private static final DynamicMacro __DATE__= new DateMacro("__DATE__".toCharArray()); //$NON-NLS-1$
    private static final DynamicMacro __TIME__ = new TimeMacro("__TIME__".toCharArray()); //$NON-NLS-1$
    private static final DynamicMacro __LINE__ = new LineMacro("__LINE__".toCharArray()); //$NON-NLS-1$

	private static final int NO_EXPANSION 		 = 0x01;
	private static final int PROTECT_DEFINED 	 = 0x02;
	private static final int STOP_AT_NL 		 = 0x04;
	private static final int CHECK_NUMBERS 		 = 0x08;
	private static final Token END_OF_INPUT = new Token(IToken.tEND_OF_INPUT, null, 0, 0);

	private interface IIncludeFileTester<T> {
    	T checkFile(String path, boolean isHeuristicMatch, IncludeSearchPathElement onPath);
    }

	final private IIncludeFileTester<InternalFileContent> createCodeReaderTester= new IIncludeFileTester<InternalFileContent>() {
    	public InternalFileContent checkFile(String path, boolean isHeuristicMatch, IncludeSearchPathElement onPath) {
			final InternalFileContent fc= fFileContentProvider.getContentForInclusion(path);
			if (fc != null) {
				fc.setFoundByHeuristics(isHeuristicMatch);
				fc.setFoundOnPath(onPath);
			}
			return fc;
    	}
    };

    private static class IncludeResolution {String fLocation; boolean fHeuristic;}
    final private IIncludeFileTester<IncludeResolution> createPathTester= new IIncludeFileTester<IncludeResolution>() {
    	public IncludeResolution checkFile(String path, boolean isHeuristicMatch, IncludeSearchPathElement onPath) {
    		if (fFileContentProvider.getInclusionExists(path)) {
    			IncludeResolution res= new IncludeResolution();
    			res.fHeuristic= isHeuristicMatch;
    			res.fLocation= path;
    			return res;
    		}
    		return null;
    	}
    };

	private final class TokenSequence implements ITokenSequence {
		private final boolean fStopAtNewline;
		TokenSequence(boolean stopAtNewline) {
			fStopAtNewline= stopAtNewline;
		}
		public Token nextToken() throws OffsetLimitReachedException {
       		final Lexer lexer= fCurrentContext.getLexer();
       		Token t= lexer.nextToken();
       		if (t.getType() == IToken.tPOUND && lexer.currentTokenIsFirstOnLine()) {
       			executeDirective(lexer, t.getOffset(), true);
       			t= lexer.currentToken();
       		}
       		if (fStopAtNewline && t.getType() == Lexer.tNEWLINE)
       			return END_OF_INPUT;
       		
       		return t;
		}
		public int getLastEndOffset() {
			return fCurrentContext.getLexer().getLastEndOffset();
		}
		public Token currentToken() {
			Token t= fCurrentContext.currentLexerToken();
       		if (fStopAtNewline && t.getType() == Lexer.tNEWLINE)
       			return END_OF_INPUT;
       		
       		return t;
		}
	}
	TokenSequence fInputToMacroExpansion= new TokenSequence(false);
	TokenSequence fLineInputToMacroExpansion= new TokenSequence(true);

    final private IParserLogService fLog;
    final private InternalFileContentProvider fFileContentProvider;

    private IIncludeFileResolutionHeuristics fIncludeFileResolutionHeuristics;
    private final ExpressionEvaluator fExpressionEvaluator;
	private final MacroDefinitionParser fMacroDefinitionParser;
	private final MacroExpander fMacroExpander;

    // configuration
    final private LexerOptions fLexOptions= new LexerOptions();
    final private char[] fAdditionalNumericLiteralSuffixes;
    final private CharArrayIntMap fKeywords;
    final private CharArrayIntMap fPPKeywords;
    private IncludeSearchPathElement[] fIncludeSearchPath;
    private String[][] fPreIncludedFiles= null;

    private int fContentAssistLimit= -1;
	private boolean fHandledCompletion= false;
	private boolean fSplitShiftRightOperator= false;

    // state information
    private final CharArrayMap<PreprocessorMacro> fMacroDictionary = new CharArrayMap<PreprocessorMacro>(512);
    private final LocationMap fLocationMap;

    /** Set of already included files */
    private final HashSet<String> fAllIncludedFiles= new HashSet<String>();

	private final Lexer fRootLexer;
	private final ScannerContext fRootContext;
	protected ScannerContext fCurrentContext;

    private boolean isCancelled= false;
	private boolean fIsFirstFetchToken= true;

	private Token fPrefetchedTokens;
    private Token fLastToken;

	private InternalFileContent fRootContent;


    public CPreprocessor(FileContent fileContent, IScannerInfo info, ParserLanguage language, IParserLogService log,
            IScannerExtensionConfiguration configuration, IncludeFileContentProvider readerFactory) {
    	if (readerFactory instanceof InternalFileContentProvider) {
        	fFileContentProvider= (InternalFileContentProvider) readerFactory;
    	} else if (readerFactory == null) {
    		fFileContentProvider= EmptyFilesProvider.getInstance();
    	} else {
    		throw new IllegalArgumentException("Illegal reader factory"); //$NON-NLS-1$
    	}
    	if (fileContent instanceof InternalFileContent) {
    		fRootContent= (InternalFileContent) fileContent;
    	} else {
    		throw new IllegalArgumentException("Illegal file content object"); //$NON-NLS-1$
    	}
    		
        fLog = log;
        fAdditionalNumericLiteralSuffixes= nonNull(configuration.supportAdditionalNumericLiteralSuffixes());
        fLexOptions.fSupportDollarInIdentifiers= configuration.support$InIdentifiers();
        fLexOptions.fSupportAtSignInIdentifiers= configuration.supportAtSignInIdentifiers();
        fLexOptions.fSupportMinAndMax = configuration.supportMinAndMaxOperators();
        fLexOptions.fSupportSlashPercentComments= configuration.supportSlashPercentComments();
        fLexOptions.fSupportUTFLiterals = configuration.supportUTFLiterals();
        fLocationMap= new LocationMap(fLexOptions);
        fKeywords= new CharArrayIntMap(40, -1);
        fPPKeywords= new CharArrayIntMap(40, -1);
        configureKeywords(language, configuration);

        fExpressionEvaluator= new ExpressionEvaluator();
        fMacroDefinitionParser= new MacroDefinitionParser();
        fMacroExpander= new MacroExpander(this, fMacroDictionary, fLocationMap, fLexOptions);
        fIncludeFileResolutionHeuristics= fFileContentProvider.getIncludeHeuristics();

        final String filePath= fRootContent.getFileLocation();
        configureIncludeSearchPath(new File(filePath).getParentFile(), info);
        setupMacroDictionary(configuration, info, language);		

        ILocationCtx ctx= fLocationMap.pushTranslationUnit(filePath, fRootContent.getSource());
        fAllIncludedFiles.add(filePath);
    	fFileContentProvider.reportTranslationUnitFile(filePath);
        fRootLexer= new Lexer(fRootContent.getSource(), fLexOptions, this, this);
        fRootContext= fCurrentContext= new ScannerContext(ctx, null, fRootLexer);
        if (info instanceof IExtendedScannerInfo) {
        	final IExtendedScannerInfo einfo= (IExtendedScannerInfo) info;
        	fPreIncludedFiles= new String[][] {einfo.getMacroFiles(), einfo.getIncludeFiles()};
        }
    }

    public void setSplitShiftROperator(boolean val) {
    	fSplitShiftRightOperator= val;
    }

	public void setComputeImageLocations(boolean val) {
    	fLexOptions.fCreateImageLocations= val;
    }

	public void setContentAssistMode(int offset) {
		fContentAssistLimit= offset;
		fRootLexer.setContentAssistMode(offset);
	}
	
	public boolean isContentAssistMode() {
		return fRootLexer.isContentAssistMode();
	}

	public void setProcessInactiveCode(boolean val) {
		fRootContext.setParseInactiveCode(val);
	}

	public void setScanComments(boolean val) {
	}

	public ILocationResolver getLocationResolver() {
		return fLocationMap;
	}

	private void configureKeywords(ParserLanguage language,	IScannerExtensionConfiguration configuration) {
		Keywords.addKeywordsPreprocessor(fPPKeywords);
		if (language == ParserLanguage.C) {
        	Keywords.addKeywordsC(fKeywords);
        } else {
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
		return fLocationMap.getCurrentFilePath();
	}

	private char[] nonNull(char[] array) {
		return array == null ? EMPTY_CHAR_ARRAY : array;
	}

    private void configureIncludeSearchPath(File directory, IScannerInfo info) {
    	String[] searchPath= info.getIncludePaths();
    	int idx= 0;
        if (info instanceof IExtendedScannerInfo) {
        	final IExtendedScannerInfo einfo= (IExtendedScannerInfo) info;
            final String[] quoteIncludeSearchPath= einfo.getLocalIncludePath();
            if (quoteIncludeSearchPath != null && quoteIncludeSearchPath.length > 0) {
            	fIncludeSearchPath= new IncludeSearchPathElement[quoteIncludeSearchPath.length + searchPath.length];
            	for (String qip : quoteIncludeSearchPath) {
					fIncludeSearchPath[idx++]= new IncludeSearchPathElement(makeAbsolute(directory, qip), true);
				}
            }
        }
        if (fIncludeSearchPath == null) {
        	fIncludeSearchPath= new IncludeSearchPathElement[searchPath.length];
        }
        for (String path : searchPath) {
			fIncludeSearchPath[idx++]= new IncludeSearchPathElement(makeAbsolute(directory, path), false);
		}
	}

	private String makeAbsolute(File directory, String inlcudePath) {
		if (directory == null || new File(inlcudePath).isAbsolute()) {
			return inlcudePath;
		}
		return ScannerUtility.createReconciledPath(directory.getAbsolutePath(), inlcudePath);
	}

	private void setupMacroDictionary(IScannerExtensionConfiguration config, IScannerInfo info, ParserLanguage lang) {
    	// built in macros
    	fMacroDictionary.put(__CDT_PARSER__.getNameCharArray(), __CDT_PARSER__);
        fMacroDictionary.put(__STDC__.getNameCharArray(), __STDC__);
        fMacroDictionary.put(__FILE__.getNameCharArray(), __FILE__);
        fMacroDictionary.put(__DATE__.getNameCharArray(), __DATE__);
        fMacroDictionary.put(__TIME__.getNameCharArray(), __TIME__);
        fMacroDictionary.put(__LINE__.getNameCharArray(), __LINE__);

        if (lang == ParserLanguage.CPP) {
            fMacroDictionary.put(__cplusplus.getNameCharArray(), __cplusplus);
        } else {
            fMacroDictionary.put(__STDC_HOSTED__.getNameCharArray(), __STDC_HOSTED__);
            fMacroDictionary.put(__STDC_VERSION__.getNameCharArray(), __STDC_VERSION__);
        }

        IMacro[] toAdd = config.getAdditionalMacros();
        if (toAdd != null) {
        	for (final IMacro macro : toAdd) {
        		addMacroDefinition(macro.getSignature(), macro.getExpansion());
        	}
        }

        final Map<String, String> macroDict= info.getDefinedSymbols();
        if (macroDict != null) {
        	for (Map.Entry<String, String> entry : macroDict.entrySet()) {
				final String key= entry.getKey();
				final String value= entry.getValue().trim();
				addMacroDefinition(key.toCharArray(), value.toCharArray());
            }
        }

        Collection<PreprocessorMacro> predefined= fMacroDictionary.values();
        for (PreprocessorMacro macro : predefined) {
        	fLocationMap.registerPredefinedMacro(macro);
		}
    }

	private void beforeFirstFetchToken() {
		if (fPreIncludedFiles != null) {
    		handlePreIncludedFiles();
    	}
		final String location = fLocationMap.getTranslationUnitPath();
		InternalFileContent content= fFileContentProvider.getContentForContextToHeaderGap(location);
		if (content != null && content.getKind() == InclusionKind.FOUND_IN_INDEX) {
			processInclusionFromIndex(0, location, content);
		}
		fLocationMap.replacingFile(fFileContentProvider, fRootContent);
		fRootContent= null;
	}

    private void handlePreIncludedFiles() {
    	final String[] imacro= fPreIncludedFiles[0];
    	if (imacro != null && imacro.length > 0) {
    		final char[] buffer= createSyntheticFile(imacro);
    		ILocationCtx ctx= fLocationMap.pushPreInclusion(new CharArray(buffer), 0, true);
    		fCurrentContext= new ScannerContext(ctx, fCurrentContext, new Lexer(buffer, fLexOptions, this, this));
    		ScannerContext preCtx= fCurrentContext;
    		try {
				while (internalFetchToken(preCtx, CHECK_NUMBERS, false).getType() != IToken.tEND_OF_INPUT) {
					// just eat the tokens
				}
            	final ILocationCtx locationCtx = fCurrentContext.getLocationCtx();
            	fLocationMap.popContext(locationCtx);
        		fCurrentContext= fCurrentContext.getParent();
        		assert fCurrentContext == fRootContext;
			} catch (OffsetLimitReachedException e) {
			}
    	}
    	final String[] include= fPreIncludedFiles[1];
    	if (include != null && include.length > 0) {
    		final char[] buffer= createSyntheticFile(include);
    		ILocationCtx ctx= fLocationMap.pushPreInclusion(new CharArray(buffer), 0, false);
    		fCurrentContext= new ScannerContext(ctx, fCurrentContext, new Lexer(buffer, fLexOptions, this, this));
    	}
    	fPreIncludedFiles= null;
    }

	private char[] createSyntheticFile(String[] files) {
		int totalLength= 0;
    	final char[] instruction= "#include <".toCharArray(); //$NON-NLS-1$
    	for (String file : files) {
    		totalLength+= instruction.length + 2 + file.length();
    	}
    	final char[] buffer= new char[totalLength];
    	int pos= 0;
    	for (String file : files) {
    		final char[] fileName= file.toCharArray();
    		System.arraycopy(instruction, 0, buffer, pos, instruction.length);
    		pos+= instruction.length;
    		System.arraycopy(fileName, 0, buffer, pos, fileName.length);
    		pos+= fileName.length;
    		buffer[pos++]= '>';
    		buffer[pos++]= '\n';
    	}
    	return buffer;
	}

    public PreprocessorMacro addMacroDefinition(char[] key, char[] value) {
     	final Lexer lex= new Lexer(key, fLexOptions, ILexerLog.NULL, null);
    	try {
    		PreprocessorMacro result= fMacroDefinitionParser.parseMacroDefinition(lex, ILexerLog.NULL, value);
    		fLocationMap.registerPredefinedMacro(result);
	    	fMacroDictionary.put(result.getNameCharArray(), result);
	    	return result;
    	} catch (Exception e) {
    		fLog.traceLog("Invalid macro definition: '" + String.valueOf(key) + "'");     //$NON-NLS-1$//$NON-NLS-2$
    		return null;
    	}
    }

    public Map<String, IMacroBinding> getMacroDefinitions() {
        Map<String, IMacroBinding> hashMap = new HashMap<String, IMacroBinding>(fMacroDictionary.size());
        for (char[] key : fMacroDictionary.keys()) {
            hashMap.put(String.valueOf(key), fMacroDictionary.get(key));
		}
        return hashMap;
    }

    public boolean isOnTopContext() {
    	ScannerContext ctx= fCurrentContext;
    	while (ctx != null && ctx.getLocationCtx() instanceof LocationCtxMacroExpansion) {
    		ctx= ctx.getParent();
    	}
    	return ctx == fRootContext;
    }

    public void cancel() {
    	isCancelled= true;
    }

    /**
     * Returns the next token from the preprocessor without concatenating string literals
     * and also without splitting the shift-right operator.
     */
    private Token fetchToken() throws OffsetLimitReachedException {
    	if (fIsFirstFetchToken) {
    		beforeFirstFetchToken();
    		fIsFirstFetchToken= false;
    	}
    	Token t= fPrefetchedTokens;
    	if (t != null) {
    		fPrefetchedTokens= (Token) t.getNext();
    		t.setNext(null);
    		return t;
    	}
    	
    	try {
			t= internalFetchToken(fRootContext, CHECK_NUMBERS, false);
		} catch (OffsetLimitReachedException e) {
			fHandledCompletion= true;
			throw e;
		}
    	final int offset= fLocationMap.getSequenceNumberForOffset(t.getOffset());
		final int endOffset= fLocationMap.getSequenceNumberForOffset(t.getEndOffset());
		t.setOffset(offset, endOffset);
		t.setNext(null);
    	return t;
    }

    private void pushbackToken(Token t) {
    	t.setNext(fPrefetchedTokens);
    	fPrefetchedTokens= t;
    }

    /**
     * Returns next token for the parser. String literals are not concatenated. When
     * the end is reached tokens with type {@link IToken#tEND_OF_INPUT}.
     * @throws OffsetLimitReachedException see {@link Lexer}.
     */
    public IToken nextTokenRaw() throws OffsetLimitReachedException {
        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }

    	Token t1= fetchToken();
    	switch (t1.getType()) {
    	case IToken.tCOMPLETION:
    		fHandledCompletion= true;
    		break;
    	case IToken.tEND_OF_INPUT:
    		if (fContentAssistLimit >= 0) {
        		int useType= fHandledCompletion ? IToken.tEOC : IToken.tCOMPLETION;
    			int sequenceNumber= fLocationMap.getSequenceNumberForOffset(fContentAssistLimit);
    			t1= new Token(useType, null, sequenceNumber, sequenceNumber);
    			fHandledCompletion= true;
    		}
    		break;
    	case IToken.t_PRAGMA:
    		handlePragmaOperator(t1);
    		return nextTokenRaw();
    	}
    	if (fLastToken != null) {
    		fLastToken.setNext(t1);
    	}
    	fLastToken= t1;
    	return t1;
    }

	private void handlePragmaOperator(Token t1) throws OffsetLimitReachedException {
		Token t2= fetchToken();
		int end;
		if (t2.getType() == IToken.tLPAREN) {
			Token t3= fetchToken();
			end= t3.getEndOffset();
			final int tt = t3.getType();
			if (tt == IToken.tSTRING || tt == IToken.tLSTRING || tt == IToken.tUTF16STRING || tt == IToken.tUTF32STRING) {
				Token t4= fetchToken();
				end= t4.getEndOffset();
				if (t4.getType() == IToken.tRPAREN) {
					fLocationMap.encounterPragmaOperator(t1.getOffset(), t3.getOffset(), t3.getEndOffset(), t4.getEndOffset());
					return;
				} else {
					end= t3.getEndOffset();
					pushbackToken(t4);
				}
			} else {
				if (t3.getType() == IToken.tRPAREN) {
					// Consume closing parenthesis
					end= t3.getEndOffset();
				} else {
					end= t2.getEndOffset();
					pushbackToken(t3);
				}
			}
		} else {
			end= t1.getEndOffset();
			pushbackToken(t2);
		} 
		fLocationMap.encounterProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, t1.getCharImage(), t1.getOffset(), end);
	}

    /**
     * Returns next token for the parser. String literals are concatenated.
     * @throws EndOfFileException when the end of the translation unit has been reached.
     * @throws OffsetLimitReachedException see {@link Lexer}.
     */
    public IToken nextToken() throws EndOfFileException {
        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }

    	Token t1= fetchToken();
    	
    	final int tt1= t1.getType();
    	switch (tt1) {
    	case IToken.tCOMPLETION:
    		fHandledCompletion= true;
    		break;
    		
    	case IToken.tEND_OF_INPUT:
    		if (fContentAssistLimit < 0) {
    			throw new EndOfFileException(t1.getOffset());
    		}
    		int useType= fHandledCompletion ? IToken.tEOC : IToken.tCOMPLETION;
    		int sequenceNumber= fLocationMap.getSequenceNumberForOffset(fContentAssistLimit);
    		t1= new Token(useType, null, sequenceNumber, sequenceNumber);
    		fHandledCompletion= true;
    		break;
    		
    	case IToken.t_PRAGMA:
    		handlePragmaOperator(t1);
    		return nextToken();
    		
    	case IToken.tSTRING:
    	case IToken.tLSTRING:
        case IToken.tUTF16STRING:
        case IToken.tUTF32STRING:
    		StringType st = StringType.fromToken(tt1);
    		Token t2;
    		StringBuffer buf= null;
    		int endOffset= 0;
    		loop: while (true) {
    			t2= fetchToken();
    			final int tt2= t2.getType();
    			switch (tt2) {
    			case IToken.tLSTRING:
    			case IToken.tSTRING:
    		    case IToken.tUTF16STRING:
    		    case IToken.tUTF32STRING:
    				st = StringType.max(st, StringType.fromToken(tt2));
    				if (buf == null) {
    					buf= new StringBuffer();
    					appendStringContent(buf, t1);
    				}
    				appendStringContent(buf, t2);
    				endOffset= t2.getEndOffset();
    				continue loop;
    		    case IToken.tINACTIVE_CODE_START:
    		    	// no support for inactive code after a string literal
    		    	skipInactiveCode();
    		    	continue loop;
    		    case IToken.t_PRAGMA:
    		    	handlePragmaOperator(t2);
    		    	continue loop;
    			default:
    				break loop;
    			}
    		}
    		pushbackToken(t2);
    		if (buf != null) {
    			char[] prefix = st.getPrefix();
    			char[] image= new char[buf.length() + prefix.length + 2];
    			int off= -1;
    			
    			for (char c : prefix)
    				image[++off] = c;
    			
    			image[++off]= '"';
    			buf.getChars(0, buf.length(), image, ++off);
    			image[image.length - 1]= '"';
    			t1= new TokenWithImage(st.getTokenValue(), null, t1.getOffset(), endOffset, image);
    		}
    		break;
    		
        case IToken.tSHIFTR:
        	if (fSplitShiftRightOperator) {
        		int offset= t1.getOffset();
        		endOffset= t1.getEndOffset();
        		
        		t1.setType(IToken.tGT_in_SHIFTR);
        		t1.setOffset(offset, offset+1);
        		t2= new Token(IToken.tGT_in_SHIFTR, t1.fSource, offset+1, endOffset);
        		pushbackToken(t2);
        	}
    	}

    	if (fLastToken != null) {
    		fLastToken.setNext(t1);
    	}
    	fLastToken= t1;
    	return t1;
    }

    public void skipInactiveCode() throws OffsetLimitReachedException {
    	final Lexer lexer= fCurrentContext.getLexer();
    	if (lexer != null) {
    		CodeState state= fCurrentContext.getCodeState();
    		while (state != CodeState.eActive) {
    			state= skipBranch(lexer, false);
    		}
    		fCurrentContext.clearInactiveCodeMarkerToken();
    	}
	}


	public int getCodeBranchNesting() {
		return fCurrentContext.getCodeBranchNesting();
	}

	private void appendStringContent(StringBuffer buf, Token t1) {
    	final char[] image= t1.getCharImage();
    	final int length= image.length;
    	int start = 1;
    	for (char c : image) {
    		if (c == '"')
    			break;
    		start++;
    	}
    	
    	if (length > 1) {
    		final int diff= image[length - 1] == '"' ? length - start - 1 : length - start;
    		if (diff > 0) {
    			buf.append(image, start, diff);
    		}
    	}
	}

	Token internalFetchToken(final ScannerContext uptoEndOfCtx, int options, boolean withinExpansion) throws OffsetLimitReachedException {
        Token ppToken= fCurrentContext.currentLexerToken();
        while (true) {
			switch (ppToken.getType()) {
        	case Lexer.tBEFORE_INPUT:
    			ppToken= fCurrentContext.nextPPToken();
        		continue;
        		
        	case Lexer.tNEWLINE:
        		if ((options & STOP_AT_NL) != 0) {
        			return ppToken;
        		}
        		ppToken= fCurrentContext.nextPPToken();
        		continue;

        	case Lexer.tOTHER_CHARACTER:
        		handleProblem(IProblem.SCANNER_BAD_CHARACTER, ppToken.getCharImage(),
        				ppToken.getOffset(), ppToken.getEndOffset());
        		ppToken= fCurrentContext.nextPPToken();
        		continue;
        		
        	case IToken.tEND_OF_INPUT:
        		if (fCurrentContext == uptoEndOfCtx || uptoEndOfCtx == null) {
        			return ppToken;
        		}
            	final ILocationCtx locationCtx = fCurrentContext.getLocationCtx();
            	fLocationMap.popContext(locationCtx);
        		fCurrentContext= fCurrentContext.getParent();
        		assert fCurrentContext != null;
            	
        		ppToken= fCurrentContext.currentLexerToken();
        		continue;

            case IToken.tPOUND:
               	{
               		final Lexer lexer= fCurrentContext.getLexer();
               		if (lexer != null && lexer.currentTokenIsFirstOnLine()) {
               			executeDirective(lexer, ppToken.getOffset(), withinExpansion);
               			ppToken= fCurrentContext.currentLexerToken();
               			continue;
               		}
               		break;
               	}
        	
        	case IToken.tIDENTIFIER:
        		fCurrentContext.nextPPToken(); // consume the identifier
        		if ((options & NO_EXPANSION) == 0) {
        			final Lexer lexer= fCurrentContext.getLexer();
        			if (lexer != null && expandMacro(ppToken, lexer, options, withinExpansion)) {
        				ppToken= fCurrentContext.currentLexerToken();
        				continue;
        			}

        			final char[] name= ppToken.getCharImage();
        			int tokenType = fKeywords.get(name);
        			if (tokenType != fKeywords.undefined) {
        				ppToken.setType(tokenType);
        			}
        		}
            	return ppToken;
        		
        	case IToken.tINTEGER:
        		if ((options & CHECK_NUMBERS) != 0) {
        			checkNumber(ppToken, false);
        		}
        		break;

        	case IToken.tFLOATINGPT:
        		if ((options & CHECK_NUMBERS) != 0) {
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

        // Integer constants written in binary are a non-standard extension
        // supported by GCC since 4.3 and by some other C compilers
        // They consist of a prefix 0b or 0B, followed by a sequence of 0 and 1 digits
        // see http://gcc.gnu.org/onlinedocs/gcc/Binary-constants.html
        boolean isBin = false;

        boolean isHex = false;
        boolean isOctal = false;
        boolean hasDot= false;

        int pos= 0;
        if (image.length > 1) {
        	if (image[0] == '0') {
        		switch (image[++pos]) {
        		case 'b':
        		case 'B':
        			isBin = true;
        			++pos;
        			break;
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
            if (isBin) {
            	switch (image[pos]) {
                case '0': case'1':
                	continue;
                default:
                	// 0 and 1 are the only allowed digits for binary integers
                	// No floating point, exponents etc. are allowed
                	break loop;
            	}
            }
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
                if (isFloat && !isHex && !hasExponent && pos+1 < image.length) {
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
                if (isFloat && isHex && !hasExponent && pos+1 < image.length) {
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
            if (isBin) {
            	// The check for bin has to come before float, otherwise binary integers
            	// with float components get flagged as BAD_FLOATING_POINT
            	handleProblem(IProblem.SCANNER_BAD_BINARY_FORMAT, image, number.getOffset(), number.getEndOffset());
            } else if (isFloat) {
            	handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, image, number.getOffset(), number.getEndOffset());
            } else if (isHex) {
            	handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT, image, number.getOffset(), number.getEndOffset());
            } else if (isOctal) {
            	handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, image, number.getOffset(), number.getEndOffset());
            } else {
            	handleProblem(IProblem.SCANNER_BAD_DECIMAL_FORMAT, image, number.getOffset(), number.getEndOffset());
            }
            return;
        }
    }

    private <T> T findInclusion(final String includeDirective, final boolean quoteInclude,
    		final boolean includeNext, final String currentFile, final IIncludeFileTester<T> tester) {
        T reader = null;
		// Filename is an absolute path
		if (new File(includeDirective).isAbsolute()) {
		    return tester.checkFile(includeDirective, false, null);
		}
		// Filename is a Linux absolute path on a windows machine
		if (File.separatorChar == '\\' && includeDirective.length() > 0) {
			final char firstChar = includeDirective.charAt(0);
			if (firstChar == '\\' || firstChar == '/') {
				if (currentFile != null && currentFile.length() > 1 && currentFile.charAt(1) == ':') {
					return tester.checkFile(currentFile.substring(0, 2) + includeDirective, false, null);
				}
			    return tester.checkFile(includeDirective, false, null);
			}
		}

        if (currentFile != null && quoteInclude && !includeNext) {
            // Check to see if we find a match in the current directory
    		final File currentDir= new File(currentFile).getParentFile();
    		if (currentDir != null) {
        		final String fileLocation = ScannerUtility.createReconciledPath(currentDir.getAbsolutePath(), includeDirective);
    			reader = tester.checkFile(fileLocation, false, null);
    			if (reader != null) {
    				return reader;
    			}
    		}
        }
        
        // Now we need to search for the file on the include search path.
        // If this is a include_next directive then the search starts with the directory 
        // in the search path after the one where the current file was found.
        IncludeSearchPathElement searchAfter= null;
        if (includeNext && currentFile != null) {
        	searchAfter = fCurrentContext.getFoundOnPath();
        	if (searchAfter == null) {
        		// the current file was found without search path
        		String directive= fCurrentContext.getFoundViaDirective();
        		if (directive == null) {
        			directive= new File(currentFile).getName();
        		}
        		searchAfter = findFileInIncludePath(currentFile, directive);
        	}
        }

        for (IncludeSearchPathElement path : fIncludeSearchPath) {
        	if (searchAfter != null) {
        		if (searchAfter.equals(path)) {
        			searchAfter= null;
        		}
        	} else if (quoteInclude || !path.isForQuoteIncludesOnly()) {
        		String fileLocation = path.getLocation(includeDirective);
        		if (fileLocation != null) {
        			reader= tester.checkFile(fileLocation, false, path);
        			if (reader != null) {
        				return reader;
        			}
        		}
        	}
        }
        if (fIncludeFileResolutionHeuristics != null) {
        	String location= fIncludeFileResolutionHeuristics.findInclusion(includeDirective, currentFile);
        	if (location != null) {
        		return tester.checkFile(location, true, null);
        	}
        }
        return null;
    }

    private IncludeSearchPathElement findFileInIncludePath(String file, String includeDirective) {
        for (IncludeSearchPathElement path : fIncludeSearchPath) {
    		String fileLocation = path.getLocation(includeDirective);
    		if (file.equals(fileLocation)) {
    			return path;
    		}
        }
        return null;
    }

    @Override
	public String toString() {
        StringBuffer buffer = new StringBuffer("Scanner @ file:");  //$NON-NLS-1$
        buffer.append(fCurrentContext.toString());
        buffer.append(" line: ");  //$NON-NLS-1$
        buffer.append(fLocationMap.getCurrentLineNumber(fCurrentContext.currentLexerToken().getOffset()));
        return buffer.toString();
    }

    private void addMacroDefinition(IIndexMacro macro) {
    	try {
    		final char[] expansionImage = macro.getExpansionImage();
    		if (expansionImage == null) {
    			// this is an undef
    			fMacroDictionary.remove(macro.getNameCharArray());
    		} else {
    			PreprocessorMacro result= MacroDefinitionParser.parseMacroDefinition(macro.getNameCharArray(), macro.getParameterList(), expansionImage);
    			final IASTFileLocation loc= macro.getFileLocation();
    			fLocationMap.registerMacroFromIndex(result, loc, -1);
    			fMacroDictionary.put(result.getNameCharArray(), result);
    		}
    	} catch (Exception e) {
    		fLog.traceLog("Invalid macro definition: '" + macro.getName() + "'");     //$NON-NLS-1$//$NON-NLS-2$
    	}
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
    	
    /**
     * Assumes that the pound token has not yet been consumed
     * @since 5.0
     */
    private void executeDirective(final Lexer lexer, final int startOffset, boolean withinExpansion) throws OffsetLimitReachedException {
    	final Token ident= lexer.nextToken();
    	switch (ident.getType()) {
    	case IToken.tCOMPLETION:
    		lexer.nextToken();
    		Token completionToken= new TokenWithImage(ident.getType(), null,
    				startOffset, ident.getEndOffset(), ("#" + ident.getImage()).toCharArray()); //$NON-NLS-1$
    		throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, completionToken);
    		
    	case Lexer.tNEWLINE:
    		return;

    	case IToken.tEND_OF_INPUT:
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
    	int condEndOffset;
    	switch (type) {
    	case IPreprocessorDirective.ppImport:
    	case IPreprocessorDirective.ppInclude:
    		executeInclude(lexer, startOffset, false, fCurrentContext.getCodeState() == CodeState.eActive, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppInclude_next:
    		executeInclude(lexer, startOffset, true, fCurrentContext.getCodeState() == CodeState.eActive, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppDefine:
    		CodeState state= fCurrentContext.getCodeState();
    		if (state == CodeState.eSkipInactive) {
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		} else {
    			executeDefine(lexer, startOffset, state == CodeState.eActive);
    		}
    		break;
    	case IPreprocessorDirective.ppUndef:
    		state= fCurrentContext.getCodeState();
    		if (state == CodeState.eSkipInactive) {
        		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		} else {
        		executeUndefine(lexer, startOffset, fCurrentContext.getCodeState() == CodeState.eActive);
    		}
    		break;
    	case IPreprocessorDirective.ppIfdef:
    		if (executeIfdef(lexer, startOffset, false, withinExpansion) == CodeState.eSkipInactive)
    			skipOverConditionalCode(lexer, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppIfndef:
    		if (executeIfdef(lexer, startOffset, true, withinExpansion) == CodeState.eSkipInactive)
    			skipOverConditionalCode(lexer, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppIf:
    		if (executeIf(lexer, startOffset, false, withinExpansion) == CodeState.eSkipInactive)
    			skipOverConditionalCode(lexer, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppElif:
    		if (executeIf(lexer, startOffset, true, withinExpansion) == CodeState.eSkipInactive) {
    			skipOverConditionalCode(lexer, withinExpansion);
    		}
    		break;
    	case IPreprocessorDirective.ppElse:
    		if (executeElse(lexer, startOffset, withinExpansion) == CodeState.eSkipInactive) {
    			skipOverConditionalCode(lexer, withinExpansion);
    		}
    		break;
    	case IPreprocessorDirective.ppEndif:
    		executeEndif(lexer, startOffset, withinExpansion);
    		break;
    	case IPreprocessorDirective.ppWarning:
    	case IPreprocessorDirective.ppError:
    		int condOffset= lexer.nextToken().getOffset();
    		condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		// Missing argument
			if (condEndOffset < condOffset) {
				condOffset= condEndOffset;
			}
    		if (fCurrentContext.getCodeState() == CodeState.eActive) {
    			int endOffset= lexer.currentToken().getEndOffset();
    			final char[] warning= lexer.getInputChars(condOffset, condEndOffset);
    			final int id= type == IPreprocessorDirective.ppError
    			? IProblem.PREPROCESSOR_POUND_ERROR
    					: IProblem.PREPROCESSOR_POUND_WARNING;
    			handleProblem(id, warning, condOffset, condEndOffset);
    			fLocationMap.encounterPoundError(startOffset, condOffset, condEndOffset, endOffset);
    		}
    		break;
    	case IPreprocessorDirective.ppPragma:
    		condOffset= lexer.nextToken().getOffset();
    		condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		// Missing argument
			if (condEndOffset < condOffset) {
				condOffset= condEndOffset;
			}
    		if (fCurrentContext.getCodeState() == CodeState.eActive) {
    			int endOffset= lexer.currentToken().getEndOffset();
    			fLocationMap.encounterPoundPragma(startOffset, condOffset, condEndOffset, endOffset);
    		}
    		break;
    	case IPreprocessorDirective.ppIgnore:
    		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		break;
    	default:
    		int endOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    		if (fCurrentContext.getCodeState() == CodeState.eActive) {
    			handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, ident.getCharImage(), startOffset, endOffset);
    		}
    		break;
    	}
    }

    private boolean hasFileBeenIncluded(String location) {
    	Boolean itHas= fFileContentProvider.hasFileBeenIncludedInCurrentTranslationUnit(location);
    	if (itHas != null) {
    		return itHas.booleanValue();
    	}
    	return fAllIncludedFiles.contains(location);
    }
    
	private void executeInclude(final Lexer lexer, int poundOffset, boolean include_next, boolean active, boolean withinExpansion) throws OffsetLimitReachedException {
		if (withinExpansion) {
			final char[] name= lexer.currentToken().getCharImage();
			final int endOffset = lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, name, poundOffset, endOffset);
			return;
		}
		
		lexer.setInsideIncludeDirective(true);
		final Token header= lexer.nextToken();
		lexer.setInsideIncludeDirective(false);

		int condEndOffset= header.getEndOffset();
		final int[] nameOffsets= new int[] {header.getOffset(), condEndOffset};
		char[] headerName= null;
		boolean userInclude= true;
		
		switch (header.getType()) {
		case Lexer.tSYSTEM_HEADER_NAME:
			userInclude= false;
			headerName = extractHeaderName(header.getCharImage(), '<', '>', nameOffsets);
			condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			break;
			
		case Lexer.tQUOTE_HEADER_NAME:
			headerName = extractHeaderName(header.getCharImage(), '"', '"', nameOffsets);
			condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			break;

		case IToken.tCOMPLETION:
			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, header);
			
		case IToken.tIDENTIFIER:
			TokenList tl= new TokenList();
			condEndOffset= nameOffsets[1]= getTokensWithinPPDirective(false, tl, false);
			Token t= tl.first();
			if (t != null) {
				switch (t.getType()) {
				case IToken.tSTRING:
					headerName = extractHeaderName(t.getCharImage(), '"', '"', new int[]{0,0});
					break;
				case IToken.tLT:
					userInclude= false;
					boolean complete= false;
					StringBuffer buf= new StringBuffer();
					t= (Token) t.getNext();
					while (t != null) {
						if (t.getType() == IToken.tGT) {
							complete= true;
							break;
						}
						buf.append(t.getImage());
						t= (Token) t.getNext();
					}
					if (complete) {
						headerName= new char[buf.length()];
						buf.getChars(0, buf.length(), headerName, 0);
					}
				}
			}
			break;
			
		default:
			condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			break;
		}
		if (headerName == null || headerName.length == 0) {
	    	if (active) {
	            handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE,
	            		lexer.getInputChars(poundOffset, condEndOffset), poundOffset, condEndOffset);
	    	}
			return;
		}

		String path= null;
		boolean reported= false;
		boolean isHeuristic= false;
		
		final String includeDirective = new String(headerName);
		if (!active) {
			// test if the include is inactive just because it was included before (bug 167100)
			final IncludeResolution resolved= findInclusion(includeDirective, userInclude, include_next,
					getCurrentFilename(), createPathTester);
			if (resolved != null && hasFileBeenIncluded(resolved.fLocation)) {
				path= resolved.fLocation;
				isHeuristic= resolved.fHeuristic;
			}
		} else {
			final InternalFileContent fi= findInclusion(includeDirective, userInclude, include_next,
					getCurrentFilename(), createCodeReaderTester);
			if (fi != null) {
				path= fi.getFileLocation();
				isHeuristic= fi.isFoundByHeuristics();
				switch (fi.getKind()) {
				case FOUND_IN_INDEX:
					processInclusionFromIndex(poundOffset, path, fi);
					break;
				case USE_SOURCE:
					AbstractCharArray source= fi.getSource();
					if (source != null && !isCircularInclusion(path)) {
						reported= true;
						fAllIncludedFiles.add(path);
						ILocationCtx ctx= fLocationMap.pushInclusion(poundOffset, nameOffsets[0], nameOffsets[1],
								condEndOffset, source, path, headerName, userInclude, isHeuristic, fi.isSource());
						ScannerContext fctx= new ScannerContext(ctx, fCurrentContext, new Lexer(source,
								fLexOptions, this, this));
						fctx.setFoundOnPath(fi.getFoundOnPath(), includeDirective);
						fCurrentContext= fctx;
					}
					fLocationMap.replacingFile(fFileContentProvider, fi);
					break;
					
				case SKIP_FILE:
					break;
				}
			} else {
				final int len = headerName.length + 2;
				StringBuilder name= new StringBuilder(len);
				name.append(userInclude ? '"' : '<');
				name.append(headerName);
				name.append(userInclude ? '"' : '>');

				final char[] nameChars= new char[len];
				name.getChars(0, len, nameChars, 0);
				handleProblem(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, nameChars, poundOffset, condEndOffset);
			}
		}

		if (!reported) {
			fLocationMap.encounterPoundInclude(poundOffset, nameOffsets[0], nameOffsets[1], condEndOffset, headerName, path, userInclude, active, isHeuristic);
		}
	}

	private void processInclusionFromIndex(int offset, String path, InternalFileContent fi) {
		List<IIndexMacro> mdefs= fi.getMacroDefinitions();
		for (IIndexMacro macro : mdefs) {
			addMacroDefinition(macro);
		}
		fLocationMap.skippedFile(fLocationMap.getSequenceNumberForOffset(offset), fi);
	}

	private char[] extractHeaderName(final char[] image, final char startDelim, final char endDelim, int[] offsets) {
		char[] headerName;
		int start= 0;
		int length= image.length;
		if (length > 0 && image[length - 1] == endDelim) {
			length--;
			offsets[1]--;
			if (length > 0 && image[0] == startDelim) {
				offsets[0]++;
				start++;
				length--;
			}
		}
		headerName= new char[length];
		System.arraycopy(image, start, headerName, 0, length);
		return headerName;
	}
	
	private boolean isCircularInclusion(String filename) {
		ILocationCtx checkContext= fCurrentContext.getLocationCtx();
		while (checkContext != null) {
			if (filename.equals(checkContext.getFilePath())) {
				return true;
			}
			checkContext= checkContext.getParent();
		}
		return false;
	}


    private void executeDefine(final Lexer lexer, int startOffset, boolean isActive) throws OffsetLimitReachedException {
		try {
			ObjectStyleMacro macrodef = fMacroDefinitionParser.parseMacroDefinition(lexer, this);
			if (isActive)
				fMacroDictionary.put(macrodef.getNameCharArray(), macrodef);
			
			final Token name= fMacroDefinitionParser.getNameToken();
			fLocationMap.encounterPoundDefine(startOffset, name.getOffset(), name.getEndOffset(),
					macrodef.getExpansionOffset(), macrodef.getExpansionEndOffset(), isActive, macrodef);
		} catch (InvalidMacroDefinitionException e) {
			lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, e.fName, e.fStartOffset, e.fEndOffset);
		}
    }

    private void executeUndefine(Lexer lexer, int startOffset, boolean isActive) throws OffsetLimitReachedException {
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
    	PreprocessorMacro definition;
    	if (isActive) {
    		definition= fMacroDictionary.remove(namechars, 0, namechars.length);
    	} else {
    		definition= fMacroDictionary.get(namechars);
    	}
    	fLocationMap.encounterPoundUndef(definition, startOffset, name.getOffset(), name.getEndOffset(), endOffset, namechars, isActive);
    }

    private CodeState executeIfdef(Lexer lexer, int offset, boolean isIfndef, boolean withinExpansion) throws OffsetLimitReachedException {
		final Token name= lexer.nextToken();
		lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    	final int tt= name.getType();
		final int nameOffset = name.getOffset();
    	final int nameEndOffset = name.getEndOffset();
		final int endOffset= lexer.currentToken().getEndOffset();
		
		boolean isActive= false;
		PreprocessorMacro macro= null;
		final Conditional conditional= fCurrentContext.newBranch(BranchKind.eIf, withinExpansion);
		if (conditional.canHaveActiveBranch(withinExpansion)) {
	    	// we need an identifier
			if (tt != IToken.tIDENTIFIER) {
	    		if (tt == IToken.tCOMPLETION) {
	    			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, name);
	    		}
	    		// report problem and treat as inactive
	    		handleProblem(IProblem.PREPROCESSOR_DEFINITION_NOT_FOUND, name.getCharImage(), offset, nameEndOffset);
	    	} else {
	    		final char[] namechars= name.getCharImage();
	    		macro= fMacroDictionary.get(namechars);
	    		isActive= (macro == null) == isIfndef;
	    		if (macro == null) {
	    			macro = new UndefinedMacro(namechars);
	    		}
	    	}
		}
		
		if (isIfndef) {
			fLocationMap.encounterPoundIfndef(offset, nameOffset, nameEndOffset, endOffset, isActive, macro);
		} else {
			fLocationMap.encounterPoundIfdef(offset, nameOffset, nameEndOffset, endOffset, isActive, macro);
		}
		return fCurrentContext.setBranchState(conditional, isActive, withinExpansion, offset);
    }

    private CodeState executeIf(Lexer lexer, int startOffset, boolean isElif, boolean withinExpansion) throws OffsetLimitReachedException {
		Conditional cond= fCurrentContext.newBranch(isElif ? BranchKind.eElif : BranchKind.eIf, withinExpansion);
		if (cond == null) {
			char[] name= lexer.currentToken().getCharImage();
			int condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, name, startOffset, condEndOffset);
			return fCurrentContext.getCodeState();
		}
		
		boolean isActive= false;
		IASTName[] refs= IASTName.EMPTY_NAME_ARRAY;
		int condOffset= lexer.nextToken().getOffset();
		int condEndOffset, endOffset;

		if (cond.canHaveActiveBranch(withinExpansion)) {
			TokenList condition= new TokenList();
			condEndOffset= getTokensWithinPPDirective(true, condition, withinExpansion);
			endOffset= lexer.currentToken().getEndOffset();
			
			if (condition.first() == null) {
				handleProblem(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR, null, startOffset, endOffset);
			} else {
				try {
					fExpressionEvaluator.clearMacrosInDefinedExpression();
					isActive= fExpressionEvaluator.evaluate(condition, fMacroDictionary, fLocationMap);
					refs = fExpressionEvaluator.clearMacrosInDefinedExpression();
				} catch (EvalException e) {
					handleProblem(e.getProblemID(), e.getProblemArg(), condOffset, endOffset);
				}
			}
		} else {
			condEndOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			endOffset= lexer.currentToken().getEndOffset();
		}

		if (isElif) {
			fLocationMap.encounterPoundElif(startOffset, condOffset, condEndOffset, endOffset, isActive, refs);
		} else {
			fLocationMap.encounterPoundIf(startOffset, condOffset, condEndOffset, endOffset, isActive, refs);
		}
		return fCurrentContext.setBranchState(cond, isActive, withinExpansion, startOffset);
    }

	private CodeState executeElse(final Lexer lexer, final int startOffset,boolean withinExpansion)
			throws OffsetLimitReachedException {
		final int endOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
		Conditional cond= fCurrentContext.newBranch(BranchKind.eElse, withinExpansion);
		if (cond == null) {
			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, Keywords.cELSE, startOffset, endOffset);
    		return fCurrentContext.getCodeState();
		}
		
		final boolean isActive= cond.canHaveActiveBranch(withinExpansion);
		fLocationMap.encounterPoundElse(startOffset, endOffset, isActive);
		return fCurrentContext.setBranchState(cond, isActive, withinExpansion, startOffset);
	}

	private CodeState executeEndif(Lexer lexer, int startOffset, boolean withinExpansion) throws OffsetLimitReachedException {
		final int endOffset= lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
		final Conditional cond= fCurrentContext.newBranch(BranchKind.eEnd, withinExpansion);
		if (cond == null) {
			handleProblem(IProblem.PREPROCESSOR_UNBALANCE_CONDITION, Keywords.cENDIF, startOffset, endOffset);
		} else {
			fLocationMap.encounterPoundEndIf(startOffset, endOffset);
		}
		return fCurrentContext.setBranchEndState(cond, withinExpansion, startOffset);
	}
	
    /**
     * Runs the preprocessor on the rest of the line, storing the tokens in the holder supplied.
     * Macro expansion is reported to the location map.
     * In case isCondition is set to <code>true</code>, identifiers with image 'defined' are
     * converted to the defined-token and its argument is not macro expanded.
     * Returns the end-offset of the last token that was consumed.
     */
    private int getTokensWithinPPDirective(boolean isCondition, TokenList result, boolean withinExpansion) throws OffsetLimitReachedException {
    	final ScannerContext scannerCtx= fCurrentContext;
    	scannerCtx.clearInactiveCodeMarkerToken();
    	int options= STOP_AT_NL;
    	if (isCondition)
    		options |= PROTECT_DEFINED;
    	
    	loop: while (true) {
			Token t= internalFetchToken(scannerCtx, options, withinExpansion);
    		switch (t.getType()) {
    		case IToken.tEND_OF_INPUT:
    		case IToken.tCOMPLETION:
    			scannerCtx.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE); // make sure the exception is thrown.
    			break loop;
    		case Lexer.tNEWLINE:
    			break loop;
    		case IToken.tIDENTIFIER:
    			if (isCondition && CharArrayUtils.equals(Keywords.cDEFINED, t.getCharImage())) {
    				t.setType(CPreprocessor.tDEFINED);
    				options |= NO_EXPANSION;	
    			}
    			break;
    		case IToken.tLPAREN:
    			break;
    		default:
				options &= ~NO_EXPANSION;	
    			break;
    		}
    		result.append(t);
    	}
    	// make sure an exception is thrown if we are running content assist at the end of the line
    	return scannerCtx.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
    }

	private void skipOverConditionalCode(final Lexer lexer, boolean withinExpansion) throws OffsetLimitReachedException {
		CodeState state= CodeState.eSkipInactive;
		while (state == CodeState.eSkipInactive) {
			state= skipBranch(lexer, withinExpansion);
		}
	}

	private CodeState skipBranch(final Lexer lexer, boolean withinExpansion) throws OffsetLimitReachedException {
		while (true) {
			final Token pound = lexer.nextDirective();
			int tt = pound.getType();
			if (tt != IToken.tPOUND) {
				if (tt == IToken.tCOMPLETION) {
					// completion in inactive code
					throw new OffsetLimitReachedException(ORIGIN_INACTIVE_CODE, pound);
				}
				// must be the end of the lexer
				return CodeState.eActive;
			}
			final Token ident = lexer.nextToken();
			tt = ident.getType();
			if (tt != IToken.tIDENTIFIER) {
				if (tt == IToken.tCOMPLETION) {
					// completion in inactive directive
					throw new OffsetLimitReachedException(ORIGIN_INACTIVE_CODE, ident);
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
				executeInclude(lexer, ident.getOffset(), false, false, withinExpansion);
				break;
			case IPreprocessorDirective.ppInclude_next:
				executeInclude(lexer, ident.getOffset(), true, false, withinExpansion);
				break;
			case IPreprocessorDirective.ppIfdef:
				return executeIfdef(lexer, pound.getOffset(), false, withinExpansion);
			case IPreprocessorDirective.ppIfndef:
				return executeIfdef(lexer, pound.getOffset(), true, withinExpansion);
			case IPreprocessorDirective.ppIf:
				return executeIf(lexer, pound.getOffset(), false, withinExpansion);
			case IPreprocessorDirective.ppElif:
				return executeIf(lexer, pound.getOffset(), true, withinExpansion);
			case IPreprocessorDirective.ppElse:
				return executeElse(lexer, pound.getOffset(), withinExpansion);
			case IPreprocessorDirective.ppEndif:
				return executeEndif(lexer, pound.getOffset(), withinExpansion);
			default:
				lexer.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
				break;
			}
		}
	}

	/**
	 * The method assumes that the identifier is consumed.
	 * <p>
	 * Checks whether the identifier causes a macro expansion. May advance the current lexer
	 * over newlines to check for the opening bracket succeeding the identifier.
	 * <p>
	 * If applicable the macro is expanded and the resulting tokens are put onto a new context.
	 * @param identifier the token where macro expansion may occur.
	 * @param lexer the input for the expansion.
	 * @param stopAtNewline whether or not tokens to be read are limited to the current line.
	 * @param isPPCondition whether the expansion is inside of a preprocessor condition. This
	 * implies a specific handling for the defined token.
	 */
	private boolean expandMacro(final Token identifier, Lexer lexer, int options, boolean withinExpansion) throws OffsetLimitReachedException {
		final char[] name= identifier.getCharImage();
        PreprocessorMacro macro= fMacroDictionary.get(name);
        if (macro == null) {
        	return false;
        }
        boolean stopAtNewline= (options & STOP_AT_NL) != 0;
        if (macro instanceof FunctionStyleMacro) {
    		Token t= lexer.currentToken();
    		if (!stopAtNewline) {
    			while (t.getType() == Lexer.tNEWLINE) {
    				t= lexer.nextToken();
    			}
    		}
    		if (t.getType() != IToken.tLPAREN) {
    			return false;
    		}
        }
        final boolean contentAssist = fContentAssistLimit>=0 && fCurrentContext == fRootContext;
        final ITokenSequence input= stopAtNewline ? fLineInputToMacroExpansion : fInputToMacroExpansion;
		final MacroExpander expander = withinExpansion ? new MacroExpander(this, fMacroDictionary,
				fLocationMap, fLexOptions) : fMacroExpander;
        TokenList replacement= expander.expand(input, (options & PROTECT_DEFINED) != 0, macro, identifier, contentAssist);
    	final IASTName[] expansions= expander.clearImplicitExpansions();
    	final ImageLocationInfo[] ili= expander.clearImageLocationInfos();
    	final Token last= replacement.last();
    	final int length= last == null ? 0 : last.getEndOffset();
    	ILocationCtx ctx= fLocationMap.pushMacroExpansion(
    			identifier.getOffset(), identifier.getEndOffset(), lexer.getLastEndOffset(), length, macro, expansions, ili);
        fCurrentContext= new ScannerContext(ctx, fCurrentContext, replacement);
        return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(fMacroExpander.getClass())) {
			return fMacroExpander;
		}
		return null;
	}
}
