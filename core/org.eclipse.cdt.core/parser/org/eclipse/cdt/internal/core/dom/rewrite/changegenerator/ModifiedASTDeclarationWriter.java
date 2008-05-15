package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.DeclarationWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclarationWriter extends DeclarationWriter {

	private final ASTModificationHelper modificationHelper;
	
	public ModifiedASTDeclarationWriter(Scribe scribe, CPPASTVisitor visitor, ModificationScopeStack stack, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.modificationHelper = new ASTModificationHelper(stack);
	}

	@Override
	protected void writeDeclarationsInNamespace(ICPPASTNamespaceDefinition namespaceDefinition, IASTDeclaration[] declarations) {
		IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(namespaceDefinition, declarations, IASTDeclaration.class);
		super.writeDeclarationsInNamespace(namespaceDefinition, modifiedDeclarations);
	}

}
