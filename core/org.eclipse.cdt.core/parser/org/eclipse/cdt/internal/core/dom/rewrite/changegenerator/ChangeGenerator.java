/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ProblemRuntimeException;
import org.eclipse.cdt.internal.core.dom.rewrite.util.FileContentHelper;
import org.eclipse.cdt.internal.core.dom.rewrite.util.FileHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ChangeGenerator extends CPPASTVisitor {

	private final LinkedHashMap<String, Integer> sourceOffsets = new LinkedHashMap<String, Integer>();
	public LinkedHashMap<IASTNode, List<ASTModification>> modificationParent = new LinkedHashMap<IASTNode, List<ASTModification>>();
	private final LinkedHashMap<IFile, MultiTextEdit> changes = new LinkedHashMap<IFile, MultiTextEdit>();
	private CompositeChange change;

	private final ASTModificationStore modificationStore;

	{
		shouldVisitExpressions = true;

		shouldVisitStatements = true;

		shouldVisitNames = true;

		shouldVisitDeclarations = true;

		shouldVisitDeclSpecifiers = true;

		shouldVisitDeclarators = true;

		shouldVisitInitializers = true;

		shouldVisitBaseSpecifiers = true;

		shouldVisitNamespaces = true;

		shouldVisitTemplateParameters = true;

		shouldVisitParameterDeclarations = true;

		shouldVisitTranslationUnit = true;

	}

	public ChangeGenerator(ASTModificationStore modificationStore) {
		this.modificationStore = modificationStore;

	}

	public void generateChange(IASTNode rootNode) throws ProblemRuntimeException {
		generateChange(rootNode, this);
	}

	public void generateChange(IASTNode rootNode, CPPASTVisitor pathProvider)
			throws ProblemRuntimeException {
		change = new CompositeChange(Messages.ChangeGenerator_compositeChange);
		initParentModList();
		rootNode.accept(pathProvider);
		for (IFile currentFile : changes.keySet()) {

			CTextFileChange subchange = new CTextFileChange(currentFile
					.getName(), currentFile);
			subchange.setEdit(changes.get(currentFile));
			change.add(subchange);
		}
	}

	private void initParentModList() {
		ASTModificationMap rootModifications = modificationStore
				.getRootModifications();
		if (rootModifications != null) {
			for (IASTNode modifiedNode : rootModifications.getModifiedNodes()) {
				List<ASTModification> modificationsForNode = rootModifications
						.getModificationsForNode(modifiedNode);
				modificationParent.put(modifiedNode.getParent(),
						modificationsForNode);
			}
		}
	}

	@Override
	public int visit(IASTTranslationUnit translationUnit) {
		if (hasChangedChild(translationUnit)) {

			synthTreatment(translationUnit);
		}
		sourceOffsets.put(translationUnit.getFileLocation().getFileName(),
				Integer.valueOf(translationUnit.getFileLocation().getNodeOffset()));
		return super.visit(translationUnit);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {

		return super.leave(tu);
	}

	private int getOffsetForNodeFile(IASTNode rootNode) {

		Integer offset = sourceOffsets.get(rootNode.getFileLocation()
				.getFileName());
		return offset == null ? 0 : offset.intValue();
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
		synthTreatment(synthNode, null);
	}

	private void synthTreatment(IASTNode synthNode, String fileScope) {

		String indent = getIndent(synthNode);
		ASTWriter synthWriter = new ASTWriter(indent);
		synthWriter.setModificationStore(modificationStore);
		String synthSource = synthWriter.write(synthNode);
		reformatSynthCode(synthNode, synthSource); /*XXX resultat wird nicht verwendet?*/


		int newOffset = synthNode.getFileLocation().getNodeOffset()
				+ synthNode.getFileLocation().getNodeLength();
		sourceOffsets.put(synthNode.getFileLocation().getFileName(), Integer.valueOf(newOffset));

	}

	private void synthTreatment(IASTTranslationUnit synthTU) {
		ASTWriter synthWriter = new ASTWriter();
		synthWriter.setModificationStore(modificationStore);

		for (ASTModification modification : modificationParent.get(synthTU)) {
			IASTFileLocation targetLocation = modification.getTargetNode()
					.getFileLocation();
			String currentFile = targetLocation.getFileName();
			IPath implPath = new Path(currentFile);
			IFile relevantFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFileForLocation(implPath);
			MultiTextEdit edit;
			if (changes.containsKey(relevantFile)) {
				edit = changes.get(relevantFile);
			} else {
				edit = new MultiTextEdit();
				changes.put(relevantFile, edit);
			}

			String newNodeCode = synthWriter.write(modification.getNewNode());
			switch (modification.getKind()) {
			case REPLACE:
				edit.addChild(new ReplaceEdit(targetLocation.getNodeOffset(),
						targetLocation.getNodeLength(), newNodeCode));
				break;
			case INSERT_BEFORE:
				edit.addChild(new InsertEdit(targetLocation.getNodeOffset(),
						newNodeCode));
				break;
			case APPEND_CHILD:
				edit.addChild(new InsertEdit(targetLocation.getNodeOffset()
						+ targetLocation.getNodeLength(), newNodeCode));
				break;
			}
		}
	}

	private String reformatSynthCode(IASTNode synthNode, String synthSource) {
		IFile relevantFile = FileHelper.getIFilefromIASTNode(synthNode);
		StringBuilder formattedCode = new StringBuilder();

		String originalCode = originalCodeOfNode(synthNode);
		CodeComparer codeComparer = new CodeComparer(originalCode, synthSource);

		int lastCommonPositionInOriginalCode = codeComparer
				.getLastCommonPositionInOriginalCode();
		if (lastCommonPositionInOriginalCode > -1) {
			formattedCode.append(originalCode.substring(0,
					lastCommonPositionInOriginalCode + 1));
		}

		int lastCommonPositionInSynthCode = codeComparer
				.getLastCommonPositionInSynthCode();
		int firstPositionOfCommonEndInSynthCode = codeComparer
				.getFirstPositionOfCommonEndInSynthCode(lastCommonPositionInSynthCode);

		int firstPositionOfCommonEndInOriginalCode = codeComparer
				.getFirstPositionOfCommonEndInOriginalCode(lastCommonPositionInSynthCode);
		if (firstPositionOfCommonEndInSynthCode == -1) {
			formattedCode.append(synthSource
					.substring(lastCommonPositionInSynthCode + 1));
		} else {
			if (lastCommonPositionInSynthCode + 1 < firstPositionOfCommonEndInSynthCode) {
				formattedCode.append(synthSource.substring(
						lastCommonPositionInSynthCode + 1,
						firstPositionOfCommonEndInSynthCode));
			}
		}

		if (firstPositionOfCommonEndInOriginalCode > -1) {
			formattedCode.append(originalCode
					.substring(firstPositionOfCommonEndInOriginalCode));
		}

		MultiTextEdit edit;
		if (changes.containsKey(relevantFile)) {
			edit = changes.get(relevantFile);
		} else {
			edit = new MultiTextEdit();
			changes.put(relevantFile, edit);
		}

		codeComparer.createChange(edit, synthNode);

		return formattedCode.toString();
	}

	public static String originalCodeOfNode(IASTNode node) {
		if (node.getFileLocation() != null) {
			IFile sourceFile = FileHelper.getIFilefromIASTNode(node);
			int nodeOffset = node.getFileLocation().getNodeOffset();
			int nodeLength = node.getFileLocation().getNodeLength();
			return FileContentHelper.getContent(sourceFile, nodeOffset,
					nodeLength);
		}
		return null;
	}

	private String getIndent(IASTNode nextNode) {
		IASTFileLocation fileLocation = nextNode.getFileLocation();
		int length = fileLocation.getNodeOffset()
				- getOffsetForNodeFile(nextNode);

		String originalSource = FileContentHelper.getContent(FileHelper
				.getIFilefromIASTNode(nextNode),
				getOffsetForNodeFile(nextNode), length);
		StringBuilder indent = new StringBuilder(originalSource);
		indent.reverse();
		String lastline = indent
				.substring(0, Math.max(indent.indexOf("\n"), 0)); //$NON-NLS-1$
		if (lastline.trim().length() == 0) {
			return lastline;
		}
		return ""; //$NON-NLS-1$
	}

	private boolean hasChangedChild(IASTNode parent) {

		return modificationParent.containsKey(parent);
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
	public int visit(IASTDeclSpecifier declSpec) {
		if (hasChangedChild(declSpec)) {
			synthTreatment(declSpec);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(declSpec);
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
		if (hasChangedChild(statement)) {
			synthTreatment(statement);
			return ASTVisitor.PROCESS_SKIP;
		}
		return super.visit(statement);
	}

	class CodeComparer {

		private final StringBuilder originalCode;
		private final StringBuilder synthCode;

		public CodeComparer(String originalCode, String synthCode) {
			this.originalCode = new StringBuilder(originalCode);
			this.synthCode = new StringBuilder(synthCode);
		}

		public int getLastCommonPositionInSynthCode() {

			int lastCommonPosition = -1;
			int originalCodePosition = -1;
			int synthCodePosition = -1;

			do {
				lastCommonPosition = synthCodePosition;
				originalCodePosition = nextInterrestingPosition(originalCode,
						originalCodePosition);
				synthCodePosition = nextInterrestingPosition(synthCode,
						synthCodePosition);
			} while (originalCodePosition > -1
					&& synthCodePosition > -1
					&& originalCode.charAt(originalCodePosition) == synthCode
							.charAt(synthCodePosition));

			return lastCommonPosition;
		}

		public int getLastCommonPositionInOriginalCode() {

			int lastCommonPosition = -1;
			int originalCodePosition = -1;
			int synthCodePosition = -1;

			do {
				lastCommonPosition = originalCodePosition;
				originalCodePosition = nextInterrestingPosition(originalCode,
						originalCodePosition);
				synthCodePosition = nextInterrestingPosition(synthCode,
						synthCodePosition);
			} while (originalCodePosition > -1
					&& synthCodePosition > -1
					&& originalCode.charAt(originalCodePosition) == synthCode
							.charAt(synthCodePosition));

			return lastCommonPosition;
		}

		public int getFirstPositionOfCommonEndInOriginalCode(int limmit) {

			int lastCommonPosition = -1;
			int originalCodePosition = -1;
			int synthCodePosition = -1;

			StringBuilder reverseOriginalCode = new StringBuilder(originalCode)
					.reverse();
			StringBuilder reverseSynthCode = new StringBuilder(synthCode)
					.reverse();

			do {
				lastCommonPosition = originalCodePosition;
				originalCodePosition = nextInterrestingPosition(
						reverseOriginalCode, originalCodePosition);
				synthCodePosition = nextInterrestingPosition(reverseSynthCode,
						synthCodePosition);
			} while (originalCodePosition > -1
					&& synthCodePosition > -1
					&& synthCodePosition < synthCode.length() - limmit
					&& reverseOriginalCode.charAt(originalCodePosition) == reverseSynthCode
							.charAt(synthCodePosition));

			if (lastCommonPosition < 0
					|| lastCommonPosition >= originalCode.length()) {
				return -1;
			}

			return originalCode.length() - lastCommonPosition;
		}

		public int getFirstPositionOfCommonEndInSynthCode(int limmit) {

			int lastCommonPosition = -1;
			int originalCodePosition = -1;
			int synthCodePosition = -1;

			StringBuilder reverseOriginalCode = new StringBuilder(originalCode)
					.reverse();
			StringBuilder reverseSynthCode = new StringBuilder(synthCode)
					.reverse();

			do {
				lastCommonPosition = synthCodePosition;
				originalCodePosition = nextInterrestingPosition(
						reverseOriginalCode, originalCodePosition);
				synthCodePosition = nextInterrestingPosition(reverseSynthCode,
						synthCodePosition);
			} while (originalCodePosition > -1
					&& synthCodePosition > -1
					&& synthCodePosition < synthCode.length() - limmit
					&& reverseOriginalCode.charAt(originalCodePosition) == reverseSynthCode
							.charAt(synthCodePosition));

			if (lastCommonPosition < 0
					|| lastCommonPosition >= synthCode.length()) {
				return -1;
			}

			return synthCode.length() - lastCommonPosition;
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
			int changeOffset = changedNode.getFileLocation().getNodeOffset();

			TextEditGroup editGroup = new TextEditGroup(Messages.ChangeGenerator_group);
			for (ASTModification currentModification : modificationParent
					.get(changedNode)) {
				if (currentModification.getAssociatedEditGroup() != null) {
					editGroup = currentModification.getAssociatedEditGroup();
					edit.addChildren(editGroup.getTextEdits());
					break;
				}
			}

			createChange(edit, changeOffset);
		}

		private void createChange(MultiTextEdit edit, int changeOffset) {

			int lastCommonPositionInSynth = getLastCommonPositionInSynthCode();
			int firstOfCommonEndInOriginal = getFirstPositionOfCommonEndInOriginalCode(lastCommonPositionInSynth);
			int lastCommonPositionInOriginal = getLastCommonPositionInOriginalCode();
			int firstOfCommonEndInSynth = getFirstPositionOfCommonEndInSynthCode(lastCommonPositionInSynth);

			if ((firstOfCommonEndInSynth >= 0 ? firstOfCommonEndInOriginal
					: originalCode.length())
					- lastCommonPositionInOriginal <= 0) {
				String insertCode = synthCode.substring(
						lastCommonPositionInSynth, firstOfCommonEndInSynth + 1);
				InsertEdit iEdit = new InsertEdit(changeOffset
						+ lastCommonPositionInOriginal, insertCode);
				edit.addChild(iEdit);
			} else if ((firstOfCommonEndInSynth >= 0 ? firstOfCommonEndInSynth
					: synthCode.length())
					- lastCommonPositionInSynth <= 0) {
				int correction = 0;
				if (lastCommonPositionInSynth > firstOfCommonEndInSynth) {
					correction = lastCommonPositionInSynth
							- firstOfCommonEndInSynth;
				}
				DeleteEdit dEdit = new DeleteEdit(changeOffset
						+ lastCommonPositionInOriginal,
						firstOfCommonEndInOriginal
								- lastCommonPositionInOriginal + correction);
				edit.addChild(dEdit);
			} else {
				String replacementCode = getReplacementCode(
						lastCommonPositionInSynth, firstOfCommonEndInSynth);
				ReplaceEdit rEdit = new ReplaceEdit(
						changeOffset
								+ Math.max(lastCommonPositionInOriginal, 0),
						(firstOfCommonEndInOriginal >= 0 ? firstOfCommonEndInOriginal
								: originalCode.length())
								- Math.max(lastCommonPositionInOriginal, 0),
						replacementCode);
				edit.addChild(rEdit);
			}
		}

		private String getReplacementCode(int lastCommonPositionInSynth,
				int firstOfCommonEndInSynth) {
			int replacementStart = Math.max(lastCommonPositionInSynth, 0);
			int replacementEnd = (firstOfCommonEndInSynth >= 0 ? firstOfCommonEndInSynth
					: synthCode.length());
			if (replacementStart < replacementEnd) {
				return synthCode.substring(replacementStart, replacementEnd);
			} else {
				return ""; //$NON-NLS-1$
			}
		}
	}

	public Change getChange() {
		return change;
	}
}
