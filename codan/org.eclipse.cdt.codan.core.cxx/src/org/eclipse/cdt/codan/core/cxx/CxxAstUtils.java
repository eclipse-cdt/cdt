/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia, Tomasz Wesolowski
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Tomasz Wesolowski - extension
 *     Marc-Andre Laperle
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Useful functions for doing code analysis on C/C++ AST.
 */
public final class CxxAstUtils {
	public static class NameFinderVisitor extends ASTVisitor {
		public IASTName name;
		{
			shouldVisitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			this.name = name;
			return PROCESS_ABORT;
		}
	}

	private static class FunctionNameFinderVisitor extends NameFinderVisitor {
		{
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTFieldReference) {
				this.name = ((IASTFieldReference) expression).getFieldName();
				return PROCESS_ABORT;
			}
			return super.visit(expression);
		}
	}

	private static class NoReturnImplicitCallFinder extends ASTVisitor {
		boolean noReturn;

		NoReturnImplicitCallFinder() {
			shouldVisitImplicitNames = true;
			shouldVisitImplicitDestructorNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof IASTImplicitName) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof IFunction && ((IFunction) binding).isNoReturn()) {
					noReturn = true;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	// Not instantiatable. All methods are static.
	private CxxAstUtils() {
	}

	public static IType unwindTypedef(IType type) {
		return SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
	}

	public static boolean isInMacro(IASTNode node) {
		IASTNodeSelector nodeSelector = node.getTranslationUnit()
				.getNodeSelector(node.getTranslationUnit().getFilePath());
		IASTFileLocation fileLocation = node.getFileLocation();
		if (fileLocation == null)
			return true;
		IASTPreprocessorMacroExpansion macro = nodeSelector.findEnclosingMacroExpansion(fileLocation.getNodeOffset(),
				fileLocation.getNodeLength());
		return macro != null;
	}

	public static IASTFunctionDefinition getEnclosingFunction(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		return (IASTFunctionDefinition) node;
	}

	public static IASTCompositeTypeSpecifier getEnclosingCompositeTypeSpecifier(IASTNode node) {
		while (node != null && !(node instanceof IASTCompositeTypeSpecifier)) {
			node = node.getParent();
		}
		return (IASTCompositeTypeSpecifier) node;
	}

	public static IASTStatement getEnclosingStatement(IASTNode node) {
		while (node != null && !(node instanceof IASTStatement)) {
			node = node.getParent();
		}
		return (IASTStatement) node;
	}

	/**
	 * @param astName the name for the declaration
	 * @param factory the factory
	 * @param index the index
	 * @return the created declaration
	 */
	public static IASTDeclaration createDeclaration(IASTName astName, INodeFactory factory, IIndex index) {
		// Depending on context, either a type or a declaration is easier to infer.
		IType inferredType = null;
		IASTSimpleDeclaration declaration = null;
		inferredType = tryInferTypeFromBinaryExpr(astName);
		if (inferredType == null)
			declaration = tryInferTypeFromFunctionCall(astName, factory, index);
		// After the inference, create the statement is needed.
		if (declaration != null) {
			// A declaration was generated.
			return declaration;
		} else if (inferredType != null) {
			// A type was inferred, create the declaration and return it.
			DeclarationGenerator generator = DeclarationGenerator.create(factory);
			IASTDeclarator declarator = generator.createDeclaratorFromType(inferredType, astName.toCharArray());
			IASTDeclSpecifier declspec = generator.createDeclSpecFromType(inferredType);
			IASTSimpleDeclaration simpleDeclaration = factory.newSimpleDeclaration(declspec);
			simpleDeclaration.addDeclarator(declarator);
			return simpleDeclaration;
		} else {
			// Fallback - return a `void` declaration.
			IASTDeclarator declarator = factory.newDeclarator(astName.copy(CopyStyle.withLocations));
			IASTSimpleDeclSpecifier declspec = factory.newSimpleDeclSpecifier();
			declspec.setType(Kind.eInt);
			IASTSimpleDeclaration simpleDeclaration = factory.newSimpleDeclaration(declspec);
			simpleDeclaration.addDeclarator(declarator);
			return simpleDeclaration;
		}
	}

	/**
	 * For any BinaryExpression, guess the type from the other operand. (A good
	 * guess for =, ==; hard to get a better guess for others)
	 *
	 * @return inferred type or {@code null} if couldn't infer
	 */
	private static IType tryInferTypeFromBinaryExpr(IASTName astName) {
		if (astName.getParent() instanceof IASTIdExpression
				&& astName.getParent().getParent() instanceof IASTBinaryExpression) {
			IASTNode binaryExpr = astName.getParent().getParent();
			for (IASTNode node : binaryExpr.getChildren()) {
				if (node != astName.getParent()) {
					// Use this expression as type source.
					return ((IASTExpression) node).getExpressionType();
				}
			}
		}
		return null;
	}

	/**
	 * For a function call, tries to find a matching function declaration.
	 * Checks the argument count.
	 *
	 * @param index the index
	 * @return a generated declaration or {@code null} if not suitable
	 */
	private static IASTSimpleDeclaration tryInferTypeFromFunctionCall(IASTName astName, INodeFactory factory,
			IIndex index) {
		if (astName.getParent() instanceof IASTIdExpression
				&& astName.getParent().getParent() instanceof IASTFunctionCallExpression
				&& astName.getParent().getPropertyInParent() == IASTFunctionCallExpression.ARGUMENT) {
			IASTFunctionCallExpression call = (IASTFunctionCallExpression) astName.getParent().getParent();
			FunctionNameFinderVisitor visitor = new FunctionNameFinderVisitor();
			call.getFunctionNameExpression().accept(visitor);
			IASTName funcname = visitor.name;
			int expectedParametersNum = 0;
			int targetParameterNum = -1;
			for (IASTNode n : call.getChildren()) {
				if (n.getPropertyInParent() == IASTFunctionCallExpression.ARGUMENT) {
					if (n instanceof IASTIdExpression && n.getChildren()[0] == astName) {
						targetParameterNum = expectedParametersNum;
					}
					expectedParametersNum++;
				}
			}
			if (targetParameterNum == -1) {
				return null;
			}
			IBinding[] bindings;
			{
				IBinding binding = funcname.resolveBinding();
				if (binding instanceof IProblemBinding) {
					bindings = ((IProblemBinding) binding).getCandidateBindings();
				} else {
					bindings = new IBinding[] { binding };
				}
			}
			try {
				index.acquireReadLock();
				Set<IIndexName> declSet = new HashSet<>();
				// fill declSet with proper declarations
				for (IBinding b : bindings) {
					if (b instanceof IFunction) {
						IFunction f = (IFunction) b;
						if (f.getParameters().length == expectedParametersNum) {
							// Consider this overload
							IIndexName[] decls = index.findDeclarations(b);
							declSet.addAll(Arrays.asList(decls));
						}
					}
				}
				HashMap<ITranslationUnit, IASTTranslationUnit> astCache = new HashMap<>();
				for (IIndexName decl : declSet) {
					// for now, just use the first overload found
					ITranslationUnit tu = getTranslationUnitFromIndexName(decl);
					if (tu == null) {
						continue;
					}

					IASTTranslationUnit ast = null;
					if (astCache.containsKey(tu)) {
						ast = astCache.get(tu);
					} else {
						ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
						astCache.put(tu, ast);
					}

					IASTName name = (IASTName) ast.getNodeSelector(null).findEnclosingNode(decl.getNodeOffset(),
							decl.getNodeLength());
					IASTNode fdecl = name;
					while (fdecl instanceof IASTName) {
						fdecl = fdecl.getParent();
					}
					assert (fdecl instanceof IASTFunctionDeclarator);
					// find the needed param number
					int nthParam = 0;
					for (IASTNode child : fdecl.getChildren()) {
						if (child instanceof IASTParameterDeclaration) {
							if (nthParam == targetParameterNum) {
								IASTParameterDeclaration pd = (IASTParameterDeclaration) child;
								IASTDeclSpecifier declspec = pd.getDeclSpecifier().copy(CopyStyle.withLocations);
								IASTDeclarator declarator = pd.getDeclarator().copy(CopyStyle.withLocations);
								setNameInNestedDeclarator(declarator, astName.copy(CopyStyle.withLocations));
								IASTSimpleDeclaration declaration = factory.newSimpleDeclaration(declspec);
								declaration.addDeclarator(declarator);
								return declaration;
							}
							nthParam++;
						}
					}
					name.getParent();
				}
			} catch (InterruptedException e) {
				// skip
			} catch (CoreException e) {
				Activator.log(e);
			} finally {
				index.releaseReadLock();
			}
		}
		return null;
	}

	private static void setNameInNestedDeclarator(IASTDeclarator declarator, IASTName astName) {
		while (declarator.getNestedDeclarator() != null) {
			declarator = declarator.getNestedDeclarator();
		}
		declarator.setName(astName);
	}

	public static ITranslationUnit getTranslationUnitFromIndexName(IIndexName decl) throws CoreException {
		IIndexFile file = decl.getFile();
		if (file != null) {
			return CoreModelUtil.findTranslationUnitForLocation(file.getLocation().getURI(), null);
		}
		return null;
	}

	/**
	 * If the function definition belongs to a class, returns the class.
	 * Otherwise, returns {@code null}.
	 *
	 * @param function the function definition to check
	 * @param index the index to use for name lookup
	 * @return either a type specifier or {@code null}
	 */
	public static IASTCompositeTypeSpecifier getCompositeTypeFromFunction(final IASTFunctionDefinition function,
			final IIndex index) {
		// Return value to be set via visitor.
		final IASTCompositeTypeSpecifier returnSpecifier[] = { null };
		final HashMap<ITranslationUnit, IASTTranslationUnit> astCache = new HashMap<>();
		function.accept(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (!(name instanceof ICPPASTQualifiedName && name.getParent().getParent() == function))
					return PROCESS_CONTINUE;
				ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
				// A qualified name may have 1 name, but in our case needs to have 2.
				// The pre-last name is either a namespace or a class.
				if (qname.getChildren().length < 2) {
					return PROCESS_CONTINUE;
				}
				IASTName namePart = (IASTName) qname.getChildren()[qname.getChildren().length - 2];
				IBinding binding = namePart.resolveBinding();
				try {
					index.acquireReadLock();
					IIndexName[] declarations = index.findDeclarations(binding);
					// Check the declarations and use first suitable
					for (IIndexName decl : declarations) {
						ITranslationUnit tu = getTranslationUnitFromIndexName(decl);
						if (tu == null) {
							continue;
						}

						IASTTranslationUnit ast = null;
						if (astCache.containsKey(tu)) {
							ast = astCache.get(tu);
						} else {
							ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
							astCache.put(tu, ast);
						}
						IASTNode node = ast.getNodeSelector(null).findEnclosingNode(decl.getNodeOffset(),
								decl.getNodeLength());
						IASTCompositeTypeSpecifier specifier = getEnclosingCompositeTypeSpecifier(node);
						if (specifier != null) {
							returnSpecifier[0] = specifier;
							break;
						}
					}
				} catch (InterruptedException e) {
					return PROCESS_ABORT;
				} catch (CoreException e) {
					Activator.log(e);
					return PROCESS_ABORT;
				} finally {
					index.releaseReadLock();
				}
				return PROCESS_ABORT;
			}
		});
		return returnSpecifier[0];
	}

	public static boolean isThrowStatement(IASTNode statement) {
		if (!(statement instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) statement).getExpression();
		if (!(expression instanceof IASTUnaryExpression))
			return false;
		return ((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_throw;
	}

	public static boolean isExitStatement(IASTNode statement) {
		NoReturnImplicitCallFinder noReturnFinder = new NoReturnImplicitCallFinder();
		statement.accept(noReturnFinder);
		if (noReturnFinder.noReturn)
			return true;

		if (!(statement instanceof IASTExpressionStatement))
			return false;
		IASTExpression expression = ((IASTExpressionStatement) statement).getExpression();
		if (!(expression instanceof IASTFunctionCallExpression))
			return false;
		IASTExpression functionNameExpression = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
		if (functionNameExpression instanceof IASTIdExpression) {
			IASTName name = ((IASTIdExpression) functionNameExpression).getName();

			IBinding binding = name.resolveBinding();
			if (binding instanceof IFunction && ((IFunction) binding).isNoReturn()) {
				return true;
			}
		}
		return functionNameExpression.getRawSignature().equals("exit"); //$NON-NLS-1$
	}

	@SuppressWarnings("restriction")
	public static IType getReturnType(IASTFunctionDefinition func) {
		// We could do this with public API (func.getDeclarator().getName().resolveBinding().getType()
		// .getReturnType()), but that would trigger resolution of the parameter types as well,
		// which is needless extra work.
		if (func instanceof ICPPASTFunctionDefinition) {
			return CPPVisitor.createType(func.getDeclarator(),
					CPPVisitor.RESOLVE_PLACEHOLDERS | CPPVisitor.ONLY_RETURN_TYPE);
		}
		return CVisitor.createType(func.getDeclarator(), CVisitor.ONLY_RETURN_TYPE);
	}
}
