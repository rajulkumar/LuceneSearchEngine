package com.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	IndexWriter writer=null;
	private ArrayList<File> queue = new ArrayList<File>();
	Analyzer analyzer=new SimpleAnalyzer(Version.LUCENE_47);
	
	static TreeMap<String,Long> map=new TreeMap<String,Long>();
	static String filesToIndex=null;
	static String index=null;
	static String termFreqFile=null;
	
	
	public static void main(String args[]) throws Exception
	{
		System.out.println("Enter the location of files to index:");
		filesToIndex=new BufferedReader(new InputStreamReader(System.in)).readLine();
		System.out.println("Enter the location of index to be created:");
		index=new BufferedReader(new InputStreamReader(System.in)).readLine();
		System.out.println("Enter the location of term frequency file to be created:");
		termFreqFile=new BufferedReader(new InputStreamReader(System.in)).readLine();
		
		System.out.println("Creating Index");
		Indexer indexer=new Indexer();
		indexer.createIndex();
		indexer.parseIndex();
		System.out.println("Term set size::"+map.size());
		writeToFile();
		System.out.println("Index creation completed");
		
		
	}
	
	public static void writeToFile() throws Exception
	{
		StringBuffer buff=new StringBuffer();
		int counter=1;
		List<Map.Entry<String,Long>> termRanks=new ArrayList<Map.Entry<String,Long>>(map.entrySet());

		Collections.sort(termRanks, new Comparator<Entry<String, Long>>() {
			public int compare(Map.Entry<String, Long> o1,
                                           Map.Entry<String, Long> o2) {
				if (o1.getValue()>=o2.getValue())
					return -1;
				else
					return 1;
						
			}
		});
		for(Entry<String,Long> termCount:termRanks)
		{
			buff.append(counter+". "+termCount.getKey()+": "+termCount.getValue());
			buff.append(System.getProperty("line.separator"));
			counter++;
		}
		
		FileWriter fw=null; 
		try
		{
			fw=new FileWriter(new File(termFreqFile+"/TermFreq.out"));
			fw.write(buff.toString());
		}
		finally
		{
			if(null!=fw)
				fw.close();
		}
	}
	
	public void createIndex() throws Exception
	{
		
		
		IndexWriterConfig indexConf=new IndexWriterConfig(Version.LUCENE_47, analyzer);
		FSDirectory dir = FSDirectory.open(new File(index));
		writer=new IndexWriter(dir, indexConf);
		
		indexFileOrDirectory(filesToIndex);
		
		closeIndex();
		
	}
	
	public void indexFileOrDirectory(String fileName) throws IOException {
		// ===================================================
		// gets the list of files in a folder (if user has submitted
		// the name of a folder) or gets a single file name (is user
		// has submitted only the file name)
		// ===================================================
		addFiles(new File(fileName));
		
		StringReader contents=null;
		int originalNumDocs = writer.numDocs();
		for (File f : queue) {
		
		    FileReader fr = null;
		    try {
			Document doc = new Document();

			// ===================================================
			// add contents of file
			// ===================================================
			fr = new FileReader(f);
							
			//contents=editFileForHtmlTags(fr);
			doc.add(new TextField("contents", editFileForHtmlTags(fr),Field.Store.YES));
			doc.add(new StringField("path", f.getPath(), Field.Store.YES));
			doc.add(new StringField("filename", f.getName(),
				Field.Store.YES));
			//doc.add(new StringField("snippet", readContents(contents,200), Field.Store.YES));

			writer.addDocument(doc);
			System.out.println("Added: " + f);
		    } catch (Exception e) {
			System.out.println("Could not add: " + f);
		    } finally {
			fr.close();
		    }
		}

		int newNumDocs = writer.numDocs();
		
		System.out.println("************************");
		System.out
			.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");

		queue.clear();
	    }
	
		private String readContents(StringReader contents,int snippetLength) throws Exception
		{
			StringBuffer buff=new StringBuffer();
			for(int i=0;i<snippetLength;i++)
			{
				buff.append(contents.read());
			}
			return buff.toString();
		}

	    private void addFiles(File file) {

		if (!file.exists()) {
		    System.out.println(file + " does not exist.");
		}
		if (file.isDirectory()) {
		    for (File f : file.listFiles()) {
			addFiles(f);
		    }
		} else {
		    String filename = file.getName().toLowerCase();
		    // ===================================================
		    // Only index text files
		    // ===================================================
		    if (filename.endsWith(".htm") || filename.endsWith(".html")
			    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
			queue.add(file);
		    } else {
			System.out.println("Skipped " + filename);
		    }
		}
	    }
	    
	    //public StringReader editFileForHtmlTags(FileReader fr) throws Exception
	    public String editFileForHtmlTags(FileReader fr) throws Exception
	    {
	    	BufferedReader br=new BufferedReader(fr);
	    	String line=null;
	    	StringBuffer buff=new StringBuffer();
	    	while(null!=(line=br.readLine()))
	    	{
	    		
	    		buff.append(line.replaceAll("<[a-z A-Z]*>|<//*[a-z A-Z]*>", ""));
	    		buff.append(System.getProperty("line.separator"));
	    		
	    	}
	    	
	    	return buff.toString();
	    }

	    public void closeIndex() throws IOException {
		writer.close();
	    }
	    
	    public void parseIndex() throws Exception
	    {
	    	
	    	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    		Fields fields=MultiFields.getFields(reader);
	    		for(String filed:fields)
	    		{
	    			Terms terms=fields.terms(filed);
	    			TermsEnum te=terms.iterator(null);
	    			while(null!=te.next())
	    			{
	    				if(te.totalTermFreq()!=-1)
	    				map.put(te.term().utf8ToString(),te.totalTermFreq());
	    			}
	    			
	    		}
	    }
	    
	   

}
