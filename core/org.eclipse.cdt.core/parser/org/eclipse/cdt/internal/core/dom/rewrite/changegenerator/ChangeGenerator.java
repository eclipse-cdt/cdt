/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ContainerNode;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ProblemRuntimeException;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ChangeGenerator extends ASTVisitor {
	private final Map<IASTNode, Map<ModificationKind, List<ASTModification>>> classifiedModifications =
			new HashMap<IASTNode, Map<ModificationKind, List<ASTModification>>>();
	private int processedOffset;
	private MultiTextEdit rootEdit;
	private CompositeChange change;

	private final ASTModificationStore modificationStore;
	private final NodeCommentMap commentMap;

	{
		shouldVisitArrayModifiers= true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitInitializers = true;
		shouldVisitNamespaces = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitPointerOperators = true;
		shouldVisitStatements = true;
		shouldVisitTemplateParameters = true;
		shouldVisitTranslationUnit = true;
		shouldVisitTypeIds = true;
	}

	public ChangeGenerator(ASTModificationStore modificationStore, NodeCommentMap commentMap) {
		this.modificationStore = modificationStore;
		this.commentMap = commentMap;
	}

	public void generateChange(IASTNode rootNode) throws ProblemRuntimeException {
		generateChange(rootNode, this);
	}

	private void generateChange(IASTNode rootNode, ASTVisitor pathProvider)
			throws ProblemRuntimeException {
		change = new CompositeChange(ChangeGeneratorMessages.ChangeGenerator_compositeChange);
		classifyModifications();
		rootNode.accept(pathProvider);
		if (rootEdit == null)
			return;
		IASTTranslationUnit ast = rootNode.getTranslationUnit();
		String source = ast.getRawSignature();
		ITranslationUnit tu = ast.getOriginatingTranslationUnit();
		formatChangedCode(source, tu);
		TextFileChange subchange= ASTRewriteAnalyzer.createCTextFileChange((IFile) tu.getResource());
		subchange.setEdit(rootEdit);
		change.add(subchange);
	}

	private void classifyModifications() {
		ASTModificationMap rootModifications = modificationStore.getRootModifications();
		if (rootModifications == null)
			return;

		for (IASTNode node : rootModifications.getModifiedNodes()) {
			List<ASTModification> modifications = rootModifications.getModificationsForNode(node);
			for (ASTModification modification : modifications) {
				Map<ModificationKind, List<ASTModification>> map = classifiedModifications.get(node);
				if (map == null) {
					map = new TreeMap<ModificationKind, List<ASTModification>>();
					classifiedModifications.put(node, map);
				}
				ModificationKind kind = modification.getKind();
				List<ASTModification> list = map.get(kind);
				if (list == null) {
					list = new ArrayList<ASTModification>(2);
					map.put(kind, list);
				}
				list.add(modification);
			}
		}
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		IASTFileLocation location = tu.getFileLocation();
		processedOffset = location.getNodeOffset();
		return super.visit(tu);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		handleAppends(tu);
		return super.leave(tu);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		handleInserts(declaration);
		if (requiresRewrite(declaration)) {
			handleReplace(declaration);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		handleInserts(declarator);
		if (requiresRewrite(declarator)) {
			handleReplace(declarator);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declarator);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		handleInserts(arrayModifier);
		if (requiresRewrite(arrayModifier)) {
			handleReplace(arrayModifier);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(arrayModifier);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		handleInserts(namespaceDefinition);
		if (requiresRewrite(namespaceDefinition)) {
			handleReplace(namespaceDefinition);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(namespaceDefinition);
	}

	@Override
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		if (!requiresRewrite(namespaceDefinition)) {
			handleAppends(namespaceDefinition);
		}
		return super.leave(namespaceDefinition);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		handleInserts(declSpec);
		if (requiresRewrite(declSpec)) {
			handleReplace(declSpec);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declSpec);
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if (!requiresRewrite(declSpec)) {
			handleAppends(declSpec);
		}
		return super.leave(declSpec);
	}

	@Override
	public int visit(IASTExpression expression) {
		handleInserts(expression);
		if (requiresRewrite(expression)) {
			handleReplace(expression);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		handleInserts(initializer);
		if (requiresRewrite(initializer)) {
			handleReplace(initializer);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(initializer);
	}

	@Override
	public int visit(IASTName name) {
		handleInserts(name);
		if (requiresRewrite(name)) {
			handleReplace(name);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		handleInserts(parameterDeclaration);
		if (requiresRewrite(parameterDeclaration)) {
			handleReplace(parameterDeclaration);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(parameterDeclaration);
	}

	@Override
	public int visit(IASTPointerOperator pointerOperator) {
		handleInserts(pointerOperator);
		if (requiresRewrite(pointerOperator)) {
			handleReplace(pointerOperator);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(pointerOperator);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		handleInserts(typeId);
		if (requiresRewrite(typeId)) {
			handleReplace(typeId);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(typeId);
	}

	@Override
	public int visit(IASTStatement statement) {
		handleInserts(statement);
		if (requiresRewrite(statement)) {
			handleReplace(statement);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(statement);
	}

	@Override
	public int leave(IASTStatement statement) {
		if (!requiresRewrite(statement)) {
			handleAppends(statement);
		}
		return super.leave(statement);
	}

	private void addChildEdit(TextEdit edit) {
		rootEdit.addChild(edit);
		processedOffset = edit.getExclusiveEnd();
	}

	private TextEdit clippedEdit(TextEdit edit, IRegion region) {
		if ((edit.getOffset() < region.getOffset() && edit.getExclusiveEnd() <= region.getOffset()) ||
				edit.getOffset() >= endOffset(region)) {
			return null;
		}
		int offset = Math.max(edit.getOffset(), region.getOffset());
		int length = Math.min(endOffset(edit), endOffset(region)) - offset;
		if (offset == edit.getOffset() && length == edit.getLength()) {
			// InsertEdit always satisfies the above condition.
			return edit;
		}
		if (edit instanceof DeleteEdit) {
			return new DeleteEdit(offset, length);
		} if (edit instanceof ReplaceEdit) {
			String replacement = ((ReplaceEdit) edit).getText();
			int start = Math.max(offset - edit.getOffset(), 0);
			int end = Math.min(endOffset(region) - offset, replacement.length());
			if (end <= start) {
				return new DeleteEdit(offset, length);
			}
			return new ReplaceEdit(offset, length, replacement.substring(start, end));
		} else {
			throw new IllegalArgumentException("Unexpected edit type: " + edit.getClass().getSimpleName()); //$NON-NLS-1$
		}
	}

	/**
	 * Applies the C++ code formatter to the code affected by refactoring.
	 *
	 * @param code The code being modified.
	 * @param tu The translation unit containing the code.
	 */
	private void formatChangedCode(String code, ITranslationUnit tu) {
		IDocument document = new Document(code);
		try {
			TextEdit edit = rootEdit.copy();
			// Apply refactoring changes to a temporary document.
			edit.apply(document, TextEdit.UPDATE_REGIONS);

			// Expand regions affected by the changes to cover complete lines. We calculate two
			// sets of regions, reflecting the state of the document before and after
			// the refactoring changes.
			TextEdit[] appliedEdits = edit.getChildren();
			TextEdit[] edits = rootEdit.removeChildren();
			IRegion[] regions = new IRegion[appliedEdits.length];
			int numRegions = 0;
			int prevEnd = -1;
			for (int i = 0; i < appliedEdits.length; i++) {
				edit = appliedEdits[i];
				int offset = edit.getOffset();
				int end = offset + edit.getLength();
				int newOffset = document.getLineInformationOfOffset(offset).getOffset();
				edit = edits[i];
				int originalEnd = edit.getExclusiveEnd();
				// Expand to the end of the line unless the end of the edit region is at
				// the beginning of line both, before and after the change.
				int newEnd = (originalEnd == 0 || code.charAt(originalEnd - 1) == '\n') && end == newOffset ?
						end : endOffset(document.getLineInformationOfOffset(end));
				if (newOffset <= prevEnd) {
					numRegions--;
					newOffset = regions[numRegions].getOffset();
				}
				prevEnd = newEnd;
				regions[numRegions] = new Region(newOffset, newEnd - newOffset);
				numRegions++;
			}

			if (numRegions < regions.length) {
				regions = Arrays.copyOf(regions, numRegions);
			}

			// Calculate formatting changes for the regions after the refactoring changes.
			ICProject project = tu.getCProject();
	        Map<String, Object> options = new HashMap<String, Object>(project.getOptions(true));
	        options.put(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT, tu);
			// Allow all comments to be indented.
			options.put(DefaultCodeFormatterConstants.FORMATTER_COMMENT_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN,
					DefaultCodeFormatterConstants.FALSE);
			CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
			code = document.get();
			TextEdit[] formatEdits = formatter.format(CodeFormatter.K_TRANSLATION_UNIT, code,
					regions, TextUtilities.getDefaultLineDelimiter(document));

			TextEdit combinedFormatEdit = new MultiTextEdit();
			for (TextEdit formatEdit : formatEdits) {
				combinedFormatEdit = TextEditUtil.merge(combinedFormatEdit, formatEdit);
			}
			formatEdits = TextEditUtil.flatten(combinedFormatEdit).removeChildren();

			MultiTextEdit result = new MultiTextEdit();
			int delta = 0;
			TextEdit edit1 = null;
			TextEdit edit2 = null;
			int i = 0;
			int j = 0;
			while (true) {
				if (edit1 == null && i < edits.length)
					edit1 = edits[i++];
				if (edit2 == null && j < formatEdits.length)
					edit2 = formatEdits[j++];
				if (edit1 == null) {
					if (edit2 == null)
						break;
					edit2.moveTree(-delta);
					result.addChild(edit2);
					edit2 = null;
				} else if (edit2 == null) {
					delta += TextEditUtil.delta(edit1);
					result.addChild(edit1);
					edit1 = null;
				} else {
					if (edit2.getExclusiveEnd() - delta <= edit1.getOffset()) {
						edit2.moveTree(-delta);
						result.addChild(edit2);
						edit2 = null;
					} else {
						TextEdit piece = clippedEdit(edit2, new Region(-1, edit1.getOffset() + delta));
						if (piece != null) {
							piece.moveTree(-delta);
							result.addChild(piece);
						}
						int d = TextEditUtil.delta(edit1);
						Region region = new Region(edit1.getOffset() + delta, edit1.getLength() + d);
						int end = endOffset(region);
						MultiTextEdit format = new MultiTextEdit();
						while ((piece = clippedEdit(edit2, region)) != null) {
							format.addChild(piece);
							// The warning "The variable edit2 may be null at this location" is bogus.
							// Make the compiler happy:
							if (edit2 != null) {
								if (edit2.getExclusiveEnd() >= end || j >= formatEdits.length) {
									break;
								}
							}
							edit2 = formatEdits[j++];
						}
						if (format.hasChildren()) {
							format.moveTree(-delta);
							edit1 = applyEdit(format, edit1);
						}
						delta += d;
						result.addChild(edit1);
						edit1 = null;

						edit2 = clippedEdit(edit2, new Region(end, Integer.MAX_VALUE - end));
					}
				}
			}
			rootEdit = result;
		} catch (MalformedTreeException e) {
			CCorePlugin.log(e);
		} catch (BadLocationException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * Applies source edit to the target one and returns the combined edit.
	 */
	private TextEdit applyEdit(TextEdit source, TextEdit target)
			throws MalformedTreeException, BadLocationException {
		source.moveTree(-target.getOffset());
		String text;
		if (target instanceof InsertEdit) {
			text = ((InsertEdit) target).getText();
		} else if (target instanceof ReplaceEdit) {
			text = ((ReplaceEdit) target).getText();
		} else {
			text = ""; //$NON-NLS-1$
		}

		IDocument document = new Document(text);
		source.apply(document, TextEdit.NONE);
		text = document.get();
		if (target.getLength() == 0) {
			return new InsertEdit(target.getOffset(), text);
		} else {
			return new ReplaceEdit(target.getOffset(), target.getLength(), text);
		}
	}

	private int endOffset(IRegion region) {
		return region.getOffset() + region.getLength();
	}

	private int endOffset(TextEdit edit) {
		return edit.getOffset() + edit.getLength();
	}

	private int endOffset(IASTFileLocation nodeLocation) {
		return nodeLocation.getNodeOffset() + nodeLocation.getNodeLength();
	}

	private int endOffset(IASTNode node) {
		return endOffset(node.getFileLocation());
	}

	private int offset(IASTNode node) {
		return node.getFileLocation().getNodeOffset();
	}

	private void handleInserts(IASTNode anchorNode) {
		List<ASTModification> modifications = getModifications(anchorNode, ModificationKind.INSERT_BEFORE);
		if (modifications.isEmpty())
			return;
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		IASTNode newNode = null;
		for (ASTModification modification : modifications) {
			boolean first = newNode == null;
			newNode = modification.getNewNode();
			if (first) {
				IASTNode prevNode = getPreviousSiblingOrPreprocessorNode(anchorNode);
				if (prevNode != null) {
					if (ASTWriter.requireBlankLineInBetween(prevNode, newNode)) {
						writer.newLine();
					}
				} else if (anchorNode.getParent() instanceof ICPPASTNamespaceDefinition) {
					writer.newLine();
				}
			}
			newNode.accept(writer);
			if (getContainingNodeList(anchorNode) != null) {
				writer.getScribe().print(", "); //$NON-NLS-1$
			}
		}
		if (ASTWriter.requireBlankLineInBetween(newNode, anchorNode)) {
			writer.newLine();
		}
		int insertPos = getOffsetIncludingComments(anchorNode);
		int length = 0;
		if (writer.getScribe().isAtBeginningOfLine()) {
			String tuCode = anchorNode.getTranslationUnit().getRawSignature();
			insertPos = skipPrecedingWhitespace(tuCode, insertPos);
			length = insertPos;
			insertPos = skipPrecedingBlankLines(tuCode, insertPos);
			length -= insertPos;
		}
		addToRootEdit(anchorNode);
		String code = writer.toString();
		if (!code.isEmpty())
			addChildEdit(new InsertEdit(insertPos, code));
		if (length != 0)
			addChildEdit(new DeleteEdit(insertPos, length));
	}

	private void handleReplace(IASTNode node) {
		List<ASTModification> modifications = getModifications(node, ModificationKind.REPLACE);
		String source = node.getTranslationUnit().getRawSignature();
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		IASTFileLocation fileLocation = node.getFileLocation();
		addToRootEdit(node);
		if (modifications.size() == 1 && modifications.get(0).getNewNode() == null) {
			// There is no replacement. We are deleting a piece of existing code.
			int offset = getOffsetIncludingComments(node);
			int endOffset = getEndOffsetIncludingComments(node);
			offset = Math.max(skipPrecedingBlankLines(source, offset), processedOffset);
			endOffset = skipTrailingBlankLines(source, endOffset);
			IASTNode[] siblingsList = getContainingNodeList(node);
			if (siblingsList != null) {
				if (siblingsList.length > 1) {
					if (node == siblingsList[0]) {
						endOffset = skipToTrailingDelimiter(source, ',', endOffset);
					} else {
						offset = skipToPrecedingDelimiter(source, ',', offset);
					}
				} else if (node.getPropertyInParent() == ICPPASTFunctionDefinition.MEMBER_INITIALIZER) {
					offset = skipToPrecedingDelimiter(source, ':', offset);
				}
			}
			IASTNode prevNode = getPreviousSiblingOrPreprocessorNode(node);
			IASTNode nextNode = getNextSiblingOrPreprocessorNode(node);
			if (prevNode != null && nextNode != null) {
				if (ASTWriter.requireBlankLineInBetween(prevNode, nextNode)) {
					writer.newLine();
				}
			} else if (node.getParent() instanceof ICPPASTNamespaceDefinition) {
				writer.newLine();
			}
			String code = writer.toString();
			if (endOffset > offset)
				addChildEdit(new DeleteEdit(offset, endOffset - offset));
			if (!code.isEmpty())
				addChildEdit(new InsertEdit(endOffset, code));
		} else {
			node.accept(writer);
			String code = writer.toString();
			int offset = fileLocation.getNodeOffset();
			int endOffset = offset + fileLocation.getNodeLength();
			String lineSeparator = writer.getScribe().getLineSeparator();
			if (code.endsWith(lineSeparator)) {
				code = code.substring(0, code.length() - lineSeparator.length());
			}
			addChildEdit(new ReplaceEdit(offset, endOffset - offset, code));
			if (node instanceof IASTStatement || node instanceof IASTDeclaration) {
				// Include trailing comments in the area to be replaced.
				int commentEnd = getEndOffsetIncludingTrailingComments(node);
				if (commentEnd > endOffset)
					addChildEdit(new DeleteEdit(endOffset, commentEnd - endOffset));
			}
		}
	}

	private void handleAppends(IASTNode node) {
		List<ASTModification> modifications = getModifications(node, ModificationKind.APPEND_CHILD);
		if (modifications.isEmpty())
			return;
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		ReplaceEdit anchor = getAppendAnchor(node);
		Assert.isNotNull(anchor);
		IASTNode precedingNode = getLastNodeBeforeAppendPoint(node);
		for (ASTModification modification : modifications) {
			IASTNode newNode = modification.getNewNode();
			if (precedingNode != null) {
				if (ASTWriter.requireBlankLineInBetween(precedingNode, newNode)) {
					writer.newLine();
				}
			} else if (node instanceof ICPPASTNamespaceDefinition) {
				writer.newLine();
			}
			precedingNode = null;
			newNode.accept(writer);
		}
		if (node instanceof ICPPASTNamespaceDefinition) {
			writer.newLine();
		}
		addToRootEdit(node);
		String code = writer.toString();
		if (!code.isEmpty())
			addChildEdit(new InsertEdit(anchor.getOffset(), code));
		addChildEdit(new ReplaceEdit(anchor.getOffset(), anchor.getLength(), anchor.getText()));
		processedOffset = endOffset(node);
	}

	private void handleAppends(IASTTranslationUnit node) {
		List<ASTModification> modifications = getModifications(node, ModificationKind.APPEND_CHILD);
		if (modifications.isEmpty())
			return;

		IASTNode prevNode = null;
		IASTDeclaration[] declarations = node.getDeclarations();
		if (declarations.length != 0) {
			prevNode = declarations[declarations.length - 1];
		} else {
			IASTPreprocessorStatement[] preprocessorStatements = node.getAllPreprocessorStatements();
			if (preprocessorStatements.length != 0) {
				prevNode = preprocessorStatements[preprocessorStatements.length - 1];
			}
		}
		int offset = prevNode != null ? getEndOffsetIncludingComments(prevNode) : 0;
		String source = node.getRawSignature();
		int endOffset = skipTrailingBlankLines(source, offset);

		addToRootEdit(node);
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		IASTNode newNode = null;
		for (ASTModification modification : modifications) {
			boolean first = newNode == null;
			newNode = modification.getNewNode();
			if (first) {
				if (prevNode != null) {
					writer.newLine();
					if (ASTWriter.requireBlankLineInBetween(prevNode, newNode)) {
						writer.newLine();
					}
				}
			}
			newNode.accept(writer);
			// TODO(sprigogin): Temporary workaround for invalid handling of line breaks in StatementWriter
			if (!writer.toString().endsWith("\n")) //$NON-NLS-1$
				writer.newLine();

		}
		if (prevNode != null) {
			IASTNode nextNode = getNextSiblingOrPreprocessorNode(prevNode);
			if (nextNode != null && ASTWriter.requireBlankLineInBetween(newNode, nextNode)) {
				writer.newLine();
			}
		}

		String code = writer.toString();
		if (!code.isEmpty())
			addChildEdit(new InsertEdit(offset, code));
		if (endOffset > offset)
			addChildEdit(new DeleteEdit(offset, endOffset - offset));
	}

	/**
	 * Returns the list of nodes the given node is part of, for example function parameters if
	 * the node is a parameter.
	 *
	 * @param node the node possibly belonging to a list.
	 * @return the list of nodes containing the given node, or <code>null</code> if the node
	 *     does not belong to a list
	 */
	private IASTNode[] getContainingNodeList(IASTNode node) {
		if (node.getPropertyInParent() == IASTStandardFunctionDeclarator.FUNCTION_PARAMETER) {
			return ((IASTStandardFunctionDeclarator) node.getParent()).getParameters();
		} else if (node.getPropertyInParent() == IASTExpressionList.NESTED_EXPRESSION) {
			return ((IASTExpressionList) node.getParent()).getExpressions();
		} else if (node.getPropertyInParent() == ICPPASTFunctionDefinition.MEMBER_INITIALIZER) {
			return ((ICPPASTFunctionDefinition) node.getParent()).getMemberInitializers();
		} else if (node.getPropertyInParent() == ICPPASTFunctionDeclarator.EXCEPTION_TYPEID) {
			return ((ICPPASTFunctionDeclarator) node.getParent()).getExceptionSpecification();
		}

		return null;
	}

	private IASTNode getLastNodeBeforeAppendPoint(IASTNode node) {
		IASTNode[] children;
		if (node instanceof ICPPASTNamespaceDefinition) {
			children = ((ICPPASTNamespaceDefinition) node).getDeclarations(true);
		} else if (node instanceof IASTCompositeTypeSpecifier) {
			children = ((IASTCompositeTypeSpecifier) node).getDeclarations(true);
		} else {
			children = node.getChildren();
		}
		for (int i = children.length; --i >= 0;) {
			IASTNode child = getReplacementNode(children[i]);
			if (child != null)
				return child;
		}
		return null;
	}

	private IASTNode getReplacementNode(IASTNode node) {
		List<ASTModification> modifications = getModifications(node, ModificationKind.REPLACE);
		if (!modifications.isEmpty()) {
			node = modifications.get(modifications.size() - 1).getNewNode();
		}
		return node;
	}

	private IASTNode getPreviousSiblingNode(IASTNode node) {
		IASTNode parent = node.getParent();
		IASTNode[] siblings;
		if (parent instanceof ICPPASTNamespaceDefinition) {
			siblings = ((ICPPASTNamespaceDefinition) parent).getDeclarations(true);
		} else if (parent instanceof IASTCompositeTypeSpecifier) {
			siblings = ((IASTCompositeTypeSpecifier) parent).getDeclarations(true);
		} else {
			siblings = parent.getChildren();
		}
		boolean beforeNode = false;
		for (int i = siblings.length; --i >= 0;) {
			IASTNode sibling = siblings[i];
			if (sibling == node) {
				beforeNode = true;
			} else if (beforeNode && getReplacementNode(sibling) != null) {
				return sibling;
			}
		}
		return null;
	}

	private IASTNode getPreviousSiblingOrPreprocessorNode(IASTNode node) {
		int offset = offset(node);
		IASTTranslationUnit ast = node.getTranslationUnit();
		IASTPreprocessorStatement[] preprocessorStatements = ast.getAllPreprocessorStatements();
		int low = 0;
		int high = preprocessorStatements.length;
		while (low < high) {
			int mid = (low + high) / 2;
			IASTNode statement = preprocessorStatements[mid];
			if (statement.isPartOfTranslationUnitFile() && endOffset(statement) > offset) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		IASTNode statement = --low >= 0 ? preprocessorStatements[low] : null;

		IASTNode originalSibling = getPreviousSiblingNode(node);
		IASTNode sibling = originalSibling == null ? null : getReplacementNode(originalSibling);
		if (statement == null || !statement.isPartOfTranslationUnitFile()) {
			return sibling;
		}
		if (sibling == null) {
			IASTNode parent = node.getParent();
			if (offset(parent) >= endOffset(statement))
				return null;
			return statement;
		}

		return endOffset(originalSibling) >= endOffset(statement) ? sibling : statement; 
	}

	private IASTNode getNextSiblingNode(IASTNode node) {
		IASTNode parent = node.getParent();
		IASTNode[] siblings;
		if (parent instanceof ICPPASTNamespaceDefinition) {
			siblings = ((ICPPASTNamespaceDefinition) parent).getDeclarations(true);
		} else if (parent instanceof IASTCompositeTypeSpecifier) {
			siblings = ((IASTCompositeTypeSpecifier) parent).getDeclarations(true);
		} else {
			siblings = parent.getChildren();
		}
		boolean beforeNode = false;
		for (IASTNode sibling : siblings) {
			if (sibling == node) {
				beforeNode = true;
			} else if (beforeNode && getReplacementNode(sibling) != null) {
				return sibling;
			}
		}
		return null;
	}

	private IASTNode getNextSiblingOrPreprocessorNode(IASTNode node) {
		int endOffset = endOffset(node);
		IASTTranslationUnit ast = node.getTranslationUnit();
		IASTPreprocessorStatement[] preprocessorStatements = ast.getAllPreprocessorStatements();
		int low = 0;
		int high = preprocessorStatements.length;
		while (low < high) {
			int mid = (low + high) / 2;
			IASTNode statement = preprocessorStatements[mid];
			if (statement.isPartOfTranslationUnitFile() && offset(statement) > endOffset) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		IASTNode statement =  high < preprocessorStatements.length ? preprocessorStatements[high] : null;

		IASTNode originalSibling = getNextSiblingNode(node);
		IASTNode sibling = originalSibling == null ? null : getReplacementNode(originalSibling);
		if (statement == null || !statement.isPartOfTranslationUnitFile()) {
			return sibling;
		}
		if (sibling == null) {
			IASTNode parent = node.getParent();
			if (endOffset(parent) <= offset(statement))
				return null;
			return statement;
		}

		return offset(originalSibling) <= offset(statement) ? sibling : statement; 
	}

	/**
	 * Returns a replace edit whose offset is the position where child appended nodes should be
	 * inserted at. The text contains the content of the code region that will be disturbed by
	 * the insertion.
	 * @param node The node to append children to.
	 * @return a ReplaceEdit object, or <code>null</code> if the node does not support appending
	 *     children to it.
	 */
	private ReplaceEdit getAppendAnchor(IASTNode node) {
		if (!(node instanceof IASTCompositeTypeSpecifier ||
				node instanceof IASTCompoundStatement ||
				node instanceof ICPPASTNamespaceDefinition)) {
			return null;
		}
		String code = node.getRawSignature();
		IASTFileLocation location = node.getFileLocation();
		int pos = location.getNodeOffset() + location.getNodeLength();
		int len = code.endsWith("}") ? 1 : 0; //$NON-NLS-1$
		int insertPos = code.length() - len;
		int startOfLine = skipPrecedingBlankLines(code, insertPos);
		if (startOfLine == insertPos) {
			// Include the closing brace in the region that will be reformatted.
			return new ReplaceEdit(pos - len, len, code.substring(insertPos));
		}
		return new ReplaceEdit(location.getNodeOffset() + startOfLine, insertPos - startOfLine, ""); //$NON-NLS-1$
	}

	/**
	 * Skips whitespace between the beginning of the line and the given position.
	 *
	 * @param text The text to scan.
	 * @param startPos The start position.
	 * @return The beginning of the line containing the start position, if there are no
	 *     non-whitespace characters between the beginning of the line and the start position.
	 *     Otherwise returns the start position.
	 */
	private int skipPrecedingWhitespace(String text, int startPos) {
		for (int pos = startPos; --pos >= 0; ) {
			char c = text.charAt(pos);
			if (c == '\n') {
				return pos + 1;
			} else if (!Character.isWhitespace(c)) {
				return startPos;
			}
		}
		return 0;
	}

	/**
	 * Skips whitespace between the beginning of the line and the given position and blank lines
	 * above that.
	 *
	 * @param text The text to scan.
	 * @param startPos The start position.
	 * @return The beginning of the first blank line preceding the start position,
	 *     or beginning of the current line, if there are no non-whitespace characters between
	 *     the beginning of the line and the start position.
	 *     Otherwise returns the start position.
	 */
	private int skipPrecedingBlankLines(String text, int startPos) {
		for (int pos = startPos; --pos >= 0;) {
			char c = text.charAt(pos);
			if (c == '\n') {
				startPos = pos + 1;
			} else if (!Character.isWhitespace(c)) {
				return startPos;
			}
		}
		return 0;
	}

	/**
	 * Skips whitespace between the given position and the end of the line and blank lines
	 * below that.
	 *
	 * @param text The text to scan.
	 * @param startPos The start position.
	 * @return The beginning of the first non-blank line following the start position, if there are
	 *     no non-whitespace characters between the start position and the end of the line.
	 *     Otherwise returns the start position.
	 */
	private int skipTrailingBlankLines(String text, int startPos) {
		for (int pos = startPos; pos < text.length(); pos++) {
			char c = text.charAt(pos);
			if (c == '\n') {
				startPos = pos + 1;
			} else if (!Character.isWhitespace(c)) {
				return startPos;
			}
		}
		return text.length();
	}

	/**
	 * Skips whitespace to the left of the given position until the given delimiter character
	 * is found.
	 *
	 * @param text The text to scan.
	 * @param delimiter the delimiter to find
	 * @param startPos The start position.
	 * @return The position of the given delimiter, or the start position if a non-whitespace
	 *     character is encountered before the given delimiter.
	 */
	private int skipToPrecedingDelimiter(String text, char delimiter, int startPos) {
		for (int pos = startPos; --pos >= 0; ) {
			char c = text.charAt(pos);
			if (c == delimiter) {
				return pos;
			} else if (!Character.isWhitespace(c)) {
				return startPos;
			}
		}
		return startPos;
	}

	/**
	 * Skips whitespace to the right of the given position until the given delimiter character
	 * is found.
	 *
	 * @param text The text to scan.
	 * @param delimiter the delimiter to find
	 * @param startPos The start position.
	 * @return The position after the given delimiter, or the start position if a non-whitespace
	 *     character is encountered before the given delimiter.
	 */
	private int skipToTrailingDelimiter(String text, char delimiter, int startPos) {
		for (int pos = startPos; pos < text.length(); pos++) {
			char c = text.charAt(pos);
			if (c == delimiter) {
				return pos + 1;
			} else if (!Character.isWhitespace(c)) {
				return startPos;
			}
		}
		return startPos;
	}

	private void addToRootEdit(IASTNode modifiedNode) {
		if (rootEdit == null) {
			rootEdit = new MultiTextEdit();
		}
		TextEditGroup editGroup = new TextEditGroup(ChangeGeneratorMessages.ChangeGenerator_group);
		for (List<ASTModification> modifications : getModifications(modifiedNode).values()) {
			for (ASTModification modification : modifications) {
				if (modification.getAssociatedEditGroup() != null) {
					editGroup = modification.getAssociatedEditGroup();
					rootEdit.addChildren(editGroup.getTextEdits());
					return;
				}
			}
		}
	}

	private int getOffsetIncludingComments(IASTNode node) {
		int nodeOffset = offset(node);

		List<IASTComment> comments = commentMap.getAllCommentsForNode(node);
		if (!comments.isEmpty()) {
			int startOffset = nodeOffset;
			for (IASTComment comment : comments) {
				int commentOffset = offset(comment);
				if (commentOffset < startOffset) {
					startOffset = commentOffset;
				}
			}
			nodeOffset = startOffset;
		}
		return nodeOffset;
	}

	private int getEndOffsetIncludingComments(IASTNode node) {
		int endOffset = 0;
		while (true) {
			IASTFileLocation fileLocation = node.getFileLocation();
			if (fileLocation != null)
				endOffset = Math.max(endOffset, endOffset(fileLocation));
			List<IASTComment> comments = commentMap.getAllCommentsForNode(node);
			if (!comments.isEmpty()) {
				for (IASTComment comment : comments) {
					int commentEndOffset = endOffset(comment);
					if (commentEndOffset >= endOffset) {
						endOffset = commentEndOffset;
					}
				}
			}
			IASTNode[] children = node.getChildren();
			if (children.length == 0)
				break;
			node = children[children.length - 1];
		}
		return endOffset;
	}

	private int getEndOffsetIncludingTrailingComments(IASTNode node) {
		int endOffset = 0;
		while (true) {
			IASTFileLocation fileLocation = node.getFileLocation();
			if (fileLocation != null)
				endOffset = Math.max(endOffset, endOffset(fileLocation));
			List<IASTComment> comments = commentMap.getTrailingCommentsForNode(node);
			if (!comments.isEmpty()) {
				for (IASTComment comment : comments) {
					int commentEndOffset = endOffset(comment);
					if (commentEndOffset >= endOffset) {
						endOffset = commentEndOffset;
					}
				}
			}
			IASTNode[] children = node.getChildren();
			if (children.length == 0)
				break;
			node = children[children.length - 1];
		}
		return endOffset;
	}

	private Map<ModificationKind, List<ASTModification>> getModifications(IASTNode node) {
		Map<ModificationKind, List<ASTModification>> modifications = classifiedModifications.get(node);
		if (modifications == null)
			return Collections.emptyMap();
		return modifications;
	}

	private List<ASTModification> getModifications(IASTNode node, ModificationKind kind) {
		Map<ModificationKind, List<ASTModification>> allModifications = getModifications(node);
		List<ASTModification> modifications = allModifications.get(kind);
		if (modifications == null)
			return Collections.emptyList();
		return modifications;
	}

	private boolean requiresRewrite(IASTNode node) {
		if (!getModifications(node, ModificationKind.REPLACE).isEmpty())
			return true;
		for (ASTModification modification : getModifications(node, ModificationKind.APPEND_CHILD)) {
			if (!isAppendable(modification))
				return true;
		}
		return false;
	}

	private boolean isAppendable(ASTModification modification) {
		if (modification.getKind() != ModificationKind.APPEND_CHILD)
			return false;
		IASTNode node = modification.getNewNode();
		if (node instanceof ContainerNode) {
			for (IASTNode containedNode : ((ContainerNode) node).getNodes()) {
				if (!(containedNode instanceof IASTDeclaration || containedNode instanceof IASTStatement))
					return false;
			}
			return true;
		}
		return node instanceof IASTDeclaration || node instanceof IASTStatement;
	}

	public Change getChange() {
		return change;
	}
}
