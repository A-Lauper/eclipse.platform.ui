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

package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IMarkRegionTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension3;

/**
 * Default implementation of <code>IMarkRegionTarget</code> using <code>ITextViewer</code>
 * and <code>IStatusLineManager</code>.
 * @since 2.0
 */
public class MarkRegionTarget implements IMarkRegionTarget {
	
	/** The text viewer. */
	private final ITextViewer fViewer;	
	/** The status line. */
	private final IStatusLineManager fStatusLine;
	
	/**
	 * Creates a MarkRegionTaret.
	 * 
	 * @param viewer the text viewer
	 * @param manager the status line manager
	 */
	public MarkRegionTarget(ITextViewer viewer, IStatusLineManager manager) {
		fViewer= viewer;
		fStatusLine= manager;		
	}
	
	/*
	 * @see IMarkregion#setMarkAtCursor(boolean)
	 */
	public void setMarkAtCursor(boolean set) {
		
		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		if (set) {
			Point selection= fViewer.getSelectedRange();
			viewerExtension.setMark(selection.x);
		
			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.set")); //$NON-NLS-1$
	
		} else {
			viewerExtension.setMark(-1);

			fStatusLine.setErrorMessage(""); //$NON-NLS-1$
			fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.cleared")); //$NON-NLS-1$								
		}
	}

	/*
	 * @see IMarkregion#swapMarkAndCursor()
	 */
	public void swapMarkAndCursor() {

		if (!(fViewer instanceof ITextViewerExtension))
			return;

		ITextViewerExtension viewerExtension= ((ITextViewerExtension) fViewer);

		int markPosition= viewerExtension.getMark();		
		if (markPosition == -1) {
			fStatusLine.setErrorMessage(EditorMessages.getString("MarkRegionTarget.markNotSet")); //$NON-NLS-1$
			fStatusLine.setMessage(""); //$NON-NLS-1$			
			return;
		}

		if (!isVisible(fViewer, markPosition)) {
			fStatusLine.setErrorMessage(EditorMessages.getString("MarkRegionTarget.markNotVisible")); //$NON-NLS-1$
			fStatusLine.setMessage(""); //$NON-NLS-1$
			return;
		}		
		
		Point selection= fViewer.getSelectedRange();		
		viewerExtension.setMark(selection.x);

		fViewer.setSelectedRange(markPosition, 0);
		fViewer.revealRange(markPosition, 0);

		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(EditorMessages.getString("Editor.mark.status.message.mark.swapped")); //$NON-NLS-1$
	}
	
	protected final static boolean isVisible(ITextViewer viewer, int offset) {
		if (viewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) viewer;
			return extension.modelOffset2WidgetOffset(offset) >= 0;
		} else {
			IRegion region= viewer.getVisibleRegion();
			int vOffset= region.getOffset();
			return (vOffset <= offset &&  offset <= vOffset + region.getLength());
		}
	}
}
