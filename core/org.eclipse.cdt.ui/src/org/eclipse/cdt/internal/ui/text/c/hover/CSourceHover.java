/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IWorkbenchPartOrientation;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.corext.util.Strings;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.text.CCodeReader;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * A text hover presenting the source of the element under the cursor.
 */
public class CSourceHover extends AbstractCEditorTextHover {

	private static final boolean DEBUG = false;

	protected static class SingletonRule implements ISchedulingRule {
		public static final ISchedulingRule INSTANCE = new SingletonRule();
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	/**
	 * Computes the source location for a given identifier.
	 */
	protected static class ComputeSourceRunnable implements ASTRunnable {

		private final ITranslationUnit fTU;
		private final IRegion fTextRegion;
		private final IProgressMonitor fMonitor;
		private String fSource;

		/**
		 * @param tUnit
		 * @param textRegion 
		 */
		public ComputeSourceRunnable(ITranslationUnit tUnit, IRegion textRegion) {
			fTU= tUnit;
			fTextRegion= textRegion;
			fMonitor= new NullProgressMonitor();
			fSource= null;
		}

		/*
		 * @see org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable#runOnAST(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				try {
					IASTName name= ast.getNodeSelector(null).findEnclosingName(fTextRegion.getOffset(), fTextRegion.getLength());
					if (name != null) {
						IBinding binding= name.resolveBinding();
						if (binding != null) {
							
							// Check for implicit names first, could be an implicit constructor call
							if(name.getParent() instanceof IASTImplicitNameOwner) {
								IASTImplicitNameOwner iastImplicitNameOwner = (IASTImplicitNameOwner) name.getParent();
								IASTName [] implicitNames = iastImplicitNameOwner.getImplicitNames();
								if(implicitNames.length == 1) {
									IBinding implicitNameBinding = implicitNames[0].resolveBinding();
									if(implicitNameBinding instanceof ICPPConstructor) {
										binding = implicitNameBinding;
									}
								}
							}
							
							if (binding instanceof IProblemBinding) {
								// report problem as source comment
								if (DEBUG) {
									IProblemBinding problem= (IProblemBinding) binding;
									fSource= "/* Indexer Problem!\n" + //$NON-NLS-1$
											" * " + problem.getMessage() +  //$NON-NLS-1$
											"\n */"; //$NON-NLS-1$
								}
							} else if (binding instanceof IMacroBinding) {
								fSource= computeSourceForMacro(ast, name, binding);
							} else {
								fSource= computeSourceForBinding(ast, binding);
							}
							if (fSource != null) {
								return Status.OK_STATUS;
							}
						}
					}
				} catch (CoreException exc) {
					return exc.getStatus();
				} catch (DOMException exc) {
					return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, "Internal Error", exc); //$NON-NLS-1$
				}
			}
			return Status.CANCEL_STATUS;
		}

		/**
		 * Compute the source for a macro. If the source location of the macro definition can be determined,
		 * the source is taken from there, otherwise the source is constructed as a <code>#define</code> directive.
		 * 
		 * @param ast  the AST of the translation unit
		 * @param name  the macro occurrence in the AST
		 * @param binding   the binding of the macro name
		 * @return the source or <code>null</code>
		 * @throws CoreException 
		 */
		private String computeSourceForMacro(IASTTranslationUnit ast, IASTName name, IBinding binding) throws CoreException {
			// Search for the macro definition
			IName[] defs = ast.getDefinitions(binding);
			for (IName def : defs) {
				String source= computeSourceForName(def, binding);
				if (source != null) {
					return source;
				}
			}
			return null;
		}

		/**
		 * Find a definition or declaration for the given binding and returns the source for it.
		 * Definitions are preferred over declarations. In case of multiple definitions or declarations,
		 * and the first name which yields source is taken.
		 * 
		 * @param ast  the AST of the translation unit
		 * @param binding  the binding
		 * @return a source string or <code>null</code>, if no source could be computed
		 * @throws CoreException  if the source file could not be loaded or if there was a
		 *                        problem with the index
		 * @throws DOMException  if there was an internal problem with the DOM
		 */
		private String computeSourceForBinding(IASTTranslationUnit ast, IBinding binding) throws CoreException, DOMException {
			IName[] names = findDefsOrDecls(ast, binding);
			
			// in case the binding is a non-explicit specialization we need
			// to consider the original binding (bug 281396)
			while (names.length == 0 && binding instanceof ICPPSpecialization) {
				IBinding specializedBinding = ((ICPPSpecialization) binding).getSpecializedBinding();
				if (specializedBinding == null || specializedBinding instanceof IProblemBinding) {
					break;
				}
				
				names = findDefsOrDecls(ast, specializedBinding);
				binding = specializedBinding;
			}
			if (names.length > 0) {
				for (IName name : names) {
					String source= computeSourceForName(name, binding);
					if (source != null) {
						return source;
					}
				}
			}
			return null;
		}

		private IName[] findDefsOrDecls(IASTTranslationUnit ast, IBinding binding) throws CoreException {
			IName[] names= findDefinitions(ast, binding);
			if (names.length == 0) {
				names= findDeclarations(ast, binding);
			}
			return names;
		}

		/**
		 * Get the source for the given name from the underlying file.
		 * 
		 * @param name  the name to get the source for
		 * @param binding  the binding of the name
		 * @return the source string or <code>null</code>, if the source could not be computed
		 * @throws CoreException  if the file could not be loaded
		 */
		private String computeSourceForName(IName name, IBinding binding) throws CoreException {
			IASTFileLocation fileLocation= name.getFileLocation();
			if (fileLocation == null) {
				return null;
			}
			int nodeOffset= fileLocation.getNodeOffset();
			int nodeLength= fileLocation.getNodeLength();
			
			String fileName= fileLocation.getFileName();
			if (DEBUG) System.out.println("[CSourceHover] Computing source for " + name + " in " + fileName);  //$NON-NLS-1$//$NON-NLS-2$
			IPath location= Path.fromOSString(fileName);
			LocationKind locationKind= LocationKind.LOCATION;
			if (name instanceof IASTName && !name.isReference()) {
				IASTName astName= (IASTName)name;
				if (astName.getTranslationUnit().getFilePath().equals(fileName)) {
					int hoverOffset = fTextRegion.getOffset();
					if (hoverOffset <= nodeOffset && nodeOffset < hoverOffset + fTextRegion.getLength() ||
							hoverOffset >= nodeOffset && hoverOffset < nodeOffset + nodeLength) {
						// bug 359352 - don't show source if its the same we are hovering on
						return null;
					}
					if (fTU.getResource() != null) {
						// reuse editor buffer for names local to the translation unit
						location= fTU.getResource().getFullPath();
						locationKind= LocationKind.IFILE;
					}
				}
			} else {
				// try to resolve path to a resource for proper encoding (bug 221029)
				IFile file= EditorUtility.getWorkspaceFileAtLocation(location, fTU);
				if (file != null) {
					location= file.getFullPath();
					locationKind= LocationKind.IFILE;
					if (name instanceof IIndexName) {
						// need to adjust index offsets to current offsets
						// in case file has been modified since last index time
						IIndexName indexName= (IIndexName) name;
						long timestamp= indexName.getFile().getTimestamp();
						IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(file, timestamp);
						if (converter != null) {
							IRegion currentLocation= converter.historicToActual(new Region(nodeOffset, nodeLength));
							nodeOffset= currentLocation.getOffset();
							nodeLength= currentLocation.getLength();
						}
					}
				}
			}
			ITextFileBufferManager mgr= FileBuffers.getTextFileBufferManager();
			mgr.connect(location, locationKind, fMonitor);
			ITextFileBuffer buffer= mgr.getTextFileBuffer(location, locationKind);
			try {
				IRegion nameRegion= new Region(nodeOffset, nodeLength);
				final int nameOffset= nameRegion.getOffset();
				final int sourceStart;
				final int sourceEnd;
				IDocument doc= buffer.getDocument();
				if (nameOffset >= doc.getLength() || nodeLength <= 0) {
					return null;
				}
				if (binding instanceof IMacroBinding) {
					ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, nameOffset, false);
					if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
						int directiveStart= partition.getOffset();
						int commentStart= searchCommentBackward(doc, directiveStart, -1);
						if (commentStart >= 0) {
							sourceStart= commentStart;
						} else {
							sourceStart= directiveStart;
						}
						sourceEnd= directiveStart + partition.getLength();
					} else {
						return null;
					}
				} else {
					// expand source range to include preceding comment, if any
					boolean isKnR= isKnRSource(name);
					sourceStart= computeSourceStart(doc, nameOffset, binding, isKnR);
					if (sourceStart == CHeuristicScanner.NOT_FOUND) {
						return null;
					}
					sourceEnd= computeSourceEnd(doc, nameOffset + nameRegion.getLength(), binding, name.isDefinition(), isKnR);
				}
				String source= buffer.getDocument().get(sourceStart, sourceEnd - sourceStart);
				return source;

			} catch (BadLocationException exc) {
				// ignore - should not happen anyway
				if (DEBUG) exc.printStackTrace();
			} finally {
				mgr.disconnect(location, LocationKind.LOCATION, fMonitor);
			}
			return null;
		}

		/**
		 * Determine if the name is part of a KnR function definition.
		 * @param name
		 * @return <code>true</code> if the name is part of a KnR function
		 */
		private boolean isKnRSource(IName name) {
			if (name instanceof IASTName) {
				IASTNode node= (IASTNode)name;
				while (node.getParent() != null) {
					if (node instanceof ICASTKnRFunctionDeclarator) {
						return node.getParent() instanceof IASTFunctionDefinition;
					}
					node= node.getParent();
				}
			}
			return false;
		}

		private int computeSourceStart(IDocument doc, int nameOffset, IBinding binding, boolean isKnR) throws BadLocationException {
			int sourceStart= nameOffset;
			CHeuristicScanner scanner= new CHeuristicScanner(doc);
			if (binding instanceof IParameter) {
				if (isKnR) {
					sourceStart= scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { ')', ';' });
				} else {
					sourceStart= scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { '>', '(', ',' });
					if (sourceStart > 0 && doc.getChar(sourceStart) == '>') {
						sourceStart= scanner.findOpeningPeer(sourceStart - 1, '<', '>');
						if (sourceStart > 0) {
							sourceStart= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { '(', ',' });
						}
					}
				}
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart= scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else if (binding instanceof ICPPTemplateParameter) {
				sourceStart= scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { '>', '<', ',' });
				if (sourceStart > 0 && doc.getChar(sourceStart) == '>') {
					sourceStart= scanner.findOpeningPeer(sourceStart - 1, '<', '>');
					if (sourceStart > 0) {
						sourceStart= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { '<', ',' });
					}
				}
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart= scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else if (binding instanceof IEnumerator) {
				sourceStart= scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { '{', ',' });
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart= scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else {
				final boolean expectClosingBrace;
				IType type= null;
				if (binding instanceof ITypedef) {
					type= ((ITypedef)binding).getType();
				} else if (binding instanceof IVariable) {
					type= ((IVariable)binding).getType();
				}
				expectClosingBrace= (type instanceof ICompositeType || type instanceof IEnumeration) && !(binding instanceof IVariable);
				final int nameLine= doc.getLineOfOffset(nameOffset);
				sourceStart= nameOffset;
				int commentBound;
				if (isKnR) {
					commentBound= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { ')', ';' });
				} else {
					commentBound= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { '{', '}', ';' });
				}
				while (expectClosingBrace && commentBound > 0 && doc.getChar(commentBound) == '}') {
					int openingBrace= scanner.findOpeningPeer(commentBound - 1, '{', '}');
					if (openingBrace != CHeuristicScanner.NOT_FOUND) {
						sourceStart= openingBrace - 1;
					}
					if (isKnR) {
						commentBound= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { ')', ';' });
					} else {
						commentBound= scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND, new char[] { '{', '}', ';' });
					}
				}
				if (commentBound == CHeuristicScanner.NOT_FOUND) {
					commentBound= -1; // unbound
				}
				sourceStart= Math.min(sourceStart, doc.getLineOffset(nameLine));
				int commentStart= searchCommentBackward(doc, sourceStart, commentBound);
				if (commentStart >= 0) {
					sourceStart= commentStart;
				} else {
					int nextNonWS= scanner.findNonWhitespaceForward(commentBound+1, sourceStart);
					if (nextNonWS != CHeuristicScanner.NOT_FOUND) {
						int nextNonWSLine= doc.getLineOfOffset(nextNonWS);
						int lineOffset= doc.getLineOffset(nextNonWSLine);
						if (doc.get(lineOffset, nextNonWS - lineOffset).trim().length() == 0) {
							sourceStart= doc.getLineOffset(nextNonWSLine);
						}
					}
				}
			}
			return sourceStart;
		}

		private int computeSourceEnd(IDocument doc, int start, IBinding binding, boolean isDefinition, boolean isKnR) throws BadLocationException {
			int sourceEnd= start;
			CHeuristicScanner scanner= new CHeuristicScanner(doc);
			// expand forward to the end of the definition/declaration
			boolean searchBrace= false;
			boolean searchSemi= false;
			boolean searchComma= false;
			if (binding instanceof ICompositeType && isDefinition || binding instanceof IEnumeration) {
				searchBrace= true;
			} else if (binding instanceof ICPPTemplateDefinition) {
				searchBrace= true;
			} else if (binding instanceof IFunction && isDefinition) {
				searchBrace= true;
			} else if (binding instanceof IParameter) {
				if (isKnR) {
					searchSemi= true;
				} else {
					searchComma= true;
				}
			} else if (binding instanceof IEnumerator || binding instanceof ICPPTemplateParameter) {
				searchComma= true;
			} else if (binding instanceof IVariable || binding instanceof ITypedef) {
				searchSemi= true;
			} else if (!isDefinition) {
				searchSemi= true;
			}
			if (searchBrace) {
				int brace= scanner.scanForward(start, CHeuristicScanner.UNBOUND, '{');
				if (brace != CHeuristicScanner.NOT_FOUND) {
					sourceEnd= scanner.findClosingPeer(brace + 1, '{', '}');
					if (sourceEnd == CHeuristicScanner.NOT_FOUND) {
						sourceEnd= doc.getLength();
					}
				}
				// expand region to include whole line
				IRegion lineRegion= doc.getLineInformationOfOffset(sourceEnd);
				sourceEnd= lineRegion.getOffset() + lineRegion.getLength();
			} else if (searchSemi) {
				int semi= scanner.scanForward(start, CHeuristicScanner.UNBOUND, ';');
				if (semi != CHeuristicScanner.NOT_FOUND) {
					sourceEnd= semi+1;
				}
				// expand region to include whole line
				IRegion lineRegion= doc.getLineInformationOfOffset(sourceEnd);
				sourceEnd= lineRegion.getOffset() + lineRegion.getLength();
			} else if (searchComma) {
				int bound;
				if (binding instanceof IParameter) {
					bound= scanner.findClosingPeer(start, '(', ')');
				} else if (binding instanceof ICPPTemplateParameter) {
					bound= scanner.findClosingPeer(start, '<', '>');
				} else if (binding instanceof IEnumerator) {
					bound= scanner.findClosingPeer(start, '{', '}');
				} else {
					bound = CHeuristicScanner.NOT_FOUND;
				}
				if (bound == CHeuristicScanner.NOT_FOUND) {
					bound= Math.min(doc.getLength(), start + 100);
				}
				int comma= scanner.scanForward(start, bound, ',');
				if (comma == CHeuristicScanner.NOT_FOUND) {
					// last argument
					sourceEnd= bound;
				} else {
					sourceEnd= comma;
					// expand region to include whole line if rest is comment
					IRegion lineRegion= doc.getLineInformationOfOffset(sourceEnd);
					int lineEnd= lineRegion.getOffset() + lineRegion.getLength();
					int nextNonWS= scanner.findNonWhitespaceForwardInAnyPartition(sourceEnd + 1, lineEnd);
					if (nextNonWS != CHeuristicScanner.NOT_FOUND) {
						String contentType= TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, nextNonWS, false);
						if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType) || ICPartitions.C_SINGLE_LINE_COMMENT.equals(contentType)) {
							sourceEnd= lineEnd;
						}
					}
				}
			}
			return sourceEnd;
		}

		/**
		 * Search for definitions for the given binding.
		 * 
		 * @param ast  the AST of the translation unit
		 * @param binding  the binding
		 * @return an array of definitions, never <code>null</code>
		 * @throws CoreException
		 */
		private IName[] findDefinitions(IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDefinitionsInAST(binding);
			if (declNames.length == 0 && ast.getIndex() != null) {
				// search definitions in index
				declNames = ast.getIndex().findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
			}
			return declNames;
		}

		/**
		 * Search for declarations for the given binding.
		 * 
		 * @param ast  the AST of the translation unit
		 * @param binding  the binding
		 * @return an array of declarations, never <code>null</code>
		 * @throws CoreException
		 */
		private IName[] findDeclarations(IASTTranslationUnit ast,
				IBinding binding) throws CoreException {
			IName[] declNames= ast.getDeclarationsInAST(binding);
			if (declNames.length == 0 && ast.getIndex() != null) {
				// search declarations in index
				declNames= ast.getIndex().findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
			}
			return declNames;
		}

		/**
		 * @return the computed source or <code>null</code>, if no source could be computed
		 */
		public String getSource() {
			return fSource;
		}

	}

	/**
	 * 
	 */
	public CSourceHover() {
		super();
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			IEditorInput input= editor.getEditorInput();
			IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();				
			IWorkingCopy copy = manager.getWorkingCopy(input);
			try {
				if (copy == null || !copy.isConsistent()) {
					return null;
				}
			} catch (CModelException exc) {
				return null;
			}
			
			String expression;
			try {
				expression = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				expression = expression.trim();
				if (expression.length() == 0)
					return null;

				//Before trying a search lets make sure that the user is not hovering over a keyword 
				if (selectionIsKeyword(expression))
					return null;

				String source= null;

				// Try with the indexer
				source= searchInIndex(copy, hoverRegion);

				if (source == null || source.trim().length() == 0)
					return null;

				// we are actually interested in the comments, too.
//				source= removeLeadingComments(source);

				String delim= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

				String[] sourceLines= Strings.convertIntoLines(source);
				String firstLine= sourceLines[0];
				if (firstLine.length() > 0 && !Character.isWhitespace(firstLine.charAt(0)))
					sourceLines[0]= ""; //$NON-NLS-1$
				Strings.trimIndentation(sourceLines, getTabWidth(), getTabWidth());

				if (!Character.isWhitespace(firstLine.charAt(0)))
					sourceLines[0]= firstLine;

				source = Strings.concatenate(sourceLines, delim);
				return source;

			} catch (BadLocationException e) {
			}
		}
		return null;
	}

	/**
	 * Searches the start of the comment preceding the given source offset.
	 * Continuous line comments are considered as one comment until a block
	 * comment is reached or a non-comment partition.
	 * 
	 * @param doc  the document
	 * @param start  the start of the backward search
	 * @param bound  search boundary (exclusive)
	 * @return the comment start offset or <code>-1</code>, if no suitable comment was found
	 * @throws BadLocationException 
	 */
	private static int searchCommentBackward(IDocument doc, int start, int bound) throws BadLocationException {
		int firstLine= doc.getLineOfOffset(start);
		if (firstLine == 0) {
			return 0;
		}
		ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, start, true);
		int currentOffset= Math.max(doc.getLineOffset(firstLine - 1), partition.getOffset() - 1);
		int commentOffset= -1;
		while (currentOffset > bound) {
			partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, currentOffset, true);
			currentOffset= partition.getOffset() - 1;
			if (ICPartitions.C_MULTI_LINE_COMMENT.equals(partition.getType()) 
					|| ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(partition.getType())) {
				final int partitionOffset= partition.getOffset();
				final int startLine= doc.getLineOfOffset(partitionOffset);
				final int lineOffset= doc.getLineOffset(startLine);
				if (partitionOffset == lineOffset || 
						doc.get(lineOffset, partitionOffset - lineOffset).trim().length() == 0) {
					return lineOffset;
				}
				return commentOffset;
			} else if (ICPartitions.C_SINGLE_LINE_COMMENT.equals(partition.getType())
					|| ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(partition.getType())) {
				final int partitionOffset= partition.getOffset();
				final int startLine= doc.getLineOfOffset(partitionOffset);
				final int lineOffset= doc.getLineOffset(startLine);
				if (partitionOffset == lineOffset || 
						doc.get(lineOffset, partitionOffset - lineOffset).trim().length() == 0) {
					commentOffset= lineOffset;
					continue;
				}
				return commentOffset;
			} else if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				if (doc.get(partition.getOffset(), partition.getLength()).trim().length() == 0) {
					continue;
				}
				if (commentOffset >= 0) {
					break;
				}
			} else {
				break;
			}
		}
		return commentOffset;
	}

	private static int getTabWidth() {
		return 4;
	}


	/**
	 * Strip the leading comment from the given source string.
	 * 
	 * @param source
	 * @return  the source string without leading comments
	 */
	protected static String removeLeadingComments(String source) {
		CCodeReader reader= new CCodeReader();
		IDocument document= new Document(source);
		int i;
		try {
			reader.configureForwardReader(document, 0, document.getLength(), true, false);
			int c= reader.read();
			while (c != -1 && (c == '\r' || c == '\n')) {
				c= reader.read();
			}
			i= reader.getOffset();
			reader.close();
		} catch (IOException ex) {
			i= 0;
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				CUIPlugin.log(ex);
			}
		}

		if (i < 0)
			return source;
		return source.substring(i);
	}

	protected String searchInIndex(final ITranslationUnit tUnit, IRegion textRegion) {
		final ComputeSourceRunnable computer= new ComputeSourceRunnable(tUnit, textRegion);
		Job job= new Job(CHoverMessages.CSourceHover_jobTitle) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					return ASTProvider.getASTProvider().runOnAST(tUnit, ASTProvider.WAIT_ACTIVE_ONLY, monitor, computer);
				} catch (Throwable t) {
					CUIPlugin.log(t);
				}
				return Status.CANCEL_STATUS;
			}
		};
		// If the hover thread is interrupted this might have negative
		// effects on the index - see http://bugs.eclipse.org/219834
		// Therefore we schedule a job to decouple the parsing from this thread.
		job.setPriority(Job.DECORATE);
		job.setSystem(true);
		job.setRule(SingletonRule.INSTANCE);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException exc) {
			job.cancel();
			return null;
		}
		return computer.getSource();
	}


	/**
	 * Test whether the given name is a known keyword.
	 * 
	 * @param name
	 * @return <code>true</code> if the name is a known keyword or <code>false</code> if the
	 *         name is not considered a keyword
	 */
	private boolean selectionIsKeyword(String name) {
		Set<String> keywords= ParserFactory.getKeywordSet(KeywordSetKey.KEYWORDS, ParserLanguage.CPP);
		return keywords.contains(name);
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				IEditorPart editor= getEditor();
				int orientation= SWT.NONE;
				if (editor instanceof IWorkbenchPartOrientation)
					orientation= ((IWorkbenchPartOrientation) editor).getOrientation();
				return new SourceViewerInformationControl(parent, false, orientation, getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				IEditorPart editor= getEditor();
				int orientation= SWT.NONE;
				if (editor instanceof IWorkbenchPartOrientation)
					orientation= ((IWorkbenchPartOrientation) editor).getOrientation();
				return new SourceViewerInformationControl(parent, true, orientation, null);
			}
		};
	}
}
