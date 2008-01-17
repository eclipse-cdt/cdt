/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

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
		private final boolean fAllowSelection;
		private IASTNode fEnclosingNode;
		private List fExpansionNodes= new ArrayList();
		private MacroExpansionExplorer fExplorer;

		private ComputeExpansionRegionRunnable(ITranslationUnit tUnit, IRegion textRegion, boolean allowSelection) {
			fTextRegion= new Position(textRegion.getOffset(), textRegion.getLength());
			fAllowSelection= allowSelection;
		}

		/*
		 * @see org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable#runOnAST(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				// try macro name match first
				IASTNode node= ast.selectNodeForLocation(ast.getFilePath(), fTextRegion.getOffset(), fTextRegion.getLength());
				if (node instanceof IASTName) {
					IASTName macroName= (IASTName) node;
					IBinding binding= macroName.getBinding();
					if (binding instanceof IMacroBinding) {
						addExpansionNode(node);
						createMacroExpansionExplorer(ast, getExpansionRegion());
						return Status.OK_STATUS;
					}
				}
				if (fAllowSelection) {
					// selection
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
							createMacroExpansionExplorer(ast, getExpansionRegion());
							return Status.OK_STATUS;
						}
					}
				}
			}
			return Status.CANCEL_STATUS;
		}

		private void createMacroExpansionExplorer(IASTTranslationUnit ast, IRegion region) {
			if (region != null) {
				fExplorer= MacroExpansionExplorer.create(ast, region);
			}
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
		
		IASTNode getEnclosingNode() {
			return fEnclosingNode;
		}

		MacroExpansionExplorer getMacroExpansionExplorer() {
			return fExplorer;
		}
	}

	private Reference fCache;

	/*
	 * @see org.eclipse.cdt.internal.ui.text.c.hover.AbstractCEditorTextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		CMacroExpansionInput input= createMacroExpansionInput(getEditor(), hoverRegion, true);
		if (input == null) {
			return null;
		}
		fCache= new SoftReference(input);
		return input.fExplorer.getFullExpansion().getCodeAfterStep();
	}
	
	public static CMacroExpansionInput createMacroExpansionInput(IEditorPart editor, IRegion hoverRegion, boolean allowSelection) {
		if (editor == null || !(editor instanceof ITextEditor)) {
			return null;
		}
		IEditorInput editorInput= editor.getEditorInput();
		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy tu = manager.getWorkingCopy(editorInput);
		if (tu == null) {
			return null;
		}
		
		IProgressMonitor monitor= new NullProgressMonitor();
		ComputeExpansionRegionRunnable computer= new ComputeExpansionRegionRunnable(tu, hoverRegion, allowSelection);
		IStatus status= ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, computer);
		if (!status.isOK()) {
			return null;
		}
		IRegion region= computer.getExpansionRegion();
		if (region == null) {
			return null;
		}
		MacroExpansionExplorer explorer= computer.getMacroExpansionExplorer();
		if (explorer == null) {
			return null;
		}
		ITextEditor textEditor= (ITextEditor)editor;
		IDocument document= textEditor.getDocumentProvider().getDocument(editorInput);
		region= alignRegion(region, document);
		
		CMacroExpansionInput input= new CMacroExpansionInput();
		input.fExplorer= explorer;
		input.fDocument= document;
		input.fRegion= region;
		return input;
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
				return new CMacroExpansionExplorationControl(parent, shellStyle, style, getMacroExpansionInput());
			}
		};
	}

	protected CMacroExpansionInput getMacroExpansionInput() {
		if (fCache == null) {
			return null;
		}
		CMacroExpansionInput input= (CMacroExpansionInput) fCache.get();
		fCache= null;
		if (input == null) {
			IEditorPart editor= getEditor();
			if (editor != null) {
				ISelectionProvider provider= editor.getSite().getSelectionProvider();
				ISelection selection= provider.getSelection();
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection= (ITextSelection) selection;
					IRegion region= new Region(textSelection.getOffset(), textSelection.getLength());
					input= createMacroExpansionInput(editor, region, true);
				}
			}
		}
		return input;
	}

}
