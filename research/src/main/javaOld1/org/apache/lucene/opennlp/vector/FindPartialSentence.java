//   package org.apache.solr.opennlp.vector;
//   
//   import java.util.ArrayList;
//   import java.util.Collections;
//   import java.util.LinkedList;
//   import java.util.List;
//   
//   import opennlp.tools.util.Span;
//   
//   /**
//    * 
//    * Find "partial sentences" containing strongest words.
//    * terms = all terms
//    * termIndexes = terms in the sentence -> terms
//    * termvec = strength of term, full list
//    */
//   
//   public class FindPartialSentence {
//     
//     public Pair<Integer,Integer> getPartial(List<String> terms, int[] termIndexes, double[] termvec) {
//       List<Integer> range = new LinkedList<Integer>();
//       List<Double> rangeD = new LinkedList<Double>();
//       List<Pair<Double,Integer>> termList = sortTerms(termIndexes, termvec, range, rangeD);
//       if (termList.size() == 0) {
//         return new Pair<Integer,Integer>(0,0);
//       } else if (termList.size() == 1) {
//         return new Pair<Integer,Integer>(0,0);
//       } else if (termList.size() == 2) {
//         return new Pair<Integer,Integer>(0,1);
//       }
//       int next = termList.size() - 1;
//       int first = termList.get(next--).getSecond();
//       int second = (next >= 0) ? termList.get(next--).getSecond() : first;
//       int third = (next >= 0) ? termList.get(next--).getSecond() : first;
//       int left = Math.min(first, Math.min(second, third));
//       int right = Math.max(first, Math.max(second, third));
//       //    int left = second;
//       //    int right = third;
//       // scan downward in term value until first is in the middle
//       while((left >= first || right <= first) && next >= 0) {
//         int pos = termList.get(next--).getSecond();
//         if ((right <= first) && (pos > right)) {
//           right = pos;
//           //        System.out.print(">");
//         } 
//         if ((left >= first) && (pos < left)) {
//           left = pos;
//           //        System.out.print("<");
//         }
//       }
//       if (next == -1) {
//         // back to square one!
//         left = Math.min(first, Math.min(second, third));
//         right = Math.max(first, Math.max(second, third));
//       }
//       Pair<Integer,Integer> span = new Pair<Integer,Integer>(left, right);
//       //    System.out.format("  -> first: %d, second: %d, third: %d, left: %d, right: %d\n", first, second, third, left, right);
//       //    System.out.format("  -> first: %s, second: %s, third: %s, left: %s, right: %s\n", terms.get(termIndexes[first]), terms.get(termIndexes[second]), terms.get(termIndexes[third]), terms.get(termIndexes[left]), terms.get(termIndexes[right]));
//       return span;
//     }
//     
//     void getTop3(int[] termIndexes, double[] termvec, List<Integer> range, List<Double> rangeD) {
//       range.add(-1);
//       range.add(-1);
//       range.add(-1);
//       rangeD.add(-1d);
//       rangeD.add(-1d);
//       rangeD.add(-1d);
//       // attempt to avoid using same term in two different positions. probably not worth it.
//       boolean[] seen = new boolean[termvec.length];
//       for(int i = 0; i < termIndexes.length; i++) {
//         int termPos = termIndexes[i];
//         double d = termvec[termPos];
//         //      if (seen[termPos]) {
//         //        continue;
//         //      }
//         if (d > range.get(0)) {
//           seen[termPos] = true;
//           range.add(0, i);
//           rangeD.add(0, d);
//         } else if (d > range.get(1)) {
//           seen[termPos] = true;
//           range.add(1, i);
//           rangeD.add(1, d);
//         } else if (d > range.get(2)) {
//           seen[termPos] = true;
//           range.add(2, i);
//           rangeD.add(2, d);
//         }
//       }
//     }
//     
//     List<Pair<Double,Integer>> sortTerms(int[] termIndexes, double[] termvec, List<Integer> range, List<Double> rangeD) {
//       List<Pair<Double, Integer>> sortedTerms = new ArrayList<Pair<Double,Integer>>();
//       for(int i = 0; i < termIndexes.length; i++) {
//         int termPos = termIndexes[i];
//         if (termPos != -1) {
//           sortedTerms.add(new Pair<Double,Integer>(termvec[termPos], i));
//         }
//       }
//       Collections.sort(sortedTerms);
//       return sortedTerms;
//     }
//   }
