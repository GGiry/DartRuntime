import java.util.HashMap;

import type.Type;

import com.google.dart.compiler.resolver.VariableElement;

public class FlowEnv {
  private final HashMap<VariableElement, Type> variableTypeMap = new HashMap<>();
  private final FlowEnv parent;

  public FlowEnv(/*maybenull*/FlowEnv parent) {
    this.parent = parent;
  }
  
  public Type getType(VariableElement variable) {
    Type type =  variableTypeMap.get(variable);
    if (type == null) {
      if (parent != null) {
        return parent.getType(variable);
      }
    }
    return type;
  }

  public void register(VariableElement variable, Type type) {
    variableTypeMap.put(variable, type);
  }
}
