/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.*;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;

/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer implements Observer {

	/** The index to be built up by this indexer. */
	public Index index;
	private JTextArea resultWindow;
	private String indexingProgress;

	/** The next docID to be generated. */
	private int lastDocID = 0;


    /* ----------------------------------------------- */


	/** Generates a new document identifier as an integer. */
	private int generateDocID() {
		return lastDocID++;
	}

	/** Generates a new document identifier based on the file name. */
	private int generateDocID( String s ) {
		return s.hashCode();
	}


    /* ----------------------------------------------- */


	/**
	 *  Initializes the index as a HashedIndex.
	 */
	public Indexer(JTextArea resultWindow) {
		this.resultWindow = resultWindow;
	}


    /* ----------------------------------------------- */


	/**
	 *  Tokenizes and indexes the file @code{f}. If @code{f} is a directory,
	 *  all its files and subdirectories are recursively processed.
	 */
	public void processFiles( File f) {
		// do not try to index fs that cannot be read
		if ( f.canRead() ) {
			if ( f.isDirectory() ) {
				String[] fs = f.list();
				// an IO error could occur
				if ( fs != null ) {
					//index = new FileIndex(fs.length, this);
					//index = new HashedIndex();
					index = new BiGramIndex();
					String progress = "\n    Indexing, please wait... ";
					if (index.hasSavedIndex()){
						progress = "\n    Reading dictionary from file... ";
					}
					for ( int i=0; i<fs.length; i++ ) {
						processFiles( new File( f, fs[i] ));
						if (i % (fs.length/100) == 0) {
							indexingProgress = progress + (i * 100) / fs.length + "%";
							resultWindow.setText(indexingProgress);
						}
					}
					index.computeScore();
				}
			} else {
				//System.err.println( "Indexing " + f.getPath() );
				// First register the document and get a docID
				int docID = generateDocID();
				index.docIDs.put( "" + docID, f.getPath() );
				try {
					//  Read the first few bytes of the file to see if it is
					// likely to be a PDF
					Reader reader = new FileReader( f );
					char[] buf = new char[4];
					reader.read( buf, 0, 4 );
					if ( buf[0] == '%' && buf[1]=='P' && buf[2]=='D' && buf[3]=='F' ) {
						// We assume this is a PDF file
						try {
							String contents = extractPDFContents( f );
							reader = new StringReader( contents );
						}
						catch ( IOException e ) {
							// Perhaps it wasn't a PDF file after all
							reader = new FileReader( f );
						}
					}
					else {
						// We hope this is ordinary text
						reader = new FileReader( f );
					}
					SimpleTokenizer tok = new SimpleTokenizer( reader );
					int offset = 0;
					while ( tok.hasMoreTokens() ) {
						String token = tok.nextToken();
						insertIntoIndex( docID, token, offset++ );
					}
					index.docLengths.put( "" + docID, offset );
					reader.close();
				}
				catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}

    
    /* ----------------------------------------------- */


	/**
	 *  Extracts the textual contents from a PDF file as one long string.
	 */
	public String extractPDFContents( File f ) throws IOException {
		FileInputStream fi = new FileInputStream( f );
		PDFParser parser = new PDFParser( fi );
		parser.parse();
		fi.close();
		COSDocument cd = parser.getDocument();
		PDFTextStripper stripper = new PDFTextStripper();
		String result = stripper.getText( new PDDocument( cd ));
		cd.close();
		return result;
	}


    /* ----------------------------------------------- */


	/**
	 *  Indexes one token.
	 */
	public void insertIntoIndex( int docID, String token, int offset ) {
		index.insert( token, docID, offset );
	}

	@Override public void update(Observable o, Object arg) {
		resultWindow.setText(indexingProgress + "\n    Writing to file... " + arg + "%");
	}
}
	
