package jdart.compiler.gen;

import java.util.ArrayList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class MethodRecorder extends MethodNode {
  public MethodRecorder() {
    super(Opcodes.ASM4);
    tryCatchBlocks = new ArrayList<>();
    localVariables = new ArrayList<>();
  }
  
  public void replay(MethodVisitor mv) {
    // visits try catch blocks
    int tryCatchCount = tryCatchBlocks == null ? 0 : tryCatchBlocks.size();
    for (int i = 0; i < tryCatchCount; ++i) {
        tryCatchBlocks.get(i).accept(mv);
    }
    
    // visits instructions
    instructions.accept(mv);
    
    // visits local variables
    int localCount = localVariables == null ? 0 : localVariables.size();
    for (int i = 0; i < localCount; ++i) {
        localVariables.get(i).accept(mv);
    }
  }
  
  @Override
  public void visitCode() {
    throw new IllegalStateException("recorder doesn't allow that");
  }
  @Override
  public void visitMaxs(int maxStack, int maxLocals) {
    throw new IllegalStateException("recorder doesn't allow that");
  }
  @Override
  public void visitEnd() {
    throw new IllegalStateException("recorder doesn't allow that");
  }
}
