import static com.google.dart.compiler.SystemLibraryManager.DEFAULT_PLATFORM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jdart.compiler.phase.ClassHierarchyAnalysisPhase;
import jdart.compiler.phase.FlowTypingPhase;
import jdart.compiler.phase.TypeHelper;
import jdart.compiler.phase.FlowTypingPhase.FTVisitor;
import jdart.compiler.type.CoreTypeRepository;
import jdart.compiler.type.TypeRepository;

import org.kohsuke.args4j.CmdLineException;

import com.google.dart.compiler.CommandLineOptions;
import com.google.dart.compiler.CommandLineOptions.CompilerOptions;
import com.google.dart.compiler.CompilerConfiguration;
import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompiler;
import com.google.dart.compiler.DefaultCompilerConfiguration;
import com.google.dart.compiler.SystemLibraryManager;
import com.google.dart.compiler.ast.DartUnit;
import com.google.dart.compiler.resolver.CompileTimeConstantAnalyzer;
import com.google.dart.compiler.resolver.MethodElement;
import com.google.dart.compiler.resolver.MethodNodeElement;
import com.google.dart.compiler.resolver.Resolver;

public class Main {
  public static void compile(File sourceFile, String sdkPath) throws IOException {
    File sdkFile = new File(sdkPath);

    CompilerOptions compilerOptions = new CompilerOptions();
    SystemLibraryManager libraryManager = new SystemLibraryManager(sdkFile, DEFAULT_PLATFORM);

    String[] options = { "--dart-sdk", sdkPath };
    try {
      CommandLineOptions.parse(options, compilerOptions);
    } catch (CmdLineException e) {
      e.printStackTrace();
    }

    final ClassHierarchyAnalysisPhase chaInstance = ClassHierarchyAnalysisPhase.getInstance();
    
    CompilerConfiguration config = new DefaultCompilerConfiguration(compilerOptions, libraryManager) {
      @Override
      public List<DartCompilationPhase> getPhases() {
        List<DartCompilationPhase> phases = new ArrayList<>();
        phases.add(new CompileTimeConstantAnalyzer.Phase());
        phases.add(new Resolver.Phase());
        
        phases.add(chaInstance);
        return phases;
      }
    };

    boolean result = DartCompiler.compilerMain(sourceFile, config);
    if (result == false) {
      System.err.println("an error occured !");
      return;
    }
    
    Set<DartUnit> units = chaInstance.getUnits();
    DartUnit mainUnit = units.iterator().next();
    MethodNodeElement mainMethod = (MethodNodeElement)mainUnit.getLibrary().getElement().getEntryPoint();
    if (mainMethod == null) {
      System.err.println("unit "+mainUnit.getSourceName()+" has no entry point");
      return;
    }
    
     // initialize core type repository
    CoreTypeRepository coreTypeRepository = CoreTypeRepository.initCoreTypeRepository(chaInstance.getCoreTypeProvider());
    TypeRepository typeRepository = new TypeRepository(coreTypeRepository);
    TypeHelper typeHelper = new TypeHelper(typeRepository);
    
    // type flow main method
    FTVisitor visitor = new FTVisitor(typeHelper);
    visitor.typeFlow(mainMethod.getNode());
  }

  public static void main(String[] args) throws IOException {
    String sdkPath = "../../dart-sdk/";

    String[] paths = { 
        "DartTest/Hello.dart"
    };

    for (String path : paths) {
      File sourceFile = new File(path);
      compile(sourceFile, sdkPath);
    }
  }
}
