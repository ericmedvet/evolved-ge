/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.ege.benchmark.mapper.element;

import com.google.common.collect.Range;
import it.units.malelab.ege.ge.genotype.BitsGenotype;
import it.units.malelab.ege.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public enum Function implements Element {
    LENGTH, SIZE, COUNT, COUNT_R, INT, ROTATE, SUBSTRING, SPLIT, SPLIT_W, LIST, CONCAT, APPLY,
    OP_ADD, OP_SUBTRACT, OP_MULT, OP_DIVIDE, OP_REMAINDER
}
