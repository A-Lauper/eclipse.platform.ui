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

package org.eclipse.jface.text.source;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;


/**
 * A vertical ruler column showing graphical representations of  annotations.
 * Will become final. Do not subclass.
 * @since 2.0
 */
public class AnnotationRulerColumn implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {
	
	/**
	 * Internal listener class.
	 */
	class InternalListener implements IViewportListener, IAnnotationModelListener, ITextListener {
		
		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int verticalPosition) {
			if (verticalPosition != fScrollPos)
				redraw();
		}
		
		/*
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			postRedraw();
		}
		
		/*
		 * @see ITextListener#textChanged(TextEvent)
		 */
		public void textChanged(TextEvent e) {
			if (e.getViewerRedrawState())
				postRedraw();
		}
	}
	
	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length. 
	 */
	private static class ReusableRegion implements IRegion {
		
		private int fOffset;
		private int fLength;

		/*
		 * @see org.eclipse.jface.text.IRegion#getLength()
		 */
		public int getLength() {
			return fLength;
		}

		/*
		 * @see org.eclipse.jface.text.IRegion#getOffset()
		 */
		public int getOffset() {
			return fOffset;
		}
		
		/**
		 * Updates this region.
		 * 
		 * @param offset the new offset
		 * @param length the new length
		 */
		public void update(int offset, int length) {
			fOffset= offset;
			fLength= length;
		}
	}
	
	/** This column's parent ruler */
	private CompositeRuler fParentRuler;
	/** The cached text viewer */
	private ITextViewer fCachedTextViewer;
	/** The cached text widget */
	private StyledText fCachedTextWidget;
	/** The ruler's canvas */
	private Canvas fCanvas;
	/** The vertical ruler's model */
	private IAnnotationModel fModel;
	/** Cache for the actual scroll position in pixels */
	private int fScrollPos;
	/** The drawable for double buffering */
	private Image fBuffer;
	/** The internal listener */
	private InternalListener fInternalListener= new InternalListener();
	/** The width of this vertical ruler */
	private int fWidth;
	/** Switch for enabling/disabling the setModel method. */
	private boolean fAllowSetModel= true;
	/**
	 * The list of annotation types to be shown in this ruler.
	 * @since 3.0
	 */
	private Set fConfiguredAnnotationTypes= new HashSet();
	/**
	 * The list of allowed annotation types to be shown in this ruler.
	 * An allowed annotation type maps to <code>true</code>, a disallowed
	 * to <code>false</code>.
	 * @since 3.0
	 */
	private Map fAllowedAnnotationTypes= new HashMap();
	/**
	 * The annotation access extension.
	 * @since 3.0
	 */
	private IAnnotationAccessExtension fAnnotationAccessExtension;
	/** 
	 * The hover for this column.
	 * @since 3.0
	 */
	private IAnnotationHover fHover;
	/**
	 * The cached annotations.
	 * @since 3.0
	 */
	private List fCachedAnnotations= new ArrayList();
	/**
	 * The hit detection cursor.
	 * @since 3.0
	 */
	private Cursor fHitDetectionCursor;
	/**
	 * The last cursor.
	 * @since 3.0
	 */
	private Cursor fLastCursor;
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param model the annotation model to get the annotations from
	 * @param width the width of the vertical ruler
	 * @param annotationAccess the annotation access
	 * @since 3.0
	 */
	public AnnotationRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		this(width, annotationAccess);
		fAllowSetModel= false;
		fModel= model;
		fModel.addAnnotationModelListener(fInternalListener);
	}
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param width the width of the vertical ruler
	 * @param annotationAccess the annotation access
	 * @since 3.0
	 */
	public AnnotationRulerColumn(int width, IAnnotationAccess annotationAccess) {
		fWidth= width;
		if (annotationAccess instanceof IAnnotationAccessExtension)
			fAnnotationAccessExtension= (IAnnotationAccessExtension) annotationAccess;
	}
	
	/**
	 * Constructs this column with the given arguments.
	 *
	 * @param model the annotation model to get the annotations from
	 * @param width the width of the vertical ruler
	 */
	public AnnotationRulerColumn(IAnnotationModel model, int width) {
		fWidth= width;
		fAllowSetModel= false;
		fModel= model;
		fModel.addAnnotationModelListener(fInternalListener);
	}
	
	/**
	 * Constructs this column with the given width.
	 *
	 * @param width the width of the vertical ruler
	 */
	public AnnotationRulerColumn(int width) {
		fWidth= width;
	}
	
	/*
	 * @see IVerticalRulerColumn#getControl()
	 */
	public Control getControl() {
		return fCanvas;
	}
	
	/*
	 * @see IVerticalRulerColumn#getWidth()
	 */
	public int getWidth() {
		return fWidth;
	}
	
	/*
	 * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		
		fParentRuler= parentRuler;
		fCachedTextViewer= parentRuler.getTextViewer();
		fCachedTextWidget= fCachedTextViewer.getTextWidget();

		fHitDetectionCursor= new Cursor(parentControl.getDisplay(), SWT.CURSOR_HAND);

		fCanvas= new Canvas(parentControl, SWT.NO_BACKGROUND);
		
		fCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				if (fCachedTextViewer != null)
					doubleBufferPaint(event.gc);
			}
		});
		
		fCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
				fCachedTextViewer= null;
				fCachedTextWidget= null;
			}
		});
		
		fCanvas.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
				mouseClicked(fParentRuler.getLineOfLastMouseButtonActivity());				
			}
			
			public void mouseDown(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
			}
			
			public void mouseDoubleClick(MouseEvent event) {
				fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
				mouseDoubleClicked(fParentRuler.getLineOfLastMouseButtonActivity());
			}
		});

		fCanvas.addMouseMoveListener(new MouseMoveListener() {
			/*
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 * @since 3.0
			 */
			public void mouseMove(MouseEvent e) {
				handleMouseMove(e);
			}
		});

		if (fCachedTextViewer != null) {
			fCachedTextViewer.addViewportListener(fInternalListener);
			fCachedTextViewer.addTextListener(fInternalListener);
		}
		
		return fCanvas;
	}
	
	/**
	 * Hook method for a mouse double click event on the given ruler line.
	 * 
	 * @param rulerLine the ruler line
	 */
	protected void mouseDoubleClicked(int rulerLine) {
	}

	/**
	 * Hook method for a mouse click event on the given ruler line.
	 * 
	 * @param rulerLine the ruler line
	 * @since 3.0
	 */
	protected void mouseClicked(int rulerLine) {
	}
	
	/**
	 * Handles mouse moves.
	 * 
	 * @param event the mouse move event
	 */
	private void handleMouseMove(MouseEvent event) {
		if (fCachedTextViewer != null) {
			int line= toDocumentLineNumber(event.y);
			Cursor cursor= (hasLineAnnotations(line) ? fHitDetectionCursor : null);
			if (cursor != fLastCursor) {
				fCanvas.setCursor(cursor);
				fLastCursor= cursor;
			}
		}				
	}

	/**
	 * Tells whether the given line contains any annotations.
	 * 
	 * @param lineNumber the line number
	 * @return <code>true</code> if the given line contains an annotation
	 */
	private boolean hasLineAnnotations(int lineNumber) {
		
		if (fModel == null)
			return false;
		
		IRegion line;
		try {
			IDocument d= fCachedTextViewer.getDocument();
			line= d.getLineInformation(lineNumber);
		}  catch (BadLocationException ex) {
			return false;
		}

		int lineStart= line.getOffset();
		int lineLength= line.getLength();
		
		Iterator e= fModel.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			
			if (a.isMarkedDeleted())
				continue;
			
			if (skip(a))
				continue;
			
			Position p= fModel.getPosition(a);
			if (p == null || p.isDeleted())
				continue;
			
			if (p.overlapsWith(lineStart, lineLength))
				return true;
		}
		
		return false;
	}

	/**
	 * Disposes the ruler's resources.
	 */
	private void handleDispose() {
		
		if (fCachedTextViewer != null) {
			fCachedTextViewer.removeViewportListener(fInternalListener);
			fCachedTextViewer.removeTextListener(fInternalListener);
		}
		
		if (fModel != null)
			fModel.removeAnnotationModelListener(fInternalListener);
		
		if (fBuffer != null) {
			fBuffer.dispose();
			fBuffer= null;
		}
		
		if (fHitDetectionCursor != null) {
			fHitDetectionCursor.dispose();
			fHitDetectionCursor= null;
		}
		
		fConfiguredAnnotationTypes.clear();
		fAllowedAnnotationTypes.clear();
		fAnnotationAccessExtension= null;
	}
	
	/**
	 * Double buffer drawing.
	 * 
	 * @param dest the GC to draw into
	 */
	private void doubleBufferPaint(GC dest) {
		
		Point size= fCanvas.getSize();
		
		if (size.x <= 0 || size.y <= 0)
			return;
		
		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}
		if (fBuffer == null)
			fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);
			
		GC gc= new GC(fBuffer);
		gc.setFont(fCachedTextWidget.getFont());
		try {
			gc.setBackground(fCanvas.getBackground());
			gc.fillRectangle(0, 0, size.x, size.y);
			
			if (fCachedTextViewer instanceof ITextViewerExtension5)
				doPaint1(gc);
			else
				doPaint(gc);
		} finally {
			gc.dispose();
		}
		
		dest.drawImage(fBuffer, 0, 0);
	}

	/**
	 * Returns the document offset of the upper left corner of the source viewer's
	 * viewport, possibly including partially visible lines.
	 * 
	 * @return document offset of the upper left corner including partially visible lines
	 */
	protected int getInclusiveTopIndexStartOffset() {
		
		if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {	
			int top= fCachedTextViewer.getTopIndex();
			if ((fCachedTextWidget.getTopPixel() % fCachedTextWidget.getLineHeight()) != 0)
				top--;
			try {
				IDocument document= fCachedTextViewer.getDocument();
				return document.getLineOffset(top);
			} catch (BadLocationException x) {
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns the first invisible document offset of the lower right corner of the source viewer's viewport,
	 * possibly including partially visible lines.
	 * 
	 * @return the first invisible document offset of the lower right corner of the viewport
	 */
	private int getExclusiveBottomIndexEndOffset() {
		
		if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {	
			int bottom= fCachedTextViewer.getBottomIndex();
			if (((fCachedTextWidget.getTopPixel() + fCachedTextWidget.getClientArea().height) % fCachedTextWidget.getLineHeight()) != 0)
				bottom++;
			try {
				IDocument document= fCachedTextViewer.getDocument();
				
				if (bottom >= document.getNumberOfLines())
					bottom= document.getNumberOfLines() - 1;
				
				return document.getLineOffset(bottom) + document.getLineLength(bottom);
			} catch (BadLocationException x) {
			}
		}
		
		return -1;
	}
	
	/**
	 * Draws the vertical ruler w/o drawing the Canvas background.
	 * 
	 * @param gc the GC to draw into
	 */
	protected void doPaint(GC gc) {
	
		if (fModel == null || fCachedTextViewer == null)
			return;
		
		int topLeft= getInclusiveTopIndexStartOffset();
		int bottomRight;
		
		if (fCachedTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;
			IRegion coverage= extension.getModelCoverage();
			bottomRight= coverage.getOffset() + coverage.getLength();
		} else if (fCachedTextViewer instanceof TextViewer) {
			// TODO remove once TextViewer implements ITextViewerExtension5
			TextViewer extension= (TextViewer) fCachedTextViewer;
			IRegion coverage= extension.getModelCoverage();
			bottomRight= coverage.getOffset() + coverage.getLength();
		} else {
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=14938
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=22487
			// add 1 as getBottomIndexEndOffset returns the inclusive offset, but we want the exclusive offset (right after the last character)
			bottomRight= fCachedTextViewer.getBottomIndexEndOffset() + 1;
		}
		int viewPort= bottomRight - topLeft;
		
		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		Point dimension= fCanvas.getSize();
		int shift= fCachedTextViewer.getTopInset();

		IDocument doc= fCachedTextViewer.getDocument();		
		
		int topLine= -1, bottomLine= -1;
		try {
			IRegion region= fCachedTextViewer.getVisibleRegion();
			topLine= doc.getLineOfOffset(region.getOffset());
			bottomLine= doc.getLineOfOffset(region.getOffset() + region.getLength());
		} catch (BadLocationException x) {
			return;
		}
				
		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		int maxLayer= 1;	// loop at least once through layers.
		
		for (int layer= 0; layer < maxLayer; layer++) {
			Iterator iter= fModel.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation annotation= (Annotation) iter.next();
				
				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (fAnnotationAccessExtension != null)
					lay= fAnnotationAccessExtension.getLayer(annotation);
				maxLayer= Math.max(maxLayer, lay+1);	// dynamically update layer maximum
				if (lay != layer)	// wrong layer: skip annotation
					continue;
				
				if (skip(annotation))
					continue;
				
				Position position= fModel.getPosition(annotation);
				if (position == null)
					continue;
				
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=20284
				// Position.overlapsWith returns false if the position just starts at the end
				// of the specified range. If the position has zero length, we want to include it anyhow
				int viewPortSize= position.getLength() == 0 ? viewPort + 1 : viewPort;
				if (!position.overlapsWith(topLeft, viewPortSize))
					continue;
					
				try {
					
					int offset= position.getOffset();
					int length= position.getLength();
					
					int startLine= doc.getLineOfOffset(offset);
					if (startLine < topLine)
						startLine= topLine;
					
					int endLine= startLine;
					if (length > 0)
						endLine= doc.getLineOfOffset(offset + length - 1);
					if (endLine > bottomLine)
						endLine= bottomLine;

					startLine -= topLine;
					endLine -= topLine;

					r.x= 0;
					r.y= (startLine * lineheight) - fScrollPos + shift;
					r.width= dimension.x;
					int lines= endLine - startLine;
					if (lines < 0)
						lines= -lines;
					r.height= (lines+1) * lineheight;
					
					if (r.y < dimension.y && fAnnotationAccessExtension != null)  // annotation within visible area
						fAnnotationAccessExtension.paint(annotation, gc, fCanvas, r);
					
				} catch (BadLocationException x) {
				}
			}
		}
	}
	
	/**
	 * Draws the vertical ruler w/o drawing the Canvas background. Implementation based
	 * on <code>ITextViewerExtension5</code>. Will replace <code>doPaint(GC)</code>.
	 * 
	 * @param gc the GC to draw into
	 */
	protected void doPaint1(GC gc) {

		if (fModel == null || fCachedTextViewer == null)
			return;
		
		ITextViewerExtension5 extension= (ITextViewerExtension5) fCachedTextViewer;

		fScrollPos= fCachedTextWidget.getTopPixel();
		int lineheight= fCachedTextWidget.getLineHeight();
		Point dimension= fCanvas.getSize();
		int shift= fCachedTextViewer.getTopInset();

		int vOffset= getInclusiveTopIndexStartOffset();
		int vLength= getExclusiveBottomIndexEndOffset() - vOffset;		

		// draw Annotations
		Rectangle r= new Rectangle(0, 0, 0, 0);
		ReusableRegion range= new ReusableRegion();
		
		int minLayer= Integer.MAX_VALUE, maxLayer= Integer.MIN_VALUE;
		fCachedAnnotations.clear();
		Iterator iter= fModel.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= (Annotation) iter.next();
			
			if (skip(annotation))
				continue;
			
			Position position= fModel.getPosition(annotation);
			if (position == null)
				continue;

			if (!position.overlapsWith(vOffset, vLength))
				continue;
			
			int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
			if (fAnnotationAccessExtension != null)
				lay= fAnnotationAccessExtension.getLayer(annotation);
			
			minLayer= Math.min(minLayer, lay);
			maxLayer= Math.max(maxLayer, lay);
			fCachedAnnotations.add(annotation);
		}

		for (int layer= minLayer; layer <= maxLayer; layer++) {
			for (int i= 0, n= fCachedAnnotations.size(); i < n; i++) {
				Annotation annotation= (Annotation) fCachedAnnotations.get(i);
				
				Position position= fModel.getPosition(annotation);
				if (position == null)
					continue;
				
				int lay= IAnnotationAccessExtension.DEFAULT_LAYER;
				if (fAnnotationAccessExtension != null)
					lay= fAnnotationAccessExtension.getLayer(annotation);
				if (lay != layer)	// wrong layer: skip annotation
					continue;

				range.update(position.getOffset(), position.getLength());
				IRegion widgetRegion= extension.modelRange2WidgetRange(range);
				if (widgetRegion == null)
					continue;

				int startLine= extension.widgetLineOfWidgetOffset(widgetRegion.getOffset());
				if (startLine == -1)
					continue;

				int endLine= extension.widgetLineOfWidgetOffset(widgetRegion.getOffset() + Math.max(widgetRegion.getLength() -1, 0));
				if (endLine == -1)
					continue;

				r.x= 0;
				r.y= (startLine * lineheight) - fScrollPos + shift;
				r.width= dimension.x;
				int lines= endLine - startLine;
				if (lines < 0)
					lines= -lines;
				r.height= (lines+1) * lineheight;

				if (r.y < dimension.y && fAnnotationAccessExtension != null)  // annotation within visible area
					fAnnotationAccessExtension.paint(annotation, gc, fCanvas, r);
			}
		}
		
		fCachedAnnotations.clear();
	}

	
	/**
	 * Post a redraw request for this column into the UI thread.
	 */
	private void postRedraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			Display d= fCanvas.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						redraw();
					}
				});
			}	
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#redraw()
	 */
	public void redraw() {
		if (fCanvas != null && !fCanvas.isDisposed()) {
			GC gc= new GC(fCanvas);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#setModel
	 */
	public void setModel(IAnnotationModel model) {
		if (fAllowSetModel && model != fModel) {
			
			if (fModel != null)
				fModel.removeAnnotationModelListener(fInternalListener);
			
			fModel= model;
			
			if (fModel != null)
				fModel.addAnnotationModelListener(fInternalListener);
			
			postRedraw();
		}
	}
	
	/*
	 * @see IVerticalRulerColumn#setFont(Font)
	 */
	public void setFont(Font font) {
	}
	
	/**
	 * Returns the cached text viewer.
	 * 
	 * @return the cached text viewer
	 */
	protected ITextViewer getCachedTextViewer() {
		return fCachedTextViewer;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel() {
		return fModel;
	}
	
	/**
	 * Adds the given annotation type to this annotation ruler column. Starting
	 * with this call, annotations of the given type are shown in this annotation
	 * ruler column.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void addAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.add(annotationType);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 * @since 3.0
	 */
	public int getLineOfLastMouseButtonActivity() {
		return fParentRuler.getLineOfLastMouseButtonActivity();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 * @since 3.0
	 */
	public int toDocumentLineNumber(int y_coordinate) {
		return fParentRuler.toDocumentLineNumber(y_coordinate);
	}

	/**
	 * Removes the given annotation type from this annotation ruler column.
	 * Annotations of the given type are no longer shown in this annotation
	 * ruler column.
	 * 
	 * @param annotationType the annotation type
	 * @since 3.0
	 */
	public void removeAnnotationType(Object annotationType) {
		fConfiguredAnnotationTypes.remove(annotationType);
		fAllowedAnnotationTypes.clear();
	}
	
	/**
	 * Returns whether the given annotation should be skipped by the drawing
	 * routine.
	 * 
	 * @param annotation the annotation
	 * @return <code>true</code> if annotation of the given type should be
	 *         skipped, <code>false</code> otherwise
	 * @since 3.0
	 */
	private boolean skip(Annotation annotation) {
		Object annotationType= annotation.getType();
		Boolean allowed= (Boolean) fAllowedAnnotationTypes.get(annotationType);
		if (allowed != null)
			return !allowed.booleanValue();
		
		boolean skip= skip(annotationType);
		fAllowedAnnotationTypes.put(annotationType, !skip ? Boolean.TRUE : Boolean.FALSE);
		return skip;
	}
	
	/**
	 * Computes whether the annotation of the given type should be skipped or
	 * not.
	 * 
	 * @param annotationType the annotation type
	 * @return <code>true</code> if annotation should be skipped, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	private boolean skip(Object annotationType) {
		if (fAnnotationAccessExtension != null) {
			Iterator e= fConfiguredAnnotationTypes.iterator();
			while (e.hasNext()) {
				if (fAnnotationAccessExtension.isSubtype(annotationType, e.next()))
					return false;
			}
			return true;
		}
		return !fConfiguredAnnotationTypes.contains(annotationType);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 * @since 3.0
	 */
	public IAnnotationHover getHover() {
		return fHover;
	}
	
	/**
	 * @param hover The hover to set.
	 * @since 3.0
	 */
	public void setHover(IAnnotationHover hover) {
		fHover= hover;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 * @since 3.0
	 */
	public void addVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 * @since 3.0
	 */
	public void removeVerticalRulerListener(IVerticalRulerListener listener) {
		throw new UnsupportedOperationException();
	}
}
