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
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * Implementation of a child document based on <code>ProjectionDocument</code>.
 * This class exists for compatibility reasons.
 * <p>
 * Internal class. Do not use. Public only for testing purposes.
 * 
 * @since 3.0
 */
public class ChildDocument extends ProjectionDocument {
	
	/**
	 * Position reflecting a visible region. The exclusive end offset of the position
	 * is considered being overlapping with the visible region.
	 */
	static private class VisibleRegion extends Position {
		
		public VisibleRegion(int offset, int length) {
			super(offset, length);
		}
		
		/**
		 * If offset is the end of the visible region and the length is 0,
		 * the offset is considered overlapping with the visible region.
		 */
		public boolean overlapsWith(int offset, int length) {
			boolean appending= (offset == this.offset + this.length) && length == 0;
			return appending || super.overlapsWith(offset, length);
		}
	}

	/**
	 * Creates a new child document.
	 * 
	 * @param masterDocument @inheritDoc
	 */
	public ChildDocument(IDocument masterDocument) {
		super(masterDocument);
	}

	/**
	 * Returns the parent document of this child document.
	 * 
	 * @return the parent document of this child document
	 * @see ProjectionDocument#getMasterDocument()
	 */
	public IDocument getParentDocument() {
		return getMasterDocument();
	}

	/**
	 * Sets the parent document range covered by this child document to the
	 * given range.
	 * 
	 * @param offset the offset of the range
	 * @param length the length of the range
	 */
	public void setParentDocumentRange(int offset, int length) throws BadLocationException {
		replaceMasterDocumentRanges(offset, length);
	}

	/**
	 * Returns the parent document range of this child document.
	 * 
	 * @return the parent document range of this child document
	 */
	public Position getParentDocumentRange() {
		IRegion coverage= getProjectionMapping().getCoverage();
		return new VisibleRegion(coverage.getOffset(), coverage.getLength());
	}
}
