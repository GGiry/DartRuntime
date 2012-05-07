import java.util.HashMap;

import type.Type;

import com.google.dart.compiler.resolver.VariableElement;

public class FlowEnv {
  private final HashMap<VariableElement, Type> variableTypeMap = new HashMap<>();

  public Type getType(VariableElement variable) {
    return variableTypeMap.get(variable);
  }

  public void register(VariableElement variable, Type type) {
    System.out.println(variable + ", " + type);
    variableTypeMap.put(variable, type);
  }
}
