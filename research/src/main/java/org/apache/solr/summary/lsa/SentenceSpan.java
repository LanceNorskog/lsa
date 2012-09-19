/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreemnets.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.summary.lsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@link SentenceSpan} contains a document with
 * begin indexes of the individual sentences.
 * 
 * Stolen from OpenNLP
 */
public class SentenceSpan {
  
  private final String document;
  
  private final WordSpan section;
  private final List<WordSpan> words;
  
  /**
   * Initializes the current instance.
   *
   * @param sentence
   * @param sentences
   */
  public SentenceSpan(String sentence, WordSpan section, WordSpan... words) {
    this.document = sentence;
    this.section = section;
    this.words = Collections.unmodifiableList(new ArrayList<WordSpan>(Arrays.asList(words)));;
  }
  
  /**
   * Retrieves the document.
   *
   * @return the document
   */
  public String getDocument() {
    return document;
  }
  
  /**
   * Retrieves the document.
   *
   * @return the document
   */
  public String getSentence() {
    return document.substring(section.getStart(), section.getEnd());
  }
  
  /**
   * Retrieves the sentences.
   *
   * @return the begin indexes of the sentences
   * in the document.
   */
  public WordSpan[] getWords() {
    return words.toArray(new WordSpan[words.size()]);
  }
  
  public WordSpan getSection() {
    return section;
  }
  
  @Override
  public String toString() {
    
    StringBuilder documentBuilder = new StringBuilder();
    
    documentBuilder.append(document);
    
    for (WordSpan sentSpan : words) {
      documentBuilder.append(",");
      documentBuilder.append(sentSpan.getCoveredText(document));
    }
    
    return documentBuilder.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof SentenceSpan) {
      SentenceSpan a = (SentenceSpan) obj;
      
      return getSentence().equals(a.getSentence())
        && section.equals(a.section)
        && Arrays.equals(getWords(), a.getWords());
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return getSentence().hashCode() ^ section.hashCode() ^ getWords().hashCode();
  }
  
}
