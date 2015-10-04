/**
 * 
 */
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

/**
 * Checker that detects variable initializations in case statements
 * that are not surrounded by curly brackets
 * 
 * 
 * @author Sarah E. Mostafa
 *
 */
public class InitializationInCaseChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.InitializationInCaseChecker"; //$NON-NLS-1$
	
	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new InitializationInCaseVisitor());
	}
	
	class InitializationInCaseVisitor extends ASTVisitor {
		
		public InitializationInCaseVisitor() {
			shouldVisitStatements = true;
		}
		
		@SuppressWarnings("restriction")
		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTSwitchStatement) {
				IASTSwitchStatement switchStmt = (IASTSwitchStatement) statement;
				IASTStatement body = switchStmt.getBody();
				if(body instanceof IASTCompoundStatement) {
					IASTStatement[] statements = ((IASTCompoundStatement) body).getStatements();
					
					for (int i = 0; i < statements.length; i++) {
						IASTStatement currentStatement = statements[i];
						if(currentStatement instanceof IASTSwitchStatement) {
							visit(currentStatement);
						}
						
						if (currentStatement instanceof IASTDeclarationStatement) {
							IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement) currentStatement;
							CPPASTSimpleDeclaration cppastSimpleDeclaration = (CPPASTSimpleDeclaration) declarationStatement.getDeclaration();
							IASTDeclarator[] declarators = cppastSimpleDeclaration.getDeclarators();
							if(declarators !=null) {
								for (int j=0; j<declarators.length; j++) {
									if (declarators[j].getInitializer() != null)
										reportProblem(ER_ID, currentStatement, currentStatement.getRawSignature());
								}
								
								if(cppastSimpleDeclaration.getDeclSpecifier() instanceof CPPASTNamedTypeSpecifier) {
									reportProblem(ER_ID, currentStatement, currentStatement.getRawSignature());
								}
									
							}
						}
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}
}
