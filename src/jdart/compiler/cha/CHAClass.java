package jdart.compiler.cha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.resolver.MethodNodeElement;

class CHAClass {
  private final CHAClass superclass;
  private final List<CHAClass> interfaces;
  final HashMap<String, ArrayList<DartMethodDefinition>> methodMap = new HashMap<>();
  
  public CHAClass(CHAClass superclass, List<CHAClass> interfaces) {
    this.superclass = superclass;
    this.interfaces = interfaces;
  }

  public void addMethod(String name, DartMethodDefinition methodDefinition) {
    if (superclass != null) {
      superclass.addMethodIfOverride(name, methodDefinition);
    }
    for(CHAClass interfaze: interfaces) {
      interfaze.addMethod(name, methodDefinition);
    }
    insertMethod(name, methodDefinition);
  }
  
  private void insertMethod(String name, DartMethodDefinition methodDefinition) {
    ArrayList<DartMethodDefinition> list = methodMap.get(name);
    if (list == null) {
      list = new ArrayList<>();
      methodMap.put(name, list);
    }
    list.add(methodDefinition); 
  }

  private boolean addMethodIfOverride(String name, DartMethodDefinition methodDefinition) {
    boolean override = false;
    if (superclass != null) {
      override = superclass.addMethodIfOverride(name, methodDefinition);
    }
    for(CHAClass interfaze: interfaces) {
      override |= interfaze.addMethodIfOverride(name, methodDefinition);
    }
    
    if (override || methodMap.containsKey(name)) {
      insertMethod(name, methodDefinition);
      return true;
    }
    return false;
  }

  public void debug() {
    for(Entry<String, ArrayList<DartMethodDefinition>> entry: methodMap.entrySet()) {
      ArrayList<DartMethodDefinition> list = new ArrayList<>();
      for(DartMethodDefinition node: entry.getValue()) {
        if (node.getElement().getModifiers().isAbstract()) {
          continue;
        }
        list.add(node);
      }
      
      if (list.size() <= 1) {
        continue;
      }
      
      System.out.println("  method "+entry.getKey());
      for(DartMethodDefinition node: list) {
        MethodNodeElement element = node.getElement();
        System.out.println("    "+element.getEnclosingElement().getName()+'.'+element.getName()+" "+element.getFunctionType());
      }
    }
  }
}