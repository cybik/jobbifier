package com.cybikbase.yafot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// Cybik's musings
import com.mtuga.tuples4j.client.Pair;
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
					// Pre-done because there's kind of a circular dep in here.
					acceptsAll(asList("a", "artifacts","id", "input-dir"));
					acceptsAll(asList("h", "?", "usage", "help"), "Show this help").forHelp();
				}
			};
			OptionSpec<String> packageName =
				opt.acceptsAll(asList("pn", "packagename"), "Package's name.")
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
				opt.acceptsAll(asList("p", "password"), "Password to lock the file with. Optional.")
					.withRequiredArg()
					.ofType(String.class)
					.describedAs("password")
					.defaultsTo("yafotyafotyafot")
					.required()
				;

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

			boolean inputDirExistence = false;
			boolean archListExistence = false;
			boolean undoArchiveAssemblage = false;
			if((inputDir != null) && inputDirValue.exists()) {
				inputDirExistence = true;
			}
			if(archList != null) {
				for(File f: archList) {
					if(f.exists())
						archListExistence = true;
				}
			}
			if(inputDirExistence && archListExistence) {
				throw new RuntimeException("User could not have provided both a valid inputdir and an archive list");
			}
			List<Pair<File, File>> oldAndNewTuples = null;
			if(inputDirExistence && !archListExistence) {
				// nop! inputDir is already set to the proper value.
			} else if(!inputDirExistence && archListExistence) {
				undoArchiveAssemblage = true;
				oldAndNewTuples = new ArrayList<>();
				inputDirValue = new File("yafot-temp-dir/archives");
				inputDirValue.mkdirs();
				Pair<File, File> currentTuple = null;
				for(File f: archList) {
					oldAndNewTuples.add(
						currentTuple = new Pair<>(
							f, new File(inputDirValue.getAbsolutePath() + File.pathSeparator + f.getPath())
						)
					);
					try {
						Files.move(currentTuple.getFirst().toPath(), currentTuple.getSecond().toPath());
					} catch (IOException e) {
						throw new RuntimeException("Couldn't assemble archives");
					}
				}
			}

			// Actual!
			List<String> arguments = prepareArguments(
				inputDirValue.getPath(), // input directory, either created or pre-existing
				outputDirValue.getPath(), // outputdir
				opts.valueOf(packageName), // packagename
				String.valueOf(opts.valueOf(version)), // packageversion
				opts.valueOf(password)  // password characters
			);
			// Call obb NAO
			invoke(arguments);

			if(undoArchiveAssemblage) {
				for(Pair<File, File> pf: oldAndNewTuples) {
					try {
						Files.move(pf.getSecond().toPath(), pf.getFirst().toPath());
					} catch (IOException e) {
						throw new RuntimeException("Couldn't restore archives to their original paths");
					}
				}
			}

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
