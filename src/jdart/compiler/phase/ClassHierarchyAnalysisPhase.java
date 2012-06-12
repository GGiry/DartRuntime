package jdart.compiler.phase;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartDirective;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMapLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartNewExpression;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartStringInterpolation;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.ConstructorNodeElement;
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.type.InterfaceType;

public class ClassHierarchyAnalysisPhase implements DartCompilationPhase {
  private final CHAVisitor visitor = new CHAVisitor();
  final LinkedHashSet<DartUnit> seenUnit = new LinkedHashSet<>();
  final ArrayDeque<DartUnit> pending = new ArrayDeque<>();
  CoreTypeProvider coreTypeProvider;
  
  private ClassHierarchyAnalysisPhase() {
    // enforce singleton
  }
  
  public static ClassHierarchyAnalysisPhase getInstance() {
    return SINGLETON;
  }
  private static final ClassHierarchyAnalysisPhase SINGLETON = new ClassHierarchyAnalysisPhase();
  
  public Set<DartUnit> getUnits() {
    return seenUnit;
  }
  
  public CHAClass getCHAClass(ClassElement classElement) {
    return visitor.classMap.get(classElement);
  }
  
  public CoreTypeProvider getCoreTypeProvider() {
    return coreTypeProvider;
  }
  
  public List<DartMethodDefinition> getOverridingMethods(ClassElement classElement, String methodName) {
    return visitor.classMap.get(classElement).methodMap.get(methodName);
  }
  
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    // this phase is a rogue phase that doesn't follow the phase control flow
    // the first time this phase it called, it crawle all libraries to performs
    // the CHA analysis which is not incremental
    //FIXME this doesn't work because there is no way to get all dependency
    //  so the current algorithm use static dependency to try to find all dependencies
    
    this.coreTypeProvider = coreTypeProvider;
    
    // already seen
    if (seenUnit.contains(unit)) {
      return unit;
    }
    
    //FIXME it always fails
    /*if (alreadyCalledOnce) {
      throw new IllegalStateException("CHA analysis failed "+unit);
    }*/
    
    pending.add(unit);
    seenUnit.add(unit);
    
    while(!pending.isEmpty()) {
      DartUnit pendingUnit = pending.poll();
      //System.out.println("visit unit "+pendingUnit.getSourceName());
      visitor.analysis(pendingUnit);
    }
    
    //visitor.debug();
    return unit;
  }
  
  static class CHAClass {
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
  
  
  private class CHAVisitor extends ASTVisitor2<Void, Void> {
    final LinkedHashMap<ClassElement, CHAClass> classMap = new LinkedHashMap<>();
    private final HashSet<NodeElement> seen = new HashSet<>();
    
    CHAVisitor() {
      
    }
    
    // entry point
    void analysis(DartUnit unit) {
      super.accept(unit, null);
    }
    
    private CHAClass getCHAClass(ClassElement element) {
      CHAClass chaClass = classMap.get(element);
      if (chaClass != null) {
        return chaClass;
      }
      return createCHAClass(element);
    }

    CHAClass createCHAClass(ClassElement element) {
      InterfaceType supertype = element.getSupertype();
      CHAClass superclass = null;
      if (supertype != null) {
        superclass = getCHAClass(supertype.getElement());
      }
      ArrayList<CHAClass> interfaces = new ArrayList<>();
      for(InterfaceType interfaceType: element.getInterfaces()) {
        interfaces.add(getCHAClass(interfaceType.getElement()));
      }
      
      CHAClass chaClass = new CHAClass(superclass, interfaces);
      for(Element member: element.getMembers()) {
        if (!(member instanceof MethodNodeElement)) {
          continue;
        }
        chaClass.addMethod(member.getName(), (DartMethodDefinition)((NodeElement)member).getNode());
      }
      
      classMap.put(element, chaClass);
      return chaClass;
    }    

    public void debug() {
      System.out.println("<========== DEBUG ===============>");
      System.out.println("count "+classMap.size());
      for(Entry<ClassElement, CHAClass> entry: classMap.entrySet()) {
        ClassElement classElement = entry.getKey();
        System.out.println("class "+classElement.getName()+" "+ classElement.getEnclosingElement());
        entry.getValue().debug();
      }
    }
    
    private void newInstantiation(Element element) {
      ClassElement classElement = (ClassElement) element;
      CHAClass chaClass = classMap.get(element);
      if (chaClass != null) {
        return;
      }
      
      createCHAClass(classElement);
    }
    
    
    // default visit accept all child nodes
    @Override
    public Void visitNode(DartNode node, Void unused) {
      acceptChildren(node);
      
      // try to find dependency
      Element element = node.getElement();
      if (element instanceof NodeElement) {
        NodeElement nodeElement = (NodeElement)element;
        if (seen.contains(nodeElement)) {
          return null;
        }
        seen.add(nodeElement);
        
        DartNode n = nodeElement.getNode();
        for(; !(n instanceof DartUnit); n = n.getParent()) {
          if (n == null) {
            return null;
          }
          // do nothing
        }
        DartUnit unit = (DartUnit) n;
        if (seenUnit.contains(unit)) {
          return null;
        }
        pending.add(unit);
        seenUnit.add(unit);
      }
      
      return null;
    }
    
    
    @Override
    public Void visitDirective(DartDirective node, Void parameter) {
      // do nothing
      return null;
    }
    
    @Override
    public Void visitNewExpression(DartNewExpression node, Void unused) {
      //FIXME some nodes have no corresponding Element
      ConstructorNodeElement element = node.getElement();
      if (element != null) {
        newInstantiation(element.getEnclosingElement());
      } else {
        DartNode constructor = node.getConstructor();
        if (constructor.getType() == null) {
          //System.err.println(node);
        } else {
          newInstantiation(constructor.getType().getElement());
        }  
      }
      super.visitNewExpression(node, null);
      return null;
    }
    
    // literals
    
    @Override
    public Void visitBooleanLiteral(DartBooleanLiteral node, Void unused) {
      newInstantiation(coreTypeProvider.getBoolType().getElement());
      return null;
    }
    @Override
    public Void visitIntegerLiteral(DartIntegerLiteral node, Void unused) {
      newInstantiation(coreTypeProvider.getIntType().getElement());
      return null;
    }
    @Override
    public Void visitDoubleLiteral(DartDoubleLiteral node, Void unused) {
      newInstantiation(coreTypeProvider.getDoubleType().getElement());
      return null;
    }
    @Override
    public Void visitStringLiteral(DartStringLiteral node, Void unused) {
      newInstantiation(coreTypeProvider.getStringType().getElement());
      return null;
    }
    @Override
    public Void visitStringInterpolation(DartStringInterpolation node, Void unused) {
      newInstantiation(coreTypeProvider.getStringType().getElement());
      super.visitStringInterpolation(node, null);
      return null;
    }
    
    @Override
    public Void visitArrayLiteral(DartArrayLiteral node, Void unused) {
      //FIXME
      //newInstantiation(coreTypeProvider.getArrayLiteralType(elementType).getElement());
      super.visitArrayLiteral(node, null);
      return null;
    }
    
    @Override
    public Void visitMapLiteral(DartMapLiteral node, Void unused) {
      //FIXME
      //newInstantiation(node.getType().getElement());
      super.visitMapLiteral(node, null);
      return null;
    }
  }
}
