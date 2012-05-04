import static com.google.dart.compiler.SystemLibraryManager.DEFAULT_PLATFORM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.google.dart.compiler.type.TypeAnalyzer;

public class Toto {
	public static boolean test2() throws IOException {
		String sdkPath = "../../dart-sdk/";
		File sdkFile = new File(sdkPath);
		File sourceFile = new File("Hello.dart");

		CompilerOptions compilerOptions = new CompilerOptions();
		SystemLibraryManager libraryManager = 
				new SystemLibraryManager(sdkFile, DEFAULT_PLATFORM);

		String[] options = {
				"--dart-sdk", sdkPath};
		try {
			CommandLineOptions.parse(options, compilerOptions);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}

		CompilerConfiguration config = 
				new DefaultCompilerConfiguration(compilerOptions, libraryManager) {
			@Override
			public List<DartCompilationPhase> getPhases() {
				List<DartCompilationPhase> phases = new ArrayList<DartCompilationPhase>();
			/*
				phases.add(new CompileTimeConstantAnalyzer.Phase());
				phases.add(new Resolver.Phase());
				phases.add(new TypeAnalyzer());
			//*/
			/*
				phases.add(new DisplayPhase());
			//*/
			/*
				phases.add(new InterpretorPhase());
			//*/
			//*
				phases.add(new FlowTypingPhase());
			//*/
				return phases;
			}
		};

		return DartCompiler.compilerMain(sourceFile, config);
	}

	public static void main(String[] args) throws IOException {
		test2();
	}
}
