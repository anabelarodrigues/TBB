package tbb.view.analitics;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import tbb.core.CoreController;
import tbb.touch.PackageSession;

/**
 * Created by Anabela on 19/04/2016.
 * This fragment retains itself across orientation changes.
 * Used to maintain state of where we are in the database analysis.
 */
public class AnalysisFragment extends Fragment {
    public int currentPackageIndex=-1, currentPackageSessionIndex=-1,
            currentTouchSequenceIndex=-1;
    public PackageSession session;
    //private ArrayList<Path> sequencePaths;

    public ArrayList<Integer> packages, packageSessions,touchSequences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);


        getNextPackageIDs();
    }

    //returns true if successfully fetches next set of package session ids (of next package)
    public boolean getNextPackageIDs(){
        boolean next = false;
        currentPackageIndex++;
        Log.d("debug", "Getting Package IDs. Packages size:" + packages.size() +
                " and currentPackageIndex is:" + currentPackageIndex);
        if(currentPackageIndex < packages.size()) {
            packageSessions = CoreController.sharedInstance().
                    getAllPackageSessions(packages.get(currentPackageIndex));
            Log.d("debug", "PackageSessions size:" + packageSessions.size());
            if(packageSessions.size()>0) {
                currentPackageSessionIndex = 0;
                next = true;
            } else {
                return getNextPackageIDs();
            }
        }
        return next;
    }


}
