package jdart.compiler.flow;

import java.util.HashSet;

import jdart.compiler.visitor.ASTVisitor2;

import com.google.dart.compiler.ast.DartArrayAccess;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBreakStatement;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.VariableElement;

// LoopVisitor returns all variables which had change during loop.
class LoopVisitor extends ASTVisitor2<Void, HashSet<VariableElement>> {
  @Override
  protected Void accept(DartNode node, HashSet<VariableElement> parameter) {
    super.accept(node, parameter);
    return null;
  }
  
  @Override
  public Void visitForStatement(DartForStatement node, HashSet<VariableElement> parameter) {
    DartExpression condition = node.getCondition();
    DartStatement init = node.getInit();
    DartStatement body = node.getBody();
    DartExpression increment = node.getIncrement();
    if (condition != null) {
      accept(condition, parameter);
    }
    if (init != null) {
      accept(init, parameter);
    } 
    if (body != null) {
      accept(body, parameter);
    }
    if (increment != null) {
      accept(increment, parameter);
    }
    return null;
  }
  
  @Override
  public Void visitBreakStatement(DartBreakStatement node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitDoWhileStatement(DartDoWhileStatement node, HashSet<VariableElement> parameter) {
    DartStatement body = node.getBody();
    DartExpression condition = node.getCondition();
    if (body != null) {
      accept(body, parameter);
    }
    if (condition != null) {
      accept(condition, parameter);
    }
    return null;
  }
  
  @Override
  public Void visitExprStmt(DartExprStmt node, HashSet<VariableElement> parameter) {
    accept(node.getExpression(), parameter);
    return null;
  }
  
  @Override
  public Void visitBinaryExpression(DartBinaryExpression node, HashSet<VariableElement> parameter) {
    accept(node.getArg1(), parameter);
    accept(node.getArg2(), parameter);
    if (node.getOperator().isAssignmentOperator()) {
      if (node.getArg1().getElement() instanceof VariableElement) {
        parameter.add((VariableElement) node.getArg1().getElement());
      }
    }
    return null;
  }
  
  @Override
  public Void visitBlock(DartBlock node, HashSet<VariableElement> parameter) {
    for (DartStatement statement : node.getStatements()) {
      accept(statement, parameter);
    }
    return null;
  }
  
  @Override
  public Void visitIfStatement(DartIfStatement node, HashSet<VariableElement> parameter) {
    accept(node.getCondition(), parameter);
    accept(node.getThenStatement(), parameter);
    DartStatement elseStatement = node.getElseStatement();
    if (elseStatement != null) {
      accept(elseStatement, parameter);
    }
    return null;
  }
  
  @Override
  public Void visitUnaryExpression(DartUnaryExpression node, HashSet<VariableElement> parameter) {
    if (node.getOperator() == Token.INC || node.getOperator() == Token.DEC) {
      if (node.getArg().getElement() instanceof VariableElement) {
        parameter.add((VariableElement) node.getArg().getElement());
      }
    }
    return null;
  }
  
  @Override
  public Void visitEmptyStatement(DartEmptyStatement node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitVariableStatement(DartVariableStatement node, HashSet<VariableElement> parameter) {
    for (DartVariable variable : node.getVariables()) {
      accept(variable, parameter);
    }
    return null;
  }
  
  @Override
  public Void visitVariable(DartVariable node, HashSet<VariableElement> parameter) {
    parameter.add(node.getElement());
    return null;
  }
  
  @Override
  public Void visitIdentifier(DartIdentifier node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitIntegerLiteral(DartIntegerLiteral node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitDoubleLiteral(DartDoubleLiteral node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitArrayAccess(DartArrayAccess node, HashSet<VariableElement> parameter) {
    accept(node.getKey(), parameter);
    return null;
  }
  
  @Override
  public Void visitParenthesizedExpression(DartParenthesizedExpression node, HashSet<VariableElement> parameter) {
    return null;
  }
  
  @Override
  public Void visitUnqualifiedInvocation(DartUnqualifiedInvocation node, HashSet<VariableElement> parameter) {
    return null;
  }
}