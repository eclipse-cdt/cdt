/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTPreprocessorSelectionResult;

/**
 * Converts the offsets relative to various contexts to the global sequence number. Also creates and stores
 * objects that are needed to conform with the IAST... interfaces.
 * @since 5.0
 */
public class LocationMap implements ILocationResolver {
	private static final IASTName[] EMPTY_NAMES = {};
	
	private String fTranslationUnitPath;
    private IASTTranslationUnit fTranslationUnit;

    private ArrayList fDirectives= new ArrayList();
    private ArrayList fProblems= new ArrayList();
    private ArrayList fComments= new ArrayList();
    private ArrayList fBuiltinMacros= new ArrayList();
	private IdentityHashMap fMacroReferences= new IdentityHashMap();
	
    private LocationCtx fRootContext= null;
    private LocationCtx fCurrentContext= null;
	private int fLastChildInsertionOffset;

	// stuff computed on demand
	private IdentityHashMap fMacroDefinitionMap= null;


    
	public void registerPredefinedMacro(IMacroBinding macro) {
		registerPredefinedMacro(macro, null, -1, -1, -1);
	}

	public void registerMacroFromIndex(IMacroBinding macro, String filename, int nameOffset, int nameEndOffset, int expansionOffset) {
		registerPredefinedMacro(macro, filename, nameOffset, nameEndOffset, expansionOffset);
	}
	
	private void registerPredefinedMacro(IMacroBinding macro, String filename, int nameOffset, int nameEndOffset, int expansionOffset) {
		ASTMacro astmacro;
		if (macro.isFunctionStyle()) {
			astmacro= new ASTFunctionMacro(fTranslationUnit, macro, filename, nameOffset, nameEndOffset, expansionOffset);
		}
		else {
			astmacro= new ASTMacro(fTranslationUnit, macro, filename, nameOffset, nameEndOffset, expansionOffset);
		}
		fBuiltinMacros.add(astmacro);
	}

	/**
	 * The outermost context must be a translation unit. You must call this method exactly once and before
	 * creating any other context.
	 */
	public ILocationCtx pushTranslationUnit(String filename, char[] buffer) {
		assert fCurrentContext == null;
		fTranslationUnitPath= filename;
		fRootContext= fCurrentContext= new LocationCtxFile(null, filename, buffer, 0, 0, 0, null);
		fLastChildInsertionOffset= 0;
		return fCurrentContext;
	}

	/**
	 * Starts an artificial context that can be used to include files without having a source that contains
	 * the include directives.
	 * @param buffer a buffer containing the include directives.
	 * @param isMacroFile whether the context is used for running the preprocessor, only.
	 */
	public ILocationCtx pushPreInclusion(char[] buffer, int offset, boolean isMacroFile) {
		assert fCurrentContext instanceof LocationCtxContainer;
		int sequenceNumber= getSequenceNumberForOffset(offset);
		fCurrentContext= new LocationCtxContainer((LocationCtxContainer) fCurrentContext, buffer, offset, offset, sequenceNumber);
		fLastChildInsertionOffset= 0;
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
	public ILocationCtx pushInclusion(int startOffset,	int nameOffset, int nameEndOffset, int endOffset, 
			char[] buffer, String filename, char[] name, boolean userInclude) {
		assert fCurrentContext instanceof LocationCtxContainer;
		int startNumber= getSequenceNumberForOffset(startOffset);	
		int nameNumber= getSequenceNumberForOffset(nameOffset);		
		int nameEndNumber= getSequenceNumberForOffset(nameEndOffset);
		int endNumber= getSequenceNumberForOffset(endOffset);
		final ASTInclusionStatement inclusionStatement= 
			new ASTInclusionStatement(fTranslationUnit, startNumber, nameNumber, nameEndNumber, endNumber, name, filename, userInclude, true);
		fDirectives.add(inclusionStatement);
		fCurrentContext= new LocationCtxFile((LocationCtxContainer) fCurrentContext, filename, buffer, startOffset, endOffset, endNumber, inclusionStatement);
		fLastChildInsertionOffset= 0;
		return fCurrentContext;
	}

	/**
	 * Creates a name representing an implicit macro expansion. The returned name can be fed into 
	 * {@link #pushMacroExpansion(int, int, int, int, IMacroBinding, IASTName[])}.
	 * @param macro the macro that has been expanded
	 * @param imageLocationInfo the image-location for the name of the macro.
	 */
	public IASTName encounterImplicitMacroExpansion(IMacroBinding macro, ImageLocationInfo imageLocationInfo) {
		return new ASTMacroReferenceName(fTranslationUnit, 0, 0, macro, imageLocationInfo);
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
		
		int nameNumber= getSequenceNumberForOffset(nameOffset);		
		int nameEndNumber= getSequenceNumberForOffset(nameEndOffset);
		int endNumber= getSequenceNumberForOffset(endOffset);
		final int length= endNumber-nameNumber;
		
		for (int i = 0; i < implicitMacroReferences.length; i++) {
			ASTMacroReferenceName name = (ASTMacroReferenceName) implicitMacroReferences[i];
			name.setOffsetAndLength(nameNumber, length);
			addMacroReference((IMacroBinding) name.getBinding(), name);
		}
		
		ASTMacroReferenceName expansion= new ASTMacroReferenceName(fTranslationUnit, nameNumber, nameEndNumber, macro, null);
		addMacroReference(macro, expansion);
		
		fCurrentContext= new LocationCtxMacroExpansion(this, (LocationCtxContainer) fCurrentContext, nameOffset, endOffset, endNumber, contextLength, imageLocations, expansion);
		fLastChildInsertionOffset= 0;
		return fCurrentContext;
	}
	
	private void addMacroReference(IMacroBinding macro, IASTName name) {
		List list= (List) fMacroReferences.get(macro);
		if (list == null) {
			list= new ArrayList();
			fMacroReferences.put(macro, list);
		}
		list.add(name);
	}

	/**
	 * Ends the current context.
	 * @param locationCtx the current context, used to check whether caller and location map are still in sync.
	 */
	public void popContext(ILocationCtx locationCtx) {
		assert fCurrentContext == locationCtx;
		final LocationCtx child= fCurrentContext;
		final LocationCtx parent= (LocationCtx) fCurrentContext.getParent();
		if (parent != null) {
			fCurrentContext= parent;
			fLastChildInsertionOffset= child.fEndOffsetInParent;
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
	public void encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset, int endOffset,
			char[] name, String filename, boolean userInclude, boolean active) {
		startOffset= getSequenceNumberForOffset(startOffset);	
		nameOffset= getSequenceNumberForOffset(nameOffset);		
		nameEndOffset= getSequenceNumberForOffset(nameEndOffset);
		endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTInclusionStatement(fTranslationUnit, startOffset, nameOffset, nameEndOffset, endOffset, name, filename, userInclude, active));
	}

	public void encounteredComment(int offset, int endOffset, boolean isBlockComment) {
		offset= getSequenceNumberForOffset(offset);
		endOffset= getSequenceNumberForOffset(endOffset);
		fComments.add(new ASTComment(fTranslationUnit, offset, endOffset, isBlockComment));
	}

	public void encounterProblem(int id, char[] arg, int offset, int endOffset) {
    	offset= getSequenceNumberForOffset(offset);
    	endOffset= getSequenceNumberForOffset(endOffset);
    	ASTProblem problem = new ASTProblem(fTranslationUnit, id, arg, offset, endOffset);
		fProblems.add(problem);
	}

	public void encounterPoundElse(int startOffset, int endOffset, boolean isActive) {
		startOffset= getSequenceNumberForOffset(startOffset);
		endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTElse(fTranslationUnit, startOffset, endOffset, isActive));
	}

	public void encounterPoundElif(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean isActive) {
		startOffset= getSequenceNumberForOffset(startOffset); 	
		condOffset= getSequenceNumberForOffset(condOffset);		
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTElif(fTranslationUnit, startOffset, condOffset, condEndOffset, isActive));
	}

	public void encounterPoundEndIf(int startOffset, int endOffset) {
		startOffset= getSequenceNumberForOffset(startOffset);
		endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTEndif(fTranslationUnit, startOffset, endOffset));
	}

	public void encounterPoundError(int startOffset, int condOffset, int condEndOffset, int endOffset) {
		startOffset= getSequenceNumberForOffset(startOffset);
		condOffset= getSequenceNumberForOffset(condOffset);
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTError(fTranslationUnit, startOffset, condOffset, condEndOffset));
	}

	public void encounterPoundPragma(int startOffset, int condOffset, int condEndOffset, int endOffset) {
		startOffset= getSequenceNumberForOffset(startOffset);
		condOffset= getSequenceNumberForOffset(condOffset);
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTPragma(fTranslationUnit, startOffset, condOffset, condEndOffset));
	}

	public void encounterPoundIfdef(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean isActive) {
		startOffset= getSequenceNumberForOffset(startOffset);
		condOffset= getSequenceNumberForOffset(condOffset);
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTIfdef(fTranslationUnit, startOffset, condOffset, condEndOffset, isActive));
	}

	public void encounterPoundIfndef(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean isActive) {
		startOffset= getSequenceNumberForOffset(startOffset);
		condOffset= getSequenceNumberForOffset(condOffset);
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTIfndef(fTranslationUnit, startOffset, condOffset, condEndOffset, isActive));
	}

	public void encounterPoundIf(int startOffset, int condOffset, int condEndOffset, int endOffset, boolean isActive) {
		startOffset= getSequenceNumberForOffset(startOffset);	
		condOffset= getSequenceNumberForOffset(condOffset);		
		condEndOffset= getSequenceNumberForOffset(condEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		fDirectives.add(new ASTIf(fTranslationUnit, startOffset, condOffset, condEndOffset, isActive));
	}

	public void encounterPoundDefine(int startOffset, int nameOffset, int nameEndOffset, int expansionOffset, int endOffset, IMacroBinding macrodef) {
		startOffset= getSequenceNumberForOffset(startOffset);	
		nameOffset= getSequenceNumberForOffset(nameOffset);		
		nameEndOffset= getSequenceNumberForOffset(nameEndOffset);
		expansionOffset= getSequenceNumberForOffset(expansionOffset);
		endOffset= getSequenceNumberForOffset(endOffset);
		ASTPreprocessorNode astMacro;
		if (!macrodef.isFunctionStyle()) {
			astMacro= new ASTMacro(fTranslationUnit, macrodef, startOffset, nameOffset, nameEndOffset, expansionOffset, endOffset);
		}
		else {
			astMacro= new ASTFunctionMacro(fTranslationUnit, macrodef, startOffset, nameOffset, nameEndOffset, expansionOffset, endOffset);
		}
		fDirectives.add(astMacro);
	}

	public void encounterPoundUndef(IMacroBinding definition, int startOffset, int nameOffset, int nameEndOffset, int endOffset, char[] name) {
		startOffset= getSequenceNumberForOffset(startOffset);	
		nameOffset= getSequenceNumberForOffset(nameOffset);		
		nameEndOffset= getSequenceNumberForOffset(nameEndOffset);
		// not using endOffset, compatible with 4.0: endOffset= getSequenceNumberForOffset(endOffset);
		final ASTUndef undef = new ASTUndef(fTranslationUnit, name, startOffset, nameOffset, nameEndOffset, definition);
		fDirectives.add(undef);
		addMacroReference(definition, undef.getMacroName());
	}

	public void setRootNode(IASTTranslationUnit root) {
		fTranslationUnit= root;
	}
	
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

	public String getContainingFilename(int sequenceNumber) {
		LocationCtx ctx= fRootContext.findSurroundingContext(sequenceNumber, 1);
		return new String(ctx.getFilePath());
	}

	public IASTFileLocation getMappedFileLocation(int sequenceNumber, int length) {
		return fRootContext.findMappedFileLocation(sequenceNumber, length);
	}
	
    public char[] getUnpreprocessedSignature(IASTFileLocation loc) {
    	if (loc instanceof ASTFileLocation) {
    		return ((ASTFileLocation) loc).getSource();
    	}
    	return CharArrayUtils.EMPTY;
	}

	public IASTNodeLocation[] getLocations(int sequenceNumber, int length) {
		ArrayList result= new ArrayList();
		fRootContext.collectLocations(sequenceNumber, length, result);
		return (IASTNodeLocation[]) result.toArray(new IASTNodeLocation[result.size()]);
	} 
	
	public IASTImageLocation getImageLocation(int sequenceNumber, int length) {
		ArrayList result= new ArrayList();
		fRootContext.collectLocations(sequenceNumber, length, result);
		if (result.size() != 1) {
			return null;
		}
		IASTNodeLocation loc= (IASTNodeLocation) result.get(0);
		if (loc instanceof IASTFileLocation) {
			IASTFileLocation floc= (IASTFileLocation) loc;
			return new ASTImageLocation(IASTImageLocation.REGULAR_CODE, 
					floc.getFileName(), floc.getNodeOffset(), floc.getNodeLength());
		}
		if (loc instanceof ASTMacroExpansionLocation) { 
			ASTMacroExpansionLocation mel= (ASTMacroExpansionLocation) loc;
			return mel.getImageLocation();
		}
		return null;
	}

	public IASTNode findSurroundingPreprocessorNode(int sequenceNumber, int length) {
		int lower=0;
		int upper= fDirectives.size()-1;
		while(lower <= upper) {
			int middle= (lower+upper)/2;
			ASTPreprocessorNode node= (ASTPreprocessorNode) fDirectives.get(middle);
			final int nodeSequenceNumber= node.getOffset();
			if (nodeSequenceNumber <= sequenceNumber) {
				final int nodeEndSequenceNumber= nodeSequenceNumber + node.getLength();
				if (sequenceNumber+length <= nodeEndSequenceNumber) {
					return node.findSurroundingNode(sequenceNumber, length);
				}
				else {
					lower= middle+1;
				}
			}
			else {
				upper= middle-1;
			}
		}
		// search for a macro-expansion
		LocationCtxMacroExpansion ctx= fRootContext.findSurroundingMacroExpansion(sequenceNumber, length);
		if (ctx != null) {
			ASTMacroReferenceName candidate= ctx.getMacroReference();
			final int candSequenceNumber = candidate.getOffset();
			final int candEndSequenceNumber = candSequenceNumber + candidate.getLength();
			if (candSequenceNumber <= sequenceNumber && sequenceNumber + length <= candEndSequenceNumber) {
				return candidate;
			}
		}
		
		return null;
	}
	
	public int getSequenceNumberForFileOffset(String filePath, int fileOffset) {
		LocationCtx ctx= fRootContext;
		if (filePath != null) {
			LinkedList contexts= new LinkedList();
			while(ctx != null) {
				if (ctx instanceof LocationCtxFile) {
					if (filePath.equals(ctx.getFilePath())) {
						break;
					}
				}
				contexts.addAll(ctx.getChildren());
				if (contexts.isEmpty()) {
					ctx= null;
				}
				else {
					ctx= (LocationCtx) contexts.removeFirst();
				}
			}
		}
		if (ctx != null) {
			return ctx.getSequenceNumberForOffset(fileOffset, true);
		}
		return -1;
	}

	// mstodo implement
	public IASTFileLocation flattenLocations(IASTNodeLocation[] locations) {
		throw new UnsupportedOperationException();
	}


	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
    	ArrayList result= new ArrayList();
    	for (Iterator iterator = fDirectives.iterator(); iterator.hasNext();) {
			Object directive= iterator.next();
			if (directive instanceof IASTPreprocessorMacroDefinition) {
				result.add(directive);
			}
		}
    	return (IASTPreprocessorMacroDefinition[]) result.toArray(new IASTPreprocessorMacroDefinition[result.size()]);
    }

    public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
    	ArrayList result= new ArrayList();
    	for (Iterator iterator = fDirectives.iterator(); iterator.hasNext();) {
			Object directive= iterator.next();
			if (directive instanceof IASTPreprocessorIncludeStatement) {
				result.add(directive);
			}
		}
    	return (IASTPreprocessorIncludeStatement[]) result.toArray(new IASTPreprocessorIncludeStatement[result.size()]);
    }

	public IASTComment[] getComments() {
    	return (IASTComment[]) fComments.toArray(new IASTComment[fComments.size()]);
	}

    public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
    	return (IASTPreprocessorStatement[]) fDirectives.toArray(new IASTPreprocessorStatement[fDirectives.size()]);
    }

    public IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
    	return (IASTPreprocessorMacroDefinition[]) fBuiltinMacros.toArray(new IASTPreprocessorMacroDefinition[fBuiltinMacros.size()]);
    }

	public IASTProblem[] getScannerProblems() {
		return (IASTProblem[]) fProblems.toArray(new IASTProblem[fProblems.size()]);
	}

	
	public IASTName[] getDeclarations(IMacroBinding binding) {
		IASTPreprocessorMacroDefinition def = getMacroDefinition(binding);
		return def == null ? EMPTY_NAMES : new IASTName[] {def.getName()};
	}

	IASTPreprocessorMacroDefinition getMacroDefinition(IMacroBinding binding) {
		if (fMacroDefinitionMap == null) {
			fMacroDefinitionMap= new IdentityHashMap();
			IASTPreprocessorMacroDefinition[] defs= getMacroDefinitions();
			for (int i = 0; i < defs.length; i++) {
				final IASTPreprocessorMacroDefinition def = defs[i];
				final IASTName name = def.getName();
				if (name != null) {
					fMacroDefinitionMap.put(name.getBinding(), def);
				}
			}
		}
		return (IASTPreprocessorMacroDefinition) fMacroDefinitionMap.get(binding);
	}

	public IASTName[] getReferences(IMacroBinding binding) {
		List list= (List) fMacroReferences.get(binding);
		if (list == null) {
			return EMPTY_NAMES;
		}
		return (IASTName[]) list.toArray(new IASTName[list.size()]);
	}
	
	public IDependencyTree getDependencyTree() {
        return new DependencyTree(fRootContext);
	}

	public void cleanup() {
	}

	public ASTPreprocessorSelectionResult getPreprocessorNode(String path, int offset, int length) {
		throw new UnsupportedOperationException();
	}

	// mstodo- old location resolver
	public char[] getUnpreprocessedSignature(IASTNodeLocation[] locations) {
		throw new UnsupportedOperationException();
	}	
	// mstodo- old location resolver
	public IASTName[] getMacroExpansions() {
		throw new UnsupportedOperationException();
	}
}
