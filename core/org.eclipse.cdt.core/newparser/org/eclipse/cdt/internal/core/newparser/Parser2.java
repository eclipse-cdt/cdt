package org.eclipse.cdt.internal.core.newparser;

/**
 * This is an attempt at a copyright clean parser.  The grammar is based
 * on the ISO C++ standard
 */
public class Parser2 {

	private boolean parsePassed = true;
	
	/**
	 * translationUnit
	 * : (declaration)*
	 * 
	 */
	public void translationUnit() throws Exception {
		Token lastBacktrack = null;
		while (LT(1) != Token.tEOF) {
			if (!declaration()) {
				// Mark as failure and try to reach a recovery point
				parsePassed = false;
				
				if (lastBacktrack != null && lastBacktrack == LA(1)) {
					// we haven't progressed from the last backtrack
					// try and find tne next definition
					for (int t = LT(1); t != Token.tEOF; t = LT(1)) {
						consume();
						// TO DO: we should really check for matching braces too
						if (t == Token.tSEMI)
							break;
					}
				} else {
					// start again from here
					lastBacktrack = LA(1);
				}
			}
		}
	}
	
	/**
	 * declaration
	 * : {"asm"} asmDefinition
	 * | {"namespace"} namespaceDefinition
	 * | {"using"} usingDeclaration
	 * | {"export"|"template"} templateDeclaration
	 * | {"extern"} linkageSpecification
	 * | simpleDeclaration
	 * 
	 * Notes:
	 * - folded in blockDeclaration
	 * - merged alternatives that required same LA
	 *   - functionDefinition into simpleDeclaration
	 *   - namespaceAliasDefinition into namespaceDefinition
	 *   - usingDirective into usingDeclaration
	 *   - explicitInstantiation and explicitSpecialization into
	 *       templateDeclaration
	 */
	public boolean declaration() throws Exception {
		switch (LT(1)) {
			case Token.t_asm:
				return true; // asmDefinition();
			case Token.t_namespace:
				return true; // namespaceDefinition();
			case Token.t_using:
				return true; // usingDeclaration();
			case Token.t_export:
			case Token.t_template:
				return true; // templateDeclaration();
			case Token.t_extern:
				return true; // linkageSpecification();
			default:
				return simpleDeclaration(); 
		}
	}
	
	/**
	 * simpleDeclaration
	 * : (declSpecifier)* (initDeclarator ("," initDeclarator)*)? 
	 *     (";" | {"{"} functionBody)
	 * 
	 * Notes:
	 * - append functionDefinition stuff to end of this rule
	 * 
	 * To do:
	 * - work in ctorInitializer and functionTryBlock
	 */
	public boolean simpleDeclaration() throws Exception {
		while (declSpecifier());
		
		if (initDeclarator()) {
			while (LT(1) == Token.tCOMMA) {
				consume();
				
				if (!initDeclarator())
					return false;
			}
		}
		
		switch (LT(1)) {
			case Token.tSEMI:
				return true;
			case Token.tLBRACE:
				return true; // functionBody();
			default:
				return false;
		}
	}
	
	/**
	 * declSpecifier
	 * : "auto" | "register" | "static" | "extern" | "mutable"
	 * | "inline" | "virtual" | "explicit"
	 * | "char" | "wchar_t" | "bool" | "short" | "int" | "long"
	 * | "signed" | "unsigned" | "float" | "double" | "void"
	 * | "const" | "volatile"
	 * | "friend" | "typedef"
	 * | ("typename")? name
	 * | {"class"|"struct"|"union"} classSpecifier
	 * | {"enum"} enumSpecifier
	 * 
	 * Notes:
	 * - folded in storageClassSpecifier, typeSpecifier, functionSpecifier
	 * - folded elaboratedTypeSpecifier into classSpecifier and enumSpecifier
	 * - find template names in name
	 */
	public boolean declSpecifier() throws Exception {
		switch (LT(1)) {
			case Token.t_auto:
			case Token.t_register:
			case Token.t_static:
			case Token.t_extern:
			case Token.t_mutable:
			case Token.t_inline:
			case Token.t_virtual:
			case Token.t_explicit:
			case Token.t_char:
			case Token.t_wchar_t:
			case Token.t_bool:
			case Token.t_short:
			case Token.t_int:
			case Token.t_long:
			case Token.t_signed:
			case Token.t_unsigned:
			case Token.t_float:
			case Token.t_double:
			case Token.t_void:
			case Token.t_const:
			case Token.t_volatile:
			case Token.t_friend:
			case Token.t_typedef:
				consume();
				return true;
			case Token.t_typename:
				consume();
				return name();
			case Token.tCOLON:
			case Token.tIDENTIFIER:
				return name();
			case Token.t_class:
			case Token.t_struct:
			case Token.t_union:
				// classSpecifier();
				return true;
			case Token.t_enum:
				// enumSpecifier();
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * name
	 * : ("::")? name2 ("::" name2)*
	 * 
	 * name2
	 * : IDENTIFER
	 * 
	 * To Do:
	 * - Handle template ids
	 */
	public boolean name() throws Exception {
		if (LT(1) == Token.tCOLONCOLON)
			consume();

		while (LT(1) == Token.tIDENTIFIER) {
			consume();
			
			if (LT(1) == Token.tCOLONCOLON)
				consume();
		}
		
		return true;
	}

	/**
	 * initDeclarator
	 * : declarator ("=" initializerClause | "(" expressionList ")")? 
	 */
	public boolean initDeclarator() {
		return false;
	}
	
	// Token management
	private Scanner scanner;
	private Token currToken;
	
	private void fetchToken() throws Exception {
		scanner.nextToken();
	}

	protected Token LA(int i) throws Exception {
		if (i < 1)
			// can't go backwards
			return null;

		if (currToken == null)
			currToken = scanner.nextToken();
		
		Token retToken = currToken;
		 
		for (; i > 1; --i) {
			if (retToken.getNext() == null)
				scanner.nextToken();
			retToken = retToken.getNext();
		}
		
		return retToken;
	}

	protected int LT(int i) throws Exception {
		return LA(i).type;
	}
	
	protected Token consume() throws Exception {
		if (currToken.getNext() == null)
			scanner.nextToken();
		Token retToken = currToken;
		currToken = currToken.getNext();
		return retToken;
	}
	
	protected Token mark() {
		return currToken;
	}
	
	protected void backup(Token mark) {
		currToken = mark;
	}
	

}
