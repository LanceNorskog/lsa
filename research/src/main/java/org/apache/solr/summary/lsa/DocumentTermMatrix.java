package org.apache.solr.summary.lsa;

import java.util.ArrayList;
import java.util.List;

/*
 * Build document/term matrix using LSAVectorizer term-vector toolkit.
 * 
 * External to Lucene, 
 */

public class DocumentTermMatrix {
  List<SentenceSpan> sentenceList;
  TermVectorizer lsav = new TermVectorizer();
  MultiTerms mt = new MultiTerms();
  // character ranges inside sentence for each term found
  private List<List<WordSpan>> spanLists = null;
  // index into global term array for each term found in order in sentence
  // in lockstep with spans
  private List<List<Integer>> indexLists = null;
  private int current = -1;
  
  public DocumentTermMatrix(boolean saveSentences, boolean saveSpans, boolean saveIndexes) {
    if (saveSentences) {
      sentenceList = new ArrayList<SentenceSpan>();
    }
    if (saveSpans) {
      spanLists = new ArrayList<List<WordSpan>>();
    }
    if (saveIndexes) {
      indexLists = new ArrayList<List<Integer>>();
    }
  }
  
  /**
   * Get dimension for sentence
   * Optionally save
   */
  public int postSentence(String sentence, int start, int end) {
    int dim = lsav.startColumn();
    current = dim;
    if (sentenceList != null) {
      sentenceList.add(new SentenceSpan(sentence, new WordSpan(start, end)));
      if (sentenceList.size() - 1 != dim) {
        throw new IllegalStateException();
      }
    }
    if (spanLists != null) {
      if (spanLists.size() == dim) {
        spanLists.add(new ArrayList<WordSpan>());
      }
    }
    if (indexLists != null) {
      if (indexLists.size() == dim) {
        indexLists.add(new ArrayList<Integer>());
      }
    }
    return dim;
  }
  
  /**
   * Add base term and optional extra terms 
   * If a stem, base term is stem and extra is real word
   * The base can become an alternate.
   * 
   * position - start of base word
   * span - end of base word + 1
   */
  public int addTerm(int dim, String base, String[] terms, WordSpan span) {
    // register alternates, resolve base term
    for(int i = 0; i < terms.length; i++) {
      base = mt.addTerm(base, terms[i]);
    }
    int index = lsav.getOrAddIndex(base);
    lsav.vectorizeTerm(base, dim);
    if (spanLists != null) {
      spanLists.get(current).add(span);
    }
    if (indexLists != null) {
      indexLists.get(current).add(index);
    }
    return index;
  }
  
  public SentenceSpan[] getSentences() {
    SentenceSpan[] sentences = new SentenceSpan[sentenceList.size()];
    for(int i = 0; i < sentences.length; i++) {
      sentences[i] = sentenceList.get(i);
    }
    return sentences;
  }
  
  public WordSpan[] getSpans(int index) {
    List<WordSpan> spanList = spanLists.get(index);
    WordSpan[] spans = new WordSpan[spanList.size()];
    for(int i = 0; i < spans.length; i++) {
      spans[i] = spanList.get(i);
    }
    return spans;
  }
  
  /**
   * entry order of term or -1 if never added
   */
  public int getTermIndex(String term) {
    return lsav.getTermIndex(term);
  }
  
  public String[] getTerms() {
    return lsav.getTerms();
  }
  
  public double[] getGlobalFreqs() {
    return lsav.getGlobalFreqs();
  }
  
  public double[] getTermFreqs(String term) {
    return lsav.getTermFreqs(term);
  }
  
  public String getBase(String term) {
    return mt.getBase(term);
  }
  
  public int getTermCount() {
    return lsav.getTermCount();
  }
  
  public int getSentenceCount() {
    return sentenceList.size();
  }
  
  public SentenceSpan getSentence(int index) {
    return sentenceList.get(index);
  }

  public int[] getTermIndexes(int index) {
    List<Integer> indexList = indexLists.get(index);
    int[] indexes = new int[indexList.size()];
    for(int i = 0; i < indexes.length; i++) {
      indexes[i] = indexList.get(i);
    }
    return indexes ;
  }

}
