package org.eclipse.cdt.internal.parser.generated;

/**
 * Thrown when the parser keeps generating exceptions for the 
 * same token. This is a RuntimeException for expediency only,
 * the CPPParser catches ParserException (hence the lockup cases)
 * and we need a way to get past those catches. At the same time,
 * CPPParser is machine generated over 9kloc. Going in and adding
 * "throws" statements to everything is just silly. So, this
 * is thrown when the parser appears to be in a loop.
 * 
 * @author songer
 */
public class ParserFatalException extends RuntimeException
{

}
