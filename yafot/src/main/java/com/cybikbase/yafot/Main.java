package com.cybikbase.yafot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Cybik's musings
import joptsimple.OptionParser;
import joptsimple.OptionSpec;

import static java.util.Arrays.asList;

public class Main {

	public static void main(String[] args) {
		(new Main()).run(args);
	}

	public void run(String[] args) {
		// If there's any shell arguments, do a shell-based invoke
		if( args.length > 0 ) {
			OptionParser optParse = generateOptionsParser();
			try {
				optParse.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			optParse.parse(args);
		} else {
			(new YafotUI(this)).execute();
		}
	}

	private static OptionParser generateOptionsParser() {
		OptionParser opt = new OptionParser() {
			{
				acceptsAll(asList("g", "gfs","id", "input-dir"));
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
		OptionSpec<File> gfs =
			opt.acceptsAll(asList("g", "gfs"), "GFS archives to pack - per file")
				.requiredUnless("id", "input-dir")
				.withRequiredArg()
				.ofType(File.class)
				.describedAs("gfsarch1,gfsarch2,...")
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
		return opt;
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
