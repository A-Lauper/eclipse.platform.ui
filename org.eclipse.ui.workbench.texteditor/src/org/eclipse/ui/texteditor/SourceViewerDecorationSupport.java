/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.MatchingCharacterPainter;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 2.1
 */
public class SourceViewerDecorationSupport {
	
	
	static class AnnotationTypePreferenceInfo {
		public Object fAnnotationType;
		public String fColorKey;
		public String fOverviewRulerKey;
		public String fEditorKey;
		public int fLayer;
	};

	
	/** The viewer */
	private ISourceViewer fSourceViewer;
	/** The viewer's overview ruler */
	private IOverviewRuler fOverviewRuler;
	/** The annotation access */
	private IAnnotationAccess fAnnotationAccess;
	/** The shared color manager */
	private ISharedTextColors fSharedTextColors;
	
	/** The editor's line painter */
	private CursorLinePainter fCursorLinePainter;
	/** The editor's margin ruler painter */
	private MarginPainter fMarginPainter;
	/** The editor's annotation painter */
	private AnnotationPainter fAnnotationPainter;
	/** The editor's peer character painter */
	private MatchingCharacterPainter fMatchingCharacterPainter;
	/** The character painter's pair matcher */
	private ICharacterPairMatcher fCharacterPairMatcher;
	
	/** Table of annotation type preference infos */
	private Map fAnnotationTypeKeyMap= new HashMap();
	/** Preference key for the cursor line highlighting */
	private String fCursorLinePainterEnableKey;
	/** Preference key for the cursor line background color */
	private String fCursorLinePainterColorKey;
	/** Preference key for the margin painter */	
	private String fMarginPainterEnableKey;
	/** Preference key for the margin painter color */
	private String fMarginPainterColorKey;
	/** Preference key for the margin painter column */
	private String fMarginPainterColumnKey;
	/** Preference key for the matching character painter */
	private String fMatchingCharacterPainterEnableKey;
	/** Preference key for the matching character painter color */
	private String fMatchingCharacterPainterColorKey;
	/** The property change listener */
	private IPropertyChangeListener fPropertyChangeListener;
	/** The preference store */
	private IPreferenceStore fPreferenceStore;


	/**
	 * Creates a new decoration support for the given viewer.
	 * 
	 * @param sourceViewer the source viewer
	 * @param overviewRuler the viewer's overview ruler
	 * @param annotationAccess the annotation access
	 * @param sharedTextColors the shared text color manager
	 */
	public SourceViewerDecorationSupport(ISourceViewer sourceViewer, IOverviewRuler overviewRuler, IAnnotationAccess annotationAccess, ISharedTextColors sharedTextColors) {
		fSourceViewer= sourceViewer;
		fOverviewRuler= overviewRuler;
		fAnnotationAccess= annotationAccess;
		fSharedTextColors= sharedTextColors;
	}
	
	/**
	 * Installs this decoration support on th given preference store. It assumes
	 * that this support has completely been configured.
	 * 
	 * @param store the preference store
	 */
	public void install(IPreferenceStore store) {
		
		fPreferenceStore= store;
		if (fPreferenceStore != null) {
			fPropertyChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePreferenceStoreChanged(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPropertyChangeListener);
		}
		
		updateTextDecorations();
		updateOverviewDecorations();
	}

	private void updateTextDecorations() {
		
		StyledText widget= fSourceViewer.getTextWidget();
		if (widget == null || widget.isDisposed())
			return;
		
		if (areMatchingCharactersShown())
			showMatchingCharacters();
		else
			hideMatchingCharacters();
			
		if (isCursorLineShown())
			showCursorLine();
		else
			hideCursorLine();
		
		if (isMarginShown())
			showMargin();
		else
			hideMargin();
		
		Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
		while (e.hasNext()) {
			Object type= e.next();
			if (areAnnotationsShown(type))
				showAnnotations(type);
			else
				hideAnnotations(type);
		}
	}
	
	public void updateOverviewDecorations() {
		Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
		while (e.hasNext()) {
			Object type= e.next();
			if (isAnnotationOverviewShown(type))
				showAnnotationOverview(type);
			else
				hideAnnotationOverview(type);
		}
	}
	
	/**
	 * Uninstalls this support from the preference store it has previously been
	 * installed on. If there is no such preference store, this call is without
	 * effect.
	 */
	public void uninstall() {
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyChangeListener);
			fPropertyChangeListener= null;
			fPreferenceStore= null;
		}
	}
	
	/**
	 * Disposes this decoration support. Internally calls
	 * <code>uninstall</code>.
	 */
	public void dispose() {
		uninstall();
		updateTextDecorations();
		updateOverviewDecorations();
	}
	
	/**
	 * Sets the character pair matcher for the matching character painter.
	 * 
	 * @param pairMatcher
	 */
	public void setCharacterPairMatcher(ICharacterPairMatcher pairMatcher) {
		fCharacterPairMatcher= pairMatcher;
	}
	
	/**
	 * Sets the preference keys for the annotation painter.
	 * 
	 * @param type the annotation type
	 * @param colorKey the preference key for the color
	 * @param editorKey the preference key for the presentation in the text area
	 * @param overviewRulerKey the preference key for the presentation in the
	 *          overview  ruler
	 */
	public void setAnnotationPainterPreferenceKeys(Object type, String colorKey, String editorKey, String overviewRulerKey, int layer) {
		AnnotationTypePreferenceInfo info= new AnnotationTypePreferenceInfo();
		info.fAnnotationType= type;
		info.fColorKey= colorKey;
		info.fEditorKey= editorKey;
		info.fOverviewRulerKey= overviewRulerKey;
		info.fLayer= layer;
		fAnnotationTypeKeyMap.put(info.fAnnotationType, info);
	}
	
	/**
	 * Sets the preference keys for the cursor line painter.
	 * @param enableKey the preference key for the cursor line painter
	 * @param colorKey the preference key for the color used by the cursor line
	 *        painter
	 */
	public void setCursorLinePainterPreferenceKeys(String enableKey, String colorKey) {
		fCursorLinePainterEnableKey= enableKey;
		fCursorLinePainterColorKey= colorKey;
	}
	
	/**
	 * Sets the preference keys for the margin painter.
	 * @param enableKey the preference key for the margin painter
	 * @param colorKey the preference key for the color used by the margin
	 *         painter
	 * @param columnKey the preference key for the margin column
	 */
	public void setMarginPainterPreferenceKeys(String enableKey, String colorKey, String columnKey) {
		fMarginPainterEnableKey= enableKey;
		fMarginPainterColorKey= colorKey;
		fMarginPainterColumnKey= columnKey;
	}
	
	/**
	 * Sets the preference keys for the matching character painter.
	 * @param enableKey the preference key for the matching character painter
	 * @param colorKey the preference key for the color used by the matching
	 *         character  painter
	 */
	public void setMatchingCharacterPainterPreferenceKeys(String enableKey, String colorKey) {
		fMatchingCharacterPainterEnableKey= enableKey;
		fMatchingCharacterPainterColorKey= colorKey;
	}
	
	private AnnotationTypePreferenceInfo getAnnotationTypePreferenceInfo(String preferenceKey) {
		Iterator e= fAnnotationTypeKeyMap.values().iterator();
		while (e.hasNext()) {
			AnnotationTypePreferenceInfo info= (AnnotationTypePreferenceInfo) e.next();
			if (info != null) {
				if (preferenceKey.equals(info.fColorKey) || preferenceKey.equals(info.fEditorKey) || preferenceKey.equals(info.fOverviewRulerKey)) 
					return info;
			}
		}
		return null;
	}
	
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		String p= event.getProperty();		
		
		if (fMatchingCharacterPainterEnableKey.equals(p) && fCharacterPairMatcher != null) {
			if (areMatchingCharactersShown())
				showMatchingCharacters();
			else
				hideMatchingCharacters();
			return;
		}
		
		if (fMatchingCharacterPainterColorKey.equals(p)) {
			if (fMatchingCharacterPainter != null) {
				fMatchingCharacterPainter.setColor(getColor(fMatchingCharacterPainterColorKey));
				fMatchingCharacterPainter.paint(IPainter.CONFIGURATION);
			}
			return;
		}
		
		if (fCursorLinePainterEnableKey.equals(p)) {
			if (isCursorLineShown())
				showCursorLine();
			else
				hideCursorLine();
			return;
		}
		
		if (fCursorLinePainterColorKey.equals(p)) {
			if (fCursorLinePainter != null) {
				hideCursorLine();
				showCursorLine();
			}					
			return;
		}
		
		if (fMarginPainterEnableKey.equals(p)) {
			if (isMarginShown())
				showMargin();
			else
				hideMargin();
			return;
		}
		
		if (fMarginPainterColorKey.equals(p)) {
			if (fMarginPainter != null) {
				fMarginPainter.setMarginRulerColor(getColor(fMarginPainterColorKey));
				fMarginPainter.paint(IPainter.CONFIGURATION);
			}
			return;
		}
		
		if (fMarginPainterColumnKey.equals(p)) {
			if (fMarginPainter != null && fPreferenceStore != null) {
				fMarginPainter.setMarginRulerColumn(fPreferenceStore.getInt(fMarginPainterColumnKey));
				fMarginPainter.paint(IPainter.CONFIGURATION);
			}
			return;
		}
		
		AnnotationTypePreferenceInfo info= getAnnotationTypePreferenceInfo(p);
		if (info != null) {
			
			if (info.fColorKey.equals(p)) {
				Color color= getColor(info.fColorKey);
				if (fAnnotationPainter != null) {
					fAnnotationPainter.setAnnotationTypeColor(info.fAnnotationType, color);
					fAnnotationPainter.paint(IPainter.CONFIGURATION);
				}
				setAnnotationOverviewColor(info.fAnnotationType, color);
				return;
			}
			
			if (info.fEditorKey.equals(p)) {
				if (areAnnotationsShown(info.fAnnotationType))
					showAnnotations(info.fAnnotationType);
				else
					hideAnnotations(info.fAnnotationType);
				return;
			}
			
			if (info.fOverviewRulerKey.equals(p)) {
				if (isAnnotationOverviewShown(info.fAnnotationType))
					showAnnotationOverview(info.fAnnotationType);
				else
					hideAnnotationOverview(info.fAnnotationType);
				return;
			}
		}			
				
	}
	
	private Color getColor(String key) {
		if (fPreferenceStore != null) {
			RGB rgb= PreferenceConverter.getColor(fPreferenceStore, key);
			return getColor(rgb);
		}
		return null;
	}
	
	private Color getColor(RGB rgb) {
		return fSharedTextColors.getColor(rgb);
	}
	
	private Color getAnnotationTypeColor(Object annotationType) {
		AnnotationTypePreferenceInfo info= (AnnotationTypePreferenceInfo) fAnnotationTypeKeyMap.get(annotationType);
		if (info != null)
			return getColor( info.fColorKey);
		return null;
	}
	
	private int getAnnotationTypeLayer(Object annotationType) {
		AnnotationTypePreferenceInfo info= (AnnotationTypePreferenceInfo) fAnnotationTypeKeyMap.get(annotationType);
		if (info != null)
			return info.fLayer;
		return 0;
	}
	
	private void showMatchingCharacters() {
		if (fMatchingCharacterPainter == null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				fMatchingCharacterPainter= new MatchingCharacterPainter(fSourceViewer, fCharacterPairMatcher);
				fMatchingCharacterPainter.setColor(getColor(fMatchingCharacterPainterColorKey));
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.addPainter(fMatchingCharacterPainter);
			}
		}
	}
	
	private void hideMatchingCharacters() {
		if (fMatchingCharacterPainter != null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.removePainter(fMatchingCharacterPainter);
				fMatchingCharacterPainter.deactivate(true);
				fMatchingCharacterPainter.dispose();
				fMatchingCharacterPainter= null;
			}
		}
	}
	
	private boolean areMatchingCharactersShown() {
		if (fPreferenceStore != null)
			return fPreferenceStore.getBoolean(fMatchingCharacterPainterEnableKey);
		return false;
	}
	
	private void showCursorLine() {
		if (fCursorLinePainter == null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				fCursorLinePainter= new CursorLinePainter(fSourceViewer);
				fCursorLinePainter.setHighlightColor(getColor(fCursorLinePainterColorKey));
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.addPainter(fCursorLinePainter);
			}
		}
	}
	
	private void hideCursorLine() {
		if (fCursorLinePainter != null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.removePainter(fCursorLinePainter);
				fCursorLinePainter.deactivate(true);
				fCursorLinePainter.dispose();
				fCursorLinePainter= null;
			}
		}
	}
	
	private boolean isCursorLineShown() {
		if (fPreferenceStore != null)
			return fPreferenceStore.getBoolean(fCursorLinePainterEnableKey);
		return false;
	}
	
	private void showMargin() {
		if (fMarginPainter == null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				fMarginPainter= new MarginPainter(fSourceViewer);
				fMarginPainter.setMarginRulerColor(getColor(fMarginPainterColorKey));
				if (fPreferenceStore != null)
					fMarginPainter.setMarginRulerColumn(fPreferenceStore.getInt(fMarginPainterColumnKey));
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.addPainter(fMarginPainter);
			}
		}
	}
	
	private void hideMargin() {
		if (fMarginPainter != null) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.removePainter(fMarginPainter);
				fMarginPainter.deactivate(true);
				fMarginPainter.dispose();
				fMarginPainter= null;
			}
		}
	}
	
	private boolean isMarginShown() {
		if (fPreferenceStore != null)
			return fPreferenceStore.getBoolean(fMarginPainterEnableKey);
		return false;
	}
	
	private void showAnnotations(Object annotationType) {
		if (fSourceViewer instanceof ITextViewerExtension2) {
			if (fAnnotationPainter == null) {
				fAnnotationPainter= new AnnotationPainter(fSourceViewer, fAnnotationAccess);
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.addPainter(fAnnotationPainter);
			}
			fAnnotationPainter.setAnnotationTypeColor(annotationType, getAnnotationTypeColor(annotationType));
			fAnnotationPainter.addAnnotationType(annotationType);
			fAnnotationPainter.paint(IPainter.CONFIGURATION);
		}
	}
	
	private void shutdownAnnotationPainter() {
		if (!fAnnotationPainter.isPaintingAnnotations()) {
			if (fSourceViewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 extension= (ITextViewerExtension2) fSourceViewer;
				extension.removePainter(fAnnotationPainter);
			}
			fAnnotationPainter.deactivate(true);
			fAnnotationPainter.dispose();
			fAnnotationPainter= null;
		} else {
			fAnnotationPainter.paint(IPainter.CONFIGURATION);
		}
	}

	private void hideAnnotations(Object annotationType) {
		if (fAnnotationPainter != null) {
			fAnnotationPainter.removeAnnotationType(annotationType);
			shutdownAnnotationPainter();
		}
	}
	
	private boolean areAnnotationsShown(Object annotationType) {
		if (fPreferenceStore != null) {
			AnnotationTypePreferenceInfo info= (AnnotationTypePreferenceInfo) fAnnotationTypeKeyMap.get(annotationType);
			if (info != null)
				return fPreferenceStore.getBoolean(info.fEditorKey);
		}
		return false;
	}
	
	private boolean isAnnotationOverviewShown(Object annotationType) {
		if (fPreferenceStore != null) {
			if (fOverviewRuler != null) {
				AnnotationTypePreferenceInfo info= (AnnotationTypePreferenceInfo) fAnnotationTypeKeyMap.get(annotationType);
				if (info != null)
					return fPreferenceStore.getBoolean(info.fOverviewRulerKey);
			}
		}
		return false;
	}
	
	private void showAnnotationOverview(Object annotationType) {
		if (fOverviewRuler != null) {
			fOverviewRuler.setAnnotationTypeColor(annotationType, getAnnotationTypeColor(annotationType));
			fOverviewRuler.setAnnotationTypeLayer(annotationType, getAnnotationTypeLayer(annotationType));
			fOverviewRuler.addAnnotationType(annotationType);
			fOverviewRuler.update();
		}
	}
	
	private void hideAnnotationOverview(Object annotationType) {
		if (fOverviewRuler != null) {
			fOverviewRuler.removeAnnotationType(annotationType);
			fOverviewRuler.update();
		}
	}
	
	public void hideAnnotationOverview() {
		if (fOverviewRuler != null) {
			Iterator e= fAnnotationTypeKeyMap.keySet().iterator();
			while (e.hasNext())
				fOverviewRuler.removeAnnotationType(e.next());
			fOverviewRuler.update();
		}
	}
	
	private void setAnnotationOverviewColor(Object annotationType, Color color) {
		if (fOverviewRuler != null) {
			fOverviewRuler.setAnnotationTypeColor(annotationType, color);
			fOverviewRuler.update();
		}
	}	
}
