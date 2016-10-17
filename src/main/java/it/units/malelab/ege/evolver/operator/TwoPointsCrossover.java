/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.operator;

import com.google.common.collect.Range;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class TwoPointsCrossover extends AbstractCrossover<BitsGenotype> {

    public TwoPointsCrossover(Random random) {
        super(random);
    }

    @Override
    public List<BitsGenotype> apply(List<BitsGenotype> parents) {
        BitsGenotype parent1 = parents.get(0);
        BitsGenotype parent2 = parents.get(1);
        /*  
            La precedente selezione casuale non è uniforme. Nella scelta di endIndex, infatti,
            con una probabilità pari a startIndex/parent.size si ricade nel valore startIndex+1.
            In questo modo spesso avviene uno scambio di 1 bit, e ciò portava ad una generazione
            di un genotipo figlio identico ad uno dei due genitori con una probabilità del 50%
         */
        /*
        int startIndex1 = Math.min(Math.max(1, random.nextInt(parent1.size())), parent1.size()-2);
        int startIndex2 = Math.min(Math.max(1, random.nextInt(parent2.size())), parent2.size()-2);
        int endIndex1 = Math.min(Math.max(startIndex1+1, random.nextInt(parent1.size())), parent1.size());
        int endIndex2 = Math.min(Math.max(startIndex2+1, random.nextInt(parent2.size())), parent2.size());
         */
        int pivot1 = 0, pivot2 = 0;
        int startIndex1, startIndex2, endIndex1, endIndex2;
        pivot1 = 1 + random.nextInt(parent1.size() - 1);
        do {
            pivot2 = 1 + random.nextInt(parent1.size() - 1);
        } while (pivot1 == pivot2);
        if (pivot1 > pivot2) {
            endIndex1 = pivot1;
            startIndex1 = pivot2;
        } else {
            endIndex1 = pivot2;
            startIndex1 = pivot1;
        }
        pivot1 = 0;
        pivot2 = 0;
        pivot1 = 1 + random.nextInt(parent2.size() - 1);
        do {
            pivot2 = 1 + random.nextInt(parent2.size() - 1);
        } while (pivot1 == pivot2);
        if (pivot1 > pivot2) {
            endIndex2 = pivot1;
            startIndex2 = pivot2;
        } else {
            endIndex2 = pivot2;
            startIndex2 = pivot1;
        }
        //System.out.printf("[%d,%d[ -- [%d,%d[\n",startIndex1,endIndex1,startIndex2,endIndex2);
        return children(
                parent1, Range.closedOpen(startIndex1, endIndex1),
                parent2, Range.closedOpen(startIndex2, endIndex2));
    }

    protected List<BitsGenotype> children(BitsGenotype parent1, Range<Integer> range1, BitsGenotype parent2, Range<Integer> range2) {
        int startIndex1 = range1.lowerEndpoint();
        int startIndex2 = range2.lowerEndpoint();
        int endIndex1 = range1.upperEndpoint();
        int endIndex2 = range2.upperEndpoint();
        int child1Size = parent1.size() - (endIndex1 - startIndex1) + (endIndex2 - startIndex2);
        int child2Size = parent2.size() - (endIndex2 - startIndex2) + (endIndex1 - startIndex1);
        BitsGenotype child1 = new BitsGenotype(child1Size);
        BitsGenotype child2 = new BitsGenotype(child2Size);
        if (startIndex1 > 0) {
            child1.set(0, parent1.slice(0, startIndex1));
        }
        if (startIndex2 > 0) {
            child2.set(0, parent2.slice(0, startIndex2));
        }
        child1.set(startIndex1, parent2.slice(startIndex2, endIndex2));
        child2.set(startIndex2, parent1.slice(startIndex1, endIndex1));
        if (endIndex1 < parent1.size()) {
            child1.set(startIndex1 + endIndex2 - startIndex2, parent1.slice(endIndex1, parent1.size()));
        }
        if (endIndex2 < parent2.size()) {
            child2.set(startIndex2 + endIndex1 - startIndex1, parent2.slice(endIndex2, parent2.size()));
        }
        return Arrays.asList(child1, child2);
    }

}
