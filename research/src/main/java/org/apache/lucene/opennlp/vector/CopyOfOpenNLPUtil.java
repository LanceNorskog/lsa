//package org.apache.lucene.opennlp.vector;
//import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Array;
//import java.net.URL;
//import java.util.Arrays;
//import java.util.List;
//
//import org.bouncycastle.util.encoders.Base64;
//
//import com.google.common.io.Resources;
//
//import opennlp.tools.chunker.ChunkerME;
//import opennlp.tools.chunker.ChunkerModel;
//import opennlp.tools.namefind.NameFinderME;
//import opennlp.tools.namefind.TokenNameFinder;
//import opennlp.tools.namefind.TokenNameFinderModel;
//import opennlp.tools.postag.POSModel;
//import opennlp.tools.postag.POSTagger;
//import opennlp.tools.postag.POSTaggerME;
//import opennlp.tools.sentdetect.SentenceDetectorME;
//import opennlp.tools.sentdetect.SentenceModel;
//import opennlp.tools.tokenize.Tokenizer;
//import opennlp.tools.tokenize.TokenizerME;
//import opennlp.tools.tokenize.TokenizerModel;
//import opennlp.tools.util.Span;
//
///**
// * Supply OpenNLP Tokenizing, POS Tagging and Chunking tools
// * @author lancenorskog
// *
// */
//
//public class CopyOfOpenNLPUtil {
//  private SentenceDetectorME sentenceSplitter = null;
//  private Tokenizer tokenizer = null;
//  private POSTagger tagger = null;
//  private ChunkerME chunker = null;
//  private TokenNameFinder nameFinder = null;
//  
//  public CopyOfOpenNLPUtil(InputStream sentenceModel, InputStream tokenizerModel, InputStream posTaggerModel,
//                     InputStream chunkerModel) {
//  }
//  
//  
//  public Span[] splitSentences(String line) {
//    if (sentenceSplitter == null) {
//      loadSentenceModel();
//    }
//    return sentenceSplitter.sentPosDetect(line);
//  }
//  
//  public Span[] getTerms(String sentence) {
//    if (tokenizer == null) {
//      loadTokenizer();
//    }
//    
//    Span[] terms = tokenizer.tokenizePos(sentence);
//    return terms;
//  }
//  
//  public synchronized String[] getTermsStrings(String sentence) {
//    if (tokenizer == null) {
//      loadTokenizer();
//    }
//    
//    String[] terms = tokenizer.tokenize(sentence);
//    return terms;
//  }
//  
//  public synchronized String[] getPOSTags(String[] words) {
//    if (tagger == null) {
//      loadTagger();
//    }
//    String[] tags = tagger.tag(words);
//    return tags;
//  }
//  
//  public synchronized String[] getChunks(String[] words, String[] tags, double[] probs) {
//    if (chunker == null) {
//      loadChunker();
//    }
//    String[] chunks = chunker.chunk(words, tags); 
//    if (probs != null)
//      chunker.probs(probs);
//    return chunks;
//  }
//  
//  public synchronized Span[] getNames(String[] words) {
//    if (nameFinder == null) {
//      loadNameFinder();
//    }
//    Span[] names = nameFinder.find(words);
//    nameFinder. clearAdaptiveData();
//    return names;
//  }
//  
//  public void loadSentenceModel() {
//    URL file = null;
//    if (sentenceSplitter == null) {
//      try {
//        file = new URL("file:///Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/en-sent.bin");
//        //    byte[] body = Resources.toByteArray(file);
//        InputStream modelStream = new ByteArrayInputStream(Resources.toByteArray(file));
//        SentenceModel model = new SentenceModel(modelStream);
//        sentenceSplitter  = new SentenceDetectorME(model);
//      } catch (Exception e) {
//        
//      } finally {
//      }
//    }
//  }
//  
//  public void loadChunker() {
//    {
//      InputStream inputCM = null;
//      
//      if (chunker == null) {
//        try {
//          inputCM = new FileInputStream("/Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/en-chunker.bin");
//          ChunkerModel modelCM = new ChunkerModel(inputCM);
//          chunker = new ChunkerME(modelCM);
//        } catch (IOException e) {
//          // Model loading failed, handle the error
//          e.printStackTrace();
//        } finally {
//          if (inputCM != null) {
//            try {
//              inputCM.close();
//            } catch (IOException e) {
//            }
//          }
//        }
//      }
//    }
//  }
//  
//  public void loadTagger() {
//    {
//      InputStream inputPOS = null;
//      
//      if (tagger == null) {
//        try {
//          inputPOS = new FileInputStream("/Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/en-pos-maxent.bin");
//          POSModel modelPOS = new POSModel(inputPOS);
//          tagger = new POSTaggerME(modelPOS);
//        }
//        catch (IOException e) {
//          // Model loading failed, handle the error
//          e.printStackTrace();
//        }
//        finally {
//          if (inputPOS != null) {
//            try {
//              inputPOS.close();
//            }
//            catch (IOException e) {
//            }
//          }
//        }
//      }
//    }
//  }
//  
//  public void loadTokenizer() {
//    {
//      InputStream inputT = null;
//      
//      if (tokenizer == null) {
//        try {
//          inputT = new FileInputStream("/Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/en-token.bin");
//          TokenizerModel model = new TokenizerModel(inputT);
//          tokenizer = new TokenizerME(model);
//        }
//        catch (IOException e) {
//          e.printStackTrace();
//        }
//        finally {
//          if (inputT != null) {
//            try {
//              inputT.close();
//            }
//            catch (IOException e) {
//            }
//          }
//        }
//      }
//    }
//  }
//  
//  public void loadNameFinder() {
//    {
//      InputStream inputT = null;
//      
//      if (nameFinder == null) {
//        try {
//          inputT = new FileInputStream("/Users/lancenorskog/Documents/open/solr/opennlp_solr/solr/contrib/opennlp/resources/en-ner-person.bin");
//          TokenNameFinderModel model = new TokenNameFinderModel(inputT);
//          nameFinder = new NameFinderME(model);
//        }
//        catch (IOException e) {
//          e.printStackTrace();
//        }
//        finally {
//          if (inputT != null) {
//            try {
//              inputT.close();
//            }
//            catch (IOException e) {
//            }
//          }
//        }
//      }
//    }
//  }
//  
//  public static void main(String[] args) {
//    String[] sentence = {"the", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog"};
//    CopyOfOpenNLPUtil tg = new CopyOfOpenNLPUtil();
//    String[] tags = tg.getPOSTags(sentence);
//    System.out.println("Words:   " + Arrays.toString(sentence));
//    System.out.println("Tagged:  " + Arrays.toString(tags));
//    double[] probs = new double[tags.length];
//    String[] chunks = tg.getChunks(sentence, tags, probs);
//    System.out.println("Chunked: " + Arrays.toString(chunks));
//    System.out.println("Probs:   " + Arrays.toString(probs));
//  }
//  
//  
//}
