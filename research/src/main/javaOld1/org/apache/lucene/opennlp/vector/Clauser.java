//  package org.apache.lucene.opennlp.vector;
//  import java.io.IOException;
//  import java.util.ArrayList;
//  import java.util.Arrays;
//  import java.util.Collections;
//  import java.util.HashMap;
//  import java.util.HashSet;
//  import java.util.List;
//  import java.util.Map;
//  import java.util.Set;
//  
//  import opennlp.tools.sentdetect.SentenceSample;
//  import opennlp.tools.util.Span;
//  
//  /**
//   * Given a sequence of chunks (grouped parts-of-speech tags), give reasonable breakdowns of long sentences
//   * into clauses. Use OpenNLP tools plus a little heuristics. 
//   */
//  
//  public class Clauser {
//    // nouns and verbs
//    public static Set<String> POS_TAGS = new HashSet<String>();
//    // How many major chunks in a clause? 
//    private static final int CLAUSE_LENGTH = 8;
//    public static Set<String> CLAUSE_TAGS = new HashSet<String>();
//    public static Set<String> CLAUSE_PUNC = new HashSet<String>();
//    
//    private NLPSentenceDetectorOp tc;
//    
//    public Clauser(NLPSentenceDetectorOp tc) {
//      this.tc = tc;
//      if (POS_TAGS.size() == 0) {
//        POS_TAGS.add("NN");
//        POS_TAGS.add("NNS");
//        POS_TAGS.add("NNP");
//        POS_TAGS.add("VB");
//        POS_TAGS.add("VBP");
//        POS_TAGS.add("VBF");
//        POS_TAGS.add("JJ");
//        POS_TAGS.add("CD");
//        CLAUSE_TAGS.add("B-NP");
//        CLAUSE_TAGS.add("B-VP");
//        CLAUSE_PUNC.add(",");
//        CLAUSE_PUNC.add(";");
//        CLAUSE_PUNC.add(":");
//      }
//    }
//    
//    public Span[] findTerms(String sentence) {
//      Span[] terms = tc.getTerms(sentence);
//      return terms;
//    }
//    
//    public boolean[] checkKosherTerms(String sentence, Span[] terms) {
//      boolean[] kosher = new boolean[terms.length];
//      for(int i = 0; i < terms.length; i++) {
//        String term = sentence.substring(terms[i].getStart(), terms[i].getEnd());
//        kosher[i] = Vectorize.isTermOk(term);
//      }
//      return kosher;
//    }
//    
//    public String[] findPOSTags(String sentence, Span[] terms) {
//      String[] words = new String[terms.length];
//      for(int i = 0; i < terms.length; i++) {
//        words[i] = sentence.substring(terms[i].getStart(), terms[i].getEnd());
//      }
//      String[] tags = tc.getPOSTags(words);
//      return tags;
//    }
//    
//    public String[] findChunks(String sentence, Span[] terms, String[] tags) {
//      String[] words = new String[terms.length];
//      for(int i = 0; i < terms.length; i++) {
//        words[i] = sentence.substring(terms[i].getStart(), terms[i].getEnd());
//      }
//      String[] chunks = new String[terms.length];
//      chunks = tc.getChunks(words, tags, null);
//      return chunks;
//    }
//    
//    /**
//     * Find sequences of B-chunks of length N.
//     * Two B-chunks in a row count as one.
//     * @param active 
//     */
//    
//    public boolean[] clumpBs(String[] chunks, int length) {
//      boolean[] clumps = new boolean[chunks.length];
//      boolean[] longClumps = new boolean[chunks.length];
//      int last = -1;
//      String prev = "";
//      for(int i = 0; i < chunks.length; i++) {
//        if (CLAUSE_TAGS.contains(chunks[i]) && !chunks[i].equals(prev)) {
//          last = i;
//          prev = chunks[i];
//          break;
//        }
//      }
//      if (last == -1)
//        return clumps;
//      clumps[last] = true;
//      longClumps[last] = true;
//      int n = 0;
//      prev = "";
//      for(int i = last + 1; i < chunks.length; i++) {
//        if (CLAUSE_TAGS.contains(chunks[i]) && !chunks[i].equals(prev)) {
//          clumps[i] = true;
//          prev = chunks[i];
//          last = i;
//          if (!clumps[i-1]) {
//            n++;
//            if (n == length) {
//              longClumps[i] = true;
//              n = 0;
//            } 
//          }
//        }
//      }
//      
//      return longClumps;
//    }
//    
//    public boolean[] clumpB2(String[] chunks, int length) {
//      boolean[] clumps = new boolean[chunks.length];
//      boolean[] longClumps = new boolean[chunks.length];
//      int last = -1;
//      String prev = "";
//      for(int i = 0; i < chunks.length; i++) {
//        if (CLAUSE_TAGS.contains(chunks[i]) && !chunks[i].equals(prev)) {
//          last = i;
//          prev = chunks[i];
//          break;
//        }
//      }
//      if (last == -1)
//        return clumps;
//      clumps[last] = true;
//      longClumps[last] = true;
//      int n = 0;
//      prev = "";
//      for(int i = last + 1; i < chunks.length; i++) {
//        if (CLAUSE_TAGS.contains(chunks[i]) && !chunks[i].equals(prev)) {
//          clumps[i] = true;
//          prev = chunks[i];
//          last = i;
//          if (!clumps[i-1]) {
//            n++;
//            if (n == length) {
//              longClumps[i] = true;
//              n = 0;
//            } 
//          }
//        }
//      }
//      
//      return longClumps;
//    }
//    
//    
//    /**
//     * Find one or more clauses inside a sentence
//     * Fill in terms and clauses lists
//     * @return 
//     */
//    public boolean[] findClauses(Span[] terms, String[] tags, boolean[] chunks) {
//      boolean[] clauses = new boolean[chunks.length] ;
//      int clause = 0;
//      for(int i = 0; i < chunks.length; i++) {
//        if (chunks[i]) {
//          clauses[0] = true;
//          clause++;
//          break;
//        }
//      }
//      for(int i = 2; i < chunks.length; i++) {
//        if (clause == CLAUSE_LENGTH) {
//          clauses[i - 1] = true;
//          clauses[i] = true;
//          clause = 0;
//        } else {
//          clause++;
//        }
//      }
//      clauses[clauses.length - 1] = true;
//      return clauses;
//    }
//    
//    public boolean[] getActiveTerms(int[] termIndex, int length, List<String> bannedTerms) {
//      boolean[] active = new boolean[length];
//      for(int i = 0; i < termIndex.length; i++) {
//        if (bannedTerms.get(termIndex[i]).length() > 0) {
//          active[i] = true;
//        }
//      }
//      return active;
//    }
//    
//    public boolean isNounVerb(String tag) {
//      return POS_TAGS.contains(tag);
//    }
//    
//    // end of toolkit
//    
//    /**
//     * 
//     * Read tagged/chunked stream of test data.
//     * Generate and print clauses from long runs inside sentences
//     * @throws IOException 
//     */
////    public static void main(String[] args) throws IOException {
////      Clauser cr = new Clauser();
////      long start = System.currentTimeMillis();
////      
////      List<SentenceSample> sentences = PDFText.readLines(new Vectorize());
////      int i = 0; 
////      for(SentenceSample ss: sentences) {
////        if (true) {
////          String sentence = ss.toString();
////          System.out.println(sentence.trim());
////          Span[] terms = cr.findTerms(sentence);
////          if (terms.length == 0)
////            continue;
////          int nterms = terms.length;
////          //      showTerms(sentence, terms, active);
////          String[] tags = cr.findPOSTags(sentence, terms);
////          //      showTags(sentence, terms, tags, active);
////          String[] chunks = cr.findChunks(sentence, terms, tags);
////          //        showChunks(chunks);
////          boolean[] clumps = cr.clumpBs(chunks, 1);
////          showVertical(sentence, terms, tags, chunks, clumps);
////          boolean[] clauses = cr.findClauses(terms, tags, clumps);
////          showClauses(sentence, terms, clauses);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
////          System.out.println();
////        }
////        if (i++ > 100)
////          break;  
////      }
////      System.out.println("Time (ms): " + (System.currentTimeMillis() - start) + ", per sentence: " + ((System.currentTimeMillis() - start) / (double) sentences.size()));
////    }
////    
////    private static void showTerms(SentenceSample sentence, Span[] terms, boolean[] active) {
////      String line = sentence.getDocument();
////      for(int i = 0; i < terms.length; i++) {
////        String term = line.substring(terms[i].getStart(), terms[i].getEnd());
////        System.out.print((active[i] ? term : "") + ",");
////      }
////      System.out.println();
////    }
////    
////    private static void showTags(SentenceSample sentence, Span[] terms, String[] tags, boolean[] active) {
////      String line = sentence.getDocument();
////      for(int i = 0; i < terms.length; i++) {
////        Span span = terms[i];
////        String term = line.substring(span.getStart(), span.getEnd());
////        System.out.print((active[i] ? term : "") + "_" + tags[i] + ",");
////      }
////      System.out.println();
////    }
////    
////    private static void showChunks(String[] chunks) {
////      for(String chunk: chunks) {
////        System.out.println(chunk + ",");
////      }
////    }
////    
////    private static void showVertical(String line, Span[] terms, String[] tags, String[] chunks, boolean[] clumps) {
////      for(int i = 0; i < terms.length; i++) {
////        Span span = terms[i];
////        String term = line.substring(span.getStart(), span.getEnd());
////        //      String chunk = chunks[i];
////        String chunk = CLAUSE_TAGS.contains(chunks[i]) ? chunks[i] : "";
////        System.out.println(tags[i] + "\t" + chunk + "\t" + (clumps[i] ? '!' : ' '));
////      }
////    }
////    
////    private static void showClauses(String sentence, Span[] terms, boolean[] clauses) {
////      int spanStart = -1;
////      int last = -1;
////      for(int i = 0; i < terms.length; i++) {
////        if (clauses[i]) {
////          if (spanStart == -1) {
////            spanStart = terms[i].getStart();
////          } else {
////            String part = sentence.substring(spanStart, terms[i].getEnd());
////            System.out.println("=> " + part);
////            spanStart = -1;
////          }
////          last = i;
////        }
////      }
////      if(spanStart != -1) {
////        String part = sentence.substring(spanStart, terms[last].getEnd());
////        if (part.length() > 1)
////          System.out.println("** " + part);
////      }
////    }
////    
//    
//    
//    
//  }
