/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;


/**
 * A simple template variable, which always evaluates to a defined string.
 * 
 * @since 3.0
 */
public class SimpleVariableResolver extends TemplateVariableResolver {

	/** The string to which this variable evaluates. */
	private String fEvaluationString;

	/*
	 * @see TemplateVariableResolver#TemplateVariableResolver(String, String)
	 */
	protected SimpleVariableResolver(String type, String description) {
		super(type, description);
	}

	/**
	 * Sets the string to which this variable evaluates.
	 * 
	 * @param evaluationString the evaluation string, may be <code>null</code>.
	 */
	public final void setEvaluationString(String evaluationString) {
		fEvaluationString= evaluationString;	
	}

	/*
	 * @see TemplateVariableResolver#evaluate(TemplateContext)
	 */
	protected String resolve(TemplateContext context) {
		return fEvaluationString;
	}

}
