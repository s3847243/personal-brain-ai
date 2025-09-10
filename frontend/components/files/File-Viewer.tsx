"use client"

import { fetchRelatedChunks, uploadFile } from '@/lib/chatApi';
import { Trash2 } from 'lucide-react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import React, { useEffect, useState } from 'react'
import { fetchDocuments } from '@/lib/chatApi';
import { BacklinkedChunk, Document } from '@/lib/types';
import TextFilePreview from './TextFilePreview';
import { deleteDocument } from '@/lib/fileApi';
const FileViewer = () => {
  const [activeTab, setActiveTab] = useState<'files' | 'timeline'>('files');
  const [viewingTab, setViewingTab] = useState<'preview' | 'backlinks'>('preview');
  const [isViewingFiles, setIsViewingFiles] = useState(false);
  const [pendingDeleteFile, setPendingDeleteFile] = useState<null | { id: string; name: string }>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [files, setFiles] = useState<Document[]>([]); // Initialize with an empty array
  const [backlinks, setBacklinks] = useState<BacklinkedChunk[]>([]);
  const [backlinksLoading, setBacklinksLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<Document | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  useEffect(() => {
    fetchDocuments()
      .then(setFiles)
      .finally();

  }, []);
  useEffect(() => {
    if (selectedFile && viewingTab === 'backlinks') {
      setBacklinksLoading(true);
      fetchRelatedChunks(selectedFile.id)
        .then(setBacklinks)
        .catch(() => setBacklinks([]))
        .finally(() => setBacklinksLoading(false));
    }
  }, [selectedFile, viewingTab]);
  const confirmDeleteFile = async () => {
    if (!pendingDeleteFile) return;
    setIsDeleting(true);
    try {
      await deleteDocument(pendingDeleteFile.id);
      setFiles(prev => prev.filter(f => f.id !== pendingDeleteFile.id));
    } catch (e) {
      console.error("Delete file failed", e);
    } finally {
      setIsDeleting(false);
      setPendingDeleteFile(null);
    }
  };
  const handleFileUpload = async (selectedFile: File) => {
    try {
      setIsUploading(true);
      const uploadedDocument = await uploadFile(selectedFile);
      console.log('File uploaded successfully:', uploadedDocument);
      setFiles(prevFiles => [uploadedDocument, ...prevFiles]);

      
    } catch (error) {
      console.error('Upload failed:', error);
    } finally {
      setIsUploading(false);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const onFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      handleFileUpload(file);
    }
    if (event.target) {
      event.target.value = '';
    }
  };
  return (
    <div className='flex flex-col h-full bg-white shadow-lg rounded-lg overflow-hidden'>
        <input
          ref={fileInputRef}
          type="file"
          onChange={onFileChange}
          style={{ display: 'none' }}
          accept=".pdf,.doc,.docx,.txt,.md" 
        />
        
        <div className='flex items-center justify-between border-b border-gray-200 p-4 bg-gray-50'>
            <h1 className='text-lg font-semibold hover:text-blue-500 transition-colors duration-200 cursor-cell'>File Viewer</h1>
            <button 
              onClick={handleUploadClick}
              disabled={isUploading}
              className='bg-blue-500 text-white px-4 py-2 rounded-lg cursor-pointer hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors duration-200'
            >
                {isUploading ? 'Uploading...' : 'Upload'}
            </button>
        </div>
        
        {!isViewingFiles && (
          <>
            <nav>
                <ul className='flex h-12'>
                    <li className='flex-1 flex items-center justify-center hover:bg-gray-100 transition-colors duration-200 cursor-pointer' 
                        onClick={() => setActiveTab('files')}>
                        Uploaded Files
                    </li>
                    <div className='w-px bg-gray-200'></div>
                    {/* <li className='flex-1 flex items-center justify-center hover:bg-gray-100 transition-colors duration-200 cursor-pointer'
                        onClick={() => setActiveTab('timeline')}>
                       Timeline
                    </li> */}
                </ul>             
            </nav>
            <div className='border-t border-gray-200 p-4'>
              {activeTab === 'files' ? (
                <>
                <ul className='space-y-2'>
                  {files.map(file => (
                    
                    <li key={file.id} className="flex items-center gap-3 justify-between">
                      <div className="min-w-0 flex-1">
                        <a
                          className="text-blue-600 hover:underline cursor-pointer truncate"
                          onClick={() => {
                            setSelectedFile(file);
                            setIsViewingFiles(true);
                          }}
                          title={file.originalFilename}
                        >
                          {file.originalFilename}
                        </a>
                      </div>

                      <button
                        type="button"
                        aria-label="Delete file"
                        onClick={() => setPendingDeleteFile({ id: file.id, name: file.originalFilename })}
                        className="p-1.5 rounded hover:bg-red-50 transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="h-4 w-4 text-red-600" />
                      </button>
                      
                    </li>
                  ))}
                </ul>
                  {/* ✅ Single global AlertDialog at the bottom of the component */}
                    <AlertDialog
                      open={!!pendingDeleteFile}
                      onOpenChange={(open) => !open && !isDeleting && setPendingDeleteFile(null)}
                    >
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle className="text-primary-foreground">Delete this file?</AlertDialogTitle>
                          <AlertDialogDescription>
                            {pendingDeleteFile
                              ? `“${pendingDeleteFile.name}” will be permanently deleted. This action cannot be undone.`
                              : "This will permanently delete the file."}
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel className="text-primary-foreground" disabled={isDeleting}>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={confirmDeleteFile} disabled={isDeleting}>
                            {isDeleting ? "Deleting…" : "Delete"}
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                </>
              ) : (
                <div>
                  <h2 className='text-lg font-semibold'>Timeline</h2>
                  <p className='text-gray-500'>No events to display</p>
                </div>
              )}
            </div>
          </>
        )}

    {isViewingFiles && (
      <div className="flex flex-col h-full bg-gradient-to-b from-slate-50 to-white">
        {/* Header Navigation */}
        <div className="border-b border-gray-200/50 bg-white/80 backdrop-blur-sm">
          <div className="px-6 py-4">
            <button 
              onClick={() => setIsViewingFiles(false)}
              className="group flex items-center space-x-3 text-gray-600 hover:text-purple-600 transition-colors duration-200"
            >
              <div className="w-8 h-8 rounded-full bg-gray-100 group-hover:bg-purple-100 flex items-center justify-center transition-colors duration-200">
                <svg className="w-4 h-4 transform group-hover:-translate-x-0.5 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </div>
              <span className="font-medium">Back to Files</span>
            </button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="border-b border-gray-200/50 bg-white/60 backdrop-blur-sm">
          <div className="px-6">
            <div className="flex">
              <button
                onClick={() => setViewingTab('preview')}
                className={`relative px-6 py-4 font-medium text-sm transition-all duration-200 ${
                  viewingTab === 'preview'
                    ? 'text-purple-600 border-b-2 border-purple-600'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <span>Preview</span>
                </div>
              </button>
              
              <button
                onClick={() => setViewingTab('backlinks')}
                className={`relative px-6 py-4 font-medium text-sm transition-all duration-200 ${
                  viewingTab === 'backlinks'
                    ? 'text-purple-600 border-b-2 border-purple-600'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                  <span>Backlinks</span>
                </div>
              </button>
            </div>
          </div>
        </div>

        {/* Content Area */}
        <div className="flex-1 overflow-hidden">
          {viewingTab === 'preview' ? (
            <div className="h-full overflow-hidden min-h-0">
              {/* File Info Header */}
              {selectedFile && (
                <div className="px-6 py-4 bg-white/50 border-b border-gray-100">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                      <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-900">{selectedFile.originalFilename}</h3>
                      <p className="text-sm text-gray-500">File Preview</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Preview Content */}
              <div className="flex-1 p-2 overflow-hidden" 
            style={{ 
              height: selectedFile ? 'calc(100% - 88px)' : '100%',  // Subtract header height
              minHeight: 0 
            }}>
                {!selectedFile ? (
                  <div className="flex flex-col items-center justify-center h-full text-gray-500 space-y-4">
                    <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center">
                      <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <p className="text-center">No file selected</p>
                  </div>
                ) : (
                  <div className="w-full h-full" style={{ height: 'calc(100% - 16px)' }}>
                    {selectedFile.originalFilename.endsWith('.pdf') ? (
                      <iframe 
                        src={selectedFile.publicUrl} 
                        className="w-full h-full border-2 border-gray-200 rounded-xl shadow-lg bg-white" 
                        title="PDF Preview"
                      />
                    ) : selectedFile.originalFilename.endsWith('.md') || selectedFile.originalFilename.endsWith('.txt') ? (
                      <div className="h-full bg-white rounded-xl border-2 border-gray-200 shadow-lg overflow-hidden">
                        <TextFilePreview url={selectedFile.publicUrl} />
                      </div>
                    ) : selectedFile.originalFilename.endsWith('.doc') || selectedFile.originalFilename.endsWith('.docx') ? (
                      <div className="flex flex-col items-center justify-center h-full bg-white rounded-xl border-2 border-gray-200 shadow-lg space-y-6">
                        <div className="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center">
                          <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                          </svg>
                        </div>
                        <div className="text-center space-y-3">
                          <p className="text-gray-600">DOC/DOCX preview is not supported in-browser</p>
                          <a 
                            href={selectedFile.publicUrl} 
                            className="inline-flex items-center space-x-2 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition-colors duration-200"
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            <span>Download File</span>
                          </a>
                        </div>
                      </div>
                    ) : (
                      <div className="flex flex-col items-center justify-center h-full bg-white rounded-xl border-2 border-gray-200 shadow-lg space-y-4">
                        <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center">
                          <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                          </svg>
                        </div>
                        <p className="text-gray-500 text-center">Unsupported file type</p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="h-full flex flex-col">
              {/* Backlinks Header */}
              <div className="px-6 py-4 bg-white/50 border-b border-gray-100">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-500 to-pink-600 flex items-center justify-center">
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">Backlinks</h3>
                    <p className="text-sm text-gray-500">Related content connections</p>
                  </div>
                </div>
              </div>

              {/* Backlinks Content */}
              <div className="flex-1 p-6 overflow-auto">
                {backlinksLoading ? (
                  <div className="flex flex-col items-center justify-center h-full space-y-4">
                    <div className="w-8 h-8 border-2 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-gray-500">Loading backlinks...</p>
                  </div>
                ) : backlinks.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-full text-gray-500 space-y-4">
                    <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center">
                      <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                      </svg>
                    </div>
                    <p className="text-center">No backlinks found</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {backlinks.map(link => (
                      <div 
                        key={`${link.sourceChunkId}-${link.relatedChunkId}`} 
                        className="bg-white/80 backdrop-blur-sm border border-gray-200/50 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow duration-200"
                      >
                        <div className="flex items-center space-x-2 mb-3">
                          <div className="w-6 h-6 rounded bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0">
                            <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                          </div>
                          <p className="text-sm text-gray-600">
                            Related to file <code className="px-2 py-1 bg-purple-100 text-purple-700 rounded text-xs font-mono">{link.relatedDocumentTitle}</code>
                          </p>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-4">
                          <p className="text-gray-900 whitespace-pre-wrap leading-relaxed">
                            {link.relatedText || <span className="text-gray-500 italic">[No text found]</span>}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    )}
    </div>
  )
}

export default FileViewer;
