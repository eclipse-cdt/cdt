package org.eclipse.cdt.codan.core.model;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.osgi.util.NLS;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ProblemPreference {

	String key();

	Class<? extends NLS> nls();

}
