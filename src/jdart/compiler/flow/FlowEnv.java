package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.Type;
import jdart.compiler.type.Types;
import jdart.compiler.type.UnionType;

import com.google.dart.compiler.resolver.VariableElement;

public class FlowEnv {
  private final/* maybenull */FlowEnv parent;
  private final Type thisType;
  private final Type returnType;
  private final Type expectedType;
  private final/* maybenull */ HashMap<VariableElement, Type> variableTypeMap;
  private final boolean inLoop;
  private final Set<VariableElement> loopSet;

  private FlowEnv(/* maybenull */FlowEnv parent, /* maybenull */Type thisType, Type returnType, Type expectedType,
      /* maybenull */HashMap<VariableElement, Type> variableTypeMap, boolean inLoop, Set<VariableElement> loopSet) {
    this.parent = parent;
    this.thisType = thisType;
    this.returnType = Objects.requireNonNull(returnType);
    this.expectedType = Objects.requireNonNull(expectedType);
    this.variableTypeMap = variableTypeMap;
    this.inLoop = inLoop;
    this.loopSet = loopSet;
  }

  public FlowEnv(Type thisType) {
    this(null, thisType, CoreTypeRepository.VOID_TYPE, CoreTypeRepository.VOID_TYPE, null, false, null);
  }

  /**
   * Create a new flow environment with a parent and an expected type.
   * 
   * @param parent
   * @param returnType 
   * @param expectedType
   * @param inLoop 
   */
  public FlowEnv(FlowEnv parent, Type returnType, Type expectedType, boolean inLoop) {
    this(parent, parent.thisType, returnType, expectedType, new HashMap<VariableElement, Type>(), inLoop, null);
  }
  
  /**
   * Create a new flow environment with a parent and an expected type.
   * 
   * @param parent
   * @param returnType 
   * @param expectedType
   * @param inLoop 
   * @param loopSet
   */
  public FlowEnv(FlowEnv parent, Type returnType, Type expectedType, boolean inLoop, Set<VariableElement> loopSet) {
    this(parent, parent.thisType, returnType, expectedType, new HashMap<VariableElement, Type>(), inLoop, loopSet);
  }
  
  /**
   * Create a new flow environment with the same return type, expected type and inLoop state as parent.
   * Use parent as parent.
   * 
   * @param parent Parent to use.
   */
  public FlowEnv(FlowEnv parent) {
    this(parent, parent.getReturnType(), parent.getExpectedType(), parent.inLoop(), parent.loopSet);
  }

  /**
   * Retrieve the type of a variable. If the current flow environment has no
   * variable registered, this call is delegated to the parent flow environment
   * if it exists.
   * 
   * @param variable
   *          a variable.
   * @return the type of the variable or null if the variable is unknown.
   */
  public Type getType(VariableElement variable) {
    if (variableTypeMap == null) {
      return null;
    }

    Type type = variableTypeMap.get(variable);
    if (type == null) {
      if (parent != null) {
        return parent.getType(variable);
      }
    }
    return type;
  }

  /**
   * Register a new type for a variable.
   * 
   * @param variable
   *          a variable
   * @param type
   *          a type
   */
  public void register(VariableElement variable, Type type) {
    Objects.requireNonNull(variable);
    Objects.requireNonNull(type);
    
    if (inLoop && loopSet != null) {
      if (loopSet.contains(variable)) {
        variableTypeMap.put(variable, Types.widening(type));
        return;
      }
    }
    variableTypeMap.put(variable, type);
  }
  
  /** TODO
   * Register a new type for a variable.
   * 
   * @param variable
   *          a variable
   * @param type
   *          a type
   */
  public void registerConditionVariable(VariableElement variable, Type type, boolean loopCondition) {
    Objects.requireNonNull(variable);
    Objects.requireNonNull(type);
    if (loopCondition) {
      variableTypeMap.put(variable, type);
    } else {
      register(variable, type);
    }
  }

  /**
   * Returns the type of '{@code this}'.
   * 
   * @return the type of '{@code this}' or null if it doesn't exist.
   */
  public Type getThisType() {
    return thisType;
  }

  /**
   * Returns the declared return type of the current function/method.
   * 
   * @return the declared return type of the current function/method.
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Create a new flow environment that will share the same variable types with
   * the current one and have a different expected type.
   * 
   * @param expectedType
   *          new expected type.
   * @return a new flow environment.
   */
  public FlowEnv expectedType(Type expectedType) {
    if (expectedType.equals(this.expectedType)) { // implicit null check
      return this;
    }
    return new FlowEnv(parent, thisType, returnType, expectedType, variableTypeMap, inLoop, loopSet);
  }

  /**
   * Returns the current expected type.
   * 
   * @return the current expected type.
   */
  public Type getExpectedType() {
    return expectedType;
  }
  
  /**
   * @return the inLoop state.
   */
  public boolean inLoop() {
    return inLoop;
  }

  @Override
  public String toString() {
    return "" + variableTypeMap + ", " + parent;
  }

  /**
   * Check if this environment has the same types as his parent. If a variable
   * doesn't exist in parent environment, the variable is ignored.
   * 
   * @return <code>true</code> If the environment is stable, false otherwise.
   */
  public boolean isStable() {
    if (parent == null) {
      return false;
    }

    for (Entry<VariableElement, Type> entry : variableTypeMap.entrySet()) {
      Type parentType = parent.getType(entry.getKey());
      if (parentType == null) { // ignore an unknown variable.
        continue;
      }
      Type widening = Types.widening(entry.getValue());
      Type parentWidening = Types.widening(parentType);
      
      if (widening instanceof UnionType) {
        UnionType uType = (UnionType) widening;
        return uType.containsType(parentWidening);
      }
      
      if (parentWidening instanceof UnionType) {
        UnionType uType = (UnionType) parentWidening;
        return uType.containsType(widening);
      }
      
      if (!widening.equals(parentWidening)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Changes variable type in this environment, using parameter values. Do not
   * create new variables in this environment.
   * 
   * @param env
   *          Environment with the new values.
   */
  public void copyAll(FlowEnv env) {
    for (Entry<VariableElement, Type> entry : variableTypeMap.entrySet()) {
      entry.setValue(env.getType(entry.getKey()));
    }
  }

  /**
   * Merges the specified environment in this {@link FlowEnv}. Only merge
   * variables already known by this.
   * 
   * @param env
   *          Environment to merge.
   */
  public void merge(FlowEnv env) {
    for (Entry<VariableElement, Type> entry : env.variableTypeMap.entrySet()) {
      VariableElement key = entry.getKey();

      if (getType(key) != null) {
        Type type1 = entry.getValue();
        Type type2 = getType(key);
        Type unionType = Types.union(type1, type2);
        register(key, unionType);
      }
    }
  }

  public void mergeWithoutUnion(FlowEnv env) {
    for (Entry<VariableElement, Type> entry : env.variableTypeMap.entrySet()) {
      VariableElement key = entry.getKey();
      if (getType(key) != null && getType(key) == NULL_TYPE) {
        register(key, entry.getValue());
      }
    }
  }

  public void mergeCommonValues(FlowEnv env) {
    for (Entry<VariableElement, Type> entry : env.variableTypeMap.entrySet()) {
      VariableElement key = entry.getKey();

      if (getType(key) != null) {
        Type type1 = entry.getValue();
        Type type2 = getType(key);
        Type unionType = type1.commonValuesWith(type2);
        register(key, unionType);
      }
    }
  }

  /** TODO
   * Returns a Map containing all values of this which are not in the specified {@link FlowEnv environment}.
   * @param beforeLoopMap 
   */
  public Map<VariableElement, Type> mapDiff(Map<VariableElement, Type> beforeLoopMap) {
    HashMap<VariableElement, Type> result = new HashMap<>();
    for (Entry<VariableElement, Type> entry : variableTypeMap.entrySet()) {
      if (!entry.getValue().equals(beforeLoopMap.get(entry.getKey()))) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  public Map<VariableElement, Type> getClonedMap() {
    return new HashMap<>(variableTypeMap);
  }
}
