package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;




public class Declarator implements IDeclaratorOwner {
	
	public Declarator(DeclSpecifier.IContainer declaration) {
		this.declaration = declaration;
		this.ownerDeclarator = null; 
	}
	
	public Declarator( IDeclaratorOwner owner )
	{
		this.ownerDeclarator = owner;
		this.declaration = null;
	}
	
	private final DeclSpecifier.IContainer declaration;
	private final IDeclaratorOwner ownerDeclarator;
	private int nameOffset;
	
	/**
	 * Returns the declaration.
	 * @return SimpleDeclaration
	 */
	public DeclSpecifier.IContainer getDeclaration() {
		return declaration;
	}

	/**
	 * Sets the declaration.
	 * @param declaration The declaration to set
	 */
	public void setDeclaration(SimpleDeclaration declaration) {
		
	}

	private String name;
	
	/**
	 * Returns the name.
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	ParameterDeclarationClause parms = null; 

	public void addParms( ParameterDeclarationClause parms )
	{
		this.parms = parms; 
	}	
	
	/**
	 * Returns the parms.
	 * @return ParameterDeclarationClause
	 */
	public ParameterDeclarationClause getParms() {
		return parms;
	}
	
	List pointerOperators = new ArrayList();
	List arrayQualifiers = new ArrayList(); 
	
	/**
	 * @return List
	 */
	public List getPointerOperators() {
		return Collections.unmodifiableList(pointerOperators);
	}

	public void addPointerOperator( PointerOperator po )
	{
		pointerOperators.add(po);
	}
	
	ExceptionSpecifier exceptionSpecifier = new ExceptionSpecifier(); 
	
	public ExceptionSpecifier getExceptionSpecifier()
	{
		return exceptionSpecifier; 
	}
	
	boolean isConst = false; 
	boolean isVolatile = false;
	boolean isPureVirtual = false; 
	/**
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/**
	 * @return List
	 */
	public List getArrayQualifiers() {
		return Collections.unmodifiableList( arrayQualifiers );
	}

	public void addArrayQualifier( ArrayQualifier q )
	{
		arrayQualifiers.add(q);
	}
	
	private ConstructorChain ctorChain = null;
	
	/**
	 * @return ConstructorChain
	 */
	public ConstructorChain getCtorChain() {
		return ctorChain;
	}

	/**
	 * Sets the ctorChain.
	 * @param ctorChain The ctorChain to set
	 */
	public void setCtorChain(ConstructorChain ctorChain) {
		this.ctorChain = ctorChain;
	}

	/**
	 * @return boolean
	 */
	public boolean isPureVirtual() {
		return isPureVirtual;
	}

	/**
	 * Sets the isPureVirtual.
	 * @param isPureVirtual The isPureVirtual to set
	 */
	public void setPureVirtual(boolean isPureVirtual) {
		this.isPureVirtual = isPureVirtual;
	}

	private Declarator innerDeclarator = null; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IDeclaratorOwner#getDeclarator()
	 */
	public Declarator getDeclarator() {
		return innerDeclarator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IDeclaratorOwner#setDeclarator(org.eclipse.cdt.internal.core.dom.Declarator)
	 */
	public void setDeclarator(Declarator input) {
		innerDeclarator = input; 		
	}

	/**
	 * @return
	 */
	public IDeclaratorOwner getOwnerDeclarator() {
		return ownerDeclarator;
	}
	
	private BitField bitField = null; 

	/**
	 * @return
	 */
	public BitField getBitField() {
		return bitField;
	}

	/**
	 * @param field
	 */
	public void setBitField(BitField field) {
		bitField = field;
	}

    /**
     * @return
     */
    public int getNameOffset()
    {
        return nameOffset;
    }

    /**
     * @param i
     */
    public void setNameOffset(int i)
    {
        nameOffset = i;
    }

}
