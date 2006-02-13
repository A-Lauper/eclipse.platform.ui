/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.quickassist;

import org.eclipse.swt.graphics.Color;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.source.Annotation;


/**
 * An <code>IQuickAssistAssistant</code> provides support for quick fixes and quick
 * assists.
 * The quick assist assistant is a {@link org.eclipse.jface.text.ITextViewer} add-on. Its
 * purpose is to propose, display, and insert completions of the content
 * of the text viewer's document at the viewer's cursor position.
 * <p>
 * A quick assist assistant has a list of {@link IQuickAssistProcessor}
 * objects each of which is registered for a  particular document content
 * type. The quick assist assistant uses the processors to provide the possible
 * quick assists.
 * </p>
 * <p>
 * The interface can be implemented by clients. By default, clients use
 * {@link QuickAssistAssistant} as the standard
 * implementer of this interface.
 * </p>
 *
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor
 */
 public interface IQuickAssistAssistant {

	/**
	 * Installs quick assist support on the given text viewer.
	 *
	 * @param textViewer the text viewer on which quick assist will work
	 */
	void install(ITextViewer textViewer);

	/**
	 * Sets the information control creator for the additional information control.
	 *
	 * @param creator the information control creator for the additional information control
	 */
	void setInformationControlCreator(IInformationControlCreator creator);

	/**
	 * Uninstalls quick assist support from the text viewer it has
	 * previously be installed on.
	 */
	void uninstall();

	/**
	 * Shows all possible quick fixes and quick assists at the viewer's cursor position.
	 *
	 * @return an optional error message if no proposals can be computed
	 */
	String showPossibleQuickAssists();
	
	/**
	 * Registers a given quick assist processor for a particular content type. If there is already
	 * a processor registered, the new processor is registered instead of the old one.
	 *
	 * @param processor the quick assist processor to register, or <code>null</code> to remove
	 *        an existing one
	 */
	void setQuickAssistProcessor(IQuickAssistProcessor processor);
	
	/**
	 * Returns the quick assist processor to be used for the given content type.
	 *
	 * @return the quick assist processor or <code>null</code> if none exists
	 */
	IQuickAssistProcessor getQuickAssistProcessor();
	
	/**
	 * Tells whether this assistant has a fix for the given annotation.
	 * <p>
	 * <em>Note: this test must be fast and optimistic i.e. it is OK to return
	 * <code>true</code> even though there might be no quick fix.</em>
	 * </p>
	 * 
	 * @param annotation the annotation
	 * @return <code>true</code> if the assistant has a fix for the given annotation
	 */
	boolean canFix(Annotation annotation);
	
	/**
	 * Tells whether this assistant has assists for the given invocation context.
	 * 
	 * @param invocationContext the invocation context
	 * @return <code>true</code> if the assistant has a fix for the given annotation
	 */
	boolean canAssist(IQuickAssistInvocationContext invocationContext);
	
	/**
	 * Sets the proposal selector's background color.
	 *
	 * @param background the background color
	 */
	void setProposalSelectorBackground(Color background);

	/**
	 * Sets the proposal's foreground color.
	 *
	 * @param foreground the foreground color
	 */
	void setProposalSelectorForeground(Color foreground);
	
	/**
	 * Adds a completion listener that will be informed before proposals are computed.
	 * 
	 * @param listener the listener
	 */
	void addCompletionListener(ICompletionListener listener);

	/**
	 * Removes a completion listener.
	 * 
	 * @param listener the listener to remove
	 */
	void removeCompletionListener(ICompletionListener listener);
	
	/**
	 * Enables displaying a status line below the proposal popup. The default is not to show the
	 * status line. The contents of the status line may be set via {@link #setStatusMessage(String)}.
	 * 
	 * @param show <code>true</code> to show a message line, <code>false</code> to not show one.
	 */
	public void setStatusLineVisible(boolean show);

	/**
	 * Sets the caption message displayed at the bottom of the completion proposal popup.
	 * 
	 * @param message the message
	 */
	public void setStatusMessage(String message);
	
}
