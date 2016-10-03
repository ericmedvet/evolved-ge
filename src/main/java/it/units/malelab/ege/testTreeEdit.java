/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege;

import it.units.malelab.ege.distance.Distance;
import it.units.malelab.ege.distance.EditDistance;
import it.units.malelab.ege.distance.TreeEditDistance;
import it.units.malelab.ege.evolver.Configuration;
import it.units.malelab.ege.evolver.genotype.BitsGenotype;
import it.units.malelab.ege.evolver.genotype.BitsGenotypeFactory;
import it.units.malelab.ege.evolver.genotype.Genotype;
import it.units.malelab.ege.evolver.initializer.RandomInitializer;
import it.units.malelab.ege.evolver.operator.ProbabilisticMutation;
import it.units.malelab.ege.evolver.operator.TwoPointsCrossover;
import it.units.malelab.ege.evolver.selector.TournamentSelector;
import it.units.malelab.ege.evolver.validator.AnyValidator;
import it.units.malelab.ege.grammar.Grammar;
import it.units.malelab.ege.mapper.BitsSGEMapper;
import it.units.malelab.ege.mapper.HierarchicalMapper;
import it.units.malelab.ege.mapper.Mapper;
import it.units.malelab.ege.mapper.MappingException;
import it.units.malelab.ege.mapper.StandardGEMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Danny
 */
public class testTreeEdit {

    public static void main(String[] arg) throws IOException {
        long actualTime, passedTime, treeTime = 0, leavesTime = 0;
        double distance;
        int n1 = 0, n2;
        Node t1, t2;
        List<String> s1, s2;
        Random random = new Random(1);
        RandomInitializer genotypeGen = new RandomInitializer<>(random, new BitsGenotypeFactory(1024));
        TreeEditDistance treeEditDist = new TreeEditDistance();
        EditDistance editDist = new EditDistance();
        Grammar grammar = BenchmarkProblems.text("cavallo a lato").getGrammar();
        //Grammar grammar = BenchmarkProblems.max().getGrammar();
        //Mapper mapper = new BitsSGEMapper<>(5, grammar);
        Mapper mapper = new HierarchicalMapper<>(grammar);
        System.out.println();
        for (Object x : genotypeGen.getGenotypes(100, new AnyValidator())) {
            n1++;
            n2 = 0;
            try {
                t1 = mapper.map((Genotype) x);
                s1 = Node.EMPTY_TREE.equals(t1) ? Collections.EMPTY_LIST : Utils.contents(t1.leaves());
                for (Object y : genotypeGen.getGenotypes(100, new AnyValidator())) {
                    n2++;
                    try {
                        System.out.print("Distance from x" + n1 + " to y" + n2 + " = ");
                        
                        t2 = mapper.map((Genotype) y);
                        actualTime = System.nanoTime();
                        distance = treeEditDist.d(t1, t2);
                        passedTime = System.nanoTime() - actualTime;
                        treeTime += passedTime;
                        //System.out.print("[Tree Edit: " + distance + ", in " + (Math.floorDiv(passedTime, 1000)) + "us]");

                        s2 = Node.EMPTY_TREE.equals(t2) ? Collections.EMPTY_LIST : Utils.contents(t2.leaves());
                        actualTime = System.nanoTime();
                        distance = editDist.d(s1, s2);
                        passedTime = System.nanoTime() - actualTime;
                        leavesTime += passedTime;
                        //System.out.println("[Leaves Edit: " + distance + ", in " + (Math.floorDiv(passedTime, 1000)) + "us]");

                    } catch (MappingException ex) {
                        //System.out.println(ex.getMessage()); 
                    }
                }
            } catch (MappingException ex) {
                //System.out.println(ex.getMessage());
            }
        }
        System.out.println("TreeEditDistance time: " + treeTime + "ns");
        System.out.println("EditDistance time: " + leavesTime + "ns");
        //System.out.println("TreeEditDistance is " + Math.floorDiv(treeTime,leavesTime) + " times slower");
    }
}
