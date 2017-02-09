/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.genotype;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

/**
 *
 * @author danny
 */
public class BitsGenotypeTest {

    public BitsGenotypeTest() {
    }

    /*@Test
  public void testSlices() {
    System.out.println("slices");

    for (int j = 2; j < 1000; j++) {
      BitsGenotype instance = new BitsGenotype(j);
      for (int i = 2; i <= j; i++) {
        List<BitsGenotype> expResult = instance.slices(i);
        List<BitsGenotype> result = instance.slices2(i);
        assertEquals("For i=" + i + " ", expResult, result);
      }
    }
  }
     */
    @Test
    public void testSlices_list() {
        System.out.println("slices list");
        Random r = new Random(1);
        for (int m = 1; m < 10; m++) {
            List<Integer> l = new ArrayList<>();
            for (int k = 0; k < m; k++) {
                l.add(r.nextInt(5) + 1);
            }
            int totalnumber = 0;
            for (Integer i : l) {
                totalnumber += i;
            }
            BitsGenotype instance = new BitsGenotype(32);
            List<BitsGenotype> result = instance.slices(l);
            //List<BitsGenotype> expResult = instance.slices2(l);
            System.out.print("input: " + l.toString() + "\nnew lengths: [");
            for (BitsGenotype i : result) {
                System.out.print(i.size()+", ");
            }
            System.out.println("\b\b]");
            System.out.print("old lengths: [");
            //for (BitsGenotype i : expResult) {
            //    System.out.print(i.size()+", ");
            //}
            System.out.println("\b\b]\n");       
            //assertEquals("For i=" + i + " ", expResult, result);
        }
    }
}
