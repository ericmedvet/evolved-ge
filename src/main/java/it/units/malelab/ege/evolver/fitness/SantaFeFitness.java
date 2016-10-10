package it.units.malelab.ege.evolver.fitness;

import it.units.malelab.ege.Node;
import java.util.List;

/**
 *
 * @author Danny
 */
public class SantaFeFitness implements FitnessComputer<String> {

    static final String IF = "if(food_ahead()){";
    static final String ELSE = "}else{";
    static final String END_IF = "}";
    static final String MOVE = "move();";
    static final String LEFT = "left();";
    static final String RIGHT = "right();";

    @Override
    public Fitness compute(Node<String> phenotypeRoot) {
        List<Node<String>> phenotype = phenotypeRoot.leaves();
        Trail trail = new Trail(); //Trail to travese
        double fitness = trail.getFood(); //Initial fitness
        try {
            if (phenotype.size() > 0) {
                while (trail.get_Energy() > 0) {
                    run(phenotype, trail, 0);
                }
                fitness = trail.getFitness();
            } else {
                throw new IllegalArgumentException("Bad phenotype size");
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error getting Fitness: " + phenotype.toString());
        }
        return new NumericFitness(fitness);
    }

    @Override
    public Fitness worstValue() {
        return new NumericFitness(Double.POSITIVE_INFINITY);
    }

    private void lookAheadElse(List<Node<String>> phenotype, int programCounter) {
        boolean found = false;
        int depth = 0;//Keep track of nested ifs
        //While program not finished and the else bloch for depth 0 is found
        while (phenotype.size() > programCounter && !found) {
            //Current token
            final String token = phenotype.get(programCounter).getContent();
            //Check else statment and correct depth
            if (token.equals(SantaFeFitness.ELSE) && depth == 0) {
                found = true; //Found
            } else //If staement increase depth
            {
                if (token.equals(SantaFeFitness.IF)) {
                    depth++;
                } else //End of if statemnt decrease depth
                {
                    if (token.equals(SantaFeFitness.END_IF)) {
                        depth--;
                    }
                }
            }
            programCounter++;
        }
    }

    /**
     * Find the end if statement
     */
    private void lookAheadEnd_If(List<Node<String>> phenotype, int programCounter) {
        boolean found = false;
        int depth = 0;//Keep track of nested ifs
        //While program not finished and the else bloch for depth 0 is found
        while (phenotype.size() > programCounter && !found) {
            //Current token
            final String token = phenotype.get(programCounter).getContent();
            //Check else statment and correct depth
            if (token.equals(SantaFeFitness.END_IF)) {
                if (depth == 0) {
                    found = true; //Found
                } else {
                    depth--;
                }
            } else //If staement increase depth
            {
                if (token.equals(SantaFeFitness.IF)) {
                    depth++;
                }
            }
            programCounter++;
        }
    }

    /**
     * Execute the program by calling the functions in Trail
     */
    private void run(List<Node<String>> phenotype, Trail trail, int programCounter) {
        //Check if end of program
        if (programCounter < phenotype.size()) {
            //Get current token
            final String token = phenotype.get(programCounter).getContent();
            //Increase program counter
            programCounter++;
            if (token.equals(SantaFeFitness.IF)) {//IF food ahead
                if (trail.food_ahead() != 1) {
                    //Find else statement
                    lookAheadElse(phenotype, programCounter);
                }
            } else if (token.equals(SantaFeFitness.LEFT)) {//left
                trail.left();
            } else if (token.equals(SantaFeFitness.MOVE)) {//move
                trail.move();
            } else if (token.equals(SantaFeFitness.RIGHT)) {//right
                trail.right();
            } else if (token.equals(SantaFeFitness.ELSE)) {
                lookAheadEnd_If(phenotype, programCounter);
            } else if (!token.equals(SantaFeFitness.END_IF)) {
                throw new IllegalArgumentException("Illegal Terminal symbol:" + token);
            }
            run(phenotype, trail, programCounter);//countinue executing program
        }
    }
}