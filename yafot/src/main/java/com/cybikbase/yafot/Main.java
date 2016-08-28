package com.cybikbase.yafot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Cybik's musings
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import static java.util.Arrays.asList;

public class Main {

	public static void main(String[] args) {
		(new Main()).run(args);
	}

	public void run(String[] args) {
		// If there's any shell arguments, do a shell-based invoke
		if( args.length > 0 ) {
			OptionParser opt = new OptionParser() {
				{
					acceptsAll(asList("a", "artifacts","id", "input-dir"));
					acceptsAll(asList("h", "?", "usage", "help"), "Show this help").forHelp();
					acceptsAll(asList("pn", "packagename"), "Package's name.")
						.withRequiredArg()
						.ofType(String.class)
						.describedAs("package_name")
						.defaultsTo("com.android.lolyourstuff")
						.required()
					;
					acceptsAll(asList("v", "version"), "Version number of the OBB package.")
						.withRequiredArg()
						.ofType(Integer.class)
						.describedAs("version")
						.defaultsTo(1)
						.required()
					;
					acceptsAll(asList("p", "password"), "Password to lock the file with. Optional.")
						.withRequiredArg()
						.ofType(String.class)
						.describedAs("password")
						.required()
					;
				}
			};

			OptionSpec<File> outputDir =
				opt.acceptsAll(asList("od", "output-dir"), "Directory to store the output OBB in")
					.withRequiredArg()
					.ofType(File.class)
					.describedAs("name")
					.defaultsTo(new File("output"))
					.required()
				;
			OptionSpec<File> artifacts =
				opt.acceptsAll(asList("a", "artifacts"), "Artifacts to pack - per file")
					.requiredUnless("id", "input-dir")
					.withRequiredArg()
					.ofType(File.class)
					.describedAs("art1,art2,...")
					.withValuesSeparatedBy(",")
				;
			OptionSpec<File> inputDir =
				opt.acceptsAll(asList("id", "input-dir"), "GFS archives to pack - pack dir")
					.requiredUnless("g", "gfs")
					.withRequiredArg()
					.ofType(File.class)
					.defaultsTo(new File("yafot-temp-input"))
					.describedAs("dir")
				;
			try {
				opt.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			OptionSet opts = opt.parse(args);

			// Stage 1: check if outputdir exists.
			File outputDirValue = null;
			if((outputDirValue = opts.valueOf(outputDir)) != null && outputDirValue.exists()) {
				throw new RuntimeException("The output directory exists. We need a clean one. For now, crashing.");
			} else {
				if(!outputDirValue.mkdirs()) throw new RuntimeException("Creating outputdir failed");
			}

			// Stage 2: List either archives or input directory. Sidenote: if we're getting a GFS list, we move all of
			// 			 them to a default temp dir and use THAT dir for our obb base.
			List<File> archList = opts.valuesOf(artifacts);
			File inputDirValue = opts.valueOf(inputDir);

		} else {
			(new YafotUI(this)).execute();
		}
	}

	protected List<String> prepareArguments(String inputDir, String outputDir, String packageName,
											String packageVersion, String pwdChars) {
		List<String> args = new ArrayList<>(
			asList(
				new String[] {
					"-d", inputDir,
					"-o", outputDir,
					"-pn", packageName,
					"-pv", packageVersion,
					"-v" // go verbose, fuck it
				}
			)
		);
		if(pwdChars != null && !pwdChars.isEmpty()) {
			args.add("-k");
			args.add(pwdChars);
		}
		return args;
	}

	protected void invoke(List<String> strings) {
		com.android.jobb.Main.main(strings.toArray(new String[0]));
	}
}
