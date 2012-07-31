//package org.apache.lucene.opennlp.vector;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import java.util.logging.Logger;
//
//import org.apache.solr.analysis.opennlp.NLPPOSTaggerOp;
//
//import opennlp.tools.sentdetect.SentenceSample;
//import opennlp.tools.stemmer.PorterStemmer;
//import opennlp.tools.stemmer.Stemmer;
//import opennlp.tools.util.Span;
//
///**
// * Turned parsed sentence list into vectors.
// * 
// * Add minSupport logic here. Terms list becomes map string->counter
// *
// */
//
//public class Vectorize {
//  public Set<String> POS_TAGS = new HashSet<String>();
//
//  static String PUNC = "[()\\[\\],;:]";
//  private final NLPPOSTaggerOp posTaggerOp;
//  
//  
//  public Vectorize(NLPPOSTaggerOp posTaggerOp, String[] goodTags) {
//    this.posTaggerOp = posTaggerOp;
//    for(String tag: goodTags) {
//      POS_TAGS.add(tag);
//    }
//  }
//
//  /**
//   * tag sentence first
//   * pick nouns/verbs for global terms
//   */
//  
//  public void vectorizePOS(List<SentenceSample> sentences,
//                           List<String> terms,
//                           List<int[]> sentence2termIndexes,
//                           List<double[]> termvecs,
//                           List<Span[]> sentence2termSpans,
//                           List<double[]> countBox,
//                           Map<String,Set<String>> stemmed,
//                           boolean usePOS) {
//    extractTerms(sentences, terms, sentence2termIndexes, sentence2termSpans, stemmed, usePOS);
//    createTermVectors(sentences, terms, sentence2termIndexes, termvecs, countBox);
//  }
//  
//  void extractTerms(List<SentenceSample> sentences,
//                    List<String> terms,
//                    List<int[]> sentence2termIndexes,
//                    List<Span[]> sentence2termSpans,
//                    Map<String,Set<String>> stemmed, 
//                    boolean usePOS) {
//    int total = 0;
//    Stemmer stemmer = new PorterStemmer();
//    
//    for (int s = 0; s < sentences.size(); s++) {
//      /* find all terms, point sentence term indexes into term list */
//      {
//        String sentence = sentences.get(s).toString();
//        Span[] splits = grind2(sentence, false);
//        sentence2termSpans.add(splits);
//        int[] termIndex = new int[splits.length];
//        sentence2termIndexes.add(termIndex);
//        Arrays.fill(termIndex, -1);
//        
//        sentence = sentence.toLowerCase();
//        String[] tags = null;
//        if (usePOS) {
//          tags = findPOSTags(sentence, splits);
//        } 
//        for (int i = 0; i < splits.length; i++) {
//          if (usePOS && !POS_TAGS.contains(tags[i])) {
//            continue;
//          }
//          Span span = splits[i];
//          String term = sentence.substring(span.getStart(), span.getEnd());
//          if (isTermOk(term)) {
//            String stem = stemmer.stem(term).toString();
//            // classif/classifi, hypothes/hypothesi mistakes in PorterStemmer
//            // if stem is long enough, almost certainly the same word
//            int length = stem.length();
//            if (length >= 7 && stem.charAt(length - 1) == 'i') {
//              stem = stem.substring(0, length - 1);
//            }
//            if (! term.equals(stem)) {
//              if (stemmed.containsKey(stem)) {
//                Set<String> stemSet = stemmed.get(stem);
//                stemSet.add(term);
//              } else {
//                Set<String> stemSet = new HashSet<String>();
//                stemSet.add(term);
//                stemmed.put(stem, stemSet);
//              }
//            }
//            int termI = terms.indexOf(stem);
//            if(termI == -1) {
//              terms.add(stem);
//              termI = total;
//              termIndex[i] = termI;
//              total++;
//            } else {
//              termIndex[i] = termI;
//            }
//          }
//        }
//      }
//    }
//  }
//  
//  void createTermVectors(List<SentenceSample> sentences,
//                         List<String> terms,
//                         List<int[]> sentence2termIndexes,
//                         List<double[]> termvecs,
//                         List<double[]> countBox) {
//    double counts[] = new double[terms.size()];
//    for (int i = 0; i < sentences.size(); i++) {
//      int[] termIndex = sentence2termIndexes.get(i);
//      double[] termvec = new double[terms.size()];
//      termvecs.add(termvec);
//      for(int indx = 0; indx < termIndex.length; indx++) {
//        int termI = termIndex[indx];
//        if (termI != -1) {
//          termvec[termI]++;
//          counts[termI]++;
//        }
//      }
//    }
//    countBox.add(counts);
//  }
//  
//  private Span[] grind2(String sentence, boolean bigrams) {
//    List<String> words = new ArrayList<String>();
//    StringBuilder sb = new StringBuilder(200);
//    int i = 0;
//    int start = 0;
//    List<Span> spans = new ArrayList<Span>();
//    while (i < sentence.length()) {
//      char c = sentence.charAt(i);
//      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
//        sb.append(c);
//      } else {
//        addTerm(words, spans, new Span(start, i), sb);
//        start = i + 1;
//      }
//      i++;
//    }
//    addTerm(words, spans, new Span(start, i), sb);
//    Span[] spanA = new Span[spans.size()];
//    for(int j = 0; j < spans.size(); j++) {
//      spanA[j] = spans.get(j);
//    }
//    return spanA;
//  }
//  
//  
//  
//  /**
//   * Add possible word. If no word, break bigram chain.
//   * If a previous word, add bigram.
//   * @param words
//   * @param span 
//   * @param spans 
//   * @param sb
//   * @return
//   */
//  private void addTerm(List<String> words, List<Span> spans, Span span, StringBuilder sb) {
//    String word = sb.toString();
//    sb.setLength(0);
//    if (word.length() > 0) {
//      words.add(word);
//      spans.add(span);
//    } 
//  }
//  
//  // TODO - stop being USASCII-centric
//  // this is to rip out greek-letter formula sentences
//  public static boolean isSentenceOk(String sentence) {
//    if (sentence.length() < 1) {
//      return false;
//    }
//    for(int ch = 0; ch < sentence.length(); ch++) {
//      if (sentence.charAt(ch) >= 128 ) {
//        return false;
//      }
//    }
//    return true;
//  }
//  
//  public static boolean isTermOk(String term) {
//    if (term.length() < 5) {
//      // greek letter rule - tend to be consistent - also allows some super-punctuation
//      if (term.length() == 1 && term.charAt(0) >= 128 ) 
//        return true;
//      return false;
//    }
//    
//    int caps = 0;
//    for(int ch = 0; ch < term.length(); ch++) {
//      if (term.charAt(ch) >= 'A' && term.charAt(ch) <= 'Z') {
//        caps++;
//      }
//    }
//    if (caps > 4) {
//      return false;
//    }
//    
//    term = term.toLowerCase();
//    for(int i = 0; i < term.length(); i++) {
//      char ch = term.charAt(i);
//      if (!((ch >= 'a' && ch <= 'z') || ch == ' ' || ch == '-'))
//        return false; 
//    }
//    return true;
//  }
//  
//  public String[] findPOSTags(String sentence, Span[] terms) {
//    String[] words = new String[terms.length];
//    for(int i = 0; i < terms.length; i++) {
//      words[i] = sentence.substring(terms[i].getStart(), terms[i].getEnd());
//    }
//    String[] tags = posTaggerOp.getPOSTags(words);
//    return tags;
//  }
//  
//
//  
//}
