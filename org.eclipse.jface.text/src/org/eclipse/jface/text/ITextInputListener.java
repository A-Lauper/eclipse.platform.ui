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
package org.eclipse.jface.text;



/**
 * Text input listeners registered with an text viewer are informed 
 * if the document serving as the text viewer's model is replaced.
 * Clients may implement this interface.
 *
 * @see ITextViewer
 * @see IDocument
 */
public interface ITextInputListener {
	
	/**
	 * Called before the input document is replaced.
	 * 
	 * @param oldInput the text viewer's previous input document
	 * @param newInput the text viewer's new input document
	 */
	void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput);
	
	/**
	 * Called after the input document has been replaced.
	 * 
	 * @param oldInput the text viewer's previous input document
	 * @param newInput the text viewer's new input document
	 */
	void inputDocumentChanged(IDocument oldInput, IDocument newInput);
}
