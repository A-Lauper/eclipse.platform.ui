/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.internal.editors.text.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AddTaskAction;
import org.eclipse.ui.texteditor.ConvertLineDelimitersAction;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.StatusTextEditor;



/**
 * The standard text editor for file resources (<code>IFile</code>).
 * <p>
 * This editor has id <code>"org.eclipse.ui.DefaultTextEditor"</code>.
 * The editor's context menu has id <code>#TextEditorContext</code>.
 * The editor's ruler context menu has id <code>#TextRulerContext</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when the default 
 * editor is needed for a workbench window.
 * </p>
 */
public class TextEditor extends StatusTextEditor {
	
	/**
	 * The encoding support for the editor.
	 * @since 2.0
	 */
	private DefaultEncodingSupport fEncodingSupport;
		
	/**
	 * Creates a new text editor.
	 */
	public TextEditor() {
		super();
		initializeEditor();
	}
	
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}
	
	/*
	 * @see IWorkbenchPart#dispose()
	 * @since 2.0
	 */
	public void dispose() {
		if (fEncodingSupport != null) {
				fEncodingSupport.dispose();
				fEncodingSupport= null;
		}

		if (fSourceViewerDecorationSupport != null) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport= null;
		}
		
		super.dispose();
	}

	/*
	 * @see AbstractTextEditor#doSaveAs
	 * @since 2.1
	 */
	public void doSaveAs() {
		if (askIfNonWorkbenchEncodingIsOk())
			super.doSaveAs();
	}
	
	/*
	 * @see AbstractTextEditor#doSave(IProgressMonitor)
	 * @since 2.1
	 */
	public void doSave(IProgressMonitor monitor){
		if (askIfNonWorkbenchEncodingIsOk())
			super.doSave(monitor);
		else
			monitor.setCanceled(true);
	}

	/**
	 * Asks the user if it is ok to store in non-workbench encoding.
	 * @return <true> if the user wants to continue
	 */
	private boolean askIfNonWorkbenchEncodingIsOk() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IStorageDocumentProvider) {
			IEditorInput input= getEditorInput();
			IStorageDocumentProvider storageProvider= (IStorageDocumentProvider)provider;
			String encoding= storageProvider.getEncoding(input);
			String defaultEncoding= storageProvider.getDefaultEncoding();
			if (encoding != null && !encoding.equals(defaultEncoding)) {
				Shell shell= getSite().getShell();
				String title= TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.title"); //$NON-NLS-1$
				String msg;
				if (input != null)
					msg= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.message1"), new String[] {input.getName(), encoding});//$NON-NLS-1$
				else
					msg= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.nonWorkbenchEncoding.message2"), new String[] {encoding});//$NON-NLS-1$
				return MessageDialog.openQuestion(shell, title, msg);
			}
		}
		return true;
	}

	/**
	 * The <code>TextEditor</code> implementation of this  <code>AbstractTextEditor</code> 
	 * method asks the user for the workspace path of a file resource and saves the document there.
	 * 
	 * @param progressMonitor the progress monitor to be used
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell= getSite().getShell();
		IEditorInput input = getEditorInput();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		
		IFile original= (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		if (original != null)
			dialog.setOriginalFile(original);
		
		dialog.create();
			
		IDocumentProvider provider= getDocumentProvider();
		if (provider == null) {
			// editor has programatically been  closed while the dialog was open
			return;
		}
		
		if (provider.isDeleted(input) && original != null) {
			String message= MessageFormat.format(TextEditorMessages.getString("Editor.warning.save.delete"), new Object[] { original.getName() }); //$NON-NLS-1$
			dialog.setErrorMessage(null);
			dialog.setMessage(message, IMessageProvider.WARNING);
		}
		
		if (dialog.open() == Dialog.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IPath filePath= dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(filePath);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			
			provider.aboutToChange(newInput);
			new ProgressMonitorDialog(shell).run(false, true, op);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			
			Throwable targetException= x.getTargetException();
			
			String title= TextEditorMessages.getString("Editor.error.save.title"); //$NON-NLS-1$
			String msg= MessageFormat.format(TextEditorMessages.getString("Editor.error.save.message"), new Object[] { targetException.getMessage() }); //$NON-NLS-1$
			
			if (targetException instanceof CoreException) {
				CoreException coreException= (CoreException) targetException;
				IStatus status= coreException.getStatus();
				if (status != null) {
					switch (status.getSeverity()) {
						case IStatus.INFO:
							MessageDialog.openInformation(shell, title, msg);
							break;
						case IStatus.WARNING:
							MessageDialog.openWarning(shell, title, msg);
							break;
						default:
							MessageDialog.openError(shell, title, msg);
					}
				} else {
				  	 MessageDialog.openError(shell, title, msg);
				}
			}
						
		} finally {
			provider.changed(newInput);
			if (success)
				setInput(newInput);
		}
		
		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}
	
	/*
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/*
	 * @see AbstractTextEditor#createActions()
	 * @since 2.0
	 */
	protected void createActions() {
		super.createActions();
		
		ResourceAction action= new AddTaskAction(TextEditorMessages.getResourceBundle(), "Editor.AddTask.", this); //$NON-NLS-1$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.ADD_TASK_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.ADD_TASK);
		setAction(ITextEditorActionConstants.ADD_TASK, action);

		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToWindows.", this, "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, action);

		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX.", this, "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, action);
		
		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac.", this, "\r"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, action);
		
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=17709
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, true);
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, true);
		markAsStateDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, true);
		
		fEncodingSupport= new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);
	}
	
	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 * @since 2.0
	 */
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusBanner(IStatus)
	 * @since 2.0
	 */
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusMessage(IStatus)
	 * @since 2.0
	 */
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}
	
	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 * @since 2.0
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}
	
	/*
	 * @see IAdaptable#getAdapter(Class)
	 * @since 2.0
	 */
	public Object getAdapter(Class adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return fEncodingSupport;
		return super.getAdapter(adapter);
	}
	
	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 * @since 2.0
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
	 * @since 2.0
	 */
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/** Preference key for showing the line number ruler */
	private final static String LINE_NUMBER_RULER= TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	/** Preference key for the foreground color of the line numbers */
	private final static String LINE_NUMBER_COLOR= TextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	/** Preference key for showing the overview ruler */
	private final static String OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
	/** Preference key for error indication in overview ruler */
	private final static String ERROR_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for warning indication in overview ruler */
	private final static String WARNING_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for info indication in overview ruler */
	private final static String INFO_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for task indication in overview ruler */
	private final static String TASK_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for bookmark indication in overview ruler */
	private final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for search result indication in overview ruler */
	private final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for unknown annotation indication in overview ruler */
	private final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for error indication */
	private final static String ERROR_INDICATION= TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION;
	/** Preference key for error color */
	private final static String ERROR_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;
	/** Preference key for warning indication */
	private final static String WARNING_INDICATION= TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION;
	/** Preference key for warning color */
	private final static String WARNING_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;
	/** Preference key for info indication */
	private final static String INFO_INDICATION= TextEditorPreferenceConstants.EDITOR_INFO_INDICATION;
	/** Preference key for info color */
	private final static String INFO_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_INFO_INDICATION_COLOR;
	/** Preference key for task indication */
	private final static String TASK_INDICATION= TextEditorPreferenceConstants.EDITOR_TASK_INDICATION;
	/** Preference key for task color */
	private final static String TASK_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_TASK_INDICATION_COLOR;
	/** Preference key for bookmark indication */
	private final static String BOOKMARK_INDICATION= TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION;
	/** Preference key for bookmark color */
	private final static String BOOKMARK_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;
	/** Preference key for search result indication */
	private final static String SEARCH_RESULT_INDICATION= TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;
	/** Preference key for search result color */
	private final static String SEARCH_RESULT_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;
	/** Preference key for unknown annotation indication */
	private final static String UNKNOWN_INDICATION= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION;
	/** Preference key for unknown annotation color */
	private final static String UNKNOWN_INDICATION_COLOR= TextEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;

	/** Preference key for highlighting current line */
	private final static String CURRENT_LINE= TextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/** Preference key for highlight color of current line */
	private final static String CURRENT_LINE_COLOR= TextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/** Preference key for showing print marging ruler */
	private final static String PRINT_MARGIN= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
	/** Preference key for print margin ruler color */
	private final static String PRINT_MARGIN_COLOR= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/** Preference key for print margin ruler column */
	private final static String PRINT_MARGIN_COLUMN= TextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;

	private IOverviewRuler fOverviewRuler;
	private IAnnotationAccess fAnnotationAccess= new AnnotationAccess();
	private SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	private LineNumberRulerColumn fLineNumberRulerColumn;
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
		fOverviewRuler= new OverviewRuler(fAnnotationAccess, VERTICAL_RULER_WIDTH, sharedColors);
		ISourceViewer sourceViewer= new SourceViewer(parent, ruler, fOverviewRuler, isOverviewRulerVisible(), styles);
		fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(sourceViewer, fOverviewRuler, fAnnotationAccess, sharedColors);
		configureSourceViewerDecorationSupport();
		return sourceViewer;
	}

	private void configureSourceViewerDecorationSupport() {
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.UNKNOWN, UNKNOWN_INDICATION_COLOR, UNKNOWN_INDICATION, UNKNOWN_INDICATION_IN_OVERVIEW_RULER, 0);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.BOOKMARK, BOOKMARK_INDICATION_COLOR, BOOKMARK_INDICATION, BOOKMARK_INDICATION_IN_OVERVIEW_RULER, 1);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.TASK, TASK_INDICATION_COLOR, TASK_INDICATION, TASK_INDICATION_IN_OVERVIEW_RULER, 2);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.SEARCH, SEARCH_RESULT_INDICATION_COLOR, SEARCH_RESULT_INDICATION, SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER, 3);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.INFO, INFO_INDICATION_COLOR, INFO_INDICATION, INFO_INDICATION_IN_OVERVIEW_RULER, 4);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.WARNING, WARNING_INDICATION_COLOR, WARNING_INDICATION, WARNING_INDICATION_IN_OVERVIEW_RULER, 5);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.ERROR, ERROR_INDICATION_COLOR, ERROR_INDICATION, ERROR_INDICATION_IN_OVERVIEW_RULER, 6);
		
		fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
		fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(PRINT_MARGIN, PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
		
		String symbolicFontName= getConfigurationElement().getAttribute("symbolicFontName"); //$NON-NLS-1$
		fSourceViewerDecorationSupport.setSymbolicFontName(symbolicFontName == null ? JFaceResources.TEXT_FONT : symbolicFontName);
	}

	private void showOverviewRuler() {
		if (fOverviewRuler != null) {
			if (getSourceViewer() instanceof ISourceViewerExtension) {
				((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(true);
				fSourceViewerDecorationSupport.updateOverviewDecorations();
			}
		}
	}

	private void hideOverviewRuler() {
		if (getSourceViewer() instanceof ISourceViewerExtension) {
			fSourceViewerDecorationSupport.hideAnnotationOverview();
			((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(false);
		}
	}

	private boolean isOverviewRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(OVERVIEW_RULER);
	}

	/**
	 * Shows the line number ruler column.
	 */
	private void showLineNumberRuler() {
		IVerticalRuler v= getVerticalRuler();
		if (v instanceof CompositeRuler) {
			CompositeRuler c= (CompositeRuler) v;
			c.addDecorator(1, createLineNumberRulerColumn());
		}
	}
	
	/**
	 * Hides the line number ruler column.
	 */
	private void hideLineNumberRuler() {
		IVerticalRuler v= getVerticalRuler();
		if (v instanceof CompositeRuler) {
			CompositeRuler c= (CompositeRuler) v;
			c.removeDecorator(1);
		}
	}
	
	/**
	 * Return whether the line number ruler column should be 
	 * visible according to the preference store settings.
	 * @return <code>true</code> if the line numbers should be visible
	 */
	private boolean isLineNumberRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(LINE_NUMBER_RULER);
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 * @param rulerColumn the ruler column to be initialized
	 */
	protected void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
		
			RGB rgb=  null;
			// foreground color
			if (store.contains(LINE_NUMBER_COLOR)) {
				if (store.isDefault(LINE_NUMBER_COLOR))
					rgb= PreferenceConverter.getDefaultColor(store, LINE_NUMBER_COLOR);
				else
					rgb= PreferenceConverter.getColor(store, LINE_NUMBER_COLOR);
			}
			rulerColumn.setForeground(sharedColors.getColor(rgb));
			
			
			rgb= null;
			// background color
			if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
				if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
					if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
						rgb= PreferenceConverter.getDefaultColor(store, PREFERENCE_COLOR_BACKGROUND);
					else
						rgb= PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
				}
			}
			rulerColumn.setBackground(sharedColors.getColor(rgb));
			
			rulerColumn.redraw();
		}
	}
	
	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		fLineNumberRulerColumn= new LineNumberRulerColumn();
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
	
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
		if (isLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		return ruler;
	}
	
	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		try {			

			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;
				
			String property= event.getProperty();	
			
			if (OVERVIEW_RULER.equals(property))  {
				if (isOverviewRulerVisible())
					showOverviewRuler();
				else
					hideOverviewRuler();
				return;
			}
			
			if (LINE_NUMBER_RULER.equals(property)) {
				if (isLineNumberRulerVisible())
					showLineNumberRuler();
				else
					hideLineNumberRuler();
				return;
			}
			
			if (fLineNumberRulerColumn != null &&
						(LINE_NUMBER_COLOR.equals(property) || 
						PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)  ||
						PREFERENCE_COLOR_BACKGROUND.equals(property))) {
					
					initializeLineNumberRulerColumn(fLineNumberRulerColumn);
			}
				
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		fSourceViewerDecorationSupport.install(getPreferenceStore());
	}

}
