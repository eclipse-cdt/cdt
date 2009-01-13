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
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * A utility that prints an AST to the console or any print stream, useful for debugging purposes.
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
	public static boolean print(IASTNode node, PrintStream out) {
		if (node == null) {
			out.println("null"); 
			return false;
		}
		
		if (node instanceof IASTTranslationUnit) {
			IASTPreprocessorStatement[] preStats = ((IASTTranslationUnit)node).getAllPreprocessorStatements();
			if (preStats != null) {
				for (IASTPreprocessorStatement stat : preStats)
					print(out, 0, stat);
			}
		}

		printAST(out, 0, node);
		
		if (node instanceof IASTTranslationUnit) {
			IASTProblem[] problems = ((IASTTranslationUnit)node).getPreprocessorProblems();
			if (problems != null) {
				for (IASTProblem problem : problems)
					print(out, 0, problem);
			}
			
			IASTComment[] comments = ((IASTTranslationUnit)node).getComments();
			if (comments != null) {
				for (IASTComment comment : comments)
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
	public static boolean printProblems(IASTNode node, PrintStream out) {
		if (node == null) {
			out.println("null");
			return false;
		}
		
		printASTProblems(out, 0, node);
		
		if (node instanceof IASTTranslationUnit) {
			IASTProblem[] problems = ((IASTTranslationUnit)node).getPreprocessorProblems();
			if (problems != null) {
				for (IASTProblem problem : problems) {
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
	
	
	
	private static void printAST(PrintStream out, int indent, IASTNode node) {
		print(out, indent, node);
		indent++;
		for(IASTNode child : node.getChildren()) {
			printAST(out, indent, child);
		}
	}
	
	
	private static void printASTProblems(PrintStream out, int indent, IASTNode node) {
		if(node instanceof IASTProblem)
			print(out, indent, node);
		
		indent++;
		for(IASTNode child : node.getChildren()) {
			printASTProblems(out, indent, child);
		}
	}
	
	
	
	
	
	private static void print(PrintStream out, int indentLevel, Object n) {
		for (int i = 0; i < indentLevel; i++)
			out.print("  "); 
		
		if (n == null) {
			out.println("NULL"); 
			return;
		}
		
		String classname = n.getClass().getName();
		out.print(classname);
		
		if (n instanceof ASTNode) {
			ASTNode node = (ASTNode) n;
			out.print(" (" + node.getOffset() + "," + node.getLength() + ") ");
			if (node.getParent() == null && !(node instanceof IASTTranslationUnit)) {
				out.print("PARENT IS NULL ");
			}
			if (PRINT_PARENT_PROPERTIES)
				out.print(node.getPropertyInParent());
		}
		
		if (n instanceof ICArrayType) {
			ICArrayType at = (ICArrayType)n;
			try {
				if (at.isRestrict()) {
					out.print(" restrict"); 
				}
			} catch (DOMException e) { 
				e.printStackTrace();
			}
		}
		
		if (n instanceof IASTName) {
			IASTName name = (IASTName)n;
			out.print(" " + ((IASTName)n).toString()); 
			if (RESOLVE_BINDINGS) {
				try {
					IBinding binding = name.resolveBinding();
					print(out, indentLevel, binding);
				} catch(Exception e) {
					System.out.println("Exception while resolving binding: " + name);
				}
			}
		} else if(n instanceof IASTDeclarator) {
			IASTDeclarator declarator = (IASTDeclarator) n;
		
			IASTPointerOperator[] pointers = declarator.getPointerOperators();
			if(pointers != null && pointers.length > 0) {
				out.println();
				for (IASTPointerOperator pointer : pointers) {
					print(out, indentLevel+1, pointer);
				}
			}
			if (declarator instanceof IASTArrayDeclarator) {
				IASTArrayDeclarator decl = (IASTArrayDeclarator)declarator;
				org.eclipse.cdt.core.dom.ast.IASTArrayModifier[] modifiers = decl.getArrayModifiers();
				for (IASTArrayModifier modifier : modifiers) {
					print(out, indentLevel+1, modifier);
				}
			}
		} else if (n instanceof ICASTPointer) {
			ICASTPointer pointer = (ICASTPointer) n;
			if (pointer.isConst())
				out.print(" const"); 
			if (pointer.isVolatile())
				out.print(" volatile"); 
			if (pointer.isRestrict())
				out.print(" restrict");
		} else if (n instanceof ICPointerType) {
			ICPointerType pointer = (ICPointerType)n;
			if (pointer.isConst())
				out.print(" const"); 
			if (pointer.isVolatile())
				out.print(" volatile"); 
			if (pointer.isRestrict())
				out.print(" restrict");
			out.println();
			try {
				print(out, indentLevel, ((ITypeContainer)n).getType());
			} catch(Exception e) {}
		} else if (n instanceof ICASTArrayModifier) {
			if (((ICASTArrayModifier)n).isRestrict()) {
				out.print(" restrict"); 
			}
		} else if (n instanceof IASTComment) {
			out.print("'" + new String(((IASTComment)n).getComment()) + "'");
//		} else if (n instanceof ICompositeType) {
//			try {
//				IField[] fields = ((ICompositeType)n).getFields();
//				if (fields == null || fields.length == 0) {
//					out.print(" no fields");
//				}
//				for (IField field : fields) {
//					out.println();
//					print(out, indentLevel + 1, field);
//				}
//			} catch (DOMException e) {
//				e.printStackTrace();
//			}
		} else if (n instanceof ITypeContainer) {
			out.println();
			try {
				print(out, indentLevel, ((ITypeContainer)n).getType());
			} catch(Exception e) {}
		} else if (n instanceof IVariable) {
			IVariable var = (IVariable) n;
			IType t;
			try {
				t = var.getType();
				out.println();
				print(out, indentLevel, t);
			} catch (DOMException e) {
				//e.printStackTrace();
			}
	
		} else if (n instanceof IProblemBinding) {
			IProblemBinding problem = (IProblemBinding)n;
			out.print(problem.getMessage());
		}
		
		out.println();
	}
	
}