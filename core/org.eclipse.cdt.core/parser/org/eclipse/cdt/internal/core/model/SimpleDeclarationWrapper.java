package org.eclipse.cdt.internal.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.AccessSpecifier;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

/**
 * @author jcamelon
 *
 */
public class SimpleDeclarationWrapper extends DeclSpecifier implements DeclSpecifier.Container, ICElementWrapper {

	private ICElement element = null; 
	private IParent parent = null; 

	private Name name = null;
	private boolean functionDefinition = false; 

	public SimpleDeclarationWrapper( IParent item )
	{
		this.parent = item; 
	}
	
	public SimpleDeclarationWrapper()
	{
	}
	
	/**
	 * Returns the item.
	 * @return CElement
	 */
	public ICElement getElement() {
		return element;
	}

	/**
	 * Sets the item.
	 * @param item The item to set
	 */
	public void setElement (ICElement item) {
		this.element = item;
	}

	/**
	 * Returns the parent.
	 * @return CElement
	 */
	public IParent getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * @param parent The parent to set
	 */
	public void setParent(IParent parent) {
		this.parent = parent;
	}
	
	public void createElements()
	{
		// creates the appropriate C Elements 
		List declaratorList = getDeclarators();
		Declarator [] declarators = (Declarator []) declaratorList.toArray( new Declarator[ declaratorList.size() ] );
		CElement parentElement = (CElement)getParent(); 
		
		for( int i = 0; i < declarators.length; ++i )
		{
			Declarator currentDeclarator = declarators[i];
			CElement declaration  = null;
			
			// instantiate the right element   
			List clause =currentDeclarator.getParameterDeclarationClause(); 
			String declaratorName = ( currentDeclarator.getName() == null ) ? "" : currentDeclarator.getName().toString();
			if( clause == null && !isTypedef())
			{ 
				// TODO - this was to get rid of the NULL pointer we've been seeing
				if (currentDeclarator.getName() == null)
					return;

				//	this is an attribute or a varaible
				if( parentElement instanceof IStructure )
				{
					declaration = createField( parentElement, declaratorName ); 
				}
				else if(( parentElement instanceof ITranslationUnit ) 
					  || ( parentElement instanceof INamespace ))
				{
					if(isExtern())
					{
						declaration = createVariableDeclaration( parentElement, declaratorName );
					}
					else
					{
						declaration = createVariable( parentElement, declaratorName );						
					}
				}
			}
			else if( isTypedef() )
			{
				declaration = createTypedef( parentElement, declaratorName );
			}
			else
			{
				Parameter [] parameters = (Parameter []) clause.toArray( new Parameter[ clause.size() ]);
				// this is a function or a method
				if( parentElement instanceof IStructure )
				{
					if (isFunctionDefinition())
					{
						declaration = createMethod( parentElement, declaratorName, parameters ); 
					}
					else
					{
						declaration = createMethodDeclaration( parentElement, declaratorName, parameters ); 
					}
		
				}
				else if(( parentElement instanceof ITranslationUnit ) 
						|| ( parentElement instanceof INamespace ))
				{
					if (isFunctionDefinition())
					{
						// if it belongs to a class, then create a method
						// else create a function
						// this will not be known until we have cross reference information
						declaration = createFunction( parentElement, declaratorName, parameters ); 
					}
					else
					{
						declaration = createFunctionDeclaration( parentElement, declaratorName, parameters ); 
					}
				}				
			}
			
			
			if( currentDeclarator.getName() != null )
			{
				// hook up the offsets
				declaration.setIdPos( currentDeclarator.getName().getStartOffset(), currentDeclarator.getName().length() );
				declaration.setPos( currentDeclarator.getName().getStartOffset(), currentDeclarator.getName().length());
			}
			else
			{
				declaration.setIdPos( classKind.getOffset(), classKind.getImage().toString().length());
				declaration.setPos( classKind.getOffset(), classKind.getImage().toString().length());
			}
			
			// add to parent
			parentElement.addChild( declaration ); 	
		}
		
	}
	
	List declarators = new LinkedList();
	String [] myString;
	
	public void addDeclarator( Object in )
	{
		declarators.add( in ); 
	}
		
	public List getDeclarators()
	{
		return declarators; 
	}
	
	DeclSpecifier declSpec = null; 

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#getDeclSpecifier()
	 */
	public DeclSpecifier getDeclSpecifier() {
		if( declSpec == null )
			declSpec = new DeclSpecifier(); 
			
		return declSpec; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.dom.DeclarationSpecifier.CElementWrapper#setDeclSpecifier(org.eclipse.cdt.internal.core.dom.DeclarationSpecifier)
	 */
	public void setDeclSpecifier(DeclSpecifier in) {
		declSpec = in; 
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.util.DeclarationSpecifier.Container#removeDeclarator(java.lang.Object)
	 */
	public void removeDeclarator(Object declarator) {
		declarators.remove( declarator );
	}

	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	private Token classKind; 

	/**
	 * Returns the functionDefinition.
	 * @return boolean
	 */
	public boolean isFunctionDefinition() {
		return functionDefinition;
	}

	/**
	 * Sets the functionDefinition.
	 * @param functionDefinition The functionDefinition to set
	 */
	public void setFunctionDefinition(boolean functionDefinition) {
		this.functionDefinition = functionDefinition;
	}

	private AccessSpecifier currentVisibility = new AccessSpecifier(); 
	/**
	 * @return int
	 */
	public int getCurrentVisibility() {
		return currentVisibility.getAccess();
	}

	/**
	 * Sets the currentVisibility.
	 * @param currentVisibility The currentVisibility to set
	 */
	public void setCurrentVisibility(int currentVisibility) {
		this.currentVisibility.setAccess( currentVisibility );
	}
	
	/**
	 * Creates a Field and fills its info
	 * @param parent
	 * @param name
	 * @return CElement
	 */
	private CElement createField(CElement parent, String name){
		Field newElement = new Field( parent, name );
		newElement.setTypeName ( getTypeName() );
		newElement.setMutable(isMutable());
		newElement.setVisibility(this.getCurrentVisibility());
		newElement.setConst(isConst());
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;
	}

	private CElement createTypedef(CElement parent, String name){
		CElement typedef = new TypeDef( parent, name ); 
		return typedef;  
	}


	/**
	 * Creates a Variable and fills its info
	 * @param parent
	 * @param name
	 * @return CElement
	 */
	private CElement createVariable(CElement parent, String name){
		Variable newElement = new Variable( parent, name );
		newElement.setTypeName ( getTypeName() );
		newElement.setConst(isConst());
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;
	}

	/**
	 * Creates a VariableDeclaration and fills its info
	 * @param parent
	 * @param name
	 * @return CElement
	 */
	private CElement createVariableDeclaration(CElement parent, String name){
		VariableDeclaration newElement = new VariableDeclaration( parent, name );
		newElement.setTypeName ( getTypeName() );
		newElement.setConst(isConst());
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;
	}


	/**
	 * Creates a MethodDeclaration and fills its info
	 * @param parent
	 * @param name
	 * @param parameters
	 * @return CElement
	 */
	private CElement createMethodDeclaration(CElement parent, String name, Parameter[] parameters){
		String[] parameterTypes = new String[parameters.length];
		for( int j = 0; j< parameters.length; ++j )
		{
			Parameter param = parameters[j];
			parameterTypes[j] = new String(param.getTypeName());
		}

		MethodDeclaration newElement = new MethodDeclaration( parent, name );
		newElement.setParameterTypes(parameterTypes);
		newElement.setReturnType( getTypeName() );
		newElement.setVisibility(this.getCurrentVisibility());
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;		
	}

	/**
	 * Creates a Method and fills its info
	 * @param parent
	 * @param name
	 * @param parameters
	 * @return CElement
	 */
	private CElement createMethod(CElement parent, String name, Parameter[] parameters){
		String[] parameterTypes = new String[parameters.length];
		for( int j = 0; j< parameters.length; ++j )
		{
			Parameter param = parameters[j];
			parameterTypes[j] = new String(param.getTypeName());
		}

		Method newElement = new Method( parent, name );
		newElement.setParameterTypes(parameterTypes);
		newElement.setReturnType( getTypeName() );
		newElement.setVisibility(this.getCurrentVisibility());
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;		
	}

	/**
	 * Creates a FunctionDeclaration and fills its info
	 * @param parent
	 * @param name
	 * @param parameters
	 * @return CElement
	 */
	private CElement createFunctionDeclaration(CElement parent, String name, Parameter[] parameters){
		String[] parameterTypes = new String[parameters.length];
		for( int j = 0; j< parameters.length; ++j )
		{
			Parameter param = parameters[j];
			parameterTypes[j] = new String(param.getTypeName());
		}

		FunctionDeclaration newElement = new FunctionDeclaration( parent, name );
		newElement.setParameterTypes(parameterTypes);
		newElement.setReturnType( getTypeName() );
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;
	}

	/**
	 * Creates a Function and fills its info
	 * @param parent
	 * @param name
	 * @param parameters
	 * @return CElement
	 */
	private CElement createFunction(CElement parent, String name, Parameter[] parameters){
		String[] parameterTypes = new String[parameters.length];
		for( int j = 0; j< parameters.length; ++j )
		{
			Parameter param = parameters[j];
			parameterTypes[j] = new String(param.getTypeName());
		}

		Function newElement = new Function( parent, name );
		newElement.setParameterTypes(parameterTypes);
		newElement.setReturnType( getTypeName() );
		newElement.setVolatile(isVolatile());
		newElement.setStatic(isStatic());
		return newElement;
	}

	/**
	 * @return Token
	 */
	public Token getClassKind() {
		return classKind;
	}

	/**
	 * Sets the classKind.
	 * @param classKind The classKind to set
	 */
	public void setClassKind(Token classKind) {
		this.classKind = classKind;
	}

}
