import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import visitor.ASTVisitor2;

import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompilerContext;
import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBooleanLiteral;
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
import com.google.dart.compiler.resolver.CoreTypeProvider;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.type.InterfaceType;

public class ClassHierarchyAnalysisPhase implements DartCompilationPhase {
  private final CHAVisitor visitor = new CHAVisitor();
  
  @Override
  public DartUnit exec(DartUnit unit, DartCompilerContext context, CoreTypeProvider coreTypeProvider) {
    visitor.analysis(unit);
    visitor.debug();
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
  
  public List<DartMethodDefinition> getOverridingMethods(MethodNodeElement element) {
    return visitor.classMap.get(element.getEnclosingElement()).methodMap.get(element.getName());
  }
  
  
  private class CHAVisitor extends ASTVisitor2<Void, Void> {
    final LinkedHashMap<ClassElement, CHAClass> classMap = new LinkedHashMap<>();

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
      return null;
    }
    
    @Override
    public Void visitUnit(DartUnit node, Void unused) {
      System.out.println("visit "+node.getSourceName());
      super.visitUnit(node, unused);
      return null;
    }
    
    @Override
    public Void visitNewExpression(DartNewExpression node, Void unused) {
      newInstantiation(node.getType().getElement());
      acceptChildren(node);
      return null;
    }
    
    // literals
    
    @Override
    public Void visitBooleanLiteral(DartBooleanLiteral node, Void unused) {
      newInstantiation(node.getType().getElement());
      return null;
    }
    @Override
    public Void visitIntegerLiteral(DartIntegerLiteral node, Void unused) {
      newInstantiation(node.getType().getElement());
      return null;
    }
    @Override
    public Void visitDoubleLiteral(DartDoubleLiteral node, Void unused) {
      newInstantiation(node.getType().getElement());
      return null;
    }
    @Override
    public Void visitStringLiteral(DartStringLiteral node, Void unused) {
      //FIXME
      //newInstantiation(node.getType().getElement());
      return null;
    }
    @Override
    public Void visitStringInterpolation(DartStringInterpolation node, Void unused) {
      newInstantiation(node.getType().getElement());
      acceptChildren(node);
      return null;
    }
    
    @Override
    public Void visitArrayLiteral(DartArrayLiteral node, Void unused) {
      newInstantiation(node.getType().getElement());
      acceptChildren(node);
      return null;
    }
    
    @Override
    public Void visitMapLiteral(DartMapLiteral node, Void unused) {
      newInstantiation(node.getType().getElement());
      acceptChildren(node);
      return null;
    }
  }
}
