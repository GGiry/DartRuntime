package jdart.compiler.gen;

import java.util.HashSet;
import java.util.Set;

public class GenResult {
  private final int varSlot;
  private final Set<Var> dependencies;

  public GenResult(int varSlot, Set<Var> dependencies) {
    this.varSlot = varSlot;
    this.dependencies = dependencies;
  }
  
  public GenResult(int varSlot, Var dependency) {
    this.varSlot = varSlot;
    HashSet<Var> dependencies = new HashSet<>();
    dependencies.add(dependency);
    this.dependencies = dependencies;
  }
  
  public int getVarSlot() {
    return varSlot;
  }
  
  public Set<Var> getDependencies() {
    return dependencies;
  }
}
