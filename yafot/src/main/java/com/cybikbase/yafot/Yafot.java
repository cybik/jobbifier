package com.cybikbase.yafot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Cybik's musings
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.FileUtils;

import static java.lang.System.exit;
import static java.util.Arrays.asList;

public class Yafot {

	public static void main(String[] args) {
		(new Yafot()).run(args);
	}

	public void run(String[] args) {
		// If there's any shell arguments, do a shell-based invoke
		if( args.length > 0 ) {
			OptionParser opt = new OptionParser() {
				{
					// Pre-done because there's kind of a circular dep in here.
					acceptsAll(asList("a","artifacts","i","input-dir","b","artifacts-patch","j","input-dir-patch"));
				}
			};
			OptionSpec<Void> halp = opt.acceptsAll(asList("h", "?", "usage", "help"), "Show this help").forHelp();
			OptionSpec<String> packageName =
				opt.acceptsAll(asList("p", "packagename"), "Package's name.")
					.withRequiredArg()
					.ofType(String.class)
					.describedAs("package_name")
					.defaultsTo("com.android.lolyourstuff")
					.required()
				;
			OptionSpec<Integer> version =
				opt.acceptsAll(asList("v", "version"), "Version number of the OBB package.")
					.withRequiredArg()
					.ofType(Integer.class)
					.describedAs("version")
					.defaultsTo(1)
					.required()
				;
			OptionSpec<String> password =
				opt.acceptsAll(asList("k", "key"), "Key [password] to lock the file with. Optional.")
					.withRequiredArg()
					.ofType(String.class)
					.describedAs("key")
					.defaultsTo("yafotyafotyafot")
					.required()
				;

			OptionSpec<File> outputDir =
				opt.acceptsAll(asList("o", "output-dir"), "Directory to store the output OBB in.")
					.withRequiredArg()
					.ofType(File.class)
					.describedAs("name")
					.defaultsTo(new File("output"))
					.required()
				;

			OptionSpec<File> artifacts =
				opt.acceptsAll(asList("a", "artifacts"), "Artifacts to pack - per file.")
					.availableUnless("i", "input-dir")
					.withRequiredArg()
					.ofType(File.class)
					.describedAs("art1,art2,...")
					.withValuesSeparatedBy(",")
				;
			OptionSpec<File> artifactsPatch =
				opt.acceptsAll(asList("b", "artifacts-patch"), "Artifacts to pack in the patch obb - per file.")
					.availableUnless("j", "input-dir-patch")
					.withOptionalArg()
					.ofType(File.class)
					.describedAs("artp1,artp2,...")
					.withValuesSeparatedBy(",")
				;

			OptionSpec<File> inputDir =
				opt.acceptsAll(asList("i", "input-dir"), "Artifacts to pack - per dir.")
					.availableUnless("a", "artifacts")
					.withRequiredArg()
					.ofType(File.class)
					.describedAs("dir")
				;
			OptionSpec<File> inputDirPatch =
				opt.acceptsAll(asList("j", "input-dir-patch"), "Artifacts to pack in patch obb - per dir.")
					.availableUnless("b", "artifacts-patch")
					.withOptionalArg()
					.ofType(File.class)
					.describedAs("dir")
				;

			try {
				opt.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			OptionSet opts = opt.parse(args);

			if(opts.has(halp)) {
				// We've done our job. gg ez.
				exit(0);
			}

			// Stage 1: check if outputdir exists.
			File outputDirValue = null;
			if((outputDirValue = opts.valueOf(outputDir)) != null && outputDirValue.exists()) {
				throw new RuntimeException("The output directory exists. We need a clean one. For now, crashing.");
			} else {
				if(!outputDirValue.mkdirs()) throw new RuntimeException("Creating outputdir failed");
			}

			File inputTempDir = new File("yafot-temp-dir");
			inputTempDir.mkdirs();

			// Stage 2: List either archives or input directory. Sidenote: if we're getting a GFS list, we move all of
			// 			 them to a default temp dir and use THAT dir for our obb base.
			List<Assemblage> allAssemblages = new ArrayList<>();

			// Main assemblage.
			allAssemblages.add((new Assemblage(opts.valuesOf(artifacts), opts.valueOf(inputDir))).run(inputTempDir));

			if(Assemblage.isThereAPatch(opts.valuesOf(artifactsPatch), opts.valueOf(inputDirPatch))) {
				allAssemblages.add(
					(new Assemblage(opts.valuesOf(artifactsPatch), opts.valueOf(inputDirPatch), true)).run(inputTempDir)
				);
			} else {
				System.out.println("No patchfile generation required.");
			}

			for(Assemblage assemblageSet: allAssemblages) {
				// Actual!
				List<String> arguments = prepareArguments(
					//inputDirValue.getPath(), // input directory, either created or pre-existing
					assemblageSet.inputDirValue.getPath(), // input directory, either created or pre-existing
					outputDirValue.getPath().trim() // file name
						+ File.separator
						+ getOutputFileName(assemblageSet, opts.valueOf(version), opts.valueOf(packageName)).trim(),
					opts.valueOf(packageName), // package name,
					String.valueOf(opts.valueOf(version)), // package version
					opts.valueOf(password)  // password characters
				);
				System.out.print("The OBB assemblage is being called using these arguments.");
				for(String s: arguments) {
					if(s.startsWith("-")){
						System.out.println();
						System.out.print('\t');
					}
					else System.out.print(" ");
					System.out.print(s);
				}
				System.out.println();
				System.out.println("Note that some of these are forced defaults, as the creator of YAFOT is using YAFOT in that way.");
				// Call obb NAO
				invoke(arguments,
					new File("yafot/output"+(assemblageSet.isPatchFile?"-patch":"")+"-obb.log"),
					new File("yafot/output"+(assemblageSet.isPatchFile?"-patch":"")+"-obb-err.log")
				);

				assemblageSet.unrun();
			}

			if(inputTempDir != null && inputTempDir.exists()) {
				System.out.println("WHY WON'T THIS DELETE " + inputTempDir.getAbsolutePath());
				try {
					FileUtils.forceDeleteOnExit(inputTempDir);
				} catch (IOException e) {
					throw new RuntimeException("Couldn't even force delete, what?");
				}
			}

		} else {
			(new YafotUI(this)).execute();
		}
	}

	protected String getOutputFileName(Assemblage assemblage, Integer packageVersion, String packageName)
	{
		return (assemblage.isPatchFile ? "patch" : " main")
				+ "." + String.valueOf(packageVersion)
				+ "." + packageName + ".obb";
	}

	protected List<String> prepareArguments(String inputDir, String outputFile, String packageName,
											String packageVersion, String pwdChars) {
		List<String> args = new ArrayList<>(
			asList(
				new String[] {
					"-d", inputDir,
					"-o", outputFile,
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

	protected void invoke(List<String> strings, File logstd, File logerr) {
		com.android.jobb.Main.main(strings.toArray(new String[0]), true, logstd, logerr);
	}
	protected void invoke(List<String> strings) {
		com.android.jobb.Main.main(strings.toArray(new String[0]), false, null, null);
	}
}
