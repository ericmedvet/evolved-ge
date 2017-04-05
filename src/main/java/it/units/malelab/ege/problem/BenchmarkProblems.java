/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.problem;

import it.units.malelab.ege.core.grammar.Node;
import it.units.malelab.ege.util.Utils;
import it.units.malelab.ege.util.distance.EditDistance;
import it.units.malelab.ege.evolver.PhenotypePrinter;
import it.units.malelab.ege.core.fitness.Fitness;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.fitness.LeafContentsDistance;
import it.units.malelab.ege.core.fitness.NumericFitness;
import it.units.malelab.ege.fitness.SantaFe;
import it.units.malelab.ege.fitness.SymbolicRegression;
import it.units.malelab.ege.core.grammar.Grammar;
import it.units.malelab.ege.problem.symbolicregression.MathUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 *
 * @author eric
 */
public class BenchmarkProblems {

    public static class Problem {

        private final Grammar<String> grammar;
        private final FitnessComputer<String> fitnessComputer;
        private final FitnessComputer<String> generalizationFitnessComputer;
        private final PhenotypePrinter<String> phenotypePrinter;

        public Problem(Grammar<String> grammar, FitnessComputer<String> fitnessComputer, FitnessComputer<String> generalizationFitnessComputer, PhenotypePrinter<String> phenotypePrinter) {
            this.grammar = grammar;
            this.fitnessComputer = fitnessComputer;
            this.generalizationFitnessComputer = generalizationFitnessComputer;
            this.phenotypePrinter = phenotypePrinter;
        }

        public Grammar<String> getGrammar() {
            return grammar;
        }

        public FitnessComputer<String> getFitnessComputer() {
            return fitnessComputer;
        }

        public FitnessComputer<String> getGeneralizationFitnessComputer() {
            return generalizationFitnessComputer;
        }

        public PhenotypePrinter<String> getPhenotypePrinter() {
            return phenotypePrinter;
        }

    }

    public static Problem harmonicCurveProblem() throws IOException {
        SymbolicRegression.TargetFunction targetFunction = new SymbolicRegression.TargetFunction() {
            @Override
            public double compute(double... v) {
                double s = 0;
                for (double i = 1; i < v[0]; i++) {
                    s = s + 1 / i;
                }
                return s;
            }

            @Override
            public String[] varNames() {
                return new String[]{"x"};
            }
        };
        return new Problem(
                Utils.parseFromFile(new File("grammars/symbolic-regression-harmonic.bnf")),
                new SymbolicRegression(
                        targetFunction,
                        new LinkedHashMap<>(MathUtils.varValuesMap("x", MathUtils.uniformSample(1, 50, 1)))),
                new SymbolicRegression(
                        targetFunction,
                        new LinkedHashMap<>(MathUtils.varValuesMap("x", MathUtils.uniformSample(51, 100, 1)))),
                MathUtils.phenotypePrinter()
        );
    }

    public static Problem classic4PolynomialProblem() throws IOException {
        return new Problem(
                Utils.parseFromFile(new File("grammars/symbolic-regression-classic4.bnf")),
                new SymbolicRegression(new SymbolicRegression.TargetFunction() {
                    @Override
                    public double compute(double... v) {
                        double x = v[0];
                        return x * x * x * x + x * x * x + x * x + x;
                    }

                    @Override
                    public String[] varNames() {
                        return new String[]{"x"};
                    }
                }, new LinkedHashMap<>(MathUtils.varValuesMap("x", MathUtils.uniformSample(-1, 1, .1)))),
                null,
                MathUtils.phenotypePrinter());
    }

    public static Problem max() throws IOException {
        return new Problem(
                Utils.parseFromFile(new File("grammars/max-grammar.bnf")),
                new FitnessComputer<String>() {
            @Override
            public Fitness compute(Node<String> phenotype) {
                return new NumericFitness(-MathUtils.compute(MathUtils.transform(phenotype), Collections.EMPTY_MAP, 1)[0]);
            }

            @Override
            public Fitness worstValue() {
                return new NumericFitness(Double.POSITIVE_INFINITY);
            }
        },
                null,
                MathUtils.phenotypePrinter()
        );
    }

    public static Problem text(String target) throws IOException {
        return new Problem(
                Utils.parseFromFile(new File("grammars/text.bnf")),
                new LeafContentsDistance<>(Arrays.asList(target.replace(" ", "_").split("")), new EditDistance<String>()),
                null,
                new PhenotypePrinter<String>() {
          @Override
          public String toString(Node<String> node) {
            StringBuilder sb = new StringBuilder();
            for (Node<String> leaf : node.leaves()) {
              sb.append(leaf.getContent());
            }
            return sb.toString();
            }
        }
        );
    }

    public static Problem santaFe() throws IOException {
        return new Problem(
                Utils.parseFromFile(new File("grammars/santa-fe.bnf")),
                new SantaFe(),
                null,
                new PhenotypePrinter<String>() {
            @Override
            public String toString(Node<String> node) {
                StringBuilder sb = new StringBuilder();
                for (Node<String> leaf : node.leaves()) {
                    sb.append(leaf.getContent());
                }
                return sb.toString();
            }
        }
        );
    }

}
