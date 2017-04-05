/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.evolver.listener;

import it.units.malelab.ege.evolver.Individual;
import it.units.malelab.ege.evolver.event.EvolutionEvent;
import it.units.malelab.ege.evolver.event.GenerationEvent;
import it.units.malelab.ege.core.fitness.FitnessComputer;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author Danny
 */
public class BestGenosImageGenerator<G extends BitsGenotype, T> extends AbstractGenerationLogger<G, T> {
    
    ArrayList<BitsGenotype> rows;
    Map<String, Object> constants;
    
    public BestGenosImageGenerator(FitnessComputer<T> generalizationFitnessComputer, Map<String, Object> constants) {
        super(generalizationFitnessComputer, constants);
        rows = new ArrayList<>();
        this.constants = constants;
    }
    
    @Override
    public synchronized void listen(EvolutionEvent<G, T> event) {
        List<Individual<G, T>> population = new ArrayList<>(((GenerationEvent) event).getPopulation());
        rows.add(population.get(0).getGenotype());
        System.out.print(">");
    }
    
    public void close() {
        try {
            int width = 0, height = rows.size();
            for (BitsGenotype g : rows) {
                if (g.size()>width) {
                    width = g.size();
                }
            }
            BitsGenotype temp;
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i<rows.size(); i++) {
                temp = rows.get(i);
                for (int j = 0; j<temp.size(); j++) {
                    if (temp.get(j)) {
                        bi.setRGB(j, i, 0xFFFFFFFF);
                    } else {
                        bi.setRGB(j, i, 0xFF000000);
                    }
                }
                for (int j=temp.size(); j<width; j++) {
                    bi.setRGB(j, i, 0xFFFF0000);
                }
            }
            ImageIO.write(bi, "PNG", new File(constants.get("problem")+"_"+constants.get("mapper")+"_"+constants.get("initGenoSize")+"_"+constants.get("run")+".PNG"));

        } catch (IOException ie) {
          ie.printStackTrace();
        }
    }
    
}
 
