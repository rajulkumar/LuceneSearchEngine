package com.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Retrieve {
	
	static Analyzer analyzer=new SimpleAnalyzer(Version.LUCENE_47);
	static StringBuffer buffer=null;
	static String resultsFilePath=null;
	
	public static void main(String args[]) throws Exception
	{
		System.out.println("Enter index location::");
		String index=new BufferedReader(new InputStreamReader(System.in)).readLine();
		System.out.println("Enter the location of query result files to be created::");
		resultsFilePath=new BufferedReader(new InputStreamReader(System.in)).readLine();
		Retrieve.searchForDocs(index);
	}

	 public static void searchForDocs(String indexLocation) throws Exception
	    {
	    	
	    	IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
	    			indexLocation)));
	    	IndexSearcher searcher = new IndexSearcher(reader);
	    	TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    	String s = "";
		    	while (!s.equalsIgnoreCase("q")) {
		    	    
		    		System.out.println("Enter the search query (q=quit):");
		    		s = br.readLine();
		    		if (s.equalsIgnoreCase("q")) {
		    		    break;
		    		}
	
		    		Query q = new QueryParser(Version.LUCENE_47, "contents",
		    			analyzer).parse(s);
		    		collector = TopScoreDocCollector.create(100, true);
		    		searcher.search(q, collector);
		    		ScoreDoc[] hits = collector.topDocs().scoreDocs;
	
		    		
		    		System.out.println("Found " + hits.length + " hits.");
		    		buffer=new StringBuffer();
		    		for (int i = 0; i < hits.length; ++i) {
		    		    int docId = hits[i].doc;
		    		    Document d = searcher.doc(docId);
		    		    System.out.println((i + 1) + ". " + d.get("filename")
		    			    + " score=" + hits[i].score);
		    		    buffer.append((i+1)+". "+d.get("filename")+ ": " + hits[i].score);
		    		    buffer.append(System.getProperty("line.separator"));
		    		    buffer.append(modifyContentsforSnippet(d.get("contents")));
		    		    buffer.append(System.getProperty("line.separator"));
		    		    buffer.append(System.getProperty("line.separator"));
		    		}
		    		
		    		writeToFile(s.replace(" ", "_")+"_"+new SimpleDateFormat("MM-dd-yy_HH.mm.ss").format(new Date())+".out");
		    }
	    }
	 
	 private static String modifyContentsforSnippet(String content)
	 {
		 if(content.trim().length()<=200)
			 return content.trim();
		 else
			 return content.trim().substring(0,200);
	 }
	 
	 public static void writeToFile(String fileName) throws Exception
	 {
		 FileWriter fw=null;
		 try
		 {
			 fw=new FileWriter(new File(resultsFilePath+"/"+fileName));
			 System.out.println("File::"+resultsFilePath+"/"+fileName);
			 fw.write(buffer.toString());
		 }
		 finally
		 {
			 if(null!=fw)
				 fw.close();
		 }
	 }
}
