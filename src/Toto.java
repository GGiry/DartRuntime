import static com.google.dart.compiler.SystemLibraryManager.DEFAULT_PLATFORM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jdart.compiler.flow.FlowTypingPhase;

import org.kohsuke.args4j.CmdLineException;

import com.google.dart.compiler.CommandLineOptions;
import com.google.dart.compiler.CommandLineOptions.CompilerOptions;
import com.google.dart.compiler.CompilerConfiguration;
import com.google.dart.compiler.DartCompilationPhase;
import com.google.dart.compiler.DartCompiler;
import com.google.dart.compiler.DefaultCompilerConfiguration;
import com.google.dart.compiler.SystemLibraryManager;
import com.google.dart.compiler.resolver.CompileTimeConstantAnalyzer;
import com.google.dart.compiler.resolver.Resolver;

public class Toto {
  public static boolean test2(File sourceFile, String sdkPath) throws IOException {
    File sdkFile = new File(sdkPath);

    CompilerOptions compilerOptions = new CompilerOptions();
    SystemLibraryManager libraryManager = new SystemLibraryManager(sdkFile, DEFAULT_PLATFORM);

    String[] options = { "--dart-sdk", sdkPath };
    try {
      CommandLineOptions.parse(options, compilerOptions);
    } catch (CmdLineException e) {
      e.printStackTrace();
    }

    CompilerConfiguration config = new DefaultCompilerConfiguration(compilerOptions, libraryManager) {
      @Override
      public List<DartCompilationPhase> getPhases() {
        List<DartCompilationPhase> phases = new ArrayList<DartCompilationPhase>();

        phases.add(new CompileTimeConstantAnalyzer.Phase());
        phases.add(new Resolver.Phase());
        // phases.add(ClassHierarchyAnalysisPhase.getInstance());
        /*
         * phases.add(new TypeAnalyzer());
         */
        /*
         * phases.add(new DisplayPhase());
         */
        /*
         * phases.add(new InterpretorPhase());
         */

        phases.add(new FlowTypingPhase());
        return phases;
      }
    };

    return DartCompiler.compilerMain(sourceFile, config);
  }

  public static void main(String[] args) throws IOException {
    String sdkPath = "../../dart-sdk/";

    String[] paths = { 
         "DartTest/For2.dart",
        "DartTest/FunctionObject.dart",
        // "DartTest/If7.dart",
        // "DartTest/BinaryOp.dart",
        // "DartTest/For.dart",
        // "DartTest/If4.dart",
        // "DartTest/While.dart"
        // "DartTest/If6.dart",
        // "DartTest/If5.dart",
        // "DartTest/If3.dart",
        // "DartTest/If2.dart",
        // "DartTest/If.dart",
        // "DartTest/Array.dart",
        // "DartTest/PropertyAccess2.dart",
        // "DartTest/GetterSetter.dart",
        // "DartTest/EmptyStatement.dart",
        // "DartTest/Throw.dart",
        // "DartTest/Super.dart",
        // "DartTest/PropertyAccess.dart",
        // "DartTest/Invocation.dart",
        // "Hello.dart"
    };

    for (String path : paths) {
      File sourceFile = new File(path);
      test2(sourceFile, sdkPath);
    }
  }
}
