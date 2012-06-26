package jdart.compiler.flow;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jdart.compiler.type.DoubleType;
import jdart.compiler.type.InterfaceType;
import jdart.compiler.type.NumType;
import jdart.compiler.type.Type;
import jdart.compiler.type.Types;
import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBreakStatement;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.resolver.VariableElement;

// LoopVisitor returns all variables which had change during loop.
class LoopVisitor extends ASTVisitor2<Map<VariableElement, Type>, FlowEnv> {
  private final FTVisitor flowTypeVisitor;

  LoopVisitor(FTVisitor ftVisitor) {
    flowTypeVisitor = ftVisitor;
  }

  @Override
  protected Map<VariableElement, Type> accept(DartNode node, FlowEnv parameter) {
    return super.accept(node, parameter);
  }

  private void addAllWithUnion(Map<VariableElement, Type> out, Map<VariableElement, Type> in) {
    for (Entry<VariableElement, Type> entry : in.entrySet()) {
      Type previousType = out.get(entry.getKey());
      if (previousType == null) {
        out.put(entry.getKey(), entry.getValue());
      } else {
        out.put(entry.getKey(), Types.union(entry.getValue(), previousType));
      }
    }
  }

  @Override
  public Map<VariableElement, Type> visitForStatement(DartForStatement node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    addAllWithUnion(map, accept(node.getInit(), parameter));
    addAllWithUnion(map, accept(node.getBody(), parameter));
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitDoWhileStatement(DartDoWhileStatement node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    addAllWithUnion(map, accept(node.getBody(), parameter));
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitBlock(DartBlock node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    for (DartStatement statement : node.getStatements()) {
      Map<VariableElement, Type> acceptMap = accept(statement, parameter);
      addAllWithUnion(map, acceptMap);
    }
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitBreakStatement(DartBreakStatement node, FlowEnv parameter) {
    return Collections.<VariableElement, Type>emptyMap();
  }

  @Override
  public Map<VariableElement, Type> visitIfStatement(DartIfStatement node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    addAllWithUnion(map, accept(node.getThenStatement(), parameter));
    if (node.getElseStatement() != null) {
      addAllWithUnion(map, accept(node.getElseStatement(), parameter));
    }
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitExprStmt(DartExprStmt node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    addAllWithUnion(map, accept(node.getExpression(), parameter));
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitUnaryExpression(DartUnaryExpression node, FlowEnv parameter) {
    return Collections.<VariableElement, Type>emptyMap();
  }

  @Override
  public Map<VariableElement, Type> visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();
    if (node.getOperator().isAssignmentOperator()) {

      // We need to remember all variable's changes during the loop.
      // Because we don't look at loop's iteration time we need widened types.

      VariableElement element = (VariableElement) node.getArg1().getElement();
      if (element != null) {
        TypeVisitor typeVisitor = new TypeVisitor();
        Type currentType = Types.widening(typeVisitor.accept(node, parameter));
        map.put(element, currentType);
        parameter.register(element, currentType);
      }
    }
    return map;
  }

  @Override
  public Map<VariableElement, Type> visitVariableStatement(DartVariableStatement node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();

    for (DartVariable variable : node.getVariables()) {
      map.putAll(accept(variable, parameter));
    }

    return map;
  }

  @Override
  public Map<VariableElement, Type> visitVariable(DartVariable node, FlowEnv parameter) {
    HashMap<VariableElement, Type> map = new HashMap<>();

    com.google.dart.compiler.type.Type type = node.getValue().getType();
    if (type != null) {
      Type asType = flowTypeVisitor.typeHelper.asType(true, type);
      map.put(node.getElement(), asType);
      parameter.register(node.getElement(), asType);
      return map;
    }

    TypeVisitor visitor = new TypeVisitor();

    Type valueType = visitor.accept(node.getValue(), parameter);

    parameter.register(node.getElement(), valueType);
    map.put(node.getElement(), valueType);

    return map;
  }

  class TypeVisitor extends ASTVisitor2<Type, FlowEnv> {
    @Override
    protected Type accept(DartNode node, FlowEnv parameter) {
      return super.accept(node, parameter);
    }

    @Override
    public Type visitBinaryExpression(DartBinaryExpression node, FlowEnv parameter) {
      Type type1 = accept(node.getArg1(), parameter);
      Type type2 = accept(node.getArg2(), parameter);

      if (type1 instanceof NumType && type2 instanceof NumType) {
        if (type1 instanceof DoubleType || type2 instanceof DoubleType) {
          return DOUBLE_NON_NULL_TYPE;
        }
        return INT_NON_NULL_TYPE;
      }

      throw new IllegalStateException("VisitBinaryExpr: " + type1 + " " + node.getOperator() + " " + type2 + " not implemented.");
    }

    @Override
    public Type visitDoubleLiteral(DartDoubleLiteral node, FlowEnv parameter) {
      return flowTypeVisitor.typeHelper.asType(true, node.getType());
    }

    @Override
    public Type visitIntegerLiteral(DartIntegerLiteral node, FlowEnv parameter) {
      return flowTypeVisitor.typeHelper.asType(true, node.getType());
    }

    @Override
    public Type visitIdentifier(DartIdentifier node, FlowEnv parameter) {
      return parameter.getType((VariableElement) node.getElement());
    }

    @Override
    public Type visitArrayAccess(DartArrayAccess node, FlowEnv parameter) {
      com.google.dart.compiler.type.InterfaceType type = (com.google.dart.compiler.type.InterfaceType) node.getTarget().getElement().getType();

      return flowTypeVisitor.typeHelper.asType(true, type.getArguments().get(0));
    }
  }
}