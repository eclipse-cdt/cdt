package org.eclipse.cdt.internal.core.dom;


import org.eclipse.cdt.core.dom.IScope;
import org.eclipse.cdt.internal.core.newparser.IParserCallback;
import org.eclipse.cdt.internal.core.newparser.Token;

/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements IParserCallback {

	private TranslationUnit translationUnit;
	
	public TranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin()
	 */
	public void argumentsBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		SimpleDeclaration decl = (SimpleDeclaration)container;
		
		int kind = ClassSpecifier.t_struct;
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassSpecifier.t_class;
				break;
			case Token.t_struct:
				kind = ClassSpecifier.t_struct;
				break;
			case Token.t_union:
				kind = ClassSpecifier.t_union;
				break;			
		}
		
		ClassSpecifier classSpecifier = new ClassSpecifier(kind, decl);
		decl.setTypeSpecifier(classSpecifier);
		return classSpecifier;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName()
	 */
	public void classSpecifierName(Object classSpecifier) {
		((ClassSpecifier)classSpecifier).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classEnd()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public Object declaratorBegin(Object container) {
		SimpleDeclaration decl = (SimpleDeclaration)container; 
		Declarator declarator = new Declarator(decl);
		decl.addDeclarator(declarator);
		return declarator; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd(Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void declaratorId(Object declarator) {
		((Declarator)declarator).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
		SimpleDeclaration decl = (SimpleDeclaration)Container;
		
		switch (specifier.getType()) {
			case Token.t_auto:
				decl.setAuto(true);
				break;
			case Token.t_register:
				decl.setRegister(true);
				break;
			case Token.t_static:
				decl.setStatic(true);
				break;
			case Token.t_extern:
				decl.setExtern(true);
				break;
			case Token.t_mutable:
				decl.setMutable(true);
				break;
			case Token.t_inline:
				decl.setInline(true);
				break;
			case Token.t_virtual:
				decl.setVirtual(true);
				break;
			case Token.t_explicit:
				decl.setExplicit(true);
				break;
			case Token.t_typedef:
				decl.setTypedef(true);
				break;
			case Token.t_friend:
				decl.setFriend(true);
				break;
			case Token.t_const:
				decl.setConst(true);
				break;
			case Token.t_volatile:
				decl.setVolatile(true);
				break;
			case Token.t_char:
				decl.setType(SimpleDeclaration.t_char);
				break;
			case Token.t_wchar_t:
				decl.setType(SimpleDeclaration.t_wchar_t);
				break;
			case Token.t_bool:
				decl.setType(SimpleDeclaration.t_bool);
				break;
			case Token.t_short:
				decl.setShort(true);
				break;
			case Token.t_int:
				decl.setType(SimpleDeclaration.t_int);
				break;
			case Token.t_long:
				decl.setLong(true);
				break;
			case Token.t_signed:
				decl.setUnsigned(false);
				break;
			case Token.t_unsigned:
				decl.setUnsigned(true);
				break;
			case Token.t_float:
				decl.setType(SimpleDeclaration.t_float);
				break;
			case Token.t_double:
				decl.setType(SimpleDeclaration.t_double);
				break;
			case Token.t_void:
				decl.setType(SimpleDeclaration.t_void);
				break;
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object simpleDeclarationBegin(Object container, Token firstToken) {
		SimpleDeclaration decl = new SimpleDeclaration();
		((IScope)container).addDeclaration(decl);
		return decl;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		translationUnit = new TranslationUnit();
		return translationUnit; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd()
	 */
	public void translationUnitEnd(Object unit) {
	}

	private Name currName;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		currName.setEnd(lastToken);
	}

	public Object baseSpecifierBegin( Object classSpecifier )
	{
		ClassSpecifier cs =(ClassSpecifier)classSpecifier;
		BaseSpecifier baseSpec = new BaseSpecifier( cs );
		return baseSpec; 
	}

	public void baseSpecifierEnd( Object baseSpecifier  )
	{
		
	}

	public void baseSpecifierVirtual( Object baseSpecifier, boolean virtual )
	{
		BaseSpecifier bs = (BaseSpecifier)baseSpecifier; 
		bs.setVirtual( virtual );
	}

	public void baseSpecifierVisibility( Object baseSpecifier, Token visibility )
	{
		int access = BaseSpecifier.t_public;  
		switch( visibility.type )
		{
		case Token.t_public:
			access = BaseSpecifier.t_public; 
			break; 
		case Token.t_protected:
			access = BaseSpecifier.t_protected;		 
			break;
		case Token.t_private:
			access = BaseSpecifier.t_private;
			break; 		
		default: 
			break;
		}
	
		((BaseSpecifier)baseSpecifier).setAccess(access);  
	}

	
	public void baseSpecifierName( Object baseSpecifier )
	{
		((BaseSpecifier)baseSpecifier).setName(currName.getName());		
	}
}
