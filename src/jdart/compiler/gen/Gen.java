package jdart.compiler.gen;

import static jdart.compiler.gen.JVMTypes.BIGINT_TYPE;
import static jdart.compiler.gen.JVMTypes.MIXEDINT_TYPE;
import static jdart.compiler.gen.JVMTypes.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.V1_7;
import static jdart.compiler.flow.Liveness.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jdart.compiler.flow.Liveness;
import jdart.compiler.flow.ProfileInfo;
import jdart.compiler.flow.Profiles;
import jdart.compiler.gen.JVMTypes.TypeContext;
import jdart.compiler.type.Types;
import jdart.compiler.visitor.ASTVisitor2;
import jdart.runtime.BigInt;
import jdart.runtime.ControlFlowException;
import jdart.runtime.RT;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.dart.compiler.ast.DartArrayLiteral;
import com.google.dart.compiler.ast.DartBinaryExpression;
import com.google.dart.compiler.ast.DartBlock;
import com.google.dart.compiler.ast.DartBooleanLiteral;
import com.google.dart.compiler.ast.DartBreakStatement;
import com.google.dart.compiler.ast.DartDoWhileStatement;
import com.google.dart.compiler.ast.DartDoubleLiteral;
import com.google.dart.compiler.ast.DartEmptyStatement;
import com.google.dart.compiler.ast.DartExprStmt;
import com.google.dart.compiler.ast.DartExpression;
import com.google.dart.compiler.ast.DartForStatement;
import com.google.dart.compiler.ast.DartFunction;
import com.google.dart.compiler.ast.DartIdentifier;
import com.google.dart.compiler.ast.DartIfStatement;
import com.google.dart.compiler.ast.DartIntegerLiteral;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.ast.DartNode;
import com.google.dart.compiler.ast.DartNullLiteral;
import com.google.dart.compiler.ast.DartParameter;
import com.google.dart.compiler.ast.DartParenthesizedExpression;
import com.google.dart.compiler.ast.DartReturnStatement;
import com.google.dart.compiler.ast.DartStatement;
import com.google.dart.compiler.ast.DartStringLiteral;
import com.google.dart.compiler.ast.DartThisExpression;
import com.google.dart.compiler.ast.DartUnaryExpression;
import com.google.dart.compiler.ast.DartUnqualifiedInvocation;
import com.google.dart.compiler.ast.DartVariable;
import com.google.dart.compiler.ast.DartVariableStatement;
import com.google.dart.compiler.parser.Token;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.LibraryElement;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.NodeElement;
import com.google.dart.compiler.resolver.VariableElement;
import com.google.dart.compiler.type.InterfaceType;

public class Gen extends ASTVisitor2<GenResult, GenEnv> {
  final Type unitType;
  private final Map<DartNode, jdart.compiler.type.Type> typeMap;
  private final Map<DartNode, Liveness> livenessMap;

  Gen(Type unitType, Map<DartNode, jdart.compiler.type.Type> typeMap, Map<DartNode, Liveness> livenessMap) {
    this.unitType = unitType;
    this.typeMap = typeMap;
    this.livenessMap = livenessMap;
  }

  // helper methods

  static String getInternalName(Element element) {
    switch(element.getKind()) {
    case CLASS: {
      ClassElement classElement = (ClassElement)element;
      String name = classElement.getName();
      int index = name.lastIndexOf('/');
      if (index != -1) {
        name = name.substring(index + 1);
      }
      int index2 = name.lastIndexOf('.');
      if (index2 != -1) {
        name = name.substring(0, index2);
      }
      return getInternalName(classElement.getEnclosingElement()) + '/' + name;
    }
    case LIBRARY: {
      LibraryElement libraryElement = ((LibraryElement)element);
      String name = libraryElement.getLibraryUnit().getSelfSourcePath().getText();
      int index2 = name.lastIndexOf('.');
      if (index2 != -1) {
        name = name.substring(0, index2);
      }
      return name;
    }
    default:
      throw new AssertionError();
    }
  }

  private static String getInternalName(com.google.dart.compiler.type.Type type) {
    return getInternalName(type.getElement());
  }

  private static String getInternalClassName(Class<?> clazz) {
    return clazz.getName().replace('.','/');
  }

  private static String getBSMDesc(Class<?>... classes) {
    String desc = "Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;";
    if (classes.length == 0) {
      return '('+desc+")Ljava/lang/invoke/CallSite;";
    }
    StringBuilder builder = new StringBuilder(64);
    builder.append('(').append(desc);
    for(Class<?> clazz: classes) {
      builder.append('L').append(getInternalClassName(clazz)).append(';');
    }
    builder.append(')').append("Ljava/lang/invoke/CallSite;");
    return builder.toString();
  }

  private static void ldcInteger(MethodVisitor mv, int value) {
    if (value >= -1 && value <= 5) {
      mv.visitInsn(ICONST_0 + value);
      return;
    }
    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
      mv.visitIntInsn(BIPUSH, value);
      return;
    }
    if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      mv.visitIntInsn(SIPUSH, value);
      return;
    }
    mv.visitLdcInsn(value);
  }

  private static void generateDefaultReturn(MethodVisitor mv, Type returnType) {
    switch(returnType.getSort()) {
    case Type.BOOLEAN:
    case Type.INT:
      mv.visitInsn(ICONST_0);
      mv.visitInsn(IRETURN);
      return;
    case Type.DOUBLE:
      mv.visitInsn(DCONST_0);
      mv.visitInsn(DRETURN);
      return;
    case Type.OBJECT:
      if (returnType == BIGINT_TYPE) {
        mv.visitInvokeDynamicInsn("ldc", "()"+BIGINT_DESC, LDC_BIGINT_BSM, "0");
      } if (returnType == BOXED_BOOLEAN_TYPE) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
      } else {
        mv.visitInsn(ACONST_NULL);
      }
      mv.visitInsn(ARETURN);
      return;
    case Type.VOID:
      mv.visitInsn(RETURN);
      return;
    default:
      throw new AssertionError("unknown return type "+returnType);
    }
  }


  // --- bootstrap method reference

  static final String RT_CLASS = getInternalClassName(RT.class);
  static final String BIGINT_CLASS = getInternalClassName(BigInt.class);
  static final String BIGINT_DESC = 'L' + BIGINT_CLASS +';';
  static final String CONTROLFLOWEXCEPTION_CLASS = getInternalClassName(ControlFlowException.class);
  static final String CONTROLFLOWEXCEPTION_DESC = 'L' + CONTROLFLOWEXCEPTION_CLASS +';';
  static final String ARITHMETHICEXCEPTION_CLASS = getInternalClassName(ArithmeticException.class);
  private static final Handle LDC_BIGINT_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "ldcBSM", getBSMDesc(String.class));
  static final Handle METHOD_CALL_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "methodCallBSM", getBSMDesc());
  static final Handle FUNCTION_CALL_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "functionCallBSM", getBSMDesc(Class.class));
  static final Handle OPERATOR_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "operatorBSM", getBSMDesc());
  static final Handle OPERATOR_OVERFLOW_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "operatorOverflowBSM", getBSMDesc());
  static final Handle OPERATOR_BIG_BSM = new Handle(H_INVOKESTATIC, RT_CLASS,
      "operatorBigBSM", getBSMDesc());

  // entry point
  public static void genAll(MethodNodeElement mainMethod, Map<DartMethodDefinition, Profiles> methodMap) throws IOException {
    Map<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> unitMap = createUnitMap(methodMap);
    for(Entry<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> unitEntry: unitMap.entrySet()) {
      EnclosingElement unitElement = unitEntry.getKey();
      genUnit(unitElement, unitEntry.getValue(), mainMethod.getEnclosingElement() == unitElement);
    }
  }

  private static Map<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> createUnitMap(Map<DartMethodDefinition, Profiles> methodMap) {
    HashMap<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> map =
        new HashMap<>();
        for(Entry<DartMethodDefinition, Profiles> entry: methodMap.entrySet()) {
          EnclosingElement enclosingElement = entry.getKey().getElement().getEnclosingElement();
          ArrayList<Entry<DartMethodDefinition, Profiles>> list = map.get(enclosingElement);
          if (list == null) {
            list = new ArrayList<>();
            map.put(enclosingElement, list);
          }
          list.add(entry);
        }
        return map;
  }

  private static void genUnit(EnclosingElement enclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>> methodList, boolean mainUnit) throws IOException {
    ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    String unitName = getInternalName(enclosingElement);
    String superName;
    String[] interfaces;
    if (enclosingElement instanceof ClassElement) {
      ClassElement classElement = (ClassElement)enclosingElement;
      superName = getInternalName(classElement.getSupertype());
      List<InterfaceType> interfaceTypes = classElement.getInterfaces();
      interfaces = new String[interfaceTypes.size()];
      for(int i=0; i<interfaces.length; i++) {
        interfaces[i] = getInternalName(interfaceTypes.get(i));
      }
    } else {
      superName = "java/lang/Object";
      interfaces = null;
    }

    Type unitType = Type.getObjectType(unitName);
    cv.visit(V1_7, ACC_PUBLIC|ACC_SUPER, unitName, null, superName, interfaces);
    //cv.visitSource(enclosingElement.getSourceInfo().getSource().getName(), null);

    for(Entry<DartMethodDefinition, Profiles> methodEntry: methodList) {
      genMethod(cv, unitType, methodEntry.getKey(), methodEntry.getValue());
    }

    if (mainUnit) {
      genMain(cv, unitType);
    }

    cv.visitEnd();
    byte[] byteArray = cv.toByteArray();

    CheckClassAdapter.verify(new ClassReader(byteArray), true, new PrintWriter(System.err));

    Path path = Paths.get(unitName+".class");
    Path directory = path.getParent();
    if (directory != null) {
      Files.createDirectories(directory);
    }
    Files.write(path, byteArray);
  }

  private static Map<FunctionDescriptor, ProfileInfo> computeFunctionDescriptorMap(Map<List<jdart.compiler.type.Type>, ProfileInfo> signatureMap) {
    HashMap<FunctionDescriptor, ProfileInfo> map = new HashMap<>();
    for(Entry<List<jdart.compiler.type.Type>, ProfileInfo> entry: signatureMap.entrySet()) {
      ProfileInfo profileInfo = entry.getValue();
      List<jdart.compiler.type.Type> types = entry.getKey();
      FunctionDescriptor signature = new FunctionDescriptor(
          JVMTypes.asJVMType(profileInfo.getReturnType(), TypeContext.RETURN_TYPE),
          JVMTypes.asJVMTypes(types, TypeContext.PARAMETER_TYPE)); 

      ProfileInfo profileInfo2 = map.get(signature);
      if (profileInfo2 == null) {
        map.put(signature, profileInfo);
      } else {
        // same signature for two profiles, may be one is less specific than the other
        if (Types.isCompatible(profileInfo.getReturnType(), profileInfo2.getReturnType()) &&
            Types.isCompatible(types, profileInfo2.getParameterTypes())) {
          // current signature is more less 
          map.put(signature, profileInfo);
        } else {
          if (Types.isCompatible(profileInfo2.getReturnType(), profileInfo.getReturnType()) &&
              Types.isCompatible(profileInfo2.getParameterTypes(), types)) {
            // already existing signature is less specific, keep it
          } else {
            // FIXME, implement but don't use an iterative algorithm
            throw new UnsupportedOperationException("NIY");
          }
        }
      }
    }
    return map;
  }

  private static void genMethod(ClassVisitor cv, Type unitType, DartMethodDefinition methodDefinition, Profiles profiles) {
    Map<List<jdart.compiler.type.Type>, ProfileInfo> signatureMap = profiles.getSignatureMap();
    Map<FunctionDescriptor, ProfileInfo> functionDescripotorMap = computeFunctionDescriptorMap(signatureMap);
    for(Entry<FunctionDescriptor, ProfileInfo> entry: functionDescripotorMap.entrySet()) {
      genMethodWithProfile(cv, unitType, methodDefinition, entry.getKey(), entry.getValue());
    }
  }

  private static void genMethodWithProfile(ClassVisitor cv, Type unitType, DartMethodDefinition methodDefinition, FunctionDescriptor functionDescriptor, ProfileInfo profileInfo) {
    boolean isStatic = methodDefinition.getModifiers().isStatic();
    if (!(methodDefinition.getElement().getEnclosingElement() instanceof ClassElement)) {
      isStatic = true;
    }

    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ((isStatic)? ACC_STATIC: 0),
        methodDefinition.getElement().getName(), functionDescriptor.getDescriptor(), null, null);

    // System.out.println(methodDefinition.getElement().getEnclosingElement().getName());
    // System.out.println(methodDefinition);

    DartFunction function = methodDefinition.getFunction();
    DartBlock body = function.getBody();
    if (body != null) {
      mv.visitCode();

      Map<DartNode, Liveness> livenessMap = profileInfo.getLivenessMap();
      Map<DartNode, jdart.compiler.type.Type> typeMap = profileInfo.getTypeMap();
      Gen gen = new Gen(unitType, typeMap, livenessMap);
      MethodRecorder methodRecorder = new MethodRecorder();
      GenEnv env = new GenEnv(mv, methodRecorder, functionDescriptor.getReturnType(), (isStatic)? 0: 1);

      List<DartParameter> parameters = function.getParameters();
      List<Type> parameterTypes = functionDescriptor.getParameterTypes();
      for(int i=0; i<parameters.size(); i++) {
        DartParameter parameter = parameters.get(i);
        Var var = env.newVar(parameterTypes.get(i));
        env.registerVar(parameter.getElement(), var);
      }
      gen.accept(body, env);

      Liveness liveness = livenessMap.get(body);
      if (liveness == ALIVE) {
        generateDefaultReturn(mv, functionDescriptor.getReturnType());
      }

      // generate exceptional paths
      methodRecorder.replay(mv);

      try {
        mv.visitMaxs(0, 0);  
      } catch (Exception e) {
        // TODO MODIFIED
      }
    }

    mv.visitEnd();
  }

  private static void genMain(ClassWriter cv, Type unitType) {
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC|ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    mv.visitMethodInsn(INVOKESTATIC, unitType.getInternalName(), "main", "()V");
    mv.visitInsn(RETURN);
    mv.visitEnd();
  }

  // --- statements

  @Override
  public GenResult visitBlock(DartBlock node, GenEnv env) {
    for (DartStatement statement : node.getStatements()) {
      accept(statement, env);
    }
    return null;
  }

  @Override
  public GenResult visitVariableStatement(DartVariableStatement node, GenEnv env) {
    for (DartVariable variable : node.getVariables()) {
      accept(variable, env);
    }
    return null;
  }

  interface SplitPathGenerator {
    public void genDefaultPath(Type type, GenEnv env);
    public void genIntPath(GenResult result, GenEnv env);
    public void genBigIntPath(GenResult result, GenEnv env);
  }

  private void genSplitPathExpr(DartExpression expr, Type type, GenEnv env, SplitPathGenerator splitPathGenerator) {
    MethodVisitor mv = env.getMethodVisitor();
    if (type != MIXEDINT_TYPE) {
      accept(expr, env);
      splitPathGenerator.genDefaultPath(type, env);
      return;
    } 

    // split-path
    MethodRecorder recorder = new MethodRecorder();
    GenResult result = accept(expr, env.newSplitPathEnv(recorder, 0));

    splitPathGenerator.genIntPath(result, env);

    Set<Var> dependencies = result.getDependencies();
    Label bigPathLabel = new Label();
    for(Var dependency: dependencies) {
      mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
      mv.visitJumpInsn(IFNONNULL, bigPathLabel);
    }
    recorder.replay(mv);
    Label endLabel = new Label();
    mv.visitJumpInsn(GOTO, endLabel);
    mv.visitLabel(bigPathLabel);

    if (dependencies.size() > 1) { // box if needed
      for(Var dependency: dependencies) {
        mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
        Label boxLabel = new Label();
        mv.visitJumpInsn(IFNONNULL, boxLabel);
        mv.visitVarInsn(ILOAD, dependency.getSlot());
        mv.visitMethodInsn(INVOKESTATIC, BIGINT_CLASS, "valueOf", "(I)"+BIGINT_DESC);
        mv.visitVarInsn(ASTORE, 1 + dependency.getSlot());
        mv.visitLabel(boxLabel);
      }
    }

    GenResult result2 = accept(expr, env.newSplitPathEnv(mv, 1));
    splitPathGenerator.genBigIntPath(result2, env);

    mv.visitLabel(endLabel);
  }

  /*
  interface SplitPathGenerator2 {
    public void genDefaultPath(Type type1, Type type2, GenEnv env);
    public void genIntPath(GenResult result, GenResult result2, GenEnv env);
    public void genAsymetricPath(GenResult result, Type type1, GenResult result2, Type type2, GenEnv env);
    public void genBigIntPath(GenResult result, GenResult result2, GenEnv env);
  }

  private void genSplitPathTwoExprs(DartExpression expr1, Type type1, DartExpression expr2, Type type2, GenEnv env, SplitPathGenerator2 splitPathGenerator) {
    MethodVisitor mv = env.getMethodVisitor();
    if (type1 != MIXEDINT_TYPE && type2 != MIXEDINT_TYPE) {
      accept(expr1, env);
      accept(expr2, env);
      splitPathGenerator.genDefaultPath(type1, type2, env);
    } else { // split-path

      MethodRecorder recorder = new MethodRecorder();
      GenEnv subEnv = env.newSplitPathEnv(recorder, 0);
      GenResult result = accept(expr1, subEnv);
      GenResult result2 = accept(expr2, subEnv);
      if (type1 == MIXEDINT_TYPE && type2 == MIXEDINT_TYPE) {

        System.out.println("expr1 "+expr1 +" "+result);
        System.out.println("expr2 "+expr2 +" "+result2);

        splitPathGenerator.genIntPath(result, result2, env);
      } else {
        splitPathGenerator.genAsymetricPath(result, type1, result2, type2, subEnv);
      }

      HashSet<Var> dependencies = new HashSet<>();
      if (result != null) {
        dependencies.addAll(result.getDependencies());
      }
      if (result2 != null) {
        dependencies.addAll(result2.getDependencies());
      }
      Label bigPathLabel = new Label();
      for(Var dependency: dependencies) {
        mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
        mv.visitJumpInsn(IFNONNULL, bigPathLabel);
      }
      recorder.replay(mv);
      Label endLabel = new Label();
      mv.visitJumpInsn(GOTO, endLabel);
      mv.visitLabel(bigPathLabel);

      if (dependencies.size() > 1) { // box if needed
        for(Var dependency: dependencies) {
          mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
          Label boxLabel = new Label();
          mv.visitJumpInsn(IFNONNULL, boxLabel);
          mv.visitVarInsn(ILOAD, dependency.getSlot());
          mv.visitMethodInsn(INVOKESTATIC, BIGINT_CLASS, "valueOf", "(I)"+BIGINT_DESC);
          mv.visitVarInsn(ASTORE, 1 + dependency.getSlot());
          mv.visitLabel(boxLabel);
        }
      }

      GenEnv subEnv2 = env.newSplitPathEnv(mv, 1);
      result = accept(expr1, subEnv2);
      result2 = accept(expr1, subEnv2);
      if (type1 == MIXEDINT_TYPE && type2 == MIXEDINT_TYPE) {
        splitPathGenerator.genBigIntPath(result, result2, env);
      } else {
        splitPathGenerator.genAsymetricPath(result, type1, result2, type2, subEnv);
      }

      mv.visitLabel(endLabel);
    }
  }*/

  @Override
  public GenResult visitVariable(DartVariable node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    VariableElement element = node.getElement();

    DartExpression value = node.getValue();
    if (value == null) {
      mv.visitInsn(ACONST_NULL);
      Var var = env.newVar(OBJECT_TYPE);
      mv.visitVarInsn(ASTORE, var.getSlot());
      env.registerVar(element, var);
      return null;
    }

    Type type = asJVMType(typeMap.get(value), TypeContext.VAR_TYPE);
    final Var var = env.newVar(type);
    genSplitPathExpr(value, type, env, new SplitPathGenerator() {
      @Override
      public void genDefaultPath(Type type, GenEnv env) {
        MethodVisitor mv = env.getMethodVisitor();
        mv.visitVarInsn(type.getOpcode(ISTORE), var.getSlot());
      }
      @Override
      public void genIntPath(GenResult result, GenEnv env) {
        MethodVisitor mv = env.getMethodVisitor();
        mv.visitVarInsn(ILOAD, result.getVarSlot());
        mv.visitVarInsn(ISTORE, var.getSlot());
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 1 + var.getSlot());
      }
      @Override
      public void genBigIntPath(GenResult result, GenEnv env) {
        MethodVisitor mv = env.getMethodVisitor();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, var.getSlot());
        mv.visitVarInsn(ALOAD, result.getVarSlot());
        mv.visitVarInsn(ASTORE, 1 + var.getSlot());
      }
    });

    env.registerVar(element, var);
    return null;
  }

  @Override
  public GenResult visitExprStmt(DartExprStmt node, GenEnv env) {
    DartExpression expression = node.getExpression();
    if (expression != null) {
      Type type = asJVMType(typeMap.get(expression), TypeContext.VAR_TYPE);
      accept(expression, env);
      if (type != Type.VOID_TYPE && type != MIXEDINT_TYPE) { // split-path doesn't store value on stack
        env.getMethodVisitor().visitInsn((type.getSize() == 1)? POP: POP2);
      }
    }
    return null;
  }

  @Override
  public GenResult visitEmptyStatement(DartEmptyStatement node, GenEnv env) {
    return null;
  }

  @Override
  public GenResult visitIfStatement(DartIfStatement node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    Label elseLabel = new Label();
    Label endLabel = new Label();
    accept(node.getCondition(), env.newIf(new IfBranches(true, elseLabel, endLabel)));

    DartStatement thenStatement = node.getThenStatement();
    accept(thenStatement, env);

    DartStatement elseStatement = node.getElseStatement();
    if (elseStatement != null && livenessMap.get(thenStatement) == ALIVE) {
      mv.visitJumpInsn(GOTO, endLabel);
    }

    mv.visitLabel(elseLabel);
    if (elseStatement != null) {
      accept(elseStatement, env);
      mv.visitLabel(endLabel);
    }
    return null;
  }

  @Override
  public GenResult visitForStatement(DartForStatement node, GenEnv env) {
    // TODO MODIFIED
    MethodVisitor mv = env.getMethodVisitor();
    Label conditionLabel = new Label();
    Label loopBodyLabel = new Label();
    Label endLabel = new Label();

    if (node.getInit() != null) {
      accept(node.getInit(), env);
    }
    mv.visitJumpInsn(GOTO, conditionLabel);

    // loop body
    mv.visitLabel(loopBodyLabel);
    accept(node.getBody(), env);
    if (node.getIncrement() != null) {
      accept(node.getIncrement(), env);
      mv.visitInsn(POP);
    }

    // loop condition
    mv.visitLabel(conditionLabel);
    if (node.getCondition() != null) {
      accept(node.getCondition(), env.newIf(new IfBranches(false, loopBodyLabel, null)));
    } else {
      mv.visitJumpInsn(GOTO, loopBodyLabel);
    }
    mv.visitLabel(endLabel);

    return null;
  }

  @Override
  public GenResult visitDoWhileStatement(DartDoWhileStatement node, GenEnv env) {
    // TODO MODIFIED

    MethodVisitor mv = env.getMethodVisitor();
    Label loopBodyLabel = new Label();
    Label endLabel = new Label();
    env.newLoopLabel(endLabel);

    // loop body
    mv.visitLabel(loopBodyLabel);
    accept(node.getBody(), env);

    // loop condition
    accept(node.getCondition(), env.newIf(new IfBranches(false, loopBodyLabel, null)));
    mv.visitLabel(endLabel);

    return null;
  }

  @Override
  public GenResult visitBreakStatement(DartBreakStatement node, GenEnv env) {
    //TODO MODIFIED

    MethodVisitor mv = env.getMethodVisitor();
    mv.visitJumpInsn(GOTO, env.getLoopLabel());

    return null;
  }

  @Override
  public GenResult visitReturnStatement(DartReturnStatement node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    Type returnType = env.getReturnType();
    DartExpression value = node.getValue();
    if (value == null) {  
      generateDefaultReturn(mv, returnType);
      return null;
    }

    Type exprType = asJVMType(typeMap.get(value), TypeContext.VAR_TYPE);
    if (exprType != MIXEDINT_TYPE) {
      accept(value, env);
      mv.visitInsn(returnType.getOpcode(IRETURN));
      return null;
    }

    GenResult result = accept(value, env);

    Label bigPathLabel = new Label();
    mv.visitVarInsn(ALOAD, 1 + result.getVarSlot());
    mv.visitJumpInsn(IFNONNULL, bigPathLabel);

    mv.visitVarInsn(ILOAD, result.getVarSlot());
    mv.visitInsn(IRETURN);

    mv.visitLabel(bigPathLabel);
    mv.visitVarInsn(ALOAD, 1 + result.getVarSlot());
    mv.visitMethodInsn(INVOKESTATIC, CONTROLFLOWEXCEPTION_CLASS, "valueOf",
        '('+BIGINT_DESC+')'+CONTROLFLOWEXCEPTION_DESC);
    mv.visitInsn(ATHROW);
    return null;
  }

  // --- expressions

  @Override
  public GenResult visitIdentifier(DartIdentifier node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    NodeElement element = node.getElement();
    switch (element.getKind()) {
    case VARIABLE:
    case PARAMETER:
      Var var = env.getVar((VariableElement) element);
      Type type = var.getType();
      int slot = var.getSlot();
      if (type == MIXEDINT_TYPE) {
        int mixedIntShift = env.getMixedIntShift();
        type = (mixedIntShift == 0)? Type.INT_TYPE: BIGINT_TYPE;
        slot += mixedIntShift;
      }
      mv.visitVarInsn(type.getOpcode(ILOAD), slot);
      return (type == MIXEDINT_TYPE)? new GenResult(slot, var): null;

    case FIELD:
      //FIXME
    case METHOD:
      //FIXME
    default:
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public GenResult visitThisExpression(DartThisExpression node, GenEnv env) {
    env.getMethodVisitor().visitVarInsn(ALOAD, 0);
    return null;
  }


  interface BinaryGenerator {
    void genBinaryNoOverFlow(Type returnType, Type type1, Type type2, GenEnv env);

    void genRawBinaryWithOverFlow(Type returnType, Type type1, Type type2, GenEnv env);

    void genBinaryOverFlowed(Type returnType, Type type1, Type type2, GenEnv env);

    void genBinaryBig(Type returnType, List<Type> types, GenEnv env);
  }

  private GenResult genBinary(DartExpression expr1, DartExpression expr2, Type returnType, GenEnv env, BinaryGenerator binaryGenerator) {
    MethodVisitor mv = env.getMethodVisitor();

    Type type1 = asJVMType(typeMap.get(expr1), TypeContext.VAR_TYPE);
    Type type2 = asJVMType(typeMap.get(expr2), TypeContext.VAR_TYPE);

    boolean needToSpill = returnType == MIXEDINT_TYPE || type1 == MIXEDINT_TYPE || type2 == MIXEDINT_TYPE;

    if (!needToSpill) {
      accept(expr1, env);
      accept(expr2, env);

      binaryGenerator.genBinaryNoOverFlow(returnType, type1, type2, env);
      return null;
    }

    Label try_start = new Label();
    Label try_end = new Label();
    Label handler = new Label();

    int resultVarSlot;
    GenResult genResult;
    if (returnType == MIXEDINT_TYPE) {
      mv.visitTryCatchBlock(try_start, try_end, handler, ARITHMETHICEXCEPTION_CLASS); 
      Var resultVar = env.newVar(MIXEDINT_TYPE);
      resultVarSlot = resultVar.getSlot();
      genResult = new GenResult(resultVarSlot, resultVar);
    } else { // never used, just here to please the compiler
      resultVarSlot = 0;
      genResult = null;
    }

    // spill & gather dependencies
    HashSet<Var> dependencies = new HashSet<>();
    int slot1;
    GenResult result1 = accept(expr1, env);
    if (result1 == null) {
      Var var = env.newVar(type1);
      slot1 = var.getSlot();
      mv.visitVarInsn(type1.getOpcode(ISTORE), slot1);
    } else {
      slot1 = result1.getVarSlot();
      dependencies.addAll(result1.getDependencies());
    }
    int slot2;
    GenResult result2 = accept(expr2, env);
    if (result2 == null) {
      Var var = env.newVar(type2);
      slot2 = var.getSlot();
      mv.visitVarInsn(type1.getOpcode(ISTORE), slot2);
    } else {
      slot2 = result2.getVarSlot();
      dependencies.addAll(result2.getDependencies());
    }

    // check dependency vars
    Label bigPathLabel = new Label();
    for(Var dependency: dependencies) {
      mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
      mv.visitJumpInsn(IFNONNULL, bigPathLabel);
    }

    Type smallType1 = type1, smallType2 = type2,
        smallReturnType = returnType;
    if (type1 == MIXEDINT_TYPE) {
      smallType1 = Type.INT_TYPE;
    }
    if (type2 == MIXEDINT_TYPE) {
      smallType2 = Type.INT_TYPE;
    }
    if (returnType == MIXEDINT_TYPE) {
      smallReturnType = Type.INT_TYPE;
    }

    // load spilled vars on stack
    mv.visitVarInsn(smallType1.getOpcode(ILOAD), slot1);
    mv.visitVarInsn(smallType2.getOpcode(ILOAD), slot2);

    mv.visitLabel(try_start);
    binaryGenerator.genRawBinaryWithOverFlow(smallReturnType, smallType1, smallType2, env);
    mv.visitLabel(try_end);

    if (returnType == MIXEDINT_TYPE) {
      mv.visitVarInsn(ISTORE, resultVarSlot);
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, 1 + resultVarSlot);
    }

    Label endLabel = new Label();
    mv.visitJumpInsn(GOTO, endLabel);

    // exception handler
    MethodVisitor sideMV = env.getSideMethodVisitor();
    if (returnType == MIXEDINT_TYPE) { 
      sideMV.visitLabel(handler);
      sideMV.visitInsn(POP);

      // reload spilled values
      sideMV.visitVarInsn(smallType1.getOpcode(ILOAD), slot1);
      sideMV.visitVarInsn(smallType2.getOpcode(ILOAD), slot2);

      binaryGenerator.genBinaryOverFlowed(BIGINT_TYPE, smallType1, smallType2, env.newSplitPathEnv(sideMV, 0));  //FIXME
      sideMV.visitVarInsn(ASTORE, 1 + resultVarSlot);
      sideMV.visitInsn(ICONST_0);
      sideMV.visitVarInsn(ISTORE, resultVarSlot);
      sideMV.visitJumpInsn(GOTO, endLabel);
    }

    mv.visitLabel(bigPathLabel);

    /*if (dependencies.size() > 1) { // box if needed
      for(Var dependency: dependencies) {
        mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
        Label boxLabel = new Label();
        mv.visitJumpInsn(IFNONNULL, boxLabel);
        mv.visitVarInsn(ILOAD, dependency.getSlot());
        mv.visitMethodInsn(INVOKESTATIC, BIGINT_CLASS, "valueOf", "(I)"+BIGINT_DESC);
        mv.visitVarInsn(ASTORE, 1 + dependency.getSlot());
        mv.visitLabel(boxLabel);
      }
    }*/

    // load all spilled variables
    ArrayList<Type> bigTypes = new ArrayList<>(4);
    if (type1 == MIXEDINT_TYPE) {
      bigTypes.add(INT_TYPE);
      mv.visitVarInsn(ILOAD, slot1);
      bigTypes.add(BIGINT_TYPE);
      mv.visitVarInsn(ALOAD, 1 + slot1);
    } else {
      mv.visitVarInsn(type1.getOpcode(ILOAD), slot1);
      bigTypes.add(type1);
    }
    if (type2 == MIXEDINT_TYPE) {
      bigTypes.add(INT_TYPE);
      mv.visitVarInsn(ILOAD, slot2);
      bigTypes.add(BIGINT_TYPE);
      mv.visitVarInsn(ALOAD, 1 + slot2);
    } else {
      mv.visitVarInsn(type2.getOpcode(ILOAD), slot2);
      bigTypes.add(type2);
    }

    Type bigReturnType = (returnType == MIXEDINT_TYPE)? BIGINT_TYPE: returnType;
    binaryGenerator.genBinaryBig(bigReturnType, bigTypes, env);

    if (returnType == MIXEDINT_TYPE) {
      mv.visitVarInsn(ASTORE, 1 + resultVarSlot);
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, resultVarSlot);
    }

    mv.visitLabel(endLabel);
    return genResult;
  }

  @Override
  public GenResult visitBinaryExpression(DartBinaryExpression node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    GenEnv subEnv = env.newIf(null);
    final DartExpression expr1 = node.getArg1();
    final DartExpression expr2 = node.getArg2();

    Type returnType = asJVMType(typeMap.get(node), TypeContext.VAR_TYPE);

    final Token operator = node.getOperator();
    IfBranches ifBranches = env.getIfBranches();
    if (ifBranches != null) {
      int opcode;
      boolean inversed = ifBranches.isInversed();

      accept(expr1, subEnv);
      accept(expr2, subEnv);

      //FIXME, check argument types
      
      switch(operator) {
      case ASSIGN:
        // TODO MODIFIED
        Type type2 = asJVMType(typeMap.get(expr2), TypeContext.VAR_TYPE);
        
        switch (type2.getSort()) {
        case Type.OBJECT:
          mv.visitVarInsn(ASTORE, subEnv.getVar((VariableElement) expr1.getElement()).getSlot());
          return null;
        case Type.INT:
          mv.visitVarInsn(ISTORE, subEnv.getVar((VariableElement) expr1.getElement()).getSlot());
          return null;
        default:
          throw new UnsupportedOperationException("Assign: " + type2.getSort());
        }
      case LT:
        opcode = (inversed)? IF_ICMPGE: IF_ICMPLT;
        mv.visitJumpInsn(opcode, ifBranches.getElseLabel());
        return null;
      case GT:
        //TODO MODIFIED
        opcode = (inversed)? IF_ICMPGT: IF_ICMPLE;
        mv.visitJumpInsn(opcode, ifBranches.getElseLabel());
        return null;
      case EQ:
        //TODO MODIFIED
        opcode = (inversed)? IF_ICMPNE: IF_ICMPEQ;
        mv.visitJumpInsn(opcode, ifBranches.getElseLabel());
        return null;
      case NE:
        //TODO MODIFIED
        opcode = (inversed)? IF_ICMPEQ: IF_ICMPNE;
        mv.visitJumpInsn(opcode, ifBranches.getElseLabel());
        return null;
      default:
        throw new UnsupportedOperationException("operator " + operator + " (" + operator.name() + ")");
      }
    }

    switch(operator) {
    case ASSIGN:
      // TODO MODIFIED
      accept(expr1, subEnv);
      accept(expr2, subEnv);

      Type type2 = asJVMType(typeMap.get(expr2), TypeContext.VAR_TYPE);
      
      switch (type2.getSort()) {
      case Type.INT:
      case Type.LONG:
        mv.visitVarInsn(ISTORE, subEnv.getVar((VariableElement) expr1.getElement()).getSlot());
        return null;
      case Type.DOUBLE:
        mv.visitVarInsn(DSTORE, subEnv.getVar((VariableElement) expr1.getElement()).getSlot());
        return null;
      case Type.OBJECT:
        mv.visitVarInsn(ASTORE, subEnv.getVar((VariableElement) expr1.getElement()).getSlot());
        return null;
      default:
        throw new UnsupportedOperationException("Assign: " + type2.getSort());
      }

    case SUB:
    case ADD:
    case MUL:
    case DIV:
    case SHL:
    case BIT_OR:
    case BIT_XOR:
      return genBinary(expr1, expr2, returnType, subEnv, new BinaryGenerator() {
        @Override
        public void genBinaryNoOverFlow(Type returnType, Type type1, Type type2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          int sort1 = type1.getSort();
          int sort2 = type2.getSort();
          if (sort1 == Type.INT && sort2 == Type.INT) { //FIXME add support of doubles
            switch(operator) {
            case ADD:
              mv.visitInsn(IADD);
              return;
            case SUB:
              mv.visitInsn(ISUB);
              return;
            case MUL:
              // TODO MODIFIED
              mv.visitInsn(IMUL);
              return;
            case SHL:
              // TODO MODIFIED
              mv.visitInsn(ISHL);
              return;
            case BIT_OR:
              // TODO MODIFIED
              mv.visitInsn(IOR);
              return;
            case BIT_XOR:
              //TODO MODIFIED
              mv.visitInsn(IXOR);
              return;
            default:
              throw new UnsupportedOperationException("binary no overflow "+returnType+" "+type1+" "+type2);
            }
          }

          //TODO MODIFIED
          if (sort1 == Type.DOUBLE && sort2 == Type.DOUBLE) {
            switch(operator) {
            case ADD:
              mv.visitInsn(DADD);
              return;
            case SUB:
              mv.visitInsn(DSUB);
              return;
            case MUL:
              mv.visitInsn(DMUL);
              return;
            case DIV:
              mv.visitInsn(DDIV);
              return;
            default:
              throw new UnsupportedOperationException("binary no overflow "+returnType+" "+type1+" "+type2);
            }
          }
          
          if (sort1 == Type.DOUBLE && sort2 == Type.INT) {
            switch(operator) {
            case MUL:
              mv.visitInsn(I2D);
              mv.visitInsn(DMUL);
              return;
            case DIV:
              mv.visitInsn(I2D);
              mv.visitInsn(DDIV);
              return;
            default:
              throw new UnsupportedOperationException("binary no overflow " + operator + " " +returnType+" "+type1+" "+type2);
            }
          }
          
          if (sort1 == Type.INT && sort2 == Type.DOUBLE) {
            switch(operator) {
            
            default:
              throw new UnsupportedOperationException("binary no overflow " + operator + " " +returnType+" "+type1+" "+type2);
            }
          }
          
          if (sort1 == Type.OBJECT && sort2 == Type.INT) {
            //TODO MODIFIED
            switch(operator) {
            case SHL:
              mv.visitMethodInsn(INVOKEDYNAMIC, RT_CLASS, "shilftLeft", "(Ljdart/runtime/BigInt;I)Ljdart/runtime/BigInt;");
              return;
            default:
              throw new UnsupportedOperationException("binary no overflow " + operator + " " +returnType+" "+type1+" "+type2);
            }
          }
          
          if (sort1 == Type.OBJECT && sort2 == Type.OBJECT) {
            //TODO MODIFIED
            switch(operator) {
            
            default:
              throw new UnsupportedOperationException("binary no overflow " + operator + " " +returnType+" "+type1+" "+type2);
            }
          }

          throw new UnsupportedOperationException("binary no overflow "+returnType+" "+type1+" "+type2);
        }

        @Override
        public void genRawBinaryWithOverFlow(Type returnType, Type type1, Type type2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          if (type1 == Type.INT_TYPE && type2 == Type.INT_TYPE) {
            switch(operator) {
            case ADD:
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "addExact", "(II)I");
              return;
            case SUB:
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "subtractExact", "(II)I");
              return;
            case MUL:
              // TODO MODIFIED
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "multiplyExact", "(II)I");
              return;
            case BIT_OR:
              // TODO MODIFIED
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "bitOr", "(II)I");
              return;
            case SHL:
              //TODO MODIFIED
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "shilftLeft", "(II)I");
              return;
            case BIT_XOR:
              // TODO MODIFIED
              mv.visitMethodInsn(INVOKESTATIC, RT_CLASS, "bitXor", "(II)I");
              return;
            default:
              throw new UnsupportedOperationException("binary with overflow " + operator + " " +returnType+" "+type1+" "+type2);
            }
          }
          throw new UnsupportedOperationException("binary with overflow "+returnType+" "+type1+" "+type2);
        }

        @Override
        public void genBinaryOverFlowed(Type returnType, Type type1, Type type2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          mv.visitInvokeDynamicInsn(operator.name(),
              Type.getMethodDescriptor(returnType, type1, type2),
              OPERATOR_OVERFLOW_BSM);
          return;
        }

        @Override
        public void genBinaryBig(Type returnType, List<Type> types, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          mv.visitInvokeDynamicInsn(operator.name(),
              Type.getMethodDescriptor(returnType, types.toArray(new Type[types.size()])),
              OPERATOR_BIG_BSM);
        }

        /*
        @Override
        public void genIntPath(GenResult result, GenResult result2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          mv.visitVarInsn(ILOAD, result.getVarSlot());
          mv.visitVarInsn(ILOAD, result2.getVarSlot());
          switch(operator) {
          case ADD:
            mv.visitInsn(IADD);
            return;
          case SUB:
            mv.visitInsn(ISUB);
            return;
          default:
            throw new UnsupportedOperationException("operator "+operator);
          }
        }

        @Override
        public void genBigIntPath(GenResult result, GenResult result2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          mv.visitVarInsn(ALOAD, 1 + result.getVarSlot());
          mv.visitVarInsn(ALOAD, 1 + result2.getVarSlot());
          String opName;
          switch(operator) {
          case ADD:
            opName = "add";
            break;
          case SUB:
            opName = "sub";
            break;
          default:
            throw new UnsupportedOperationException("operator "+operator);
          }
          mv.visitMethodInsn(INVOKEVIRTUAL, BIGINT_CLASS, opName, '('+BIGINT_DESC+')'+BIGINT_DESC);
        }

        @Override
        public void genDefaultPath(Type type1, Type type2, GenEnv env) {
          MethodVisitor mv = env.getMethodVisitor();
          if (type1 == Type.INT_TYPE && type2 == Type.INT_TYPE) {
            switch(operator) {
            case ADD:
              mv.visitInsn(IADD);
              return;
            case SUB:
              mv.visitInsn(ISUB);
              return;
            default:
              throw new UnsupportedOperationException("operator "+operator);
            }
          }
          throw new UnsupportedOperationException("operator "+operator+" "+type1+" "+type2);
        }

        @Override
        public void genAsymetricPath(GenResult result, Type type1, GenResult result2, Type type2, GenEnv env) {
          throw new UnsupportedOperationException("operator "+operator+" "+type1+" "+type2);
        }

         */
      });

    default:
      // so it's a boolean operator
    }


    // push true or false
    Label elseLabel = new Label();
    Label endLabel = new Label();
    ifBranches = new IfBranches(true, elseLabel, endLabel);
    visitBinaryExpression(node, subEnv.newIf(ifBranches));
    mv.visitInsn(ICONST_1);
    mv.visitJumpInsn(GOTO, endLabel);
    mv.visitLabel(elseLabel);
    mv.visitInsn(ICONST_0);
    mv.visitLabel(endLabel);
    return null;
  }

  @Override
  public GenResult visitUnaryExpression(DartUnaryExpression node, GenEnv env) {
    // TODO MODIFIED
    MethodVisitor mv = env.getMethodVisitor();
    accept(node.getArg(), env);
    Token operator = node.getOperator();
    int varSlot = env.getVar((VariableElement) node.getArg().getElement()).getSlot();
    switch (operator) {
    case INC:
      mv.visitIincInsn(varSlot, 1);
      return null;
    case DEC:
      mv.visitIincInsn(varSlot, -1);
      return null;
    default:
      throw new UnsupportedOperationException("operator " + operator + " (" + operator.name() + ")");
    }
  }

  @Override
  public GenResult visitParenthesizedExpression(DartParenthesizedExpression node, GenEnv parameter) {
    return accept(node.getExpression(), parameter);
  }

  interface MethodGenerator {
    void genMethodCall(Type returnType, Type[] parameterType, GenEnv env);
  }

  private GenResult genMethodCall(List<DartExpression> exprs, Type declaredReturnType, Type returnType, GenEnv env, MethodGenerator methodGenerator) {
    MethodVisitor mv = env.getMethodVisitor();

    Type[] parameterTypes = new Type[exprs.size()];
    Type[] bigTypes = null;  // lazy allocated
    for(int i=0; i<exprs.size(); i++) {
      DartExpression expr = exprs.get(i);
      Type type = asJVMType(typeMap.get(expr), TypeContext.VAR_TYPE);
      if (type == MIXEDINT_TYPE) {
        if (bigTypes == null) {
          bigTypes = new Type[exprs.size()];
          System.arraycopy(parameterTypes, 0, bigTypes, 0, i);
        }
        parameterTypes[i] = Type.INT_TYPE;
        bigTypes[i] = BIGINT_TYPE;
      } else {
        parameterTypes[i] =  type;
        if (bigTypes != null) {
          bigTypes[i] = type;
        }
      }
    }

    Label try_start = new Label();
    Label try_end = new Label();
    Label handler = new Label();

    int resultVarSlot;
    GenResult genResult;
    if (declaredReturnType == MIXEDINT_TYPE) {
      mv.visitTryCatchBlock(try_start, try_end, handler, CONTROLFLOWEXCEPTION_CLASS); 
      Var resultVar = env.newVar(MIXEDINT_TYPE);
      resultVarSlot = resultVar.getSlot();
      genResult = new GenResult(resultVarSlot, resultVar);
    } else { // never used, just here to please the compiler
      resultVarSlot = 0;
      genResult = null;
    }

    if (bigTypes == null) { // easy case, no mixed int types involved as parameter
      for(DartExpression expr: exprs) {
        accept(expr, env);
      }

      mv.visitLabel(try_start);
      methodGenerator.genMethodCall(returnType, parameterTypes, env);
      mv.visitLabel(try_end);

      if (declaredReturnType != MIXEDINT_TYPE) {
        return null;
      }

      mv.visitVarInsn(ISTORE, resultVarSlot);
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, 1 + resultVarSlot);

      // exception handler
      Label endLabel = new Label();
      MethodVisitor sideMV = env.getSideMethodVisitor();
      sideMV.visitLabel(handler);
      sideMV.visitFieldInsn(GETFIELD, CONTROLFLOWEXCEPTION_CLASS, "value", BIGINT_DESC);
      sideMV.visitVarInsn(ASTORE, 1 + resultVarSlot);
      sideMV.visitInsn(ICONST_0);
      sideMV.visitVarInsn(ISTORE, resultVarSlot);
      sideMV.visitJumpInsn(GOTO, endLabel);

      mv.visitLabel(endLabel);
      return genResult;
    }

    // so we need to spill all values into local variables
    HashSet<Var> dependencies = new HashSet<>();

    int[] slots = new int[exprs.size()];
    for(int i=0; i<exprs.size(); i++) {
      DartExpression expr = exprs.get(i);
      GenResult result = accept(expr, env);
      int slot;
      if (result == null) {
        Type type = parameterTypes[i];
        Var var = env.newVar(type);
        slot = var.getSlot();
        mv.visitVarInsn(type.getOpcode(ISTORE), slot);
      } else {
        slot = result.getVarSlot();
        dependencies.addAll(result.getDependencies());
      }
      slots[i] = slot;
    }

    // declare a second try/catch blocks
    Label try_start2 = new Label();
    Label try_end2 = new Label();
    if (declaredReturnType == MIXEDINT_TYPE) {
      mv.visitTryCatchBlock(try_start2, try_end2, handler, CONTROLFLOWEXCEPTION_CLASS);
    }

    Label bigPathLabel = new Label();
    for(Var dependency: dependencies) {
      mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
      mv.visitJumpInsn(IFNONNULL, bigPathLabel);
    }

    for(int i=0; i<exprs.size(); i++) {
      mv.visitVarInsn(parameterTypes[i].getOpcode(ILOAD), slots[i]);
    }
    mv.visitLabel(try_start);
    methodGenerator.genMethodCall(returnType, parameterTypes, env);
    mv.visitLabel(try_start);

    Label endOfCallLabel = new Label();
    mv.visitJumpInsn(GOTO, endOfCallLabel);

    Label endLabel = new Label();
    MethodVisitor sideMV = env.getSideMethodVisitor();
    if (declaredReturnType == MIXEDINT_TYPE) { // exception handler
      sideMV.visitLabel(handler);

      sideMV.visitFieldInsn(GETFIELD, CONTROLFLOWEXCEPTION_CLASS, "value", BIGINT_DESC);
      sideMV.visitVarInsn(ASTORE, 1 + resultVarSlot);
      sideMV.visitInsn(ICONST_0);
      sideMV.visitVarInsn(ISTORE, resultVarSlot);
      sideMV.visitJumpInsn(GOTO, endLabel);
    }

    mv.visitLabel(bigPathLabel);

    if (dependencies.size() > 1) { // box if needed
      for(Var dependency: dependencies) {
        mv.visitVarInsn(ALOAD, 1 + dependency.getSlot());
        Label boxLabel = new Label();
        mv.visitJumpInsn(IFNONNULL, boxLabel);
        mv.visitVarInsn(ILOAD, dependency.getSlot());
        mv.visitMethodInsn(INVOKESTATIC, BIGINT_CLASS, "valueOf", "(I)"+BIGINT_DESC);
        mv.visitVarInsn(ASTORE, 1 + dependency.getSlot());
        mv.visitLabel(boxLabel);
      }
    }

    for(int i=0; i<exprs.size(); i++) {
      DartExpression expr = exprs.get(i);
      int slot = slots[i];
      Type type = asJVMType(typeMap.get(expr), TypeContext.VAR_TYPE);
      if (type == MIXEDINT_TYPE) {
        slot++;
      }
      mv.visitVarInsn(bigTypes[i].getOpcode(ILOAD), slot);
    }

    mv.visitLabel(try_start2);
    methodGenerator.genMethodCall(returnType, bigTypes, env);
    mv.visitLabel(try_end2);

    mv.visitLabel(endOfCallLabel);

    if (declaredReturnType == MIXEDINT_TYPE) {
      mv.visitVarInsn(ISTORE, resultVarSlot);
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, 1 + resultVarSlot);
    }

    mv.visitLabel(endLabel);
    return genResult;
  }

  @Override
  public GenResult visitUnqualifiedInvocation(final DartUnqualifiedInvocation node, GenEnv env) {
    jdart.compiler.type.Type flowReturnType = typeMap.get(node);
    Type declaredReturnType = asJVMType(flowReturnType, TypeContext.VAR_TYPE);
    Type returnType = asJVMType(flowReturnType, TypeContext.RETURN_TYPE);
    return genMethodCall(node.getArguments(), declaredReturnType, returnType, env, new MethodGenerator() {
      @Override
      public void genMethodCall(Type returnType, Type[] parameterType, GenEnv env) {
        MethodVisitor mv = env.getMethodVisitor();
        NodeElement nodeElement = node.getTarget().getElement();
        switch (nodeElement.getKind()) {
        case METHOD:
          EnclosingElement enclosingElement = nodeElement.getEnclosingElement();
          String methodDesc = Type.getMethodDescriptor(returnType, parameterType);
          if (enclosingElement instanceof ClassElement) {
            mv.visitInvokeDynamicInsn(nodeElement.getName(), methodDesc, METHOD_CALL_BSM);
          } else {
            Type libraryType = Type.getObjectType(getInternalName(enclosingElement));
            mv.visitInvokeDynamicInsn(nodeElement.getName(), methodDesc, FUNCTION_CALL_BSM, libraryType);
          }
          return;

        default:
          throw new UnsupportedOperationException("NIY");
        }
      }
    });
  }

  //--- literals

  @Override
  public GenResult visitIntegerLiteral(DartIntegerLiteral node, GenEnv env) {
    //TODO use Java 8 BigInteger.intValueExact()
    MethodVisitor mv = env.getMethodVisitor();

    BigInteger value = node.getValue();
    int intValue = value.intValue();
    BigInteger intBigValue = BigInteger.valueOf(intValue);
    if (value.equals(intBigValue)) {  // it's a small int constant
      ldcInteger(mv, intValue);
      return null;
    }

    // big int constant
    mv.visitInvokeDynamicInsn("ldc", "()"+BIGINT_DESC, LDC_BIGINT_BSM, value.toString());
    return null;
  }

  @Override
  public GenResult visitDoubleLiteral(DartDoubleLiteral node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitLdcInsn(node.getValue());
    return null;
  }

  @Override
  public GenResult visitBooleanLiteral(DartBooleanLiteral node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitInsn(node.getValue()?ICONST_1: ICONST_0);
    return null;
  }

  @Override
  public GenResult visitStringLiteral(DartStringLiteral node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitLdcInsn(node.getValue());
    return null;
  }

  @Override
  public GenResult visitNullLiteral(DartNullLiteral node, GenEnv env) {
    MethodVisitor mv = env.getMethodVisitor();
    mv.visitInsn(ACONST_NULL);
    return null;
  }

  @Override
  public GenResult visitArrayLiteral(DartArrayLiteral node, GenEnv env) {
    throw new UnsupportedOperationException();
  }
}
