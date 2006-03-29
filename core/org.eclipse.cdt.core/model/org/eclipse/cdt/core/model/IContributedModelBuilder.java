package org.eclipse.cdt.core.model;

/**
 * Interface supported by model builders for contributed languages.
 * 
 * Model builders parse a <code>TranslationUnit</code> (i.e., a file) and
 * return a hierarchy of <code>ICElement</code>s which represent the high-level
 * structure of that file (what modules, classes, functions, and similar
 * constructs are contained in it, and on what line(s) the definition occurs).
 * 
 * The translation unit to parse and the initial element map are given to
 * <code>IAdditionalLanguage#createModelBuilder</code>, which will presumably
 * pass that information on to the model builder constructor.
 * 
 * @author Jeff Overbey
 */
public interface IContributedModelBuilder {
	/**
	 * Callback used when a <code>TranslationUnit</code> needs to be parsed.
	 * 
	 * The translation unit to parse is given to
	 * <code>ILanguage#createModelBuilder</code>, which will presumably
	 * pass it on to the model builder constructor.
	 */
	public abstract void parse(boolean quickParseMode) throws Exception;
}
