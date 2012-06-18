package jdart.compiler.gen;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdart.compiler.flow.ProfileInfo;
import jdart.compiler.flow.Profiles;
import jdart.compiler.type.Type;
import jdart.compiler.visitor.ASTVisitor2;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.dart.compiler.ast.DartClass;
import com.google.dart.compiler.ast.DartMethodDefinition;
import com.google.dart.compiler.resolver.ClassElement;
import com.google.dart.compiler.resolver.ClassNodeElement;
import com.google.dart.compiler.resolver.Element;
import com.google.dart.compiler.resolver.EnclosingElement;
import com.google.dart.compiler.resolver.LibraryElement;
import com.google.dart.compiler.type.InterfaceType;

public class Gen extends ASTVisitor2<Void, GenEnv> {
  
  // helper methods
  
  private static String getInternalName(Element element) {
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
  
  // entry point
  public void gen(Map<DartMethodDefinition, Profiles> methodMap) throws IOException {
    Map<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> unitMap = createUnitMap(methodMap);
    for(Entry<EnclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>>> unitEntry: unitMap.entrySet()) {
      genUnit(unitEntry.getKey(), unitEntry.getValue());
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
  
  private void genUnit(EnclosingElement enclosingElement, ArrayList<Entry<DartMethodDefinition, Profiles>> methodList) throws IOException {
    ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    
    String name = getInternalName(enclosingElement);
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
    
    cv.visit(V1_7, ACC_PUBLIC|ACC_SUPER, name, null, superName, interfaces);
    //cv.visitSource(enclosingElement.getSourceInfo().getSource().getName(), null);
    
    for(Entry<DartMethodDefinition, Profiles> methodEntry: methodList) {
      genMethod(cv, methodEntry.getKey(), methodEntry.getValue());
    }
    
    cv.visitEnd();
    byte[] byteArray = cv.toByteArray();
    
    CheckClassAdapter.verify(new ClassReader(byteArray), true, new PrintWriter(System.err));
    
    Path path = Paths.get(name+".class");
    Path directory = path.getParent();
    if (directory != null) {
      Files.createDirectories(directory);
    }
    Files.write(path, byteArray);
  }

  private void genMethod(ClassVisitor cv, DartMethodDefinition methodDefinition, Profiles profiles) {
    Map<List<Type>, ProfileInfo> signatureMap = profiles.getSignatureMap();
    
    
  }
}
