//package org.apache.lucene.opennlp.vector;
//
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.io.LineIterator;
//
//import opennlp.tools.sentdetect.SentenceSample;
//import opennlp.tools.util.Span;
//
///**
// * Do LSA-based text summarization.
// * Create term vectors based on sentences in a document.
// * Locate the most central sentences, based on row (sentence) feature vectors.
// * Find tag cloud based on column (term) feature vectors.
// *
// */
//
//public class PDFText {
//  
//  static List<SentenceSample> readLines(Vectorize vrz) throws IOException {
//    Reader reader = new FileReader("/Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/sentence/PDF_sentences.txt");
//    List<SentenceSample> samples = new ArrayList<SentenceSample>();
//    
//    LineIterator liter = new LineIterator(reader);
//    while(liter.hasNext()) {
//      String line = liter.next();
//      if (! Vectorize.isSentenceOk(line))
//        continue;
//      samples.add(new SentenceSample(line, new Span(0, line.length())));
//    }
//    return samples;
//  }
//  
//  static public void main(String[] args) throws IOException {
//    modeA();
//  }
//  
//  private static void modeA() throws IOException {
//    Vectorize vrz = new Vectorize();
//    boolean BIGRAMS = false;
//    boolean SINGULAR = true;
//    
//    List<String> terms = new ArrayList<String>();
//    List<double[]> termvecs = new ArrayList<double[]>();
//    List<SentenceSample> sentences = PDFText.readLines(vrz);
//    List<int[]> sentence2termList = new ArrayList<int[]>();
//    List<Span[]> termSpans = new ArrayList<Span[]>();
//    long last = System.currentTimeMillis();
//    
//    List<double[]> countBox = new ArrayList<double[]>();
//    Map<String,Set<String>> stemmed = new HashMap<String,Set<String>>();
//    vrz.vectorizePOS(sentences, terms, sentence2termList, termvecs, termSpans, countBox, stemmed);
//    showStemmed(terms, stemmed);
//    System.out.flush();
//    double[] counts = countBox.get(0);
//    System.out.println("vectorize: " + (System.currentTimeMillis() - last));
//    System.out.flush();
//    last = System.currentTimeMillis();
//    
//    SVDSentences svds = new SVDSentences(terms, counts, termvecs, SINGULAR);
//    System.out.println("svd: " + (System.currentTimeMillis() - last));
//    System.out.flush();
//    last = System.currentTimeMillis();
//    
//    Sorter sorter = new Sorter();
//    
//    List<Double> sentenceVecs = new ArrayList<Double>();
//    List<Integer> sortedSentences = sorter.getSentenceList(svds, sentenceVecs);
//    System.out.println("sort sentences: " + (System.currentTimeMillis() - last));
//    System.out.flush();
//    
//    showSentences(sentences, sortedSentences, sentenceVecs);
//    List<Double> termVecs = new ArrayList<Double>();
//    last = System.currentTimeMillis();
//    List<Integer> sortedTerms = sorter.getTermList(svds, termVecs);
//    System.out.println("sort terms: " + (System.currentTimeMillis() - last));
//    System.out.flush();
//    last = System.currentTimeMillis();
//    showTerms(terms, stemmed, sortedTerms, counts, termVecs);
//    double[] strengths = svds.getTermStrengths();
//    int count = 0;
//    for(int index: sortedSentences) {
//      FindPartialSentence finder = new FindPartialSentence();
//      Clauser clauser = new Clauser();
//      SentenceSample sentence = sentences.get(index);
//      System.out.println(sentence.toString().trim()); 
//      
//      int[] sentence2terms = sentence2termList.get(index);
//      last = System.currentTimeMillis();
//      Pair<Integer,Integer> partial = finder.getPartial(terms, sentence2terms, strengths);
//      Span[] spans = termSpans.get(index);
//      last = System.currentTimeMillis();
//      String[] tags = clauser.findPOSTags(sentence.toString(), spans);
//      last = System.currentTimeMillis();
//      String[] chunks = clauser.findChunks(sentence.toString(), spans, tags);
//      last = System.currentTimeMillis();
//      if (spans.length != sentence2terms.length)
//        spans.hashCode();
//      last = System.currentTimeMillis();
//      last = System.currentTimeMillis();
//      clauses1(terms, termVecs, clauser, sentence2terms, partial, chunks);
//      clauses2(terms, termVecs, clauser, sentence2terms, partial, chunks);
//      System.out.println();
//      if (count++ > 20)
//        break;
//      
//    }
//  }
//  
//  private static void showStemmed(List<String> terms, Map<String,Set<String>> stemmed) {
//    int stemmedCount = 0;
//    Set<String> multi = new HashSet<String>();
//    for(Set<String> stemSet: stemmed.values()) {
//      stemmedCount += stemSet.size();
//    }
//    System.out.println("Terms: " + terms.size() + ", stems: " + stemmed.size() + ", stemmed: " + stemmedCount);
//    
//  }
//  
//  private static void clauses2(List<String> terms,
//                               List<Double> termVecs,
//                               Clauser clauser,
//                               int[] sentence2terms,
//                               Pair<Integer,Integer> partial,
//                               String[] chunks) {
//    // TODO Auto-generated method stub
//    
//  }
//  
//  private static void clauses1(List<String> terms,
//                               List<Double> termVecs,
//                               Clauser clauser,
//                               int[] sentence2terms,
//                               Pair<Integer,Integer> partial,
//                               String[] chunks) {
//    boolean[] clauses = clauser.clumpB2(chunks, 1);
//    for(int i = 0; i < sentence2terms.length; i++) {
//      System.out.print((clauses[i] ? chunks[i] + "," : ""));
//      if (i < partial.getFirst() || i > partial.getSecond()) 
//        continue;
//      
//      int indx = sentence2terms[i];
//      if (indx == -1) {
//        System.out.print("! ");
//        continue;
//      }
//      String term = terms.get(indx);
//      int termI = terms.indexOf(term);
//      String strength = Double.toString(termVecs.get(termI)).substring(0, 3);
//      System.out.print( term + "#" + strength + " ");
//    }
//    System.out.println();
//  }
//  
//  // tag everything first, pick terms from tags!
//  
//  private static void modeB() throws IOException {
//    Vectorize vrz = new Vectorize();
//    boolean BIGRAMS = false;
//    boolean SINGULAR = true;
//    
//    List<String> terms = new ArrayList<String>();
//    List<double[]> termvecs = new ArrayList<double[]>();
//    List<SentenceSample> sentences = PDFText.readLines(vrz);
//    List<int[]> sentence2termIndexes = new ArrayList<int[]>();
//    List<Span[]> sentence2termSpans = new ArrayList<Span[]>();
//    List<double[]> countBox = new ArrayList<double[]>();
//    Map<String,Set<String>> stemmed = new HashMap<String,Set<String>>();
//    vrz.vectorizePOS(sentences, terms, sentence2termIndexes, termvecs, sentence2termSpans, countBox, stemmed);
//    double[] counts = countBox.get(0);
//    SVDSentences svds = new SVDSentences(terms, counts, termvecs, SINGULAR);
//    Sorter sorter = new Sorter();
//    
//    List<Double> fvec = new ArrayList<Double>();
//    List<Integer> sortedSentences = sorter.getSentenceList(svds, fvec);
//    showSentences(sentences, sortedSentences, fvec);
//    fvec.clear();
//    List<Integer> sortedTerms = sorter.getTermList(svds, fvec);
//    showTerms(terms, stemmed, sortedTerms, counts, fvec);
//    double[] strengths = svds.getTermStrengths();
//    for(int walk = 0; walk < sentences.size(); walk++) {
//      FindPartialSentence finder = new FindPartialSentence();
//      Clauser clauser = new Clauser();
//      int index = sortedSentences.get(walk);
//      SentenceSample sentence = sentences.get(index);
//      int[] sentence2terms = sentence2termIndexes.get(index);
//      long last = System.currentTimeMillis();
//      Pair<Integer,Integer> partial = finder.getPartial(terms, sentence2terms, strengths);
//      Span[] spans = sentence2termSpans.get(index);
//      System.out.println("partial: " + (System.currentTimeMillis() - last));
//      System.out.flush();
//      last = System.currentTimeMillis();
//      String[] tags = clauser.findPOSTags(sentence.toString(), spans);
//      System.out.println("tags: " + (System.currentTimeMillis() - last));
//      System.out.flush();
//      last = System.currentTimeMillis();
//      String[] chunks = clauser.findChunks(sentence.toString(), spans, tags);
//      System.out.println("chunks: " + (System.currentTimeMillis() - last));
//      System.out.flush();
//      last = System.currentTimeMillis();
//      if (spans.length != sentence2terms.length)
//        spans.hashCode();
//      last = System.currentTimeMillis();
//      System.out.println("active: " + (System.currentTimeMillis() - last));
//      System.out.flush();
//      last = System.currentTimeMillis();
//      boolean[] clauses = clauser.clumpB2(chunks, 1);
//      System.out.println("clauses: " + (System.currentTimeMillis() - last));
//      System.out.print(sentence.toString()); 
//      System.out.print(" => ");
//      for(int i = partial.getFirst(); i <= partial.getSecond() && i < sentence2terms.length; i++) {
//        int indx = sentence2terms[i];
//        System.out.print(((indx > -1) ? terms.get(indx) : "!") + " ");
//      }
//      System.out.println();
//      if (walk > 20)
//        break;
//    }
//  }
//  
//  public static double[] getTermCounts(List<String> terms, List<double[]> termvecs) {
//    double[] counts = new double[terms.size()];
//    for(int i = 0; i < termvecs.size(); i++) {
//      double[] vec = termvecs.get(i);
//      for(int t = 0; t < vec.length; t++)
//        counts[t] += vec[t];
//    }
//    return counts;
//    
//  }
//  
//  static void showTerms(List<String> terms, Map<String,Set<String>> stemmed, List<Integer> sortedTerms, double[] counts, List<Double> fvec) {
//    for(int i = 0; i < sortedTerms.size(); i++) {
//      int index = sortedTerms.get(i);
//      String term = terms.get(index);
//      if (stemmed.keySet().contains(term)) {
//        Set<String> stemList = stemmed.get(term);
//        term = stemList.toString();
//        String shortest = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//        for(String s: stemList) {
//          if (s.length() < shortest.length()) {
//            shortest = s;
//          }
//        }
//        term = shortest + " -> " + term;
//      }
//      double d = fvec.get(i);
//      if (d >= 0.00001) {
//        System.out.println("{#" + Math.round(d) + ", " + d + "} " + term);
//      }
//      if (i > 50)
//        break;
//      
//    }
//    System.out.println("Total above standard deviation: " + sortedTerms.size());
//    
//  }
//  
//  private static void showSentences(List<SentenceSample> sentences, List<Integer> sortedIndexes, List<Double> fvec) {
//    for(int i = 0; i < sortedIndexes.size(); i++) {
//      int index = sortedIndexes.get(i);
//      String sentence = sentences.get(index).toString();
//      double d = fvec.get(i);
//      System.out.print("{" + i + "#" + index + ", " + d + "} " + sentence);
//      
//      if (i > 10)
//        break;
//    }
//    System.out.println("Total above standard deviation: " + sortedIndexes.size());
//    
//  }
//  
//}
