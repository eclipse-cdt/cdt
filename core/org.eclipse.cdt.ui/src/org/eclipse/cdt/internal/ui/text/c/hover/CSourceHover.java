/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Paulo Garcia (BlackBerry)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.io.IOException;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
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
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.HeuristicResolver;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.corext.util.Strings;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.text.CCodeReader;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICPartitions;
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

/**
 * A text hover presenting the source of the element under the cursor.
 */
public class CSourceHover extends AbstractCEditorTextHover {
	private static final boolean DEBUG = false;

	protected static class SingletonRule implements ISchedulingRule {
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
	 * Computes the source location for a given identifier.
	 */
	protected static class ComputeSourceRunnable implements ASTRunnable {
		private final ITranslationUnit fTU;
		private final IRegion fTextRegion;
		private final String fSelection;
		private final IProgressMonitor fMonitor;
		private String fSource;

		/**
		 * @param tUnit the translation unit
		 * @param textRegion the selected region
		 * @param selection the text of the selected region without
		 */
		public ComputeSourceRunnable(ITranslationUnit tUnit, IRegion textRegion, String selection) {
			fTU = tUnit;
			fTextRegion = textRegion;
			fSelection = selection;
			fMonitor = new NullProgressMonitor();
			fSource = null;
		}

		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null) {
				try {
					IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
					if (SemanticUtil.isAutoOrDecltype(fSelection)) {
						IASTNode node = nodeSelector.findEnclosingNode(fTextRegion.getOffset(),
								fTextRegion.getLength());
						IType type = CPPSemantics.resolveDecltypeOrAutoType(node);
						if (type != null && !(type instanceof IProblemType))
							fSource = ASTTypeUtil.getType(type, false);
					} else {
						IASTName name = nodeSelector.findEnclosingName(fTextRegion.getOffset(),
								fTextRegion.getLength());
						if (name != null) {
							IASTNode parent = name.getParent();
							if (parent instanceof ICPPASTTemplateId) {
								name = (IASTName) parent;
							}
							IBinding binding = name.resolveBinding();
							if (binding != null) {
								// Check for implicit names first, could be an implicit constructor call.
								if (name.getParent() instanceof IASTImplicitNameOwner) {
									IASTImplicitNameOwner implicitNameOwner = (IASTImplicitNameOwner) name.getParent();
									IASTName[] implicitNames = implicitNameOwner.getImplicitNames();
									if (implicitNames.length == 1) {
										IBinding implicitNameBinding = implicitNames[0].resolveBinding();
										if (implicitNameBinding instanceof ICPPConstructor) {
											binding = implicitNameBinding;
										}
									}
								}
								if (binding instanceof ICPPUnknownBinding) {
									try {
										CPPSemantics.pushLookupPoint(name);
										IBinding[] resolved = HeuristicResolver
												.resolveUnknownBinding((ICPPUnknownBinding) binding);
										if (resolved.length == 1) {
											binding = resolved[0];
										}
									} finally {
										CPPSemantics.popLookupPoint();
									}
								}
								if (binding instanceof IProblemBinding) {
									// Report problem as source comment.
									if (DEBUG) {
										IProblemBinding problem = (IProblemBinding) binding;
										fSource = "/* Problem:\n" + //$NON-NLS-1$
												" * " + problem.getMessage() + //$NON-NLS-1$
												"\n */"; //$NON-NLS-1$
									}
								} else if (binding instanceof IMacroBinding) {
									fSource = computeSourceForMacro(ast, name, binding);
								} else if (binding instanceof IEnumerator) {
									// Show integer value for enumerators (bug 285126).
									fSource = computeSourceForEnumerator(ast, (IEnumerator) binding);
								} else {
									fSource = computeSourceForBinding(ast, binding);
								}
							}
						}
					}
					if (fSource != null) {
						return Status.OK_STATUS;
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (DOMException e) {
					return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, "Internal Error", e); //$NON-NLS-1$
				}
			}
			return Status.CANCEL_STATUS;
		}

		/**
		 * Compute the source for a macro. If the source location of the macro definition can be
		 * determined, the source is taken from there, otherwise the source is constructed as
		 * a {@code #define} directive.
		 *
		 * @param ast  the AST of the translation unit
		 * @param name  the macro occurrence in the AST
		 * @param binding   the binding of the macro name
		 * @return the source or {@code null}
		 * @throws CoreException
		 */
		private String computeSourceForMacro(IASTTranslationUnit ast, IASTName name, IBinding binding)
				throws CoreException {
			// Search for the macro definition
			IName[] defs = ast.getDefinitions(binding);
			for (IName def : defs) {
				String source = computeSourceForName(def, binding);
				if (source != null) {
					return source;
				}
			}
			return null;
		}

		/**
		 * Computes the source for a enumerator. If the value of the enumerator can be retrieved,
		 * the method will return a string with the value, otherwise it will fall back showing
		 * the enumerator constant.
		 *
		 * @param ast  the AST of the translation unit
		 * @param binding   the binding of the enumerator name
		 * @return the enumerator value, source or {@code null}
		 * @throws CoreException
		 */
		private String computeSourceForEnumerator(IASTTranslationUnit ast, IEnumerator binding) throws CoreException {
			Number numValue = binding.getValue().numberValue();
			if (numValue != null) {
				return numValue.toString();
			} else {
				// Search for the enumerator definition
				IName[] defs = ast.getDefinitions(binding);
				for (IName def : defs) {
					String source = computeSourceForName(def, binding);
					if (source != null) {
						return source;
					}
				}
			}
			return null;
		}

		/**
		 * Find a definition or declaration for the given binding and returns the source for it.
		 * Definitions are preferred over declarations. In case of multiple definitions or
		 * declarations, and the first name which yields source is taken.
		 *
		 * @param ast  the AST of the translation unit
		 * @param binding  the binding
		 * @return a source string or {@code null}, if no source could be computed
		 * @throws CoreException  if the source file could not be loaded or if there was
		 *                        a problem with the index
		 * @throws DOMException  if there was an internal problem with the DOM
		 */
		private String computeSourceForBinding(IASTTranslationUnit ast, IBinding binding)
				throws CoreException, DOMException {
			IName[] names = findDefsOrDecls(ast, binding);

			// In case the binding is a non-explicit specialization we need
			// to consider the original binding (bug 281396).
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
					String source = computeSourceForName(name, binding);
					if (source != null) {
						return source;
					}
				}
			}
			return null;
		}

		private IName[] findDefsOrDecls(IASTTranslationUnit ast, IBinding binding) throws CoreException {
			IName[] names = findDefinitions(ast, binding);
			if (names.length == 0) {
				names = findDeclarations(ast, binding);
			}
			return names;
		}

		/**
		 * Returns the source for the given name from the underlying file.
		 *
		 * @param name  the name to get the source for
		 * @param binding  the binding of the name
		 * @return the source string or {@code null}, if the source could not be computed
		 * @throws CoreException  if the file could not be loaded
		 */
		private String computeSourceForName(IName name, IBinding binding) throws CoreException {
			IASTFileLocation fileLocation = name.getFileLocation();
			if (fileLocation == null) {
				return null;
			}
			int nodeOffset = fileLocation.getNodeOffset();
			int nodeLength = fileLocation.getNodeLength();

			String fileName = fileLocation.getFileName();
			if (DEBUG)
				System.out.println("[CSourceHover] Computing source for " + name + " in " + fileName); //$NON-NLS-1$//$NON-NLS-2$
			IPath location = Path.fromOSString(fileName);
			LocationKind locationKind = LocationKind.LOCATION;
			if (name instanceof IASTName && !name.isReference()) {
				IASTName astName = (IASTName) name;
				if (astName.getTranslationUnit().getFilePath().equals(fileName)) {
					int hoverOffset = fTextRegion.getOffset();
					if (hoverOffset <= nodeOffset && nodeOffset < hoverOffset + fTextRegion.getLength()
							|| hoverOffset >= nodeOffset && hoverOffset < nodeOffset + nodeLength) {
						// Bug 359352 - don't show source if its the same we are hovering on.
						return computeHoverForDeclaration(astName);
					}
					if (fTU.getResource() != null) {
						// Reuse editor buffer for names local to the translation unit
						location = fTU.getResource().getFullPath();
						locationKind = LocationKind.IFILE;
					}
				}
			} else {
				// Try to resolve path to a resource for proper encoding (bug 221029)
				IFile file = EditorUtility.getWorkspaceFileAtLocation(location, fTU);
				if (file != null) {
					location = file.getFullPath();
					locationKind = LocationKind.IFILE;
					if (name instanceof IIndexName) {
						// Need to adjust index offsets to current offsets
						// in case file has been modified since last index time.
						IIndexName indexName = (IIndexName) name;
						long timestamp = indexName.getFile().getTimestamp();
						IPositionConverter converter = CCorePlugin.getPositionTrackerManager()
								.findPositionConverter(file, timestamp);
						if (converter != null) {
							IRegion currentLocation = converter.historicToActual(new Region(nodeOffset, nodeLength));
							nodeOffset = currentLocation.getOffset();
							nodeLength = currentLocation.getLength();
						}
					}
				}
			}
			ITextFileBufferManager mgr = FileBuffers.getTextFileBufferManager();
			mgr.connect(location, locationKind, fMonitor);
			ITextFileBuffer buffer = mgr.getTextFileBuffer(location, locationKind);
			try {
				IRegion nameRegion = new Region(nodeOffset, nodeLength);
				final int nameOffset = nameRegion.getOffset();
				final int sourceStart;
				final int sourceEnd;
				IDocument doc = buffer.getDocument();
				if (nameOffset >= doc.getLength() || nodeLength <= 0) {
					return null;
				}
				if (binding instanceof IMacroBinding) {
					ITypedRegion partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, nameOffset,
							false);
					if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
						int directiveStart = partition.getOffset();
						int commentStart = searchCommentBackward(doc, directiveStart, -1);
						if (commentStart >= 0) {
							sourceStart = commentStart;
						} else {
							sourceStart = directiveStart;
						}
						sourceEnd = directiveStart + partition.getLength();
					} else {
						return null;
					}
				} else {
					// Expand source range to include preceding comment, if any
					boolean isKnR = isKnRSource(name);
					sourceStart = computeSourceStart(doc, nameOffset, binding, isKnR);
					if (sourceStart == CHeuristicScanner.NOT_FOUND) {
						return null;
					}
					sourceEnd = computeSourceEnd(doc, nameOffset + nameRegion.getLength(), binding, name.isDefinition(),
							isKnR);
				}
				String source = buffer.getDocument().get(sourceStart, sourceEnd - sourceStart);
				return source;
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			} finally {
				mgr.disconnect(location, LocationKind.LOCATION, fMonitor);
			}
			return null;
		}

		/**
		 * Computes the hover containing the deduced type for a declaration based on {@code auto}
		 * keyword.
		 *
		 * @param name the name of the declarator
		 * @return the hover text, if the declaration is based on {@code auto} keyword,
		 *     otherwise {@code null}.
		 */
		private String computeHoverForDeclaration(IASTName name) {
			ICPPASTDeclarator declarator = ASTQueries.findAncestorWithType(name, ICPPASTDeclarator.class);
			if (declarator == null)
				return null;
			IASTDeclaration declaration = ASTQueries.findAncestorWithType(declarator, IASTDeclaration.class);
			IASTDeclSpecifier declSpec = null;
			if (declaration instanceof IASTSimpleDeclaration) {
				declSpec = ((IASTSimpleDeclaration) declaration).getDeclSpecifier();
			} else if (declaration instanceof IASTParameterDeclaration) {
				declSpec = ((IASTParameterDeclaration) declaration).getDeclSpecifier();
			}
			if (!(declSpec instanceof ICPPASTSimpleDeclSpecifier)
					|| ((ICPPASTSimpleDeclSpecifier) declSpec).getType() != IASTSimpleDeclSpecifier.t_auto) {
				return null;
			}
			IType type = CPPVisitor.createType(declarator);
			if (type instanceof IProblemType)
				return null;
			return ASTTypeUtil.getType(type, false) + " " + name.getRawSignature(); //$NON-NLS-1$
		}

		/**
		 * Determines if the name is part of a KnR function definition.
		 *
		 * @param name
		 * @return {@code true} if the name is part of a KnR function
		 */
		private boolean isKnRSource(IName name) {
			if (name instanceof IASTName) {
				IASTNode node = (IASTNode) name;
				while (node.getParent() != null) {
					if (node instanceof ICASTKnRFunctionDeclarator) {
						return node.getParent() instanceof IASTFunctionDefinition;
					}
					node = node.getParent();
				}
			}
			return false;
		}

		private int computeSourceStart(IDocument doc, int nameOffset, IBinding binding, boolean isKnR)
				throws BadLocationException {
			int sourceStart = nameOffset;
			CHeuristicScanner scanner = new CHeuristicScanner(doc);
			if (binding instanceof IParameter) {
				if (isKnR) {
					sourceStart = scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { ')', ';' });
				} else {
					sourceStart = scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND,
							new char[] { '>', '(', ',' });
					if (sourceStart > 0 && doc.getChar(sourceStart) == '>') {
						sourceStart = scanner.findOpeningPeer(sourceStart - 1, '<', '>');
						if (sourceStart > 0) {
							sourceStart = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
									new char[] { '(', ',' });
						}
					}
				}
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart = scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else if (binding instanceof ICPPTemplateParameter) {
				sourceStart = scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { '>', '<', ',' });
				if (sourceStart > 0 && doc.getChar(sourceStart) == '>') {
					sourceStart = scanner.findOpeningPeer(sourceStart - 1, '<', '>');
					if (sourceStart > 0) {
						sourceStart = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
								new char[] { '<', ',' });
					}
				}
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart = scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else if (binding instanceof IEnumerator) {
				sourceStart = scanner.scanBackward(nameOffset, CHeuristicScanner.UNBOUND, new char[] { '{', ',' });
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					return sourceStart;
				}
				sourceStart = scanner.findNonWhitespaceForward(sourceStart + 1, nameOffset);
				if (sourceStart == CHeuristicScanner.NOT_FOUND) {
					sourceStart = nameOffset;
				}
			} else {
				final boolean expectClosingBrace;
				IType type = null;
				if (binding instanceof ITypedef) {
					type = ((ITypedef) binding).getType();
				} else if (binding instanceof IVariable) {
					type = ((IVariable) binding).getType();
				}
				expectClosingBrace = (type instanceof ICompositeType || type instanceof IEnumeration)
						&& !(binding instanceof IVariable);
				final int nameLine = doc.getLineOfOffset(nameOffset);
				sourceStart = nameOffset;
				int commentBound;
				if (isKnR) {
					commentBound = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
							new char[] { ')', ';' });
				} else {
					commentBound = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
							new char[] { '{', '}', ';' });
				}
				while (expectClosingBrace && commentBound > 0 && doc.getChar(commentBound) == '}') {
					int openingBrace = scanner.findOpeningPeer(commentBound - 1, '{', '}');
					if (openingBrace != CHeuristicScanner.NOT_FOUND) {
						sourceStart = openingBrace - 1;
					}
					if (isKnR) {
						commentBound = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
								new char[] { ')', ';' });
					} else {
						commentBound = scanner.scanBackward(sourceStart, CHeuristicScanner.UNBOUND,
								new char[] { '{', '}', ';' });
					}
				}
				if (commentBound == CHeuristicScanner.NOT_FOUND) {
					commentBound = -1; // unbound
				}
				sourceStart = Math.min(sourceStart, doc.getLineOffset(nameLine));
				int commentStart = searchCommentBackward(doc, sourceStart, commentBound);
				if (commentStart >= 0) {
					sourceStart = commentStart;
				} else {
					int nextNonWS = scanner.findNonWhitespaceForward(commentBound + 1, sourceStart);
					if (nextNonWS != CHeuristicScanner.NOT_FOUND) {
						int nextNonWSLine = doc.getLineOfOffset(nextNonWS);
						int lineOffset = doc.getLineOffset(nextNonWSLine);
						if (doc.get(lineOffset, nextNonWS - lineOffset).trim().isEmpty()) {
							sourceStart = doc.getLineOffset(nextNonWSLine);
						}
					}
				}
			}
			return sourceStart;
		}

		private int computeSourceEnd(IDocument doc, int start, IBinding binding, boolean isDefinition, boolean isKnR)
				throws BadLocationException {
			int sourceEnd = start;
			CHeuristicScanner scanner = new CHeuristicScanner(doc);
			// Expand forward to the end of the definition/declaration
			boolean searchBrace = false;
			boolean searchSemi = false;
			boolean searchComma = false;
			if (binding instanceof ICompositeType && isDefinition || binding instanceof IEnumeration) {
				searchBrace = true;
			} else if (binding instanceof ICPPTemplateDefinition) {
				searchBrace = true;
			} else if (binding instanceof IFunction && isDefinition) {
				searchBrace = true;
			} else if (binding instanceof IParameter) {
				if (isKnR) {
					searchSemi = true;
				} else {
					searchComma = true;
				}
			} else if (binding instanceof IEnumerator || binding instanceof ICPPTemplateParameter) {
				searchComma = true;
			} else if (binding instanceof IVariable || binding instanceof ITypedef) {
				searchSemi = true;
			} else if (!isDefinition) {
				searchSemi = true;
			}
			if (searchBrace) {
				int brace = scanner.scanForward(start, CHeuristicScanner.UNBOUND, '{');
				if (brace != CHeuristicScanner.NOT_FOUND) {
					sourceEnd = scanner.findClosingPeer(brace + 1, '{', '}');
					if (sourceEnd == CHeuristicScanner.NOT_FOUND) {
						sourceEnd = doc.getLength();
					}
				}
				// Expand region to include whole line
				IRegion lineRegion = doc.getLineInformationOfOffset(sourceEnd);
				sourceEnd = lineRegion.getOffset() + lineRegion.getLength();
			} else if (searchSemi) {
				int semi = scanner.scanForward(start, CHeuristicScanner.UNBOUND, ';');
				if (semi != CHeuristicScanner.NOT_FOUND) {
					sourceEnd = semi + 1;
				}
				// Expand region to include whole line
				IRegion lineRegion = doc.getLineInformationOfOffset(sourceEnd);
				sourceEnd = lineRegion.getOffset() + lineRegion.getLength();
			} else if (searchComma) {
				int bound;
				if (binding instanceof IParameter) {
					bound = scanner.findClosingPeer(start, '(', ')');
				} else if (binding instanceof ICPPTemplateParameter) {
					bound = scanner.findClosingPeer(start, '<', '>');
				} else if (binding instanceof IEnumerator) {
					bound = scanner.findClosingPeer(start, '{', '}');
				} else {
					bound = CHeuristicScanner.NOT_FOUND;
				}
				if (bound == CHeuristicScanner.NOT_FOUND) {
					bound = Math.min(doc.getLength(), start + 100);
				}
				int comma = scanner.scanForward(start, bound, ',');
				if (comma == CHeuristicScanner.NOT_FOUND) {
					// last argument
					sourceEnd = bound;
				} else {
					sourceEnd = comma;
					// expand region to include whole line if rest is comment
					IRegion lineRegion = doc.getLineInformationOfOffset(sourceEnd);
					int lineEnd = lineRegion.getOffset() + lineRegion.getLength();
					int nextNonWS = scanner.findNonWhitespaceForwardInAnyPartition(sourceEnd + 1, lineEnd);
					if (nextNonWS != CHeuristicScanner.NOT_FOUND) {
						String contentType = TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, nextNonWS,
								false);
						if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType)
								|| ICPartitions.C_SINGLE_LINE_COMMENT.equals(contentType)) {
							sourceEnd = lineEnd;
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
		 * @return an array of definitions, never {@code null}
		 * @throws CoreException
		 */
		private IName[] findDefinitions(IASTTranslationUnit ast, IBinding binding) throws CoreException {
			if (binding instanceof ICPPAliasTemplateInstance) {
				binding = ((ICPPAliasTemplateInstance) binding).getTemplateDefinition();
			}
			IName[] declNames = ast.getDefinitionsInAST(binding);
			if (declNames.length == 0 && ast.getIndex() != null) {
				// search definitions in index
				declNames = ast.getIndex().findNames(binding,
						IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
			}
			return declNames;
		}

		/**
		 * Search for declarations for the given binding.
		 *
		 * @param ast  the AST of the translation unit
		 * @param binding  the binding
		 * @return an array of declarations, never {@code null}
		 * @throws CoreException
		 */
		private IName[] findDeclarations(IASTTranslationUnit ast, IBinding binding) throws CoreException {
			IName[] declNames = ast.getDeclarationsInAST(binding);
			if (declNames.length == 0 && ast.getIndex() != null) {
				// search declarations in index
				declNames = ast.getIndex().findNames(binding,
						IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
			}
			return declNames;
		}

		/**
		 * @return the computed source or {@code null}, if no source could be computed
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

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		IEditorPart editor = getEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy workingCopy = manager.getWorkingCopy(input);
			try {
				if (workingCopy == null || !workingCopy.isConsistent()) {
					return null;
				}
			} catch (CModelException e) {
				return null;
			}

			String expression;
			try {
				expression = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				expression = expression.trim();
				if (expression.isEmpty())
					return null;

				// Before trying a search lets make sure that the user is not hovering
				// over a keyword other than 'auto', 'decltype' or 'typeof'.
				if (selectionIsKeyword(expression) && !SemanticUtil.isAutoOrDecltype(expression))
					return null;

				// Try with the indexer.
				String source = searchInIndex(workingCopy, hoverRegion, expression);

				if (source == null || source.trim().isEmpty())
					return null;

				// We are actually interested in the comments, too.
				//				source= removeLeadingComments(source);

				String delim = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

				String[] sourceLines = Strings.convertIntoLines(source);
				String firstLine = sourceLines[0];
				boolean ignoreFirstLine = firstLine.length() > 0 && !Character.isWhitespace(firstLine.charAt(0));
				Strings.trimIndentation(sourceLines, getTabWidth(), getTabWidth(), !ignoreFirstLine);

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
	 * @return the comment start offset or {@code -1}, if no suitable comment was found
	 * @throws BadLocationException
	 */
	private static int searchCommentBackward(IDocument doc, int start, int bound) throws BadLocationException {
		int firstLine = doc.getLineOfOffset(start);
		if (firstLine == 0) {
			return 0;
		}
		ITypedRegion partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, start, true);
		int currentOffset = Math.max(doc.getLineOffset(firstLine - 1), partition.getOffset() - 1);
		int commentOffset = -1;
		while (currentOffset > bound) {
			partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, currentOffset, true);
			currentOffset = partition.getOffset() - 1;
			if (ICPartitions.C_MULTI_LINE_COMMENT.equals(partition.getType())
					|| ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(partition.getType())) {
				final int partitionOffset = partition.getOffset();
				final int startLine = doc.getLineOfOffset(partitionOffset);
				final int lineOffset = doc.getLineOffset(startLine);
				if (partitionOffset == lineOffset
						|| doc.get(lineOffset, partitionOffset - lineOffset).trim().isEmpty()) {
					return lineOffset;
				}
				return commentOffset;
			} else if (ICPartitions.C_SINGLE_LINE_COMMENT.equals(partition.getType())
					|| ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(partition.getType())) {
				final int partitionOffset = partition.getOffset();
				final int startLine = doc.getLineOfOffset(partitionOffset);
				final int lineOffset = doc.getLineOffset(startLine);
				if (partitionOffset == lineOffset
						|| doc.get(lineOffset, partitionOffset - lineOffset).trim().isEmpty()) {
					commentOffset = lineOffset;
					continue;
				}
				return commentOffset;
			} else if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				if (doc.get(partition.getOffset(), partition.getLength()).trim().isEmpty()) {
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
		CCodeReader reader = new CCodeReader();
		IDocument document = new Document(source);
		int i;
		try {
			reader.configureForwardReader(document, 0, document.getLength(), true, false);
			int c = reader.read();
			while (c != -1 && (c == '\r' || c == '\n')) {
				c = reader.read();
			}
			i = reader.getOffset();
			reader.close();
		} catch (IOException e) {
			i = 0;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				CUIPlugin.log(e);
			}
		}

		if (i < 0)
			return source;
		return source.substring(i);
	}

	protected String searchInIndex(final ITranslationUnit tUnit, IRegion textRegion, String selection) {
		final ComputeSourceRunnable computer = new ComputeSourceRunnable(tUnit, textRegion, selection);
		Job job = new Job(CHoverMessages.CSourceHover_jobTitle) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					return ASTProvider.getASTProvider().runOnAST(tUnit, ASTProvider.WAIT_ACTIVE_ONLY, monitor,
							computer);
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
		} catch (InterruptedException e) {
			job.cancel();
			return null;
		}
		return computer.getSource();
	}

	/**
	 * Checks whether the given name is a known keyword.
	 *
	 * @param name
	 * @return {@code true} if the name is a known keyword or {@code false} if the
	 *         name is not considered a keyword
	 */
	private boolean selectionIsKeyword(String name) {
		Set<String> keywords = ParserFactory.getKeywordSet(KeywordSetKey.KEYWORDS, ParserLanguage.CPP);
		return keywords.contains(name);
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				IEditorPart editor = getEditor();
				int orientation = SWT.NONE;
				if (editor instanceof IWorkbenchPartOrientation)
					orientation = ((IWorkbenchPartOrientation) editor).getOrientation();
				return new SourceViewerInformationControl(parent, false, orientation, getTooltipAffordanceString());
			}
		};
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				IEditorPart editor = getEditor();
				int orientation = SWT.NONE;
				if (editor instanceof IWorkbenchPartOrientation)
					orientation = ((IWorkbenchPartOrientation) editor).getOrientation();
				return new SourceViewerInformationControl(parent, true, orientation, null);
			}
		};
	}
}
