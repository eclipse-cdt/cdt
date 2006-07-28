package org.eclipse.dd.dsf.debug;

import java.util.Map;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Expressions service provides access to the debugger's expression evaluator.  
 * This service has dependencies on the Modules service, RunControl service, and
 * Stack service, as all may be used to provide context for an expression to be
 * evaluated.
 */
public interface IExpressions extends IDataModelService {
    
    /** 
     * Expression context.  Since some expressions have children, expression 
     * contexts can be have an arbitrary number of parents of type 
     * ExpressionContext.
     */
    public interface IExpressionDMC extends IDataModelContext<IExpressionData> {
        String getExpression();
    }
    
    /**
     * Expression data.  It is based pretty closely on what DFW's info-retrieve
     * node for expressions.
     */
    public interface IExpressionData extends IDataModelData {
        String getName();
        public enum BasicType { basic, pointer, array, composite, enumeration, function }
        BasicType getBasicType();
        String getTypeName();
        String getEncoding();
        String getTypeId();
        int getBitCount();
        String getNaturalValue();
        String getHexValue();
        String getOctalValue();
        String getBinaryValue();
        String getStringValue();
        IAddress getAddress();
        IRegisters.RegisterDMC getRegister();
        Map<String,String> getEnumerations();
    }

    /**
     * Event indicating that a given expression is changed.  If an expression 
     * is changed,  it's implied that all the children of that expression are
     * changed too.
     */
    public interface IExpressionChangedEvent extends IDataModelEvent<IExpressionDMC> {}

    /**
     * Returns the context for the specified expression.
     * @param symCtx Symbol context in which to evaluate the expression.  This parameter is required and cannot be null.
     * @param execCtx Optional execution context for the evaluation.  Can be null.
     * @param frameCtx Optional stack frame context for the evaluation.  Can be null.
     * @param expression  Expression to evaluate.
     * @return Expression context.
     */
    IExpressionDMC getExpressionContext(IModules.ISymbolDMC symCtx, IRunControl.IExecutionDMC execCtx, IStack.IFrameDMC frameCtx, String expression);
    
    /**
     * Retrieves the sub-expressions of the given expression.
     * @param exprCtx Expression context to evaluate.
     * @param done The return parameter is an Iterable because it's possible 
     * that the sub-expressions as members of an array which could be very large.    
     */
    void getSubExpressions(IExpressionDMC exprCtx, GetDataDone<Iterable<IExpressionDMC>> done);
    
    /**
     * For object oriented languages, this method returns the expressions 
     * representing base types of the given expression type.
     * @param exprContext
     * @param done
     */
    void getBaseExpressions(IExpressionDMC exprContext, GetDataDone<IExpressionDMC[]> done);
}
