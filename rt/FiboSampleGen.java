
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
    
public class FiboSampleGen {
  public static void main(String[] args) throws IOException {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    MethodVisitor mv;
    
    cw.visit(V1_7, ACC_PUBLIC | ACC_SUPER, "FiboSample", null, "java/lang/Object", null);

    {
    mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "fibo", "(I)I", null, null);
    mv.visitCode();
    
    Label try_start0 = new Label();
    Label try_end0 = new Label();
    Label handler0 = new Label();
    mv.visitTryCatchBlock(try_start0, try_end0, handler0, "jdart/runtime/ControlFlowException");
    Label try_start1 = new Label();
    Label try_end1 = new Label();
    Label handler1 = new Label();
    mv.visitTryCatchBlock(try_start1, try_end1, handler1, "jdart/runtime/ControlFlowException");
    Label try_start2 = new Label();
    Label try_end2 = new Label();
    Label handler2 = new Label();
    mv.visitTryCatchBlock(try_start2, try_end2, handler2, "java/lang/ArithmeticException");
    
    mv.visitVarInsn(ILOAD, 0);
    mv.visitInsn(ICONST_2);
    Label l0 = new Label();
    mv.visitJumpInsn(IF_ICMPGE, l0);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);
    
    mv.visitLabel(l0);
    mv.visitVarInsn(ILOAD, 0);
    mv.visitInsn(ICONST_1);
    mv.visitInsn(ISUB);
    mv.visitLabel(try_start0);
    mv.visitMethodInsn(INVOKESTATIC, "FiboSample", "fibo", "(I)I");
    mv.visitLabel(try_end0);
    mv.visitVarInsn(ISTORE, 1);
    mv.visitInsn(ACONST_NULL);
    mv.visitVarInsn(ASTORE, 2);
    
    Label l1 = new Label();
    mv.visitLabel(l1);        // join point with handler0
    
    mv.visitVarInsn(ILOAD, 0);
    mv.visitInsn(ICONST_2);
    mv.visitInsn(ISUB);
    mv.visitLabel(try_start1);
    mv.visitMethodInsn(INVOKESTATIC, "FiboSample", "fibo", "(I)I");
    mv.visitLabel(try_end1);
    mv.visitVarInsn(ISTORE, 3);
    mv.visitInsn(ACONST_NULL);
    mv.visitVarInsn(ASTORE, 4);
    
    Label l2 = new Label();
    mv.visitLabel(l2);       // join point with handler1
    
    mv.visitVarInsn(ALOAD, 2);
    Label l3 = new Label();
    mv.visitJumpInsn(IFNONNULL, l3);
    mv.visitVarInsn(ALOAD, 4);
    mv.visitJumpInsn(IFNONNULL, l3);
    
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitLabel(try_start2);
    mv.visitMethodInsn(INVOKESTATIC, "jdart/runtime/RT", "addExact", "(II)I");
    mv.visitLabel(try_end2);
    mv.visitVarInsn(ISTORE, 5);
    mv.visitInsn(ACONST_NULL);
    mv.visitVarInsn(ASTORE, 6);
    
    Label l4 = new Label();
    mv.visitLabel(l4);        // joint point with addBig & handler2
    
    mv.visitVarInsn(ALOAD, 6);
    Label l5 = new Label();
    mv.visitJumpInsn(IFNONNULL, l5);
    mv.visitVarInsn(ILOAD, 5);
    mv.visitInsn(IRETURN);
    
    
    // --- handlers and special cases 
    
    mv.visitLabel(handler0);
    mv.visitVarInsn(ASTORE, 3);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 1);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitFieldInsn(GETFIELD, "jdart/runtime/ControlFlowException", "value", "Ljdart/runtime/BigInt;");
    mv.visitVarInsn(ASTORE, 2);
    mv.visitJumpInsn(GOTO, l1);
    
    mv.visitLabel(handler1);
    mv.visitVarInsn(ASTORE, 5);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 3);
    mv.visitVarInsn(ALOAD, 5);
    mv.visitFieldInsn(GETFIELD, "jdart/runtime/ControlFlowException", "value", "Ljdart/runtime/BigInt;");
    mv.visitVarInsn(ASTORE, 4);
    mv.visitJumpInsn(GOTO, l2);
    
    mv.visitLabel(l3);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitVarInsn(ALOAD, 4);
    mv.visitMethodInsn(INVOKESTATIC, "jdart/runtime/RT", "addBig", "(ILjdart/runtime/BigInt;ILjdart/runtime/BigInt;)Ljdart/runtime/BigInt;");
    mv.visitVarInsn(ASTORE, 6);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 5);
    mv.visitJumpInsn(GOTO, l4);
    
    mv.visitLabel(handler2);
    mv.visitVarInsn(ASTORE, 7);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ILOAD, 3);
    mv.visitMethodInsn(INVOKESTATIC, "jdart/runtime/RT", "addOverflowed", "(II)Ljdart/runtime/BigInt;");
    mv.visitVarInsn(ASTORE, 6);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 5);
    mv.visitJumpInsn(GOTO, l4);
    
    mv.visitLabel(l5);
    mv.visitVarInsn(ALOAD, 6);
    mv.visitMethodInsn(INVOKESTATIC, "jdart/runtime/ControlFlowException", "value", "(Ljdart/runtime/BigInt;)Ljdart/runtime/ControlFlowException;");
    mv.visitInsn(ATHROW);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    }
    
    {
    mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    
    Label try_start0 = new Label();
    Label try_end0 = new Label();
    Label handler0 = new Label();
    mv.visitTryCatchBlock(try_start0, try_end0, handler0, "jdart/runtime/ControlFlowException");
    
    mv.visitIntInsn(BIPUSH, 40);
    mv.visitLabel(try_start0);
    mv.visitMethodInsn(INVOKESTATIC, "FiboSample", "fibo", "(I)I");
    mv.visitLabel(try_end0);
    mv.visitVarInsn(ISTORE, 1);
    mv.visitInsn(ACONST_NULL);
    mv.visitVarInsn(ASTORE, 2);
    
    Label l0 = new Label();
    mv.visitLabel(l0);    // join point with handler 0
    
    mv.visitVarInsn(ALOAD, 2);
    Label l1 = new Label();
    mv.visitJumpInsn(IFNONNULL, l1);
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    mv.visitVarInsn(ILOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");
    
    Label l2 = new Label();
    mv.visitLabel(l2);    // join point with print big
    
    mv.visitInsn(RETURN);
    
    
    // exceptional code 
    
    mv.visitLabel(handler0);
    mv.visitVarInsn(ASTORE, 3);
    mv.visitVarInsn(ALOAD, 3);
    mv.visitFieldInsn(GETFIELD, "jdart/runtime/ControlFlowException", "value", "Ljava/math/BigInteger;");
    mv.visitVarInsn(ASTORE, 2);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, 1);
    mv.visitJumpInsn(GOTO, l0);
    
    mv.visitLabel(l1);
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
    mv.visitJumpInsn(GOTO, l2);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    }
    cw.visitEnd();

    byte[] array = cw.toByteArray();
    
    CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    
    Files.write(Paths.get("FiboSample.class"), array);
  }
}
