/*******************************************************************************
 * Copyright (c) 2007, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFileNomination;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSpecification;
import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Converts the offsets relative to various contexts to the global sequence number. Also creates
 * and stores objects that are needed to conform with the IAST... interfaces.
 * @since 5.0
 */
public class LocationMap implements ILocationResolver {
	private final LexerOptions fLexerOptions;
	private String fTranslationUnitPath;
	private IASTTranslationUnit fTranslationUnit;

	private ArrayList<ASTPreprocessorNode> fDirectives = new ArrayList<>();
	private ArrayList<ASTProblem> fProblems = new ArrayList<>();
	private ArrayList<ASTComment> fComments = new ArrayList<>();
	private ArrayList<ASTMacroDefinition> fBuiltinMacros = new ArrayList<>();
	private ArrayList<ASTPreprocessorName> fMacroReferences = new ArrayList<>();

	private LocationCtxFile fRootContext;
	private LocationCtx fCurrentContext;
	private int fLastChildInsertionOffset;

	// Stuff computed on demand
	private IdentityHashMap<IBinding, IASTPreprocessorMacroDefinition> fMacroDefinitionMap;
	private List<ISkippedIndexedFilesListener> fSkippedFilesListeners = new ArrayList<>();
	// Keyed by file location.
	private Map<String, LocationCtxFile> fFileContexts;

	public LocationMap(LexerOptions lexOptions) {
		fLexerOptions = lexOptions;
	}

	@Override
	public LexerOptions getLexerOptions() {
		return fLexerOptions;
	}

	public void registerPredefinedMacro(IMacroBinding macro) {
		registerPredefinedMacro(macro, null, -1);
	}

	public void registerMacroFromIndex(IMacroBinding macro, IName originalDefinition, int expansionOffset) {
		registerPredefinedMacro(macro, originalDefinition, expansionOffset);
	}

	private void registerPredefinedMacro(IMacroBinding macro, IName originalDefinition, int expansionOffset) {
		ASTMacroDefinition astMacro;
		if (macro.isFunctionStyle()) {
			astMacro = new ASTFunctionStyleMacroDefinition(fTranslationUnit, macro, originalDefinition,
					expansionOffset);
		} else {
			astMacro = new ASTMacroDefinition(fTranslationUnit, macro, originalDefinition, expansionOffset);
		}
		fBuiltinMacros.add(astMacro);
	}

	/**
	 * The outermost context must be a translation unit. You must call this method exactly once and before
	 * creating any other context.
	 */
	public ILocationCtx pushTranslationUnit(String filename, AbstractCharArray buffer) {
		assert fCurrentContext == null;
		fTranslationUnitPath = filename;
		fCurrentContext = fRootContext = new LocationCtxFile(null, filename, buffer, 0, 0, 0, null, true);
		fLastChildInsertionOffset = 0;
		return fCurrentContext;
	}

	/**
	 * Starts an artificial context that can be used to include files without having a source that contains
	 * the include directives.
	 * @param buffer a buffer containing the include directives.
	 * @param isMacroFile whether the context is used for running the preprocessor, only.
	 */
	public ILocationCtx pushPreInclusion(AbstractCharArray buffer, int offset, boolean isMacroFile) {
		assert fCurrentContext instanceof LocationCtxContainer;
		int sequenceNumber = getSequenceNumberForOffset(offset);
		fCurrentContext = new LocationCtxContainer((LocationCtxContainer) fCurrentContext, buffer, offset, offset,
				sequenceNumber);
		fLastChildInsertionOffset = 0;
		return fCurrentContext;
	}

	/**
	 * Starts a context for an included file.
	 * @param buffer the buffer containing the content of the inclusion.
	 * @param filename the filename of the included file
	 * @param startOffset offset in the current context.
	 * @param nameOffset offset in the current context.
	 * @param endOffset offset in the current context
	 * @param name name of the include without delimiters ("" or <>)
	 * @param userInclude <code>true</code> when specified with double-quotes.
	 */
	public ILocationCtx pushInclusion(int startOffset, int nameOffset, int nameEndOffset, int endOffset,
			AbstractCharArray buffer, String filename, char[] name, boolean userInclude, boolean heuristic,
			boolean isSource) {
		assert fCurrentContext instanceof LocationCtxContainer;
		int startNumber = getSequenceNumberForOffset(startOffset);
		int nameNumber = getSequenceNumberForOffset(nameOffset);
		int nameEndNumber = getSequenceNumberForOffset(nameEndOffset);
		int endNumber = getSequenceNumberForOffset(endOffset);
		boolean exported = isExportedIncludeAt(endOffset);
		final ASTInclusionStatement inclusionStatement = new ASTInclusionStatement(fTranslationUnit, startNumber,
				nameNumber, nameEndNumber, endNumber, name, filename, userInclude, true, heuristic, exported, null);
		fDirectives.add(inclusionStatement);
		fCurrentContext = new LocationCtxFile((LocationCtxContainer) fCurrentContext, filename, buffer, startOffset,
				endOffset, endNumber, inclusionStatement, isSource);
		fLastChildInsertionOffset = 0;
		return fCurrentContext;
	}

	/**
	 * Creates a name representing an implicit macro expansion. The returned name can be fed into
	 * {@link #pushMacroExpansion(int, int, int, int, IMacroBinding, IASTName[], ImageLocationInfo[])}
	 * @param macro the macro that has been expanded
	 * @param imageLocationInfo the image-location for the name of the macro.
	 */
	public IASTName encounterImplicitMacroExpansion(IMacroBinding macro, ImageLocationInfo imageLocationInfo) {
		return new ASTMacroReferenceName(null, IASTPreprocessorMacroExpansion.NESTED_EXPANSION_NAME, 0, 0, macro,
				imageLocationInfo);
	}

	/**
	 * Creates a name representing a macro in a defined-expression. The returned name can be fed into
	 * {@link #encounterPoundIf(int, int, int, int, boolean, IASTName[])}.
	 */
	public IASTName encounterDefinedExpression(IMacroBinding macro, int startOffset, int endOffset) {
		int startNumber = getSequenceNumberForOffset(startOffset);
		int endNumber = getSequenceNumberForOffset(endOffset);
		return new ASTMacroReferenceName(null, IASTPreprocessorStatement.MACRO_NAME, startNumber, endNumber, macro,
				null);
	}

	/**
	 * Creates a new context for the result of a (recursive) macro-expansion.
	 * @param nameOffset offset within the current context where the name for the macro-expansion starts.
	 * @param nameEndOffset offset within the current context where the name for the macro-expansion ends.
	 * @param endOffset offset within the current context where the entire macro-expansion ends.
	 * @param macro the outermost macro that got expanded.
	 * @param implicitMacroReferences an array of implicit macro-expansions.
	 * @param imageLocations an array of image-locations for the new context.
	 */
	public ILocationCtx pushMacroExpansion(int nameOffset, int nameEndOffset, int endOffset, int contextLength,
			IMacroBinding macro, IASTName[] implicitMacroReferences, ImageLocationInfo[] imageLocations) {
		assert fCurrentContext instanceof LocationCtxContainer;

		int nameNumber = getSequenceNumberForOffset(nameOffset);
		int nameEndNumber = getSequenceNumberForOffset(nameEndOffset);
		int endNumber = getSequenceNumberForOffset(endOffset);
		final int length = endNumber - nameNumber;

		ASTMacroExpansion expansion = new ASTMacroExpansion(fTranslationUnit, nameNumber, endNumber);
		ASTMacroReferenceName explicitRef = new ASTMacroReferenceName(expansion,
				IASTPreprocessorMacroExpansion.EXPANSION_NAME, nameNumber, nameEndNumber, macro, null);
		addMacroReference(explicitRef);
		for (IASTName implicitMacroReference : implicitMacroReferences) {
			ASTMacroReferenceName name = (ASTMacroReferenceName) implicitMacroReference;
			name.setParent(expansion);
			name.setOffsetAndLength(nameNumber, length);
			addMacroReference(name);
		}

		LocationCtxMacroExpansion expansionCtx = new LocationCtxMacroExpansion(this,
				(LocationCtxContainer) fCurrentContext, nameOffset, endOffset, endNumber, contextLength, imageLocations,
				explicitRef);
		expansion.setContext(expansionCtx);
		fCurrentContext = expansionCtx;
		fLastChildInsertionOffset = 0;
		return fCurrentContext;
	}

	private void addMacroReference(ASTPreprocessorName name) {
		if (name != null) {
			fMacroReferences.add(name);
		}
	}

	/**
	 * Ends the current context.
	 * @param locationCtx the current context, used to check whether caller and location map are still in sync.
	 */
	public void popContext(ILocationCtx locationCtx) {
		assert fCurrentContext == locationCtx;
		final LocationCtx child = fCurrentContext;
		final LocationCtx parent = (LocationCtx) fCurrentContext.getParent();
		if (parent != null) {
			fCurrentContext = parent;
			fLastChildInsertionOffset = child.fEndOffsetInParent;
			parent.addChildSequenceLength(child.getSequenceLength());
		}
	}

	/**
	 * Reports an inclusion that is not performed.
	 * @param startOffset offset in the current context.
	 * @param nameOffset offset in the current context.
	 * @param endOffset offset in the current context
	 * @param name name of the include without delimiters ("" or <>)
	 * @param filename the filename of the included file
	 * @param userInclude <code>true</code> when specified with double-quotes.
	 * @param active <code>true</code> when include appears in active code.
	 */
	public ASTInclusionStatement encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset,
			int endOffset, char[] name, String filename, boolean userInclude, boolean active, boolean heuristic,
			IFileNomination nominationDelegate) {
		boolean exported = isExportedIncludeAt(endOffset);
		startOffset = getSequenceNumberForOffset(startOffset);
		nameOffset = getSequenceNumberForOffset(nameOffset);
		nameEndOffset = getSequenceNumberForOffset(nameEndOffset);
		endOffset = getSequenceNumberForOffset(endOffset);
		final ASTInclusionStatement inc = new ASTInclusionStatement(fTranslationUnit, startOffset, nameOffset,
				nameEndOffset, endOffset, name, filename, userInclude, active, heuristic, exported, nominationDelegate);
		fDirectives.add(inc);
		return inc;
	}

	private boolean isExportedIncludeAt(int includeEndOffset) {
		boolean exported = false;
		if (fLexerOptions.fIncludeExportPatterns != null && fCurrentContext instanceof LocationCtxFile) {
			LocationCtxFile context = (LocationCtxFile) fCurrentContext;
			exported = context.isInsideIncludeExportBlock();
			if (!exported) {
				int distance = context.getOffsetOfIncludeExport() - includeEndOffset;
				if (distance >= 0 && CharArrayUtils.indexOf('\n', context.getSource(includeEndOffset, distance)) < 0) {
					exported = true;
				}
			}
		}
		return exported;
	}

	public void encounteredComment(int offset, int endOffset, boolean isBlockComment, AbstractCharArray input) {
		ASTComment comment = new ASTComment(fTranslationUnit, getCurrentFilePath(), offset, endOffset, isBlockComment);
		if (fLexerOptions.fIncludeExportPatterns != null && fCurrentContext instanceof LocationCtxFile) {
			CharSequence text = getTrimmedCommentText(input.subSequence(offset, endOffset), isBlockComment);
			IncludeExportPatterns patterns = fLexerOptions.fIncludeExportPatterns;
			if (patterns.getIncludeExportPattern() != null
					&& patterns.getIncludeExportPattern().matcher(text).matches()) {
				((LocationCtxFile) fCurrentContext).setOffsetOfIncludeExport(offset);
			} else if (patterns.getIncludeBeginExportsPattern() != null
					&& patterns.getIncludeBeginExportsPattern().matcher(text).matches()) {
				((LocationCtxFile) fCurrentContext).setInsideIncludeExportBlock(true);
			} else if (patterns.getIncludeEndExportsPattern() != null
					&& patterns.getIncludeEndExportsPattern().matcher(text).matches()) {
				((LocationCtxFile) fCurrentContext).setInsideIncludeExportBlock(false);
			}
		}
		fComments.add(comment);
	}

	private CharSequence getTrimmedCommentText(CharSequence comment, boolean isBlockComment) {
		int end = comment.length() - (isBlockComment ? 2 : 0);
		int begin;
		for (begin = 2; begin < end; begin++) {
			if (!Character.isWhitespace(comment.charAt(begin)))
				break;
		}
		if (end <= begin)
			return ""; //$NON-NLS-1$
		while (--end >= begin) {
			if (!Character.isWhitespace(comment.charAt(end)))
				break;
		}
		return comment.subSequence(begin, end + 1);
	}

	public void encounterProblem(int id, char[] arg, int offset, int endOffset) {
		offset = getSequenceNumberForOffset(offset);
		endOffset = getSequenceNumberForOffset(endOffset);
		ASTProblem problem = new ASTProblem(fTranslationUnit, IASTTranslationUnit.SCANNER_PROBLEM, id, arg, false,
				offset, endOffset);
		fProblems.add(problem);
	}

	public ASTElse encounterPoundElse(int startOffset, int endOffset, boolean isActive) {
		startOffset = getSequenceNumberForOffset(startOffset);
		endOffset = getSequenceNumberForOffset(endOffset);
		final ASTElse astElse = new ASTElse(fTranslationUnit, startOffset, endOffset, isActive);
		fDirectives.add(astElse);
		return astElse;
	}

	public ASTElif encounterPoundElif(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean taken,
			IASTName[] macrosInDefinedExpression) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTElif elif = new ASTElif(fTranslationUnit, startOffset, condOffset, condEndOffset, taken);
		fDirectives.add(elif);

		for (IASTName element : macrosInDefinedExpression) {
			ASTMacroReferenceName name = (ASTMacroReferenceName) element;
			name.setParent(elif);
			name.setPropertyInParent(IASTPreprocessorStatement.MACRO_NAME);
			addMacroReference(name);
		}
		return elif;
	}

	public ASTEndif encounterPoundEndIf(int startOffset, int endOffset) {
		startOffset = getSequenceNumberForOffset(startOffset);
		endOffset = getSequenceNumberForOffset(endOffset);
		final ASTEndif stmt = new ASTEndif(fTranslationUnit, startOffset, endOffset);
		fDirectives.add(stmt);
		return stmt;
	}

	public void encounterPoundError(int startOffset, int condOffset, int condEndOffset, int endOffset) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTError(fTranslationUnit, startOffset, condOffset, condEndOffset));
	}

	public void encounterPoundPragma(int startOffset, int condOffset, int condEndOffset, int endOffset) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTPragma(fTranslationUnit, startOffset, condOffset, condEndOffset));
	}

	public void encounterPragmaOperator(int startNumber, int condNumber, int condEndNumber, int endNumber) {
		fDirectives.add(new ASTPragmaOperator(fTranslationUnit, startNumber, condNumber, condEndNumber, endNumber));
	}

	public ASTIfdef encounterPoundIfdef(int startOffset, int condOffset, int condEndOffset, int endOffset,
			boolean taken, IMacroBinding macro) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTIfdef ifdef = new ASTIfdef(fTranslationUnit, startOffset, condOffset, condEndOffset, taken, macro);
		fDirectives.add(ifdef);
		addMacroReference(ifdef.getMacroReference());
		return ifdef;
	}

	public ASTIfndef encounterPoundIfndef(int startOffset, int condOffset, int condEndOffset, int endOffset,
			boolean taken, IMacroBinding macro) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTIfndef ifndef = new ASTIfndef(fTranslationUnit, startOffset, condOffset, condEndOffset, taken, macro);
		fDirectives.add(ifndef);
		addMacroReference(ifndef.getMacroReference());
		return ifndef;
	}

	public ASTIf encounterPoundIf(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean taken,
			IASTName[] macrosInDefinedExpression) {
		startOffset = getSequenceNumberForOffset(startOffset);
		condOffset = getSequenceNumberForOffset(condOffset);
		condEndOffset = getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTIf astif = new ASTIf(fTranslationUnit, startOffset, condOffset, condEndOffset, taken);
		fDirectives.add(astif);
		for (IASTName element : macrosInDefinedExpression) {
			ASTMacroReferenceName name = (ASTMacroReferenceName) element;
			name.setParent(astif);
			name.setPropertyInParent(IASTPreprocessorStatement.MACRO_NAME);
			addMacroReference(name);
		}
		return astif;
	}

	public void encounterPoundDefine(int startOffset, int nameOffset, int nameEndOffset, int expansionOffset,
			int endOffset, boolean isActive, IMacroBinding macrodef) {
		startOffset = getSequenceNumberForOffset(startOffset);
		nameOffset = getSequenceNumberForOffset(nameOffset);
		nameEndOffset = getSequenceNumberForOffset(nameEndOffset);
		expansionOffset = getSequenceNumberForOffset(expansionOffset);
		endOffset = getSequenceNumberForOffset(endOffset);
		ASTPreprocessorNode astMacro;
		if (!macrodef.isFunctionStyle()) {
			astMacro = new ASTMacroDefinition(fTranslationUnit, macrodef, startOffset, nameOffset, nameEndOffset,
					expansionOffset, endOffset, isActive);
		} else {
			astMacro = new ASTFunctionStyleMacroDefinition(fTranslationUnit, macrodef, startOffset, nameOffset,
					nameEndOffset, expansionOffset, endOffset, isActive);
		}
		fDirectives.add(astMacro);
	}

	public void encounterPoundUndef(IMacroBinding definition, int startOffset, int nameOffset, int nameEndOffset,
			int endOffset, char[] name, boolean isActive) {
		startOffset = getSequenceNumberForOffset(startOffset);
		nameOffset = getSequenceNumberForOffset(nameOffset);
		nameEndOffset = getSequenceNumberForOffset(nameEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTUndef undef = new ASTUndef(fTranslationUnit, name, startOffset, nameOffset, nameEndOffset, definition,
				isActive);
		fDirectives.add(undef);
		addMacroReference(undef.getMacroName());
	}

	@Override
	public void setRootNode(IASTTranslationUnit root) {
		fTranslationUnit = root;
		if (fTranslationUnit instanceof ISkippedIndexedFilesListener) {
			fSkippedFilesListeners.add((ISkippedIndexedFilesListener) root);
		}
	}

	@Override
	public String getTranslationUnitPath() {
		return fTranslationUnitPath;
	}

	/**
	 * Line number of offset in current context.
	 * @param offset in current context.
	 */
	public int getCurrentLineNumber(int offset) {
		return fCurrentContext.getLineNumber(offset);
	}

	/**
	 * Returns the filename of the current context. If the context is a macro-expansion the filename of
	 * the enclosing file is returned.
	 */
	public String getCurrentFilePath() {
		return fCurrentContext.getFilePath();
	}

	/**
	 * Returns the sequence number corresponding to the offset in the current context.
	 * <p>
	 * You must insert all child contexts before the given offset before conversion.
	 */
	int getSequenceNumberForOffset(int offset) {
		return fCurrentContext.getSequenceNumberForOffset(offset, offset < fLastChildInsertionOffset);
	}

	@Override
	public String getContainingFilePath(int sequenceNumber) {
		LocationCtx ctx = fRootContext.findSurroundingContext(sequenceNumber, 1);
		return ctx.getFilePath();
	}

	@Override
	public boolean isPartOfSourceFile(int sequenceNumber) {
		LocationCtx ctx = fRootContext.findSurroundingContext(sequenceNumber, 1);
		if (ctx == fRootContext && fTranslationUnit != null)
			return !fTranslationUnit.isHeaderUnit();

		return ctx.isSourceFile();
	}

	@Override
	public ASTFileLocation getMappedFileLocation(int sequenceNumber, int length) {
		return fRootContext.findMappedFileLocation(sequenceNumber, length);
	}

	@Override
	public int convertToSequenceEndNumber(int sequenceNumber) {
		return fRootContext.convertToSequenceEndNumber(sequenceNumber);
	}

	@Override
	public char[] getUnpreprocessedSignature(IASTFileLocation loc) {
		ASTFileLocation floc = convertFileLocation(loc);
		if (floc == null) {
			return CharArrayUtils.EMPTY;
		}
		return floc.getSource();
	}

	@Override
	public IASTPreprocessorMacroExpansion[] getMacroExpansions(IASTFileLocation loc) {
		ASTFileLocation floc = convertFileLocation(loc);
		if (floc == null) {
			return IASTPreprocessorMacroExpansion.EMPTY_ARRAY;
		}

		LocationCtxFile ctx = floc.getLocationContext();
		ArrayList<IASTPreprocessorMacroExpansion> list = new ArrayList<>();

		ctx.collectMacroExpansions(floc.getNodeOffset(), floc.getNodeLength(), list);
		return list.toArray(new IASTPreprocessorMacroExpansion[list.size()]);
	}

	private ASTFileLocation convertFileLocation(IASTFileLocation loc) {
		if (loc == null) {
			return null;
		}
		if (loc instanceof ASTFileLocation) {
			return (ASTFileLocation) loc;
		}
		final String fileName = loc.getFileName();
		final int nodeOffset = loc.getNodeOffset();
		final int nodeLength = loc.getNodeLength();
		int sequenceNumber = getSequenceNumberForFileOffset(fileName, nodeOffset);
		if (sequenceNumber < 0) {
			return null;
		}

		int length = 0;
		if (nodeLength > 0) {
			length = getSequenceNumberForFileOffset(fileName, nodeOffset + nodeLength - 1) + 1 - sequenceNumber;
			if (length < 0) {
				return null;
			}
		}
		return getMappedFileLocation(sequenceNumber, length);
	}

	@Override
	public IASTNodeLocation[] getLocations(int sequenceNumber, int length) {
		ArrayList<IASTNodeLocation> result = new ArrayList<>();
		fRootContext.collectLocations(sequenceNumber, length, result);
		return result.toArray(new IASTNodeLocation[result.size()]);
	}

	@Override
	public boolean isPartOfTranslationUnitFile(int sequenceNumber) {
		return fRootContext.isThisFile(sequenceNumber);
	}

	@Override
	public IASTImageLocation getImageLocation(int sequenceNumber, int length) {
		ArrayList<IASTNodeLocation> result = new ArrayList<>();
		fRootContext.collectLocations(sequenceNumber, length, result);
		if (result.size() != 1) {
			return null;
		}
		IASTNodeLocation loc = result.get(0);
		if (loc instanceof IASTFileLocation) {
			IASTFileLocation floc = (IASTFileLocation) loc;
			return new ASTImageLocation(IASTImageLocation.REGULAR_CODE, floc.getFileName(), floc.getNodeOffset(),
					floc.getNodeLength());
		}
		if (loc instanceof ASTMacroExpansionLocation) {
			ASTMacroExpansionLocation mel = (ASTMacroExpansionLocation) loc;
			return mel.getImageLocation();
		}
		return null;
	}

	@Override
	public void findPreprocessorNode(ASTNodeSpecification<?> nodeSpec) {
		final int sequenceStart = nodeSpec.getSequenceStart();
		final int sequenceEnd = nodeSpec.getSequenceEnd();

		// check directives
		int from = findLastNodeBefore(fDirectives, sequenceStart);
		for (int i = from + 1; i < fDirectives.size(); i++) {
			ASTPreprocessorNode directive = fDirectives.get(i);
			if (directive.getOffset() > sequenceEnd) {
				break;
			}
			directive.findNode(nodeSpec);
		}

		// check macro references and expansions
		from = findLastMacroReferenceBefore(fMacroReferences, sequenceStart);
		for (int i = from + 1; i < fMacroReferences.size(); i++) {
			ASTPreprocessorNode macroRef = fMacroReferences.get(i);
			if (macroRef.getOffset() > sequenceEnd) {
				break;
			}
			if (macroRef.getPropertyInParent() == IASTPreprocessorMacroExpansion.NESTED_EXPANSION_NAME) {
				continue;
			}
			nodeSpec.visit(macroRef);
			IASTNode parent = macroRef.getParent();
			if (parent instanceof ASTMacroExpansion) {
				ASTMacroExpansion expansion = (ASTMacroExpansion) parent;
				assert expansion.getMacroReference() == macroRef;

				if (nodeSpec.canContainMatches(expansion)) {
					nodeSpec.visit(expansion);
					if (!nodeSpec.requiresClass(IASTPreprocessorMacroExpansion.class)) {
						LocationCtxMacroExpansion ctx = expansion.getContext();
						if (fTranslationUnit != null) {
							FindNodeByImageLocation visitor = new FindNodeByImageLocation(ctx.fSequenceNumber,
									ctx.getSequenceLength(), nodeSpec);
							fTranslationUnit.accept(visitor);
						}
						ASTPreprocessorName[] nestedMacros = expansion.getNestedMacroReferences();
						for (ASTPreprocessorName nested : nestedMacros) {
							IASTImageLocation imgLoc = nested.getImageLocation();
							if (imgLoc != null
									&& imgLoc.getLocationKind() == IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION) {
								nodeSpec.visit(nested, imgLoc);
							}
						}
					}
				}
			}
		}
	}

	private int findLastNodeBefore(ArrayList<? extends ASTPreprocessorNode> nodes, int sequenceStart) {
		int lower = -1;
		int upper = nodes.size() - 1;
		while (lower < upper) {
			int middle = (lower + upper + 1) / 2;
			ASTPreprocessorNode candidate = nodes.get(middle);
			if (candidate.getOffset() + candidate.getLength() >= sequenceStart) {
				upper = middle - 1;
			} else {
				lower = middle;
			}
		}
		return lower;
	}

	private int findLastMacroReferenceBefore(ArrayList<? extends ASTPreprocessorName> nodes, int sequenceStart) {
		int lower = -1;
		int upper = nodes.size() - 1;
		while (lower < upper) {
			int middle = (lower + upper + 1) / 2;
			ASTPreprocessorNode candidate = nodes.get(middle);
			IASTNode parent = candidate.getParent();
			if (parent instanceof ASTMacroExpansion) {
				candidate = (ASTMacroExpansion) parent;
			}
			if (candidate.getOffset() + candidate.getLength() >= sequenceStart) {
				upper = middle - 1;
			} else {
				lower = middle;
			}
		}
		return lower;
	}

	@Override
	public int getSequenceNumberForFileOffset(String filePath, int fileOffset) {
		LocationCtxFile ctx = fRootContext;
		if (filePath != null) {
			if (fFileContexts == null) {
				// Build a map of file contexts keyed by file locations to speed up subsequent calls.
				fFileContexts = new HashMap<>();
				fFileContexts.put(fRootContext.getFilePath(), fRootContext);
				ArrayDeque<LocationCtxContainer> queue = new ArrayDeque<>();
				for (LocationCtxContainer c = fRootContext; c != null; c = queue.pollFirst()) {
					for (LocationCtx child : c.getChildren()) {
						if (child instanceof LocationCtxFile) {
							LocationCtxFile childFileContext = (LocationCtxFile) child;
							String path = childFileContext.getFilePath();
							if (!fFileContexts.containsKey(path)) {
								fFileContexts.put(path, childFileContext);
								queue.add(childFileContext);
							}
						} else if (child instanceof LocationCtxContainer) {
							queue.add((LocationCtxContainer) child);
						}
					}
				}
			}
			ctx = fFileContexts.get(filePath);
		}
		if (ctx == null) {
			return -1;
		}
		return ctx.getSequenceNumberForOffset(fileOffset, true);
		//		LocationCtx ctx= fRootContext;
		//		if (filePath != null) {
		//			ArrayDeque<LocationCtx> contexts= new ArrayDeque<>();
		//			while (ctx != null) {
		//				if (ctx instanceof LocationCtxFile) {
		//					if (filePath.equals(ctx.getFilePath())) {
		//						break;
		//					}
		//				}
		//				contexts.addAll(ctx.getChildren());
		//				ctx= contexts.pollFirst();
		//			}
		//		}
		//		if (ctx != null) {
		//			return ctx.getSequenceNumberForOffset(fileOffset, true);
		//		}
		//		return -1;
	}

	@Override
	public IASTFileLocation flattenLocations(IASTNodeLocation[] locations) {
		if (locations.length == 0) {
			return null;
		}
		IASTFileLocation from = locations[0].asFileLocation();
		IASTFileLocation to = locations[locations.length - 1].asFileLocation();
		if (from == to) {
			return from;
		}
		if (from instanceof ASTFileLocation && to instanceof ASTFileLocation) {
			int sequenceNumber = ((ASTFileLocation) from).getSequenceNumber();
			int length = ((ASTFileLocation) from).getSequenceEndNumber() - sequenceNumber;
			if (length > 0) {
				return getMappedFileLocation(sequenceNumber, length);
			}
		}
		return null;
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		ArrayList<IASTPreprocessorMacroDefinition> result = new ArrayList<>();
		for (ASTPreprocessorNode directive : fDirectives) {
			if (directive instanceof IASTPreprocessorMacroDefinition) {
				result.add((IASTPreprocessorMacroDefinition) directive);
			}
		}
		return result.toArray(new IASTPreprocessorMacroDefinition[result.size()]);
	}

	@Override
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		ArrayList<IASTPreprocessorIncludeStatement> result = new ArrayList<>();
		for (ASTPreprocessorNode directive : fDirectives) {
			if (directive instanceof IASTPreprocessorIncludeStatement) {
				result.add((IASTPreprocessorIncludeStatement) directive);
			}
		}
		return result.toArray(new IASTPreprocessorIncludeStatement[result.size()]);
	}

	@Override
	public IASTComment[] getComments() {
		return fComments.toArray(new IASTComment[fComments.size()]);
	}

	@Override
	public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		return fDirectives.toArray(new IASTPreprocessorStatement[fDirectives.size()]);
	}

	@Override
	public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
		return fBuiltinMacros.toArray(new IASTPreprocessorMacroDefinition[fBuiltinMacros.size()]);
	}

	@Override
	public IASTProblem[] getScannerProblems() {
		return fProblems.toArray(new IASTProblem[fProblems.size()]);
	}

	@Override
	public int getScannerProblemsCount() {
		return fProblems.size();
	}

	@Override
	public IASTName[] getDeclarations(IMacroBinding binding) {
		IASTPreprocessorMacroDefinition def = getMacroDefinition(binding);
		return def == null ? IASTName.EMPTY_NAME_ARRAY : new IASTName[] { def.getName() };
	}

	IASTPreprocessorMacroDefinition getMacroDefinition(IMacroBinding binding) {
		if (fMacroDefinitionMap == null) {
			fMacroDefinitionMap = new IdentityHashMap<>();
			for (int i = 0; i < fBuiltinMacros.size(); i++) {
				final IASTPreprocessorMacroDefinition def = fBuiltinMacros.get(i);
				final IASTName name = def.getName();
				if (name != null) {
					fMacroDefinitionMap.put(name.getBinding(), def);
				}
			}
			IASTPreprocessorMacroDefinition[] defs = getMacroDefinitions();
			for (final IASTPreprocessorMacroDefinition def : defs) {
				final IASTName name = def.getName();
				if (name != null) {
					fMacroDefinitionMap.put(name.getBinding(), def);
				}
			}
		}
		return fMacroDefinitionMap.get(binding);
	}

	@Override
	public IASTName[] getReferences(IMacroBinding binding) {
		List<IASTName> result = new ArrayList<>();
		for (IASTName name : fMacroReferences) {
			if (name.getBinding() == binding) {
				result.add(name);
			}
		}
		return result.toArray(new IASTName[result.size()]);
	}

	public IASTName[] getMacroReferences() {
		return fMacroReferences.toArray(new IASTName[fMacroReferences.size()]);
	}

	public ASTPreprocessorName[] getNestedMacroReferences(ASTMacroExpansion expansion) {
		final IASTName explicitRef = expansion.getMacroReference();
		List<ASTPreprocessorName> result = new ArrayList<>();
		for (ASTPreprocessorName name : fMacroReferences) {
			if (name.getParent() == expansion && name != explicitRef) {
				result.add(name);
			}
		}
		return result.toArray(new ASTPreprocessorName[result.size()]);
	}

	@Override
	public IDependencyTree getDependencyTree() {
		return new DependencyTree(fRootContext);
	}

	public void cleanup() {
	}

	public void skippedFile(int sequenceNumber, InternalFileContent fi) {
		for (ISkippedIndexedFilesListener l : fSkippedFilesListeners) {
			l.skippedFile(sequenceNumber, fi);
		}
	}

	public void parsingFile(InternalFileContentProvider fileContentProvider, InternalFileContent fileContent) {
		for (ISkippedIndexedFilesListener l : fSkippedFilesListeners) {
			l.parsingFile(fileContentProvider, fileContent);
		}
	}

	public IFileNomination reportPragmaOnceSemantics(ILocationCtx locationCtx) {
		if (locationCtx == fRootContext) {
			if (fTranslationUnit != null) {
				fTranslationUnit.setPragmaOnceSemantics(true);
			}
			return fTranslationUnit;
		} else if (locationCtx instanceof LocationCtxFile) {
			ASTInclusionStatement stmt = ((LocationCtxFile) locationCtx).getInclusionStatement();
			if (stmt != null) {
				stmt.setPragamOnceSemantics(true);
			}
			return stmt;
		}
		return null;
	}

	public void endTranslationUnit(int endOffset, CharArrayObjectMap<char[]> sigMacros) {
		if (fTranslationUnit != null) {
			int offset = getSequenceNumberForOffset(endOffset);
			((ASTNode) fTranslationUnit).setLength(offset);

			if (sigMacros != null) {
				ISignificantMacros sig = sigMacros.isEmpty() ? ISignificantMacros.NONE
						: new SignificantMacros(sigMacros);
				fTranslationUnit.setSignificantMacros(sig);
			}
		}
	}
}
