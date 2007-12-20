/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

/**
 * A hover to explore macro expansion.
 *
 * @since 5.0
 */
public class CMacroExpansionHover extends AbstractCEditorTextHover implements IInformationProviderExtension2 {

	/**
	 * AST visitor to find a node (declaration, statement or expression) enclosing a given source range.
	 */
    private static class FindEnclosingNodeAction extends ASTVisitor {
    	{
    		shouldVisitTranslationUnit= true;
    		shouldVisitDeclarations   = true;
    		shouldVisitStatements     = true;
    		shouldVisitExpressions    = true;
    	}
    	
    	private final int fOffset;
    	private final int fEndOffset;
		private final String fFilePath;
    	private IASTNode fBestMatch= null;
		private int fBestOffset= -1;
		private int fBestEndOffset= Integer.MAX_VALUE;
    	
		public FindEnclosingNodeAction(String filePath, int offset, int length) {
			fFilePath= filePath;
			fOffset= offset;
			fEndOffset= offset + length;
		}
    	
    	private int processNode(IASTNode node) {
    		IASTFileLocation location= node.getFileLocation();
    		if (location != null && fFilePath.equals(location.getFileName())) {
    			final int startOffset = location.getNodeOffset();
				if (startOffset <= fOffset) {
    				int endOffset= startOffset + location.getNodeLength();
    				if (endOffset >= fEndOffset) {
    					if (startOffset > fBestOffset || endOffset < fBestEndOffset) {
	    					fBestMatch= node;
	    					fBestOffset= startOffset;
	    					fBestEndOffset= endOffset;
	    					boolean isPerfectMatch= startOffset == fOffset || endOffset == fEndOffset;
	    					if (isPerfectMatch) {
	    						return PROCESS_ABORT;
	    					}
    					}
    				} else {
            			return PROCESS_SKIP;
    				}
    			} else {
        			return PROCESS_ABORT;
    			}
    		}
    		return PROCESS_CONTINUE;
    	}
    	
		public int visit(IASTTranslationUnit tu) {
    		return processNode(tu);
    	}
    	
    	public int visit(IASTDeclaration declaration) {
    		return processNode(declaration);
    	}
    	
    	public int visit(IASTExpression expression) {
    		return processNode(expression);
    	}
 
    	public int visit(IASTStatement statement) {
    		return processNode(statement);
    	}
    	
    	public IASTNode getNode() {
    		return fBestMatch;
    	}
    }

	/**
	 * AST visitor to collect nodes (declaration, statement or expression) enclosed in a given source range.
	 */
    private static class CollectEnclosedNodesAction extends ASTVisitor {
    	{
    		shouldVisitDeclarations   = true;
    		shouldVisitStatements     = true;
    		shouldVisitExpressions    = true;
    	}
    	
    	private final int fOffset;
    	private final int fEndOffset;
		private final String fFilePath;
    	private List fMatches= new ArrayList();
    	
		public CollectEnclosedNodesAction(String filePath, int offset, int length) {
			fFilePath= filePath;
			fOffset= offset;
			fEndOffset= offset + length;
		}
    	
    	private int processNode(IASTNode node) {
    		IASTFileLocation location= node.getFileLocation();
    		if (location != null && fFilePath.equals(location.getFileName())) {
    			final int startOffset = location.getNodeOffset();
				if (startOffset >= fOffset) {
    				int endOffset= startOffset + location.getNodeLength();
    				if (endOffset <= fEndOffset) {
    					fMatches.add(node);
    				} else {
            			return PROCESS_SKIP;
    				}
    			} else if (startOffset >= fEndOffset) {
        			return PROCESS_ABORT;
    			}
    		}
    		return PROCESS_CONTINUE;
    	}
    	
    	public int visit(IASTTranslationUnit tu) {
    		return processNode(tu);
    	}
    	
    	public int visit(IASTDeclaration declaration) {
    		return processNode(declaration);
    	}
    	
    	public int visit(IASTExpression expression) {
    		return processNode(expression);
    	}
 
    	public int visit(IASTStatement statement) {
    		return processNode(statement);
    	}
    	
    	public Collection getNodes() {
    		return fMatches;
    	}
    }

	/**
	 * Computes the source location for a given identifier.
	 */
	private static class ComputeExpansionRegionRunnable implements ASTRunnable {
		private final Position fTextRegion;
		private IASTNode fEnclosingNode;
		private Map fMacroDict;
		private List fExpansionNodes= new ArrayList();

		/**
		 * @param tUnit
		 * @param textRegion
		 */
		private ComputeExpansionRegionRunnable(ITranslationUnit tUnit, IRegion textRegion) {
			fTextRegion= new Position(textRegion.getOffset(), textRegion.getLength());
		}

		/*
		 * @see org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable#runOnAST(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				// try object style macro expansion first
				IASTNode node= ast.selectNodeForLocation(ast.getFilePath(), fTextRegion.getOffset(), fTextRegion.getLength());
				if (node instanceof IASTName) {
					IASTName macroName= (IASTName) node;
					IBinding binding= macroName.getBinding();
					if (binding instanceof IMacroBinding) {
						IMacroBinding macroBinding= (IMacroBinding) binding;
						if (!macroBinding.isFunctionStyle()) {
							addExpansionNode(node);
							buildMacroDictionary(ast);
							return Status.OK_STATUS;
						}
					}
				}
				// function style macro or selection
				FindEnclosingNodeAction nodeFinder= new FindEnclosingNodeAction(ast.getFilePath(), fTextRegion.getOffset(), fTextRegion.getLength());
				ast.accept(nodeFinder);
				fEnclosingNode= nodeFinder.getNode();
				if (fEnclosingNode != null) {
					boolean macroOccurrence= false;
					IASTNodeLocation[] locations= fEnclosingNode.getNodeLocations();
					for (int i = 0; i < locations.length; i++) {
						IASTNodeLocation location= locations[i];
						if (location instanceof IASTMacroExpansion) {
							IASTFileLocation fileLocation= location.asFileLocation();
							if (fileLocation != null && ast.getFilePath().equals(fileLocation.getFileName())) {
								if (fTextRegion.overlapsWith(fileLocation.getNodeOffset(), fileLocation.getNodeLength())) {
									nodeFinder= new FindEnclosingNodeAction(ast.getFilePath(), fileLocation.getNodeOffset(), fileLocation.getNodeLength());
									ast.accept(nodeFinder);
									addExpansionNode(nodeFinder.getNode());
									macroOccurrence= true;
								}
							}
						}
					}
					if (macroOccurrence) {
						CollectEnclosedNodesAction nodeCollector= new CollectEnclosedNodesAction(ast.getFilePath(), fTextRegion.getOffset(), fTextRegion.getLength());
						ast.accept(nodeCollector);
						fExpansionNodes.addAll(nodeCollector.getNodes());
						buildMacroDictionary(ast);
						return Status.OK_STATUS;
					}
				}
			}
			return Status.CANCEL_STATUS;
		}

		private void buildMacroDictionary(IASTTranslationUnit ast) {
			Map macroDict= new HashMap(500);
			IASTPreprocessorMacroDefinition[] macroDefs;
			final IASTPreprocessorMacroDefinition[] localMacroDefs= ast.getMacroDefinitions();
			for (macroDefs= localMacroDefs; macroDefs != null; macroDefs= (macroDefs == localMacroDefs) ? ast.getBuiltinMacroDefinitions() : null) {
				for (int i = 0; i < macroDefs.length; i++) {
					IASTPreprocessorMacroDefinition macroDef= macroDefs[i];
					StringBuffer macroName= new StringBuffer();
					macroName.append(macroDef.getName().toCharArray());
					if (macroDef instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
						macroName.append('(');
						IASTPreprocessorFunctionStyleMacroDefinition functionMacro= (IASTPreprocessorFunctionStyleMacroDefinition)macroDef;
						IASTFunctionStyleMacroParameter[] macroParams= functionMacro.getParameters();
						for (int j = 0; j < macroParams.length; j++) {
							if (j > 0) {
								macroName.append(',');
							}
							IASTFunctionStyleMacroParameter param= macroParams[j];
							macroName.append(param.getParameter());
						}
						macroName.append(')');
					}
					macroDict.put(macroName.toString(), macroDef.getExpansion());
				}
			}
			fMacroDict= macroDict;
		}

		private void addExpansionNode(IASTNode node) {
			if (node != null) {
				fEnclosingNode= computeCommonAncestor(node, fEnclosingNode);
				fExpansionNodes.add(node);
			}
		}

		private IASTNode computeCommonAncestor(IASTNode node, IASTNode other) {
			if (node == null) {
				return null;
			}
			if (other == null) {
				return node;
			}
			if (node == other) {
				return other;
			}
			List ancestors= new ArrayList();
			while (node != null) {
				node= node.getParent();
				ancestors.add(node);
			}
			while (other != null) {
				if (ancestors.contains(other)) {
					return other;
				}
				other= other.getParent();
			}
			return null;
		}

		IRegion getExpansionRegion() {
			if (fEnclosingNode != null) {
				int startOffset= Integer.MAX_VALUE;
				int endOffset= 0;
				for (Iterator it= fExpansionNodes.iterator(); it.hasNext(); ) {
					IASTNode node= (IASTNode) it.next();
					if (node != fEnclosingNode) {
						while (node != null && node.getParent() != fEnclosingNode) {
							node= node.getParent();
						}
					}
					if (node != null) {
						IASTFileLocation location= node.getFileLocation();
						if (location != null) {
							startOffset= Math.min(startOffset, location.getNodeOffset());
							endOffset= Math.max(endOffset, location.getNodeOffset() + location.getNodeLength());
						}
					}
				}
				if (endOffset > startOffset) {
					return new Region(startOffset, endOffset - startOffset);
				}
			}
			return null;
		}
		
		Map getMacroDictionary() {
			return fMacroDict;
		}

		IASTNode getEnclosingNode() {
			return fEnclosingNode;
		}
	}

	private static class Cache {
		ComputeExpansionRegionRunnable fComputer;
		IWorkingCopy fTranslationUnit;
		String fExpansionText;
		String fExpandedText;
	}
	
	private WeakReference fCache;

	/*
	 * @see org.eclipse.cdt.internal.ui.text.c.hover.AbstractCEditorTextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IEditorPart editor = getEditor();
		if (editor == null || !(editor instanceof ITextEditor)) {
			return null;
		}
		IEditorInput input= editor.getEditorInput();
		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy tu = manager.getWorkingCopy(input);
		if (tu == null) {
			return null;
		}

		IProgressMonitor monitor= new NullProgressMonitor();
		ComputeExpansionRegionRunnable computer= new ComputeExpansionRegionRunnable(tu, hoverRegion);
		IStatus status= ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, computer);
		if (!status.isOK()) {
			return null;
		}
		IRegion region= computer.getExpansionRegion();
		if (region == null) {
			return null;
		}
		ITextEditor textEditor= (ITextEditor)editor;
		IDocument document= textEditor.getDocumentProvider().getDocument(input);
		region= alignRegion(region, document);
		
		String expansionText;
		try {
			expansionText= document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException exc) {
			CUIPlugin.getDefault().log(exc);
			return null;
		}
		// expand
		String expandedText= expandMacros(computer.getMacroDictionary(), expansionText, tu.isCXXLanguage());

		// format
		final String lineDelimiter= TextUtilities.getDefaultLineDelimiter(document);
		final int nodeKind = getNodeKind(computer.getEnclosingNode());
		String formattedText= formatSource(nodeKind, expandedText, lineDelimiter, tu.getCProject());

		// cache objects for later macro exploration
		Cache cache= new Cache();
		cache.fComputer= computer;
		cache.fExpansionText= expansionText;
		cache.fTranslationUnit= tu;
		cache.fExpandedText= formattedText;
		fCache= new WeakReference(cache);
		return formattedText;
	}

	private static int getNodeKind(IASTNode node) {
		if (node instanceof IASTDeclaration) {
			return CodeFormatter.K_TRANSLATION_UNIT;
		}
		if (node instanceof IASTStatement) {
			return CodeFormatter.K_STATEMENTS;
		}
		if (node instanceof IASTExpression) {
			return CodeFormatter.K_EXPRESSION;
		}
		if (node instanceof IASTTranslationUnit) {
			return CodeFormatter.K_TRANSLATION_UNIT;
		}
		return CodeFormatter.K_UNKNOWN;
	}

	private static final IRegion alignRegion(IRegion region, IDocument document) {
		try {
			int start= document.getLineOfOffset(region.getOffset());
			int end= document.getLineOfOffset(region.getOffset() + region.getLength());
			if (start == end) {
				return region;
			}
			int offset= document.getLineOffset(start);
			if (document.get(offset, region.getOffset()- offset).trim().length() > 0) {
				offset= region.getOffset();
			}
			int endOffset;
			if (document.getNumberOfLines() > end + 1) {
				endOffset= document.getLineOffset(end + 1);
			} else {
				endOffset= document.getLineOffset(end) + document.getLineLength(end);
			}
			int oldEndOffset= region.getOffset() + region.getLength();
			if (document.get(oldEndOffset, endOffset - oldEndOffset).trim().length() > 0) {
				endOffset= oldEndOffset;
			}
			return new Region(offset, endOffset - offset);
			
		} catch (BadLocationException x) {
			return region;
		}
	}

	/**
	 * Format given source content according given options.
	 * 
	 * @param kind
	 *            one of {@link CodeFormatter#K_TRANSLATION_UNIT},
	 *            {@link CodeFormatter#K_STATEMENTS},
	 *            {@link CodeFormatter#K_EXPRESSION}
	 * @param content
	 *            the source content
	 * @param lineDelimiter
	 *            the line delimiter to be used
	 * @param project
	 * @return the formatted source text or the original if the text could not
	 *         be formatted successfully
	 */
	private static String formatSource(int kind, String content, String lineDelimiter, ICProject project) {
        TextEdit edit= CodeFormatterUtil.format(kind, content, 0, lineDelimiter, project.getOptions(true));
        if (edit != null) {
        	IDocument doc= new Document(content);
        	try {
				edit.apply(doc);
            	content= doc.get().trim();
			} catch (MalformedTreeException exc) {
				CUIPlugin.getDefault().log(exc);
			} catch (BadLocationException exc) {
				CUIPlugin.getDefault().log(exc);
			}
        }
        return content;
	}

	private String expandMacros(Map macroDict, String expansionText, boolean isCpp) {
        final IScannerInfo scannerInfo= new ScannerInfo(macroDict);
		final CodeReader codeReader= new CodeReader(expansionText.toCharArray());
		final IScannerExtensionConfiguration configuration;
		final ParserLanguage parserLanguage;
		if (isCpp) {
			configuration= new GPPScannerExtensionConfiguration();
			parserLanguage= ParserLanguage.CPP;
		} else {
			configuration= new GCCScannerExtensionConfiguration();
			parserLanguage= ParserLanguage.C;
		}
		CPreprocessor preprocessor= new CPreprocessor(codeReader, scannerInfo, parserLanguage, ParserUtil.getParserLogService(), configuration, NullCodeReaderFactory.getInstance());
		StringBuffer expandedText= new StringBuffer(expansionText.length());
		IToken token= null;
		IToken prevToken;
		while (true) {
			try {
				prevToken= token;
				token= preprocessor.nextToken();
			} catch (EndOfFileException exc) {
				break;
			}
			if (requireSpace(prevToken, token)) {
				expandedText.append(' ');
			}
			expandedText.append(token.getImage());
		}
		return expandedText.toString();
	}

	private static boolean requireSpace(IToken prevToken, IToken token) {
		if (prevToken == null) {
			return false;
		}
		if (prevToken.isOperator() && token.isOperator()) {
			return true;
		}
		switch (prevToken.getType()) {
		case IToken.tLPAREN: case IToken.tLBRACKET:
			return false;
			
		// bit operations
		case IToken.tAMPERASSIGN:
		case IToken.tBITOR: case IToken.tBITORASSIGN:
		case IToken.tSHIFTL: case IToken.tSHIFTLASSIGN:
		case IToken.tSHIFTR: case IToken.tSHIFTRASSIGN:
		case IToken.tXOR: case IToken.tXORASSIGN:
		
        // logical operations
		case IToken.tAND: case IToken.tOR:

		// arithmetic
		case IToken.tDIV: case IToken.tDIVASSIGN:
		case IToken.tMINUS: case IToken.tMINUSASSIGN:
		case IToken.tMOD: case IToken.tMODASSIGN:
		case IToken.tPLUS: case IToken.tPLUSASSIGN:
		case IToken.tSTARASSIGN:
		case IGCCToken.tMAX: case IGCCToken.tMIN:
			
		// comparison
		case IToken.tEQUAL: case IToken.tNOTEQUAL:
		case IToken.tGT: case IToken.tGTEQUAL:
		case IToken.tLT: case IToken.tLTEQUAL:

			// other
		case IToken.tASSIGN: case IToken.tCOMMA:
			return true;
		}
		
		switch (token.getType()) {
		case IToken.tRPAREN: case IToken.tRBRACKET:
			return false;

		// bit operations
		case IToken.tAMPER: case IToken.tAMPERASSIGN:
		case IToken.tBITOR: case IToken.tBITORASSIGN:
		case IToken.tSHIFTL: case IToken.tSHIFTLASSIGN:
		case IToken.tSHIFTR: case IToken.tSHIFTRASSIGN:
		case IToken.tXOR: case IToken.tXORASSIGN:
		
        // logical operations
		case IToken.tAND: case IToken.tOR:

		// arithmetic
		case IToken.tDIV: case IToken.tDIVASSIGN:
		case IToken.tMINUS: case IToken.tMINUSASSIGN:
		case IToken.tMOD: case IToken.tMODASSIGN:
		case IToken.tPLUS: case IToken.tPLUSASSIGN:
		case IToken.tSTAR: case IToken.tSTARASSIGN:
		case IGCCToken.tMAX: case IGCCToken.tMIN:
			
		// comparison
		case IToken.tEQUAL: case IToken.tNOTEQUAL:
		case IToken.tGT: case IToken.tGTEQUAL:
		case IToken.tLT: case IToken.tLTEQUAL:
			return true;

		// other
		case IToken.tASSIGN:
			return true;
			
		case IToken.tCOMMA:
			return false;
		}

		char lastChar= prevToken.getCharImage()[prevToken.getLength() - 1];
		char nextChar= token.getCharImage()[0];
		if (Character.isJavaIdentifierPart(lastChar) && Character.isJavaIdentifierPart(nextChar)) {
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new CMacroExpansionControl(parent, getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle= SWT.RESIZE;
				int style= SWT.V_SCROLL | SWT.H_SCROLL;
				return new CMacroExpansionControl(parent, shellStyle, style, createMacroExpansionInput());
			}
		};
	}

	protected CMacroExpansionInput createMacroExpansionInput() {
		// TODO compute all expansion steps
		Cache cache= (Cache) fCache.get();
		fCache= null;
		if (cache != null) {
			CMacroExpansionInput input= new CMacroExpansionInput();
			input.fExpansions= new String[] { cache.fExpansionText, cache.fExpandedText };
			return input;
		}
		return null;
	}

}
