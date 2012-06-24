package jdart.compiler.gen;

import org.objectweb.asm.Label;

class IfBranches {
  private final boolean inverseCondition;
  private final Label elseLabel;
  private final Label endLabel;

  public IfBranches(boolean inverseCondition, Label elseLabel, Label endLabel) {
    this.inverseCondition = inverseCondition;
    this.elseLabel = elseLabel;
    this.endLabel = endLabel;
  }

  public boolean isInversed() {
    return inverseCondition;
  }
  public Label getElseLabel() {
    return elseLabel;
  }
  public Label getEndLabel() {
    return endLabel;
  }
}
