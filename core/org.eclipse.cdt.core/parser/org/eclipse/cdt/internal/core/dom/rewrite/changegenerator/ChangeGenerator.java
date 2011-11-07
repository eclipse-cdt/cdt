/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ProblemRuntimeException;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.FileContentHelper;
import org.eclipse.cdt.internal.core.dom.rewrite.util.FileHelper;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.formatter.CCodeFormatter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
	private final LinkedHashMap<String, Integer> sourceOffsets = new LinkedHashMap<String, Integer>();
	public LinkedHashMap<IASTNode, List<ASTModification>> modificationParent = new LinkedHashMap<IASTNode, List<ASTModification>>();
	private final LinkedHashMap<IFile, MultiTextEdit> changes = new LinkedHashMap<IFile, MultiTextEdit>();
	private CompositeChange change;

	private final ASTModificationStore modificationStore;
	private NodeCommentMap commentMap;

	{
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDeclarators = true;
		shouldVisitArrayModifiers= true;
		shouldVisitInitializers = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitTranslationUnit = true;
	}

	public ChangeGenerator(ASTModificationStore modificationStore, NodeCommentMap commentMap) {
		this.modificationStore = modificationStore;
		this.commentMap = commentMap;
	}

	public void generateChange(IASTNode rootNode) throws ProblemRuntimeException {
		generateChange(rootNode, this);
	}

	public void generateChange(IASTNode rootNode, ASTVisitor pathProvider)
			throws ProblemRuntimeException {
		change = new CompositeChange(Messages.ChangeGenerator_compositeChange);
		initParentModList();
		rootNode.accept(pathProvider);
		for (IFile currentFile : changes.keySet()) {
			MultiTextEdit edit = changes.get(currentFile);
			edit = formatChangedCode(edit, currentFile);
			TextFileChange subchange= ASTRewriteAnalyzer.createCTextFileChange(currentFile);
			subchange.setEdit(edit);
			change.add(subchange);
		}
	}

	private void initParentModList() {
		ASTModificationMap rootModifications = modificationStore.getRootModifications();
		if (rootModifications != null) {
			for (IASTNode modifiedNode : rootModifications.getModifiedNodes()) {
				List<ASTModification> modificationsForNode = rootModifications.getModificationsForNode(modifiedNode);
				IASTNode modifiedNodeParent = determineParentToBeRewritten(modifiedNode, modificationsForNode);
				List<ASTModification> list = modificationParent.get(modifiedNodeParent != null ?
						modifiedNodeParent : modifiedNode);
				if (list != null) {
					list.addAll(modificationsForNode);
				} else {
					List<ASTModification> modifiableList = new ArrayList<ASTModification>(modificationsForNode);
					modificationParent.put(modifiedNodeParent != null ?
							modifiedNodeParent : modifiedNode, modifiableList);
				}
			}
		}
	}

	private IASTNode determineParentToBeRewritten(IASTNode modifiedNode, List<ASTModification> modificationsForNode) {
		IASTNode modifiedNodeParent = modifiedNode;
		for (ASTModification currentModification : modificationsForNode) {
			if (currentModification.getKind() == ASTModification.ModificationKind.REPLACE) {
				modifiedNodeParent = modifiedNode.getParent();
				break;
			}
		}
		modifiedNodeParent = modifiedNodeParent != null ? modifiedNodeParent : modifiedNode;
		return modifiedNodeParent;
	}

	/**
	 * Applies the C++ code formatter to the code affected by refactoring.
	 * 
	 * @param edit The text edit produced by refactoring.
	 * @param file The file being modified.
	 * @return The text edit containing formatted refactoring changes, or the original text edit
	 *     in case of errors.
	 */
	private MultiTextEdit formatChangedCode(MultiTextEdit edit, IFile file) {
		String code;
		try {
			code = FileContentHelper.getContent(file, 0);
		} catch (IOException e) {
			CCorePlugin.log(e);
			return edit;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return edit;
		}
		IDocument document = new Document(code);
		try {
			TextEdit tempEdit = edit.copy();
			tempEdit.apply(document, TextEdit.UPDATE_REGIONS);
			TextEdit[] edits = tempEdit.getChildren();
			IRegion[] regions = new IRegion[edits.length];
			for (int i = 0; i < edits.length; i++) {
				regions[i] = edits[i].getRegion();
			}
			ICProject project = CCorePlugin.getDefault().getCoreModel().create(file.getProject());
			Map<String, String> options = project.getOptions(true);
			CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
			code = document.get();
			TextEdit[] formatEdits = formatter.format(CCodeFormatter.K_TRANSLATION_UNIT, code,
					regions, TextUtilities.getDefaultLineDelimiter(document));
			MultiTextEdit resultEdit = new MultiTextEdit();
			edits = edit.getChildren();
			for (int i = 0; i < edits.length; i++) {
				IRegion region = regions[i];
				int offset = region.getOffset();
				TextEdit formatEdit = formatEdits[i];
				formatEdit.moveTree(-offset);
				document = new Document(code.substring(offset, offset + region.getLength()));
				formatEdit.apply(document, TextEdit.NONE);
				TextEdit textEdit = edits[i];
				resultEdit.addChild(
						new ReplaceEdit(textEdit.getOffset(), textEdit.getLength(), document.get()));
			}
			return resultEdit;
		} catch (MalformedTreeException e) {
			CCorePlugin.log(e);
			return edit;
		} catch (BadLocationException e) {
			CCorePlugin.log(e);
			return edit;
		}
	}

	@Override
	public int visit(IASTTranslationUnit translationUnit) {
		if (hasChangedChild(translationUnit)) {
			synthTreatment(translationUnit);
		}
		IASTFileLocation location = translationUnit.getFileLocation();
		sourceOffsets.put(location.getFileName(), Integer.valueOf(location.getNodeOffset()));
		return super.visit(translationUnit);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		return super.leave(tu);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (hasChangedChild(declaration)) {
			synthTreatment(declaration);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declaration);
	}

	private void synthTreatment(IASTNode synthNode) {
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		synthNode.accept(writer);
		String synthSource = writer.toString();
		createChange(synthNode, synthSource);
		
		IASTFileLocation fileLocation = synthNode.getFileLocation();
		int newOffset = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
		sourceOffsets.put(fileLocation.getFileName(), Integer.valueOf(newOffset));
	}

	private void handleAppends(IASTNode node) {
		ChangeGeneratorWriterVisitor writer =
				new ChangeGeneratorWriterVisitor(modificationStore, commentMap);
		List<ASTModification> modifications = modificationParent.get(node);
		ReplaceEdit anchor = getAppendAnchor(node);
		Assert.isNotNull(anchor);
		IASTNode precedingNode = getLastNodeBeforeAppendPoint(node);
		if (precedingNode != null &&
				ASTWriter.requireBlankLineInBetween(precedingNode, modifications.get(0).getNewNode())) {
			writer.newLine();
		}
		for (ASTModification modification : modifications) {
			IASTNode newNode = modification.getNewNode();
			newNode.accept(writer);
		}
		String code = writer.toString();
		IFile file = FileHelper.getFileFromNode(node);
		MultiTextEdit parentEdit = getEdit(node, file);
		ReplaceEdit edit = new ReplaceEdit(anchor.getOffset(), anchor.getLength(),
				code + anchor.getText());
		parentEdit.addChild(edit);
		IASTFileLocation fileLocation = node.getFileLocation();
		int newOffset = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
		sourceOffsets.put(fileLocation.getFileName(), Integer.valueOf(newOffset));
	}

	private IASTNode getLastNodeBeforeAppendPoint(IASTNode node) {
		IASTNode[] children;
		if (node instanceof IASTCompositeTypeSpecifier) {
			children = ((IASTCompositeTypeSpecifier) node).getDeclarations(true);
		} else {
			children = node.getChildren();
		}
		return children.length > 0 ? children[children.length - 1] : null;
	}

	private boolean isAppendable(Iterable<ASTModification> modifications) {
		for (ASTModification modification : modifications) {
			if (!isAppendable(modification))
				return false;
		}
		return true;
	}

	private boolean isAppendable(ASTModification modification) {
		if (modification.getKind() != ModificationKind.APPEND_CHILD)
			return false;
		IASTNode node = modification.getNewNode();
		return node instanceof IASTDeclaration || node instanceof IASTStatement;
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
		IFile file = FileHelper.getFileFromNode(node);
		String code = originalCodeOfNode(node, file);
		IASTFileLocation location = node.getFileLocation();
		int pos = location.getNodeOffset() + location.getNodeLength();
		int len = code.endsWith("}") ? 1 : 0; //$NON-NLS-1$
		int startOfLine = skipPrecedingBlankLines(code, code.length() - len);
		if (startOfLine < 0) {
			// Include the closing brace in the region that will be reformatted.
			return new ReplaceEdit(pos - len, len, code.substring(code.length() - len));
		}
		return new ReplaceEdit(location.getNodeOffset() + startOfLine, 0, ""); //$NON-NLS-1$
	}

	/**
	 * Skips blank lines preceding the given position.
	 * @param text the text to scan
	 * @param pos the position after that blank lines. 
	 * @return the beginning of the first blank line, or -1 if the beginning of the line
	 * 	   corresponding to the given position contains non-whitespace characters.
	 */
	private int skipPrecedingBlankLines(String text, int pos) {
		int lineStart = -1;
		while (--pos >= 0) {
			char c = text.charAt(pos);
			if (c == '\n') {
				lineStart = pos + 1;
			} else if (!Character.isWhitespace(c)) {
				break;
			}
		}
		return lineStart;
	}

	private void synthTreatment(IASTTranslationUnit synthTU) {
		ASTWriter synthWriter = new ASTWriter();
		synthWriter.setModificationStore(modificationStore);

		for (ASTModification modification : modificationParent.get(synthTU)) {
			IASTNode targetNode = modification.getTargetNode();
			IASTFileLocation targetLocation = targetNode.getFileLocation();
			String currentFile = targetLocation.getFileName();
			IPath implPath = new Path(currentFile);
			IFile relevantFile= ResourceLookup.selectFileForLocation(implPath, null);
			if (relevantFile == null || !relevantFile.exists()) { // If not in workspace or local file system
			    throw new UnhandledASTModificationException(modification);
			}
			MultiTextEdit edit;
			if (changes.containsKey(relevantFile)) {
				edit = changes.get(relevantFile);
			} else {
				edit = new MultiTextEdit();
				changes.put(relevantFile, edit);
			}
			String newNodeCode = synthWriter.write(modification.getNewNode(), commentMap);

			switch (modification.getKind()) {
			case REPLACE:
				edit.addChild(new ReplaceEdit(targetLocation.getNodeOffset(),
						targetLocation.getNodeLength(), newNodeCode));
				break;
			case INSERT_BEFORE:
				if (ASTWriter.requireBlankLineInBetween(modification.getNewNode(), targetNode)) {
					newNodeCode = newNodeCode + "\n";  //$NON-NLS-1$
				}
				edit.addChild(new InsertEdit(getOffsetIncludingComments(targetNode), newNodeCode));
				break;
			case APPEND_CHILD:
				if (targetNode instanceof IASTTranslationUnit &&
						((IASTTranslationUnit) targetNode).getDeclarations().length > 0) {
					IASTTranslationUnit tu = (IASTTranslationUnit) targetNode;
					IASTDeclaration lastDecl = tu.getDeclarations()[tu.getDeclarations().length - 1];
					targetLocation = lastDecl.getFileLocation();
				}
				String lineDelimiter = FileHelper.determineLineDelimiter(
						FileHelper.getFileFromNode(targetNode));
				edit.addChild(new InsertEdit(targetLocation.getNodeOffset() + targetLocation.getNodeLength(),
						lineDelimiter + lineDelimiter + newNodeCode));
				break;
			}
		}
	}

	private void createChange(IASTNode synthNode, String synthSource) {
		IFile relevantFile = FileHelper.getFileFromNode(synthNode);
		String originalCode = originalCodeOfNode(synthNode, relevantFile);
		CodeComparer codeComparer = new CodeComparer(originalCode, synthSource);
		codeComparer.createChange(getEdit(synthNode, relevantFile), synthNode);
	}

	private MultiTextEdit getEdit(IASTNode modifiedNode, IFile file) {
		MultiTextEdit edit = changes.get(file);
		if (edit == null) {
			edit = new MultiTextEdit();
			changes.put(file, edit);
		}
		TextEditGroup editGroup = new TextEditGroup(Messages.ChangeGenerator_group);
		for (ASTModification currentModification : modificationParent.get(modifiedNode)) {
			if (currentModification.getAssociatedEditGroup() != null) {
				editGroup = currentModification.getAssociatedEditGroup();
				edit.addChildren(editGroup.getTextEdits());
				break;
			}
		}
		return edit;
	}

	private String originalCodeOfNode(IASTNode node, IFile sourceFile) {
		int nodeOffset = getOffsetIncludingComments(node);
		int nodeLength = getNodeLengthIncludingComments(node);
		return FileContentHelper.getContent(sourceFile, nodeOffset,	nodeLength);
	}

	private int getNodeLengthIncludingComments(IASTNode node) {
		int nodeOffset = node.getFileLocation().getNodeOffset();
		int nodeLength = node.getFileLocation().getNodeLength();

		ArrayList<IASTComment> comments = commentMap.getAllCommentsForNode(node);
		if (!comments.isEmpty()) {
			int startOffset = nodeOffset;
			int endOffset = nodeOffset + nodeLength;
			for (IASTComment comment : comments) {
				IASTFileLocation commentLocation = comment.getFileLocation();
				if (commentLocation.getNodeOffset() < startOffset) {
					startOffset = commentLocation.getNodeOffset();
				}
				if (commentLocation.getNodeOffset() + commentLocation.getNodeLength() >= endOffset) {
					endOffset = commentLocation.getNodeOffset() + commentLocation.getNodeLength();
				}
			}
			nodeLength = endOffset - startOffset;
		}
		return nodeLength;
	}

	private int getOffsetIncludingComments(IASTNode node) {
		int nodeOffset = node.getFileLocation().getNodeOffset();

		ArrayList<IASTComment> comments = commentMap.getAllCommentsForNode(node);
		if (!comments.isEmpty()) {
			int startOffset = nodeOffset;
			for (IASTComment comment : comments) {
				IASTFileLocation commentLocation = comment.getFileLocation();
				if (commentLocation.getNodeOffset() < startOffset) {
					startOffset = commentLocation.getNodeOffset();
				}
			}
			nodeOffset = startOffset;
		}
		return nodeOffset;
	}

	private boolean hasChangedChild(IASTNode node) {
		return modificationParent.containsKey(node);
	}

	private boolean hasAppendsOnly(IASTNode node) {
		List<ASTModification> modifications = modificationParent.get(node);
		if (modifications == null)
			return false;
		return isAppendable(modifications);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		if (hasChangedChild(declarator)) {
			synthTreatment(declarator);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declarator);
	}

	@Override
	public int visit(IASTArrayModifier mod) {
		if (hasChangedChild(mod)) {
			synthTreatment(mod);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(mod);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		if (hasChangedChild(namespaceDefinition) && !hasAppendsOnly(namespaceDefinition)) {
			synthTreatment(namespaceDefinition);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(namespaceDefinition);
	}

	@Override
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		if (hasAppendsOnly(namespaceDefinition)) {
			handleAppends(namespaceDefinition);
		}
		return super.leave(namespaceDefinition);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (hasChangedChild(declSpec) && !hasAppendsOnly(declSpec)) {
			synthTreatment(declSpec);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declSpec);
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if (hasAppendsOnly(declSpec)) {
			handleAppends(declSpec);
		}
		return super.leave(declSpec);
	}

	@Override
	public int visit(IASTExpression expression) {
		if (hasChangedChild(expression)) {
			synthTreatment(expression);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		if (hasChangedChild(initializer)) {
			synthTreatment(initializer);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(initializer);
	}

	@Override
	public int visit(IASTName name) {
		if (hasChangedChild(name)) {
			synthTreatment(name);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		if (hasChangedChild(parameterDeclaration)) {
			synthTreatment(parameterDeclaration);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(parameterDeclaration);
	}

	@Override
	public int visit(IASTStatement statement) {
		if (hasChangedChild(statement) && !hasAppendsOnly(statement)) {
			synthTreatment(statement);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(statement);
	}

	@Override
	public int leave(IASTStatement statement) {
		if (hasAppendsOnly(statement)) {
			handleAppends(statement);
		}
		return super.leave(statement);
	}

	class CodeComparer {
		private final StringBuilder originalCode;
		private final StringBuilder synthCode;
		private int lastCommonInSynthStart;
		private int lastCommonInOriginalStart;
		private int firstCommonInSynthEnd;
		private int  firstCommonInOriginalEnd;

		public CodeComparer(String originalCode, String synthCode) {
			this.originalCode = new StringBuilder(originalCode);
			this.synthCode = new StringBuilder(synthCode);
			calculatePositions();
		}

		private void calculatePositions() {
			lastCommonInSynthStart = calcLastCommonPositionInSynthCode();
			lastCommonInOriginalStart = calcLastCommonPositionInOriginalCode();
			firstCommonInSynthEnd =
					calcFirstPositionOfCommonEndInSynthCode(lastCommonInSynthStart, lastCommonInOriginalStart);
			firstCommonInOriginalEnd =
					calcFirstPositionOfCommonEndInOriginalCode(lastCommonInOriginalStart, lastCommonInSynthStart);
			trimTrailingNewlines();
		}

		private void trimTrailingNewlines() {
			int prevOrigEnd = firstCommonInOriginalEnd - 1;
			while (prevOrigEnd > lastCommonInOriginalStart && prevOrigEnd > -1 &&
					isUninterresting(originalCode, prevOrigEnd)) {
				firstCommonInOriginalEnd = prevOrigEnd;
				prevOrigEnd--;
			}

			while (firstCommonInOriginalEnd > 0 && firstCommonInOriginalEnd + 1 < originalCode.length() &&
					(originalCode.charAt(firstCommonInOriginalEnd) == ' ' || originalCode.charAt(firstCommonInOriginalEnd) == '\t')) {
				firstCommonInOriginalEnd++;
			}

			int prevSynthEnd = firstCommonInSynthEnd - 1;
			while (prevSynthEnd > lastCommonInSynthStart && prevSynthEnd > -1 &&
					isUninterresting(synthCode, prevSynthEnd)) {
				firstCommonInSynthEnd = prevSynthEnd;
				prevSynthEnd--;
			}
			while (firstCommonInSynthEnd > 0 && firstCommonInSynthEnd + 1 < synthCode.length() &&
					(synthCode.charAt(firstCommonInSynthEnd) == ' ' || synthCode.charAt(firstCommonInSynthEnd) == '\t')) {
				firstCommonInSynthEnd++;
			}
		}

		public int getLastCommonPositionInSynthCode() {
			return lastCommonInSynthStart;
		}

		public int getLastCommonPositionInOriginalCode() {
			return lastCommonInOriginalStart;
		}

		public int getFirstPositionOfCommonEndInOriginalCode() {
			return firstCommonInOriginalEnd;
		}

		public int getFirstPositionOfCommonEndInSynthCode() {
			return firstCommonInSynthEnd;
		}

		public int calcLastCommonPositionInSynthCode() {
			return findLastCommonPosition(synthCode, originalCode);
		}

		public int calcLastCommonPositionInOriginalCode() {
			return findLastCommonPosition(originalCode, synthCode);
		}

		private int calcFirstPositionOfCommonEndInOriginalCode(int originalLimit, int synthLimit) {
			StringBuilder reverseOriginalCode = new StringBuilder(originalCode).reverse();
			StringBuilder reverseSynthCode = new StringBuilder(synthCode).reverse();
			int lastCommonPosition = findLastCommonPosition(reverseOriginalCode, reverseSynthCode,
					reverseOriginalCode.length() - originalLimit - 1,
					reverseSynthCode.length() - synthLimit - 1);

			if (lastCommonPosition < 0 || lastCommonPosition >= originalCode.length()) {
				return -1;
			}

			return originalCode.length() - lastCommonPosition - 1;
		}

		private int calcFirstPositionOfCommonEndInSynthCode(int synthLimit, int originalLimit) {
			StringBuilder reverseOriginalCode = new StringBuilder(originalCode).reverse();
			StringBuilder reverseSynthCode = new StringBuilder(synthCode).reverse();

			int lastCommonPosition = findLastCommonPosition(reverseSynthCode, reverseOriginalCode,
					reverseSynthCode.length() - synthLimit - 1,
					reverseOriginalCode.length() - originalLimit - 1);

			if (lastCommonPosition < 0 || lastCommonPosition >= synthCode.length()) {
				return -1;
			}

			return synthCode.length() - lastCommonPosition - 1;
		}

		private int findLastCommonPosition(StringBuilder first, StringBuilder second) {
			return findLastCommonPosition(first, second, first.length(), second.length());
		}

		private int findLastCommonPosition(StringBuilder first, StringBuilder second, int firstLimit,
				int secondLimit) {
			int firstIndex = -1;
			int secondIndex = -1;
			int lastCommonIndex = -1;

			do {
				lastCommonIndex = firstIndex;
				firstIndex = nextInterrestingPosition(first, firstIndex);
				secondIndex = nextInterrestingPosition(second, secondIndex);
			} while (firstIndex > -1 && firstIndex <= firstLimit && secondIndex > -1 &&
					secondIndex <= secondLimit && first.charAt(firstIndex) == second.charAt(secondIndex));
			return lastCommonIndex;
		}

		private int nextInterrestingPosition(StringBuilder code, int position) {
			do {
				position++;
				if (position >= code.length()) {
					return -1;
				}
			} while (isUninterresting(code, position));
			return position;
		}

		private boolean isUninterresting(StringBuilder code, int position) {
			switch (code.charAt(position)) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				return true;

			default:
				return false;
			}
		}

		protected void createChange(MultiTextEdit edit, IASTNode changedNode) {
			int changeOffset = getOffsetIncludingComments(changedNode);
			createChange(edit, changeOffset);
		}

		private void createChange(MultiTextEdit edit, int changeOffset) {
			int i = (firstCommonInSynthEnd >= 0 ?
					firstCommonInOriginalEnd : originalCode.length()) - lastCommonInOriginalStart;
			if (i <= 0) {
				String insertCode = synthCode.substring(lastCommonInSynthStart,
						firstCommonInSynthEnd);
				InsertEdit iEdit = new InsertEdit(changeOffset + lastCommonInOriginalStart,
						insertCode);
				edit.addChild(iEdit);
			} else if ((firstCommonInSynthEnd >= 0 ?
					firstCommonInSynthEnd : synthCode.length()) - lastCommonInSynthStart <= 0) {
				int correction = 0;
				if (lastCommonInSynthStart > firstCommonInSynthEnd) {
					correction = lastCommonInSynthStart - firstCommonInSynthEnd;
				}
				DeleteEdit dEdit = new DeleteEdit(changeOffset + lastCommonInOriginalStart,
						firstCommonInOriginalEnd - lastCommonInOriginalStart + correction);
				edit.addChild(dEdit);
			} else {
				String replacementCode = getReplacementCode(lastCommonInSynthStart,
						firstCommonInSynthEnd);
				ReplaceEdit rEdit = new ReplaceEdit(
						changeOffset + Math.max(lastCommonInOriginalStart, 0),
						(firstCommonInOriginalEnd >= 0 ?
								firstCommonInOriginalEnd :
								originalCode.length()) - Math.max(lastCommonInOriginalStart, 0),
						replacementCode);
				edit.addChild(rEdit);
			}
		}

		private String getReplacementCode(int lastCommonPositionInSynth, int firstOfCommonEndInSynth) {
			int replacementStart = Math.max(lastCommonPositionInSynth, 0);
			int replacementEnd = firstOfCommonEndInSynth >= 0 ?
					firstOfCommonEndInSynth : synthCode.length();
			if (replacementStart < replacementEnd) {
				return synthCode.substring(replacementStart, replacementEnd);
			}
			return ""; //$NON-NLS-1$
		}
	}

	public Change getChange() {
		return change;
	}
}
