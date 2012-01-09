/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.rewrite.MacroExpansionExplorer;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;

/**
 * An input object to the {@link CMacroExpansionExplorationControl}.
 * 
 * @since 5.0
 */
public class CMacroExpansionInput {

	private static class SingletonRule implements ISchedulingRule {
		public static final ISchedulingRule INSTANCE = new SingletonRule();
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	/**
	 * Computes the expansion region for a selection.
	 */
	private static class ExpansionRegionComputer implements ASTRunnable {
		private final Position fTextRegion;
		private final boolean fAllowSelection;
		private IASTNode fEnclosingNode;
		private List<IASTNode> fExpansionNodes= new ArrayList<IASTNode>();
		private MacroExpansionExplorer fExplorer;
		private IRegion fExpansionRegion;

		private ExpansionRegionComputer(ITranslationUnit tUnit, IRegion textRegion, boolean allowSelection) {
			fTextRegion= new Position(textRegion.getOffset(), textRegion.getLength());
			fAllowSelection= allowSelection;
		}

		/*
		 * @see org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable#runOnAST(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				final IASTNodeSelector nodeSelector = ast.getNodeSelector(ast.getFilePath());
				// try exact macro name match first
				IASTNode node= nodeSelector.findName(fTextRegion.getOffset(), fTextRegion.getLength());
				if (node instanceof IASTName) {
					IASTName macroName= (IASTName) node;
					IBinding binding= macroName.getBinding();
					// skip macro references that belong to a macro definition or an undef directive
					if (binding instanceof IMacroBinding && !macroName.isDefinition() &&
							macroName.getParent() instanceof IASTPreprocessorMacroExpansion) {
						addExpansionNode(node);
						createMacroExpansionExplorer(ast);
						return Status.OK_STATUS;
					}
				}
				if (fAllowSelection) {
					// selection
					boolean macroOccurrence= false;
					fEnclosingNode= nodeSelector.findEnclosingNode(fTextRegion.getOffset(), fTextRegion.getLength());
					if (fEnclosingNode == null) {
						// selection beyond last declaration
						fEnclosingNode= ast;
					}
					else if (fEnclosingNode.getParent() instanceof IASTPreprocessorMacroExpansion) {
						// selection enclosed by the name of a macro expansion
						fEnclosingNode= fEnclosingNode.getParent();
					}
					
					if (fEnclosingNode instanceof IASTPreprocessorMacroExpansion) {
						// selection enclosed by a macro expansion
						addExpansionNode(fEnclosingNode);
						macroOccurrence= true;
					}
					else {
						IASTNodeLocation[] locations= fEnclosingNode.getNodeLocations();
						for (int i = 0; i < locations.length; i++) {
							IASTNodeLocation location= locations[i];
							if (location instanceof IASTMacroExpansionLocation) {
								IASTFileLocation fileLocation= location.asFileLocation();
								if (fileLocation != null && ast.getFilePath().equals(fileLocation.getFileName())) {
									if (fTextRegion.overlapsWith(fileLocation.getNodeOffset(), fileLocation.getNodeLength())) {
										addExpansionNode(nodeSelector.findEnclosingNode(fileLocation.getNodeOffset(), fileLocation.getNodeLength()));
										macroOccurrence= true;
									}
								}
							}
						}
					}
					if (macroOccurrence) {
						createMacroExpansionExplorer(ast);
						return Status.OK_STATUS;
					}
				}
			}
			return Status.CANCEL_STATUS;
		}

		private void createMacroExpansionExplorer(IASTTranslationUnit ast) {
			IRegion region= getExpansionRegion();
			if (region != null) {
				fExplorer= MacroExpansionExplorer.create(ast, region);
				fExpansionRegion= region;
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
			List<IASTNode> ancestors= new ArrayList<IASTNode>();
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
			if (fExpansionRegion != null)
				return fExpansionRegion;
			if (fEnclosingNode != null) {
				int startOffset= Integer.MAX_VALUE;
				int endOffset= fTextRegion.getOffset() + fTextRegion.getLength();
				for (Iterator<IASTNode> it= fExpansionNodes.iterator(); it.hasNext(); ) {
					IASTNode node= it.next();
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
					startOffset= Math.min(startOffset, fTextRegion.getOffset());
					return new Region(startOffset, endOffset - startOffset);
				}
			}
			return null;
		}
		
		MacroExpansionExplorer getMacroExpansionExplorer() {
			return fExplorer;
		}
	}

	final MacroExpansionExplorer fExplorer;
	boolean fStartWithFullExpansion= true;

	private CMacroExpansionInput(MacroExpansionExplorer explorer) {
		Assert.isNotNull(explorer);
		fExplorer= explorer;
	}

	@Override
	public String toString() {
		return fExplorer.getFullExpansion().getCodeAfterStep();
	}
	
	/**
	 * Creates an input object for the macro expansion exploration control {@link CMacroExpansionExplorationControl}.
	 * 
	 * @param editor  the active editor
	 * @param textRegion  the text region where to consider macro expansions
	 * @param force  force computation of the input, if <code>true</code> this may trigger a parse
	 * @return an instance of {@link CMacroExpansionInput} or <code>null</code> if no macro was found in the region
	 */
	public static CMacroExpansionInput create(IEditorPart editor, IRegion textRegion, boolean force) {
		if (editor == null || !(editor instanceof ITextEditor)) {
			return null;
		}
		IEditorInput editorInput= editor.getEditorInput();
		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy tu = manager.getWorkingCopy(editorInput);
		try {
			if (tu == null || !tu.isConsistent()) {
				return null;
			}
		} catch (CModelException exc) {
			return null;
		}
		
		ExpansionRegionComputer computer= new ExpansionRegionComputer(tu, textRegion, force);
		doRunOnAST(computer, tu, force);

		MacroExpansionExplorer explorer= computer.getMacroExpansionExplorer();
		if (explorer == null) {
			return null;
		}

		CMacroExpansionInput input= new CMacroExpansionInput(explorer);

		return input;
	}

	private static void doRunOnAST(final ASTRunnable runnable, final ITranslationUnit tu, boolean force) {
		Job job= new Job(CHoverMessages.CMacroExpansionInput_jobTitle) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, runnable);
			}};

		// If the hover thread is interrupted this might have negative
		// effects on the index - see http://bugs.eclipse.org/219834
		// Therefore we schedule a job to decouple the parsing from this thread.
		job.setPriority(force ? Job.INTERACTIVE : Job.DECORATE);
		job.setSystem(true);
		job.setRule(SingletonRule.INSTANCE);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException exc) {
			job.cancel();
		}
	}

	/**
	 * Expand the given text region to span complete lines of the document and
	 * add a number of lines before and after the region.
	 * 
	 * @param region  the text region
	 * @param document  the underlying text document
	 * @param contextLines  the number of lines to add
	 * @return an extended region
	 */
	public static final IRegion expandRegion(IRegion region, IDocument document, int contextLines) {
		try {
			int start= document.getLineOfOffset(region.getOffset());
			start= Math.max(start - contextLines, 0);
			int offset= document.getLineOffset(start);
			CHeuristicScanner scanner= new CHeuristicScanner(document);
			offset= scanner.findNonWhitespaceForward(offset, region.getOffset() + 1);
			
			int end= document.getLineOfOffset(region.getOffset() + region.getLength());
			end= Math.min(end + contextLines, document.getNumberOfLines() - 1);

			final int endOffset;
			if (document.getNumberOfLines() > end + 1) {
				endOffset= document.getLineOffset(end + 1);
			} else {
				endOffset= document.getLineOffset(end) + document.getLineLength(end);
			}
			return new Region(offset, endOffset - offset);
			
		} catch (BadLocationException x) {
			return region;
		}
	}

}
