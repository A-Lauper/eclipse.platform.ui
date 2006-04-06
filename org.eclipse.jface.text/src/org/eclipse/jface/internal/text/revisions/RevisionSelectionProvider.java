/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.revisions.Revision;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * A selection provider for annotate revisions. Selections of a revision can currently happen in
 * following ways - note that this list may be changed in the future:
 * <ul>
 * <li>when the user clicks the revision ruler with the mouse</li>
 * <li>when the caret is moved to a revision's line (only on post-selection)</li>
 * </ul>
 * <p>
 * The is read-only, i.e.it does not support {@link #setSelection(ISelection)}.
 * </p>
 * 
 * @since 3.2
 */
public final class RevisionSelectionProvider implements ISelectionProvider {

	/**
	 * Post selection listener on the viewer that remembers the selection provider it is registered
	 * with.
	 */
    private final class PostSelectionListener implements ISelectionChangedListener {
        private final IPostSelectionProvider fPostProvider;

		public PostSelectionListener(IPostSelectionProvider postProvider) {
			postProvider.addPostSelectionChangedListener(this);
			fPostProvider= postProvider;
        }

		public void selectionChanged(SelectionChangedEvent event) {
	    	ISelection selection= event.getSelection();
	    	if (selection instanceof ITextSelection) {
	    		ITextSelection ts= (ITextSelection) selection;
	            int offset= ts.getOffset();
	            setSelectedRevision(fPainter.getRevision(offset));
	        }
	    	
	    }
		
		public void dispose() {
			fPostProvider.removePostSelectionChangedListener(this);
		}
    }

	private final RevisionPainter fPainter;
	private final ListenerList fListeners= new ListenerList();
	
	/**
	 * The text viewer once we are installed, <code>null</code> if not installed.
	 */
	private ITextViewer fViewer;
	/**
	 * The selection listener on the viewer, or <code>null</code>.
	 */
	private PostSelectionListener fSelectionListener;
	
	/**
	 * The last selection, or <code>null</code>.
	 */
	private Revision fSelection;

	/**
	 * Creates a new selection provider.
	 * 
	 * @param painter the painter that the created provider interacts with
	 */
    RevisionSelectionProvider(RevisionPainter painter) {
		fPainter= painter;
    }

	/*
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    	fListeners.add(listener);
    }

    /*
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    	fListeners.remove(listener);
    }
    
	/*
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
    	if (fSelection == null)
    		return StructuredSelection.EMPTY;
	    return new StructuredSelection(fSelection);
    }

	/*
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
    	throw new UnsupportedOperationException();
    }

    /**
	 * Installs the selection provider on the viewer.
	 * 
	 * @param viewer the viewer on which we listen to for post selection events
	 */
    void install(ITextViewer viewer) {
    	uninstall();
		fViewer= viewer;
		if (fViewer != null) {
			ISelectionProvider provider= fViewer.getSelectionProvider();
			if (provider instanceof IPostSelectionProvider) {
	            IPostSelectionProvider postProvider= (IPostSelectionProvider) provider;
	            fSelectionListener= new PostSelectionListener(postProvider);
            }
		}
    }
    
    /**
     * Uninstalls the selection provider.
     */
    void uninstall() {
    	fViewer= null;
    	if (fSelectionListener != null) {
    		fSelectionListener.dispose();
    		fSelectionListener= null;
    	}
    }

    /**
	 * Private protocol used by {@link RevisionPainter} to signal selection of a revision.
	 * 
	 * @param revision the selected revision, or <code>null</code> for none
	 */
    void revisionSelected(Revision revision) {
    	setSelectedRevision(revision);
    }

	/**
	 * Updates the currently selected revision and sends out an event if it changed.
	 * 
	 * @param revision the newly selected revision or <code>null</code> for none
	 */
	private void setSelectedRevision(Revision revision) {
		if (revision != fSelection) {
			fSelection= revision;
			fireSelectionEvent();
		}
    }

    private void fireSelectionEvent() {
	    ISelection selection= getSelection();
	    SelectionChangedEvent event= new SelectionChangedEvent(this, selection);
	    
	    Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
	        ((ISelectionChangedListener) listeners[i]).selectionChanged(event);
        }
    }
}
