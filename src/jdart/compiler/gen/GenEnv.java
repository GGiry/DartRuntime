package jdart.compiler.gen;

import java.util.HashMap;
import java.util.Objects;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.dart.compiler.resolver.VariableElement;

class GenEnv {
  private final MethodVisitor methodVisitor; 
  private final MethodVisitor sideMethodVisitor;
  private final Type returnType;
  private final int mixedIntShift;  // 0 for int and 1 for BigInt
  private final IfBranches ifBranches;

  private final /*maybenull*/GenEnv parent;
  private final HashMap<VariableElement, Var> variableMap;
  private int slotCount;

  private final Label breakLabel;
  private final Label continueLabel;

  private GenEnv(MethodVisitor methodVisitor, MethodVisitor sideMethodVisitor, Type returnType, int mixedIntShift, IfBranches ifBranches, /*maybenull*/GenEnv parent, HashMap<VariableElement, Var> variableMap, int slotCount, Label breakabel, Label continueLabel) {
    this.methodVisitor = methodVisitor;
    this.sideMethodVisitor = sideMethodVisitor;
    this.returnType = returnType;
    this.mixedIntShift = mixedIntShift;
    this.ifBranches = ifBranches;
    this.parent = parent;
    this.variableMap = variableMap;
    this.slotCount = slotCount;
    this.breakLabel = breakabel;
    this.continueLabel = continueLabel;
  }

  public GenEnv(MethodVisitor methodVisitor, MethodVisitor sideMethodVisitor, Type returnType, int slotCount) {
    this(methodVisitor, sideMethodVisitor, returnType, 0, null, null, new HashMap<VariableElement, Var>(), slotCount, null, null);
  }

  public MethodVisitor getMethodVisitor() {
    return methodVisitor;
  }
  public MethodVisitor getSideMethodVisitor() {
    return sideMethodVisitor;
  }
  public Type getReturnType() {
    return returnType;
  }
  public int getMixedIntShift() {
    return mixedIntShift;
  }
  public /*maybenull*/IfBranches getIfBranches() {
    return ifBranches;
  }

  public GenEnv newSplitPathEnv(MethodVisitor mv, int mixedIntShift) {
    return new GenEnv(mv, sideMethodVisitor, returnType, mixedIntShift, ifBranches, parent, variableMap, slotCount, breakLabel, continueLabel);
  }

  public GenEnv newIf(IfBranches ifBranches) {
    return new GenEnv(methodVisitor, sideMethodVisitor, returnType, mixedIntShift, ifBranches, parent, variableMap, slotCount, breakLabel, continueLabel);
  }

  public Var newVar(Type type) {
    Objects.requireNonNull(type);
    int slot = slotCount;
    slotCount += type.getSize();
    Var var = new Var(type, slot);
    return var;
  }

  public void registerVar(VariableElement element, Var var) {
    Objects.requireNonNull(element);
    Objects.requireNonNull(var);
    variableMap.put(element, var);
  }

  public Var getVar(VariableElement element) {
    Var var = variableMap.get(element);
    if (var != null) {
      return var;
    }
    if (parent != null) {
      return parent.getVar(element);
    }
    return null;
  }

  public GenEnv newLoopLabels(Label breakLabel, Label continueLabel) {
    return new GenEnv(methodVisitor, sideMethodVisitor, returnType, mixedIntShift, ifBranches, parent, variableMap, slotCount, breakLabel, continueLabel);
  }

  public Label getBreakLabel() {
    return breakLabel;
  }

  public Label getContinueLabel() {
    return continueLabel;
  }

  @Override
  public String toString() {
    return "GenEnv " + variableMap;
  }
}
