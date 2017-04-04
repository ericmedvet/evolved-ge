evolved-ge
==========

evolved-ge is a modular Java framework for experimenting with [Grammatica Evolution](https://en.wikipedia.org/wiki/Grammatical_evolution) (GE), an Evolutionary Algorith (EA) often considered a form of Genetic Programming (GP).
evolved-ge can be used to:
* apply existing GE variants to user-provided problems;
* investigate about specific aspects of existing variants (e.g., diversity, locality, redundancy, evolvability);
* design and assess new variants or extensions.

It currently contains the implementations of several mappers:
* standard GE mapper [1];
* breath-first mapper (used in [2]);
* piGE mapper [3];
* SGE mapper [4] (and a variant);
* WHGE/HGE mapper [5].

Moreover, beyond the mapper, it can operate with different EA-related settings:
* diversity promotion [6];
* different replacement strategies, different selection criteria, different genetic operators.

Finally, it can be easily configured and instrumented to output several metrics describing ongoing evolutions.

Defining a problem
==================
The user is expected to provide the problem as a pair consisting of a file of the grammar defining the language for the solutions and a Java class implementing the [`FintessComputer`](src/main/java/it/units/malelab/ege/evolver/fitness/FitnessComputer.java) inteface.
The latter allows evolved-ge to associate each candidate solution with an indication of its ability to solve the problem.
`FintessComputer` operates on individuals as trees (`Node<T>`): in an individual tree, each leaf node is a terminal symbol of the grammar.

A simple problem example follows.
The goal is to generate a target string: each solution (individual) represents a string and its fitness is given by its distance from the target string (class [`LeafContentsDistanceFitness`](src/main/java/it/units/malelab/ege/evolver/fitness/LeafContentsDistanceFitness.java)).
```java
public class LeafContentsDistanceFitness<T> implements FitnessComputer<T> {
  
  private final List<T> target;
  private final Distance<List<T>> distance;

  public LeafContentsDistanceFitness(List<T> target, Distance<List<T>> distance) {
    this.target = target;
    this.distance = distance;
  }

  @Override
  public Fitness compute(Node<T> phenotype) {
    double d = distance.d(Utils.contents(phenotype.leaves()), target);
    return new NumericFitness(d);
  }

  @Override
  public Fitness worstValue() {
    return new NumericFitness(Double.POSITIVE_INFINITY);
  }
  
}
```

The [BNF](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) grammar of the corresponding problem is:
```
<text> ::= <sentence> <text> | <sentence>
<sentence> ::= <Word> _ <sentence> | <word> _ <sentence> | <word> <punct> 
<word> ::= <letter> <word> | <letter>
<Word> ::= <Letter> <word>
<letter> ::= <vowel> | <consonant>
<vowel> ::= a | o | u | e | i
<consonant> ::= q | w | r | t | y | p | s | d | f | g | h | j | k | l | z | x | c | v | b | n | m
<Letter> ::= <Vowel> | <Consonant>
<Vowel> ::= A | O | U | E | I
<Consonant> ::= Q | W | R | T | Y | P | S | D | F | G | H | J | K | L | Z | X | C | V | B | N | M
<punct> ::= ! | ? | .
```

Sample usage
============
Performing an evolutionary run with evolved-ge consists in three steps:
1. building a configuration object (suitable for the chosen evolver)
2. setting zero or more [`EvolutionListener`](src/main/java/it/units/malelab/ege/evolver/listener/EvolutionListener.java)
3. starting the [`Evolver`](src/main/java/it/units/malelab/ege/evolver/Evolver.java)

A simple code could be:
```java
public static void main(String[] args) {
  StandardConfiguration<BitsGenotype, String> configuration = StandardConfiguration.createDefault(
      BenchmarkProblems.harmonicCurveProblem(),
      random);
  List<EvolutionListener<BitsGenotype, String>> listeners = new ArrayList<>();
      listeners.add(new CollectorGenerationLogger<>(
              Collections.EMPTY_MAP,
              System.out, true, 10, " ", " | ",
              new Population<>("%5.2f"),
              new Best<>("%5.2f")));
  Evolver<BitsGenotype, String> evolver = new StandardEvolver<>(
    Runtime.getRuntime().availableProcessors() - 1,
    configuration,
    random,
    false);
  evolver.go(listeners);  
}
```
Note that this code uses the `Problem` class for briefly defining the problem (see [`BenchmarkProblems`](src/main/java/it/units/malelab/ege/problems/BenchmarkProblems.java)) through the `createDefault` method.
However, the user may act more directly by setting the `fitnessComputer` field in the `StandardConfiguration` object and the grammar directly in the mapper (which itself is a field in the `StandardConfiguration` object).

The available evolvers include:
* the [`StandardEvolver`](src/main/java/it/units/malelab/ege/evolver/StandardEvolver.java), which implements the generic _m+n_ replacement strategy with or without overlapping (depending on the values for `populationSize` (for _m_), `offspringSize` (for _n_), and `overlapping` fields in the [`StandardConfiguration`](src/main/java/it/units/malelab/ege/evolver/StandardConfiguration.java));
* the [`PartitionEvolver`](src/main/java/it/units/malelab/ege/evolver/PartitionEvolver.java), which implements the modular diversity promotion mechanism sketched in [6].

The available [listeners](src/main/java/it/units/malelab/ege/evolver/listener) include:
* a modular [`CollectorGenerationLogger`](src/main/java/it/units/malelab/ege/evolver/listener/CollectorGenerationLogger.java), which at each generation collects ad logs to a `PrintStream` (optionally with a customizable format) a set of specified metrics (through some [collectors](src/main/java/it/units/malelab/ege/evolver/listener/collector)) concerning the current population (e.g., best individual, stats, diversity measure); it can be used to log both on the standard output or on a file (possibly in a csv format);
* a [`ConfigurationSaverListener`](src/main/java/it/units/malelab/ege/evolver/listener/CollectorGenerationLogger.java), useful for saving the configuration of each run in an automated execution of several runs.


References
==========
1. Ryan, Conor, J. J. Collins, and Michael O. Neill. "Grammatical evolution: Evolving programs for an arbitrary language." European Conference on Genetic Programming. Springer Berlin Heidelberg, 1998.
2. Fagan, David, et al. "An analysis of genotype-phenotype maps in grammatical evolution." European Conference on Genetic Programming. Springer Berlin Heidelberg, 2010.
3. O’Neill, Michael, et al. "πgrammatical evolution." Genetic and Evolutionary Computation Conference. Springer Berlin Heidelberg, 2004.
4. Lourenço, Nuno, Francisco B. Pereira, and Ernesto Costa. "SGE: a structured representation for grammatical evolution." International Conference on Artificial Evolution (Evolution Artificielle). Springer International Publishing, 2015.
5. Medvet, Eric. "Hierarchical Grammatical Evolution." ACM Genetic and Evolutionary Computation Conference (GECCO), 2017
6. Medvet, Eric, Alberto Bartoli, and Giovanni Squillero. "An Effective Diversity Promotion Mechanism in Grammatical Evolution" ACM Genetic and Evolutionary Computation Conference (GECCO), 2017
