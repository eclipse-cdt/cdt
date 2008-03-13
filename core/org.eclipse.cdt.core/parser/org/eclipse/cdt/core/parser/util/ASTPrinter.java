/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.io.PrintStream;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;


/**
 * A utility that prints an AST to the console, useful for debugging purposes.
 * 
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("nls")
public class ASTPrinter {
	
	private static boolean PRINT_PARENT_PROPERTIES = false;
	private static boolean RESOLVE_BINDINGS = false;
	
	/**
	 * Prints the AST to the given PrintStream.
	 * 
	 * @return Always returns false, boolean return type allows this method
	 * to be called from a conditional breakpoint during debugging.
	 */
	public static boolean print(IASTNode root, PrintStream out) {
		if(root == null) {
			out.println("null"); 
			return false;
		}
		
		if(root instanceof IASTTranslationUnit) {
			IASTPreprocessorStatement[] preStats = ((IASTTranslationUnit)root).getAllPreprocessorStatements();
			if(preStats != null) {
				for(IASTPreprocessorStatement stat : preStats)
					print(out, 0, stat);
			}
		}

		root.accept(new PrintVisitor(out));
		
		if(root instanceof IASTTranslationUnit) {
			IASTProblem[] problems = ((IASTTranslationUnit)root).getPreprocessorProblems();
			if(problems != null) {
				for(IASTProblem problem : problems)
					print(out, 0, problem);
			}
			
			IASTComment[] comments = ((IASTTranslationUnit)root).getComments();
			if(comments != null) {
				for(IASTComment comment : comments)
					print(out, 0, comment);
			}
		}
		return false;
	}

	
		
	/**
	 * Prints the AST to stdout.
	 * 
	 * @return Always returns false, boolean return type allows this method
	 * to be called from a conditional breakpoint during debugging.
	 */
	public static boolean print(IASTNode root) {
		return print(root, System.out);
	}
	
	
	/**
	 * Prints problem nodes in the AST to the given printstream.
	 * 
	 * @return Always returns false, boolean return type allows this method
	 * to be called from a conditional breakpoint during debugging.
	 */
	public static boolean printProblems(IASTNode root, PrintStream out) {
		if(root == null) {
			out.println("null");
			return false;
		}
		
		root.accept(new ProblemVisitor(out));
		
		if(root instanceof IASTTranslationUnit) {
			IASTProblem[] problems = ((IASTTranslationUnit)root).getPreprocessorProblems();
			if(problems != null) {
				for(IASTProblem problem : problems) {
					print(out, 0, problem);
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * Prints problem nodes in the AST to stdout.
	 * 
	 * @return Always returns false, boolean return type allows this method
	 * to be called from a conditional breakpoint during debugging.
	 */
	public static boolean printProblems(IASTNode root) {
		return printProblems(root, System.out);
	}
	
	
	
	private static void print(PrintStream out, int indentLevel, Object n) {
		for(int i = 0; i < indentLevel; i++)
			out.print("  "); 
		
		if(n == null) {
			out.println("NULL"); 
			return;
		}
		
		String classname = n.getClass().getName();
		out.print(classname);
		
		if(n instanceof ASTNode) {
			ASTNode node = (ASTNode) n;
			out.print(" (" + node.getOffset() + "," + node.getLength() + ") ");
			if(node.getParent() == null && !(node instanceof IASTTranslationUnit)) {
				out.print("PARENT IS NULL ");
			}
			if(PRINT_PARENT_PROPERTIES)
				out.print(node.getPropertyInParent());
		}
		
		if(n instanceof ICArrayType) {
			ICArrayType at = (ICArrayType)n;
			try {
				if(at.isRestrict()) {
					out.print(" restrict"); 
				}
			} catch (DOMException e) { 
				e.printStackTrace();
			}
		}
		
		if(n instanceof IASTName) {
			out.print(" " + ((IASTName)n).toString()); 
		}
		else if(n instanceof ICASTPointer) {
			ICASTPointer pointer = (ICASTPointer) n;
			if(pointer.isConst())
				out.print(" const"); 
			if(pointer.isVolatile())
				out.print(" volatile"); 
			if(pointer.isRestrict())
				out.print(" restrict");
		}
		else if(n instanceof ICPointerType) {
			ICPointerType pointer = (ICPointerType)n;
			try {
				if(pointer.isConst())
					out.print(" const"); 
				if(pointer.isVolatile())
					out.print(" volatile"); 
				if(pointer.isRestrict())
					out.print(" restrict");
			} catch (DOMException e) {
				e.printStackTrace();
			}
			out.println();
			try {
				print(out, indentLevel, ((ITypeContainer)n).getType());
			} catch(Exception e) {}
		}
		else if(n instanceof ICASTArrayModifier) {
			if(((ICASTArrayModifier)n).isRestrict()) {
				out.print(" restrict"); 
			}
		}
		else if(n instanceof IASTComment) {
			out.print("'" + new String(((IASTComment)n).getComment()) + "'");
		}
//		else if(n instanceof ICompositeType) {
//			try {
//				IField[] fields = ((ICompositeType)n).getFields();
//				if(fields == null || fields.length == 0) {
//					out.print(" no fields");
//				}
//				for(IField field : fields) {
//					out.println();
//					print(out, indentLevel + 1, field);
//				}
//			} catch (DOMException e) {
//				e.printStackTrace();
//			}
//		}
		else if(n instanceof ITypeContainer) {
			out.println();
			try {
				print(out, indentLevel, ((ITypeContainer)n).getType());
			} catch(Exception e) {}
		}
		else if(n instanceof IVariable) {
			IVariable var = (IVariable) n;
			IType t;
			try {
				t = var.getType();
				out.println();
				print(out, indentLevel, t);
			} catch (DOMException e) {
				//e.printStackTrace();
			}
	
		}
		else if(n instanceof IProblemBinding) {
			IProblemBinding problem = (IProblemBinding)n;
			out.print(problem.getMessage());
		}
		
			
		out.println();
	}

	
	private static class ProblemVisitor extends CASTVisitor {
		private PrintStream out;
		
		ProblemVisitor(PrintStream out) {
			this.out = out;
			shouldVisitProblems = true;
			shouldVisitDeclarations = true;
			shouldVisitStatements = true;
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTProblem problem) {
			print(out, 1, problem);
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTDeclaration declaration) {
			if(declaration instanceof IASTProblemDeclaration)
				print(out, 0, declaration);
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTExpression expression) {
			if(expression instanceof IASTProblemExpression)
				print(out, 0, expression);
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(IASTStatement statement) {
			if(statement instanceof IASTProblemStatement)
				print(out, 0, statement);
			return PROCESS_CONTINUE;
		}
	}
	
	
	private static class PrintVisitor extends CASTVisitor {

		
		private PrintStream out;
		private int indentLevel = 0;
		
		PrintVisitor(PrintStream out) {
			this.out = out;
			shouldVisitDesignators = true;
			shouldVisitNames = true;
			shouldVisitDeclarations = true;
			shouldVisitInitializers = true;
			shouldVisitParameterDeclarations = true;
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
			shouldVisitEnumerators = true;
			shouldVisitTranslationUnit = true;
			shouldVisitProblems = true;
		}
		
		private void print(IASTNode node) {
			ASTPrinter.print(out, indentLevel,  node);
		}
		
		private void print(IBinding binding) {
			ASTPrinter.print(out, indentLevel, binding);
		}
		

		@Override
		public int visit(IASTComment comment) {
			print(comment);
			indentLevel++;
			return super.visit(comment);
		}

		@Override
		public int visit(ICASTDesignator designator) {
			print(designator);
			indentLevel++;
			return super.visit(designator);
		}
		
		@Override
		public int visit(IASTDeclaration declaration) {
			print(declaration);
			indentLevel++;
			return super.visit(declaration);
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			print(declarator);
			indentLevel++;
			IASTPointerOperator[] pointers = declarator.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				print(pointers[i]);
			}
			if(declarator instanceof IASTArrayDeclarator) {
				IASTArrayDeclarator decl = (IASTArrayDeclarator)declarator;
				org.eclipse.cdt.core.dom.ast.IASTArrayModifier[] modifiers = decl.getArrayModifiers();
				for(int i = 0; i < modifiers.length; i++) {
					print(modifiers[i]);
				}
			}
			return super.visit(declarator);
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			print(declSpec);
			indentLevel++;
			return super.visit(declSpec);
		}

		@Override
		public int visit(IASTEnumerator enumerator) {
			print(enumerator);
			indentLevel++;
			return super.visit(enumerator);
		}

		@Override
		public int visit(IASTExpression expression) {
			print(expression);
			indentLevel++;
			return super.visit(expression);
		}

		@Override
		public int visit(IASTInitializer initializer) {
			print(initializer);
			indentLevel++;
			return super.visit(initializer);
		}

		@Override
		public int visit(IASTName name) {
			print(name);
			if(RESOLVE_BINDINGS) {
				try {
					IBinding binding = name.resolveBinding();
					print(binding);
				} catch(Exception e) {
					System.out.println("Exception while resolving binding: " + name);
				}
			}
			indentLevel++;
			return super.visit(name);
		}

		@Override
		public int visit(IASTParameterDeclaration parameterDeclaration) {
			print(parameterDeclaration);
			indentLevel++;
			return super.visit(parameterDeclaration);
		}

		@Override
		public int visit(IASTProblem problem) {
			print(problem);
			indentLevel++;
			return super.visit(problem);
		}

		@Override
		public int visit(IASTStatement statement) {
			print(statement);
			indentLevel++;
			return super.visit(statement);
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			print(tu);
			indentLevel++;
			return super.visit(tu);
		}

		@Override
		public int visit(IASTTypeId typeId) {
			print(typeId);
			indentLevel++;
			return super.visit(typeId);
		}

		@Override
		public int leave(IASTComment comment) {
			indentLevel--;
			return super.leave(comment);
		}
		
		@Override
		public int leave(ICASTDesignator designator) {
			indentLevel--;
			return super.leave(designator);
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			indentLevel--;
			return super.leave(declaration);
		}

		@Override
		public int leave(IASTDeclarator declarator) {
			indentLevel--;
			return super.leave(declarator);
		}

		@Override
		public int leave(IASTDeclSpecifier declSpec) {
			indentLevel--;
			return super.leave(declSpec);
		}

		@Override
		public int leave(IASTEnumerator enumerator) {
			indentLevel--;
			return super.leave(enumerator);
		}

		@Override
		public int leave(IASTExpression expression) {
			indentLevel--;
			return super.leave(expression);
		}

		@Override
		public int leave(IASTInitializer initializer) {
			indentLevel--;
			return super.leave(initializer);
		}

		@Override
		public int leave(IASTName name) {
			indentLevel--;
			return super.leave(name);
		}

		@Override
		public int leave(IASTParameterDeclaration parameterDeclaration) {
			indentLevel--;
			return super.leave(parameterDeclaration);
		}

		@Override
		public int leave(IASTProblem problem) {
			indentLevel--;
			return super.leave(problem);
		}

		@Override
		public int leave(IASTStatement statement) {
			indentLevel--;
			return super.leave(statement);
		}

		@Override
		public int leave(IASTTranslationUnit tu) {
			indentLevel--;
			return super.leave(tu);
		}

		@Override
		public int leave(IASTTypeId typeId) {
			indentLevel--;
			return super.leave(typeId);
		}
		
	}	
}