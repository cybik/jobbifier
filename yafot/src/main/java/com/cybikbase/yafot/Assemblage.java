package com.cybikbase.yafot;

import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cybik on 16-08-28.
 */
public class Assemblage {

    private List<File> archList;
    protected File inputDirValue;
    private boolean undoArchiveAssemblage = false;
    private List<Pair<File, File>> oldAndNewTuples = null;
    protected boolean isPatchFile = false;

    public Assemblage(List<File> vs, File v) {
        this(vs,v,false);
    }
    public Assemblage(List<File> vs, File v, boolean isPatch) {
        archList = vs;
        inputDirValue = v;
        isPatchFile = isPatch;
    }


    public Assemblage run() {
        // Stage 2: List either archives or input directory. Sidenote: if we're getting a GFS list, we move all of
        // 			 them to a default temp dir and use THAT dir for our obb base.

        boolean inputDirExistence = false;
        boolean archListExistence = false;
        if((inputDirValue != null) && inputDirValue.exists()) {
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

        if(inputDirExistence && !archListExistence) {
            // nop! inputDir is already set to the proper value.
        } else if(!inputDirExistence && archListExistence) {
            undoArchiveAssemblage = true;
            oldAndNewTuples = new ArrayList<>();
            inputDirValue = new File("yafot-temp-dir/archives-"+(isPatchFile?"patch":"main"));
            inputDirValue.mkdirs();
            Pair<File, File> currentTuple = null;
            for(File f: archList) {
                oldAndNewTuples.add(
                        currentTuple = new Pair<>(
                                f, new File(inputDirValue.getAbsolutePath() + File.separator + f.getName())
                        )
                );
                try {
                    System.out.println(
                            "Trying to move " + currentTuple.getValue0().getAbsolutePath()
                                    + " to " + currentTuple.getValue1().getAbsolutePath()
                    );
                    Files.move(currentTuple.getValue0().toPath(), currentTuple.getValue1().toPath());
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't assemble archives");
                }
            }
        }
        return this;
    }

    public static boolean isThereAPatch(List<File> archList, File inputDir) {
        boolean inputDirExistence = false;
        boolean archListExistence = false;
        if((inputDir != null) && inputDir.exists()) {
            inputDirExistence = true;
        }
        if(archList != null) {
            for(File f: archList) {
                System.out.println("Checking for " + f.getAbsolutePath());
                if(f.exists())
                    archListExistence = true;
            }
        }

        if(inputDirExistence && archListExistence)
            throw new RuntimeException("User could not have provided both a valid inputdir and an archive list");

        return (inputDirExistence || archListExistence);

    }

    public void unrun() {
        if(undoArchiveAssemblage) {
            for(Pair<File, File> pf: oldAndNewTuples) {
                try {
                    Files.move(pf.getValue1().toPath(), pf.getValue0().toPath());
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't restore archives to their original paths");
                }
            }
        }
    }
}
