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
package org.eclipse.ui.internal.editors.quickdiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;

/**
 * Default provider for the quickdiff display - the saved document is taken as
 * the reference.
 * 
 * @since 3.0
 */
public class LastSaveReferenceProvider implements IQuickDiffReferenceProvider, IElementStateListener {

	/** <code>true</code> if the document has been read. */
	private boolean fDocumentRead= false;
	/**
	 * The reference document - might be <code>null</code> even if <code>fDocumentRead</code>
	 * is <code>true</code>.
	 */
	private IDocument fReference= null;
	/**
	 * Our unique id that makes us comparable to another instance of the same
	 * provider. See extension point reference.
	 */
	private String fId;
	/** The current document provider. */
	private IDocumentProvider fDocumentProvider;
	/** The current editor input. */
	private IEditorInput fEditorInput;
	/** Private lock no one else will synchronize on. */
	private final Object fLock= new Object();
	/**
	 * The progress monitor for a currently running <code>getReference</code>
	 * operation, or <code>null</code>.
	 */
	private IProgressMonitor fProgressMonitor;
	/** The text editor we run upon. */
	private ITextEditor fEditor;

	/**
	 * A job to put the reading of file contents into a background.
	 */
	private final class ReadJob extends Job {

		/**
		 * Creates a new instance.
		 */
		public ReadJob() {
			super(QuickDiffMessages.getString("LastSaveReferenceProvider.LastSaveReferenceProvider.readJob.label")); //$NON-NLS-1$
			setSystem(true);
			setPriority(SHORT);
		}

		/**
		 * Calls
		 * {@link LastSaveReferenceProvider#readDocument(IProgressMonitor, boolean)}
		 * and returns {@link Status#OK_STATUS}.
		 * 
		 * {@inheritdoc}
		 * 
		 * @param monitor {@inheritDoc}
		 * @return {@link Status#OK_STATUS}
		 */
		protected IStatus run(IProgressMonitor monitor) {
			readDocument(monitor, false);
			return Status.OK_STATUS;
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#getReference(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IDocument getReference(IProgressMonitor monitor) {
		if (!fDocumentRead)
			readDocument(monitor, true); // force reading it
		return fReference;
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#dispose()
	 */
	public void dispose() {
		IProgressMonitor monitor= fProgressMonitor;
		if (monitor != null) {
			monitor.setCanceled(true);
		}
		
		IDocumentProvider provider= fDocumentProvider;
		
		synchronized (fLock) {
			if (provider != null)
				provider.removeElementStateListener(this);
			fEditorInput= null;
			fDocumentProvider= null;
			fReference= null;
			fDocumentRead= false;
			fProgressMonitor= null;
			fEditor= null;
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider#getId()
	 */
	public String getId() {
		return fId;
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation#setActiveEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setActiveEditor(ITextEditor targetEditor) {
		IDocumentProvider provider= null;
		IEditorInput input= null;
		if (targetEditor != null) {
			provider= targetEditor.getDocumentProvider();
			input= targetEditor.getEditorInput();
		}
		
		
		// dispose if the editor input or document provider have changed
		// note that they may serve multiple editors
		if (provider != fDocumentProvider || input != fEditorInput) {
			dispose();
			synchronized (fLock) {
				fEditor= targetEditor;
				fDocumentProvider= provider;
				fEditorInput= input;
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation#isEnabled()
	 */
	public boolean isEnabled() {
		return fEditorInput != null && fDocumentProvider != null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation#setId(java.lang.String)
	 */
	public void setId(String id) {
		fId= id;
	}

	/**
	 * Reads in the saved document into <code>fReference</code>.
	 * 
	 * @param monitor a progress monitor, or <code>null</code>
	 * @param force <code>true</code> if the reference document should also
	 *        be read if the current document is <code>null</code>,<code>false</code>
	 *        if it should only be updated if it already existed.
	 */
	private void readDocument(IProgressMonitor monitor, boolean force) {

		// protect against concurrent disposal
		IDocumentProvider prov= fDocumentProvider;
		IEditorInput inp= fEditorInput;
		IDocument doc= fReference;
		ITextEditor editor= fEditor;
		
		if (prov instanceof IStorageDocumentProvider && inp instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) inp;
			IStorageDocumentProvider provider= (IStorageDocumentProvider) prov;
			
			if (doc == null)
				if (force || fDocumentRead)
					doc= new Document();
				else
					return;

			IJobManager jobMgr= Platform.getJobManager();
			IFile file= input.getFile();
			
			try {
				fProgressMonitor= monitor;

				// this protects others from not being able to delete the file,
				// and protects ourselves from concurrent access to fReference
				// (in the case there already is a valid fReference)

				// one might argue that this rule should already be in the Job
				// description we're running in, however:
				// 1) we don't mind waiting for someone else here
				// 2) we do not take long, or require other locks etc. -> short
				// delay for any other job requiring the lock on file
				jobMgr.beginRule(file, monitor);
				
				InputStream stream= getFileContents(file);
				if (stream == null)
					return;
				
				String encoding= getEncoding(input, provider);
				if (encoding == null)
					return;
				
				setDocumentContent(doc, stream, encoding, monitor);
				
			} catch (IOException e) {
				return;
			} finally {
				jobMgr.endRule(file);
				fProgressMonitor= null;
			}
			
			if (monitor != null && monitor.isCanceled())
				return;
			
			// update state
			synchronized (fLock) {
				if (fDocumentProvider == provider && fEditorInput == input) {
					// only update state if our provider / input pair has not
					// been updated in between (dispose or setActiveEditor)
					fReference= doc;
					fDocumentRead= true;
					addElementStateListener(editor, prov);
				}
			}
		}
	}

	/* utility methods */

	/**
	 * Adds this as element state listener in the UI thread as it can otherwise 
	 * conflict with other listener additions, since DocumentProvider is not
	 * thread-safe.
	 * 
	 * @param editor the editor to get the display from
	 * @param provider the document provider to register as element state listener
	 */
	private void addElementStateListener(ITextEditor editor, final IDocumentProvider provider) {
		// addElementStateListener adds at most once - no problem to call
		// repeatedly
		Runnable runnable= new Runnable() {
			public void run() {
				synchronized (fLock) {
					if (fDocumentProvider == provider)
						provider.addElementStateListener(LastSaveReferenceProvider.this);
				}
			}
		};
		
		Display display= null;
		if (editor != null) {
			IWorkbenchPartSite site= editor.getSite();
			if (site != null)
				site.getWorkbenchWindow().getShell().getDisplay();
		}
		
		if (display != null && !display.isDisposed())
			display.asyncExec(runnable);
		else
			runnable.run();
	}

	/**
	 * Gets the contents of <code>file</code> as an input stream.
	 * 
	 * @param file the <code>IFile</code> which we want the content for
	 * @return an input stream for the file's content
	 */
	private static InputStream getFileContents(IFile file) {
		InputStream stream= null;
		try {
			if (file != null)
				stream= file.getContents();
		} catch (CoreException e) {
			// ignore
		}
		return stream;
	}

	/**
	 * Returns the encoding of the file corresponding to <code>input</code>.
	 * If no encoding can be found, the default encoding as returned by 
	 * <code>provider.getDefaultEncoding()</code> is returned.
	 * 
	 * @param input the current editor input
	 * @param provider the current document provider
	 * @return the encoding for the file corresponding to <code>input</code>,
	 *         or the default encoding
	 */
	private static String getEncoding(IFileEditorInput input, IStorageDocumentProvider provider) {
		String encoding= provider.getEncoding(input);
		if (encoding == null)
			encoding= provider.getDefaultEncoding();
		return encoding;
	}

	/**
	 * Initializes the given document with the given stream using the given
	 * encoding.
	 * 
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @param monitor a progress monitor for cancellation, or <code>null</code>
	 * @exception IOException if the given stream can not be read
	 */
	private static void setDocumentContent(IDocument document, InputStream contentStream, String encoding, IProgressMonitor monitor) throws IOException {
		Reader in= null;
		try {
			final int DEFAULT_FILE_SIZE= 15 * 1024;
			
			in= new BufferedReader(new InputStreamReader(contentStream, encoding), DEFAULT_FILE_SIZE);
			StringBuffer buffer= new StringBuffer(DEFAULT_FILE_SIZE);
			char[] readBuffer= new char[2048];
			int n= in.read(readBuffer);
			while (n > 0) {
				if (monitor != null && monitor.isCanceled())
					return;
				
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			
			document.set(buffer.toString());
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
					// ignore
				}
			}
		}
	}

	/* IElementStateListener implementation */

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		if (!isDirty && element == fEditorInput) {
			// document has been saved or reverted - recreate reference
			new ReadJob().schedule();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
	 */
	public void elementContentAboutToBeReplaced(Object element) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
	 */
	public void elementContentReplaced(Object element) {
		if (element == fEditorInput) {
			// document has been reverted or replaced
			new ReadJob().schedule();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDeleted(java.lang.Object)
	 */
	public void elementDeleted(Object element) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
	}
}
