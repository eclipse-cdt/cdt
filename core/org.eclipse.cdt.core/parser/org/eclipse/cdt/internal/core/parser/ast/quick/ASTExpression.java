/*
 * Created on Jun 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;



/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ASTExpression implements IASTExpression {

	private final Kind kind; 
	private final IASTExpression lhs, rhs, third; 
	private final IASTTypeId typeId;
	private final String literal, idExpression; 
	private final IASTNewExpressionDescriptor newDescriptor;

	/**
	 * @param kind
	 * @param lhs
	 * @param rhs
	 * @param id
	 * @param typeId
	 * @param literal
	 */
	public ASTExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression third, IASTTypeId typeId, String idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) {
		this.kind = kind; 
		this.lhs =lhs; 
		this.rhs = rhs; 
		this.third = third;
		this.typeId = typeId; 
		this.literal = literal;
		this.newDescriptor = newDescriptor;
		this.idExpression = idExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getExpressionKind()
	 */
	public Kind getExpressionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLHSExpression()
	 */
	public IASTExpression getLHSExpression() {
		return lhs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getRHSExpression()
	 */
	public IASTExpression getRHSExpression() {
		return rhs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		return literal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return typeId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getNewExpressionDescriptor()
	 */
	public IASTNewExpressionDescriptor getNewExpressionDescriptor() {
		return newDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getThirdExpression()
	 */
	public IASTExpression getThirdExpression() {
		return third;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
	 */
	public int evaluateExpression() throws ExpressionEvaluationException {
		// primary expressions
		if( getExpressionKind() == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL )
			return Integer.parseInt( getLiteralString() );
		if( getExpressionKind() == IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION ) 
			return getLHSExpression().evaluateExpression();
		// unary not 
		if( getExpressionKind() == IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION ) 
			return ( ( getLHSExpression().evaluateExpression() == 0 ) ? 1 : 0 ); 
		
		// multiplicative expressions 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY )
			return ( getLHSExpression().evaluateExpression() * getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_DIVIDE )
			return ( getLHSExpression().evaluateExpression() / getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_MODULUS )
			return ( getLHSExpression().evaluateExpression() % getRHSExpression().evaluateExpression()) ;
		// additives 
		if( getExpressionKind() == IASTExpression.Kind.ADDITIVE_PLUS )
			return ( getLHSExpression().evaluateExpression() + getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.ADDITIVE_MINUS )
			return ( getLHSExpression().evaluateExpression() - getRHSExpression().evaluateExpression()) ; 
		// shift expression 
		if( getExpressionKind() == IASTExpression.Kind.SHIFT_LEFT )
			return ( getLHSExpression().evaluateExpression() << getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.SHIFT_RIGHT )
			return ( getLHSExpression().evaluateExpression() >> getRHSExpression().evaluateExpression()) ;
		// relational 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_LESSTHAN )
			return ( getLHSExpression().evaluateExpression() < getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_GREATERTHAN )
			return ( getLHSExpression().evaluateExpression() > getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO )
			return ( getLHSExpression().evaluateExpression() <= getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO )
			return ( getLHSExpression().evaluateExpression() >= getRHSExpression().evaluateExpression() ? 1 : 0 ) ;
		// equality 
		if( getExpressionKind() == IASTExpression.Kind.EQUALITY_EQUALS )
			return ( getLHSExpression().evaluateExpression() == getRHSExpression().evaluateExpression() ? 1 : 0 ) ;  
		if( getExpressionKind() == IASTExpression.Kind.EQUALITY_NOTEQUALS )
			return ( getLHSExpression().evaluateExpression() != getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		 // and  
		if( getExpressionKind() == IASTExpression.Kind.ANDEXPRESSION )
			return ( getLHSExpression().evaluateExpression() & getRHSExpression().evaluateExpression() ) ;
		 // xor
		if( getExpressionKind() == IASTExpression.Kind.EXCLUSIVEOREXPRESSION )
			return ( getLHSExpression().evaluateExpression() ^ getRHSExpression().evaluateExpression() ) ;
		// or 
		if( getExpressionKind() == IASTExpression.Kind.INCLUSIVEOREXPRESSION )
			return ( getLHSExpression().evaluateExpression() | getRHSExpression().evaluateExpression() ) ;
		// logical and
		if( getExpressionKind() == IASTExpression.Kind.LOGICALANDEXPRESSION )
			return( ( getLHSExpression().evaluateExpression() != 0 ) &&  ( getRHSExpression().evaluateExpression() != 0 ) ) ? 1 : 0 ;	 
		// logical or  
		if( getExpressionKind() == IASTExpression.Kind.LOGICALOREXPRESSION )
			return( ( getLHSExpression().evaluateExpression() != 0 ) || ( getRHSExpression().evaluateExpression() != 0 ) ) ? 1 : 0 ;	 

		throw new ExpressionEvaluationException();  
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getIdExpression()
     */
    public String getIdExpression()
    {
    	return idExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
     */
    public void reconcileReferences() throws ASTNotImplementedException
    {
    	throw new ASTNotImplementedException();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException
	{
		throw new ASTNotImplementedException();
	}


}
