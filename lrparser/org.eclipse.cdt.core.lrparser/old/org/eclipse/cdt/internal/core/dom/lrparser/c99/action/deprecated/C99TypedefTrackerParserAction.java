/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.action.deprecated;

import java.util.LinkedList;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.lrparser.symboltable.TypedefSymbolTable;
/**
 * A simple set of trial and undo actions that just keep track
 * of typedef names. This information is then fed back to the parser
 * in order to disambiguate certain parser grammar rules.
 * 
 * The command design pattern is used to implement undo actions.
 * 
 * @author Mike Kucera
 */
public class C99TypedefTrackerParserAction {

	private static final boolean DEBUG = true;
	
	  
	// provides limited access to the token stream
	private final ITokenStream parser;
	
	// The symbolTable currently in use 
	private TypedefSymbolTable symbolTable = TypedefSymbolTable.EMPTY_TABLE;
	
	// A stack that keeps track of scopes in the symbol table, used to "close" scopes and to undo the opening of scopes
	private final LinkedList<TypedefSymbolTable> symbolTableScopeStack = new LinkedList<TypedefSymbolTable>();
	
	// keeps track of nested declarations
	private final LinkedList<DeclaratorFrame> declarationStack = new LinkedList<DeclaratorFrame>();


	// "For every action there is an equal and opposite reaction." - Newton's third law
	private final LinkedList<IUndoAction> undoStack = new LinkedList<IUndoAction>();
	
	
	/**
	 * A command object that provides undo functionality.
	 */
	private interface IUndoAction {
		void undo();
	}
	
	
	/**
	 * Undoes the last fired action.
	 */
	public void undo() {
		undoStack.removeLast().undo();
	}
	
	
	public C99TypedefTrackerParserAction(ITokenStream parser) {
		this.parser = parser;
	}
	

	/**
	 * Lexer feedback hack, used by the parser to identify typedefname tokens.
	 */
	public boolean isTypedef(String ident) {
		return symbolTable.contains(ident);
	}
	
	
	/**
	 * Methods used by tests, package local access.
	 */
	TypedefSymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	int undoStackSize() {
		return undoStack.size();
	}
	
	LinkedList<DeclaratorFrame> getDeclarationStack() {
		return declarationStack;
	}
	
	
	/**
	 * Called from the grammar file in places where a scope is created.
	 * 
	 * Scopes are created by compound statements, however special care
	 * must also be taken with for loops because they may contain
	 * declarations.
	 * 
	 * TODO: scope object now need to be handled explicitly
	 */
	public void openSymbolScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		symbolTableScopeStack.add(symbolTable);
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				symbolTable = symbolTableScopeStack.removeLast();
			}
		});
	}
	

	public void closeSymbolScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final TypedefSymbolTable undoTable = symbolTable;
		symbolTable = symbolTableScopeStack.removeLast(); // close the scope

		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				symbolTableScopeStack.add(symbolTable);
				symbolTable = undoTable;
			}
		});
	}
	
	
	
	/**
	 * Called from the grammar before a declaration is about to be reduced.
	 */
	public void openDeclarationScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		declarationStack.add(new DeclaratorFrame());
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.removeLast();
			}
		});
	}
	
	
	public void closeDeclarationScope() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame undoFrame = declarationStack.removeLast();
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.add(undoFrame);
			}
		});
	}
	
	
	public void consumeFunctionDefinition() {
		if(DEBUG) DebugUtil.printMethodTrace();

		final DeclaratorFrame frame = declarationStack.removeLast();
		
		final TypedefSymbolTable undoTable = symbolTable;
		symbolTable = symbolTableScopeStack.removeLast();
		
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				symbolTableScopeStack.add(symbolTable);
				symbolTable = undoTable;
				
				declarationStack.add(frame);
			}
		});
	}




	public void consumeDeclSpecToken() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		IToken token = parser.getRightIToken();
		final int kind = token.getKind();
		
		// creates a DeclSpec if there isn't one already
		DeclaratorFrame frame = declarationStack.getLast();
		final DeclSpec declSpec = frame.getDeclSpec();
		declSpec.add(kind);
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declSpec.remove(kind);
			}
		});
	}
	
	
	
	public void consumeDirectDeclaratorIdentifier() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		frame.setDeclaratorName(parser.getRightIToken());
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				frame.setDeclaratorName(null);
			}
		});
	}
	
	
	public void  consumeDeclaratorComplete() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.getLast();
		
		IToken token       = frame.getDeclaratorName();
		DeclSpec declSpec  = frame.getDeclSpec();

		String ident = (token == null) ? null : token.toString();
		//System.out.println("declarator complete: " + ident);
		
		final TypedefSymbolTable oldTable = symbolTable;
		if(declSpec.isTypedef()) {
			//System.out.println("adding typedef: " + ident);
			symbolTable = symbolTable.add(ident);
		}
		
		declarationStack.removeLast();
		declarationStack.add(new DeclaratorFrame(frame.getDeclSpec())); // reset the declarator
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.removeLast();
				declarationStack.add(frame);
				symbolTable = oldTable;
			}
		});
	}
	
	
	
	
	public void consumeDeclaratorCompleteParameter() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		
		//declarationStack.getLast().addNestedDeclaration(parameterBinding);
		
		// parameter declarations can only have one declarator, so don't reset
		//declarationStack.add(new DeclaratorFrame()); // reset

		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				//declarationStack.removeLast();
				//declarationStack.getLast().removeLastNestedDeclaration();
				declarationStack.add(frame);
			}
		});
	}
	
	
	/**
	 * This is a special case for the rule:
	 *     parameter_declaration ::= declaration_specifiers
	 *     
     * In this case there is no declarator at all
     * 
     * TODO: creating bindings that have no identifier seems really dumb,
     * why does it need to be done? Why not just have a null binding or
     * for that matter don't even have a name node
     * 
	 */
	public void consumeParameterDeclarationWithoutDeclarator() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.add(frame);
			}
		});
	}
	
	
	public void consumeDeclaratorCompleteField() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();

		declarationStack.add(new DeclaratorFrame(frame.getDeclSpec()));  // reset the declarator

		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();
				
				declarationStack.removeLast();

				declarationStack.add(frame);
			}
		});
	}

	
	/**
	 * An abstract declarator used as part of an expression, eg) a cast.
	 * Only need the type.
	 * 
	 * TODO: this isn't enough, I need a binding for the abstract declarator
	 * what I really need is a consumeDeclaratorCompleteTypeId similar to above
	 */
	public void consumeTypeId() {
		if(DEBUG) DebugUtil.printMethodTrace();
		
		final DeclaratorFrame frame = declarationStack.removeLast();
		
		undoStack.add(new IUndoAction() {
			@Override
			public void undo() {
				if(DEBUG) DebugUtil.printMethodTrace();

				declarationStack.add(frame);
			}
		});
	}
	
}
