package lsa.toolkit;

import java.util.ArrayList;
import java.util.List;

/*
 * Build document/term matrix using TermVectorizer term-vector toolkit.
 * 
 * External to Lucene, 
 */

public class DocumentTermMatrix {
  List<SentenceSpan> sentenceList;
  TermVectorizer tv = new TermVectorizer();
  TermSet mt = new TermSet();
  // character ranges inside sentence for each term found
  private List<List<WordSpan>> spanLists = null;
  // index into global term array for each term found in order in sentence
  // in lockstep with spans
  private List<List<String>> indexLists = null;
  private int current = -1;
  
  public DocumentTermMatrix(boolean saveSentences, boolean saveSpans, boolean saveIndexes) {
    if (saveSentences) {
      sentenceList = new ArrayList<SentenceSpan>();
    }
    if (saveSpans) {
      spanLists = new ArrayList<List<WordSpan>>();
    }
    if (saveIndexes) {
      indexLists = new ArrayList<List<String>>();
    }
  }
  
  /**
   * Get dimension for sentence
   * Optionally save
   */
  public int postSentence(SentenceSpan span) {
    int dim = tv.startColumn();
    current = dim;
    if (sentenceList != null) {
      sentenceList.add(span);
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
        indexLists.add(new ArrayList<String>());
      }
    }
    return dim;
  }
  
  /**
   * Vectorize term and add to optional tracking structures
   * position - start of base word
   * span - end of base word + 1
   */
  public int addTerm(int dim, String term, WordSpan span) {
    int index = tv.getOrAddIndex(term);
    tv.vectorizeTerm(term, dim);
    if (spanLists != null) {
      spanLists.get(current).add(span);
    }
    if (indexLists != null) {
      indexLists.get(current).add(term);
    }
    return index;
  }
  
  public int trimTerms(int trimTerms) {
    return tv.trimTerms(trimTerms);
  }

  /**
   * Seal data after build phase.
   */
  public void finish() {
    tv.truncateVectors();
  }

  public SentenceSpan[] getSentences() {
    SentenceSpan[] sentences = new SentenceSpan[sentenceList.size()];
    for(int i = 0; i < sentences.length; i++) {
      sentences[i] = sentenceList.get(i);
    }
    return sentences;
  }
  
  public WordSpan[] getSpans(int dim) {
    List<WordSpan> spanList = spanLists.get(dim);
    WordSpan[] spans = new WordSpan[spanList.size()];
    for(int i = 0; i < spans.length; i++) {
      spans[i] = spanList.get(i);
    }
    return spans;
  }
  
  public String[] getTerms() {
    return tv.getTerms();
  }
  
  public double[] getGlobalFreqs() {
    return tv.getGlobalFreqs();
  }
  
  public double[] getTermFreqs(String term) {
    return tv.getTermFreqs(term);
  }
  
  public String getBase(String term) {
    return mt.getBase(term);
  }
  
  public int getTermCount() {
    return tv.getTermCount();
  }
  
  public int getSentenceCount() {
    return sentenceList.size();
  }
  
  public SentenceSpan getSentence(int index) {
    return sentenceList.get(index);
  }

  public int[] getTermIndexes(int dim) {
    List<String> indexList = indexLists.get(dim);
    int[] indexes = new int[indexList.size()];
    for(int i = 0; i < indexes.length; i++) {
      indexes[i] = tv.getTermIndex(indexList.get(i));
    }
    return indexes ;
  }

}
